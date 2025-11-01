data "aws_region" "current" {}

locals {
  common_tags = merge(
    {
      Terraform = "true"
      Component = var.name
    },
    var.tags,
  )

  container_name = "${var.name}-app"
  environment = [
    for key, value in var.environment :
    {
      name  = key
      value = value
    }
  ]
}

resource "aws_security_group" "service" {
  name        = "${var.name}-svc-sg"
  description = "Security group for ECS service ${var.name}"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-svc-sg"
    },
  )
}

resource "aws_security_group_rule" "from_sg" {
  for_each = { for idx, sg_id in var.allowed_security_group_ids : idx => sg_id }

  type                     = "ingress"
  from_port                = var.container_port
  to_port                  = var.container_port
  protocol                 = "tcp"
  source_security_group_id = each.value
  security_group_id        = aws_security_group.service.id
  description              = "Allow traffic from security group ${each.value}"
}

resource "aws_security_group_rule" "from_cidr" {
  for_each = toset(var.allowed_cidr_blocks)

  type              = "ingress"
  from_port         = var.container_port
  to_port           = var.container_port
  protocol          = "tcp"
  cidr_blocks       = [each.value]
  security_group_id = aws_security_group.service.id
  description       = "Allow traffic from CIDR ${each.value}"
}

resource "aws_cloudwatch_log_group" "this" {
  name              = "/ecs/${var.name}"
  retention_in_days = var.cloudwatch_log_retention_in_days

  tags = merge(
    local.common_tags,
    {
      Name = "/ecs/${var.name}"
    },
  )
}

data "aws_iam_policy_document" "execution_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "execution" {
  name               = "${var.name}-execution-role"
  assume_role_policy = data.aws_iam_policy_document.execution_assume_role.json

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "execution_default" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "task" {
  name               = "${var.name}-task-role"
  assume_role_policy = data.aws_iam_policy_document.execution_assume_role.json

  tags = local.common_tags
}

data "aws_iam_policy_document" "task_inline" {
  count = length(var.task_role_policy_statements) > 0 ? 1 : 0

  dynamic "statement" {
    for_each = var.task_role_policy_statements

    content {
      sid       = statement.value.sid
      effect    = statement.value.effect
      actions   = statement.value.actions
      resources = statement.value.resources
    }
  }
}

resource "aws_iam_role_policy" "task_inline" {
  count = length(var.task_role_policy_statements) > 0 ? 1 : 0

  name   = "${var.name}-task-inline"
  role   = aws_iam_role.task.id
  policy = data.aws_iam_policy_document.task_inline[0].json
}

resource "aws_ecs_cluster" "this" {
  name = "${var.name}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-cluster"
    },
  )
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name       = aws_ecs_cluster.this.name
  capacity_providers = var.capacity_providers

  dynamic "default_capacity_provider_strategy" {
    for_each = length(var.capacity_provider_strategy) > 0 ? var.capacity_provider_strategy : []

    content {
      capacity_provider = default_capacity_provider_strategy.value.name
      weight            = try(default_capacity_provider_strategy.value.weight, null)
      base              = try(default_capacity_provider_strategy.value.base, null)
    }
  }
}

resource "aws_ecs_task_definition" "this" {
  family                   = "${var.name}-task"
  cpu                      = tostring(var.cpu)
  memory                   = tostring(var.memory)
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = local.container_name
      image     = var.container_image
      essential = true
      command   = length(var.command) > 0 ? var.command : null
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]
      environment = local.environment
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.this.name
          awslogs-region        = data.aws_region.current.name
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-task"
    },
  )
}

resource "aws_ecs_service" "this" {
  name                              = "${var.name}-service"
  cluster                           = aws_ecs_cluster.this.id
  desired_count                     = var.desired_count
  task_definition                   = aws_ecs_task_definition.this.arn
  enable_execute_command            = var.enable_execute_command
  health_check_grace_period_seconds = var.health_check_grace_period_seconds
  launch_type                       = length(var.capacity_provider_strategy) == 0 ? "FARGATE" : null

  dynamic "capacity_provider_strategy" {
    for_each = length(var.capacity_provider_strategy) > 0 ? var.capacity_provider_strategy : []

    content {
      capacity_provider = capacity_provider_strategy.value.name
      weight            = try(capacity_provider_strategy.value.weight, null)
      base              = try(capacity_provider_strategy.value.base, null)
    }
  }

  lifecycle {
    ignore_changes = [
      desired_count,
      capacity_provider_strategy,
    ]
  }

  network_configuration {
    assign_public_ip = var.assign_public_ip
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.service.id]
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = local.container_name
    container_port   = var.container_port
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-service"
    },
  )

  depends_on = [
    aws_security_group_rule.from_sg,
    aws_security_group_rule.from_cidr,
    aws_iam_role_policy_attachment.execution_default,
    aws_ecs_cluster_capacity_providers.this,
  ]
}
