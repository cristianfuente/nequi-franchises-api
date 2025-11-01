locals {
  name_prefix = "${var.project_name}-${var.environment}"
  tags = merge(
    {
      Project     = var.project_name
      Environment = var.environment
      Terraform   = "true"
    },
    var.tags,
  )
}

module "network" {
  source = "../../modules/network"

  name                 = local.name_prefix
  cidr_block           = var.vpc_cidr
  azs                  = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway   = var.enable_nat_gateway
  single_nat_gateway   = var.single_nat_gateway
  tags                 = local.tags
}

module "container_registry" {
  source = "../../modules/container_registry"

  name                 = var.repository_name
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true
  lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Retain the last ${var.ecr_image_retention} images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = var.ecr_image_retention
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
  tags = local.tags
}

module "dynamodb" {
  source = "../../modules/dynamodb_table"

  name                   = var.dynamodb_table_name
  hash_key               = var.dynamodb_partition_key
  hash_key_type          = var.dynamodb_partition_key_type
  range_key              = var.dynamodb_sort_key
  range_key_type         = var.dynamodb_sort_key_type
  billing_mode           = var.dynamodb_billing_mode
  read_capacity          = var.dynamodb_read_capacity
  write_capacity         = var.dynamodb_write_capacity
  ttl_attribute          = var.dynamodb_ttl_attribute
  point_in_time_recovery = var.dynamodb_point_in_time_recovery
  global_secondary_indexes = [
    {
      name            = "byFranchise"
      hash_key        = "franchiseId"
      hash_key_type   = "S"
      projection_type = "ALL"
    }
  ]
  tags = local.tags
}

resource "aws_security_group" "apigw_vpc_link" {
  name        = "${local.name_prefix}-apigw-link-sg"
  description = "Security group that protects API Gateway VPC link ENIs"
  vpc_id      = module.network.vpc_id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [module.network.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.tags,
    {
      Name = "${local.name_prefix}-apigw-link-sg"
    },
  )
}

module "load_balancer" {
  source = "../../modules/load_balancer"

  name                  = "${local.name_prefix}-service"
  vpc_id                = module.network.vpc_id
  subnet_ids            = module.network.private_subnet_ids
  listener_port         = 80
  listener_protocol     = "HTTP"
  target_group_port     = var.container_port
  target_group_protocol = "HTTP"
  health_check = {
    path                = "/actuator/health"
    matcher             = "200-399"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    interval            = 30
    timeout             = 5
  }
  allowed_cidr_blocks = [module.network.cidr_block]
  tags                = local.tags
}

locals {
  container_image = "${module.container_registry.repository_url}:${var.container_image_tag}"
  container_environment = merge(
    {
      DYNAMODB_TABLE_NAME    = module.dynamodb.table_name
      AWS_REGION             = var.aws_region
      ENVIRONMENT            = var.environment
      SPRING_PROFILES_ACTIVE = "dev"
      APP_DYNAMO_CORE_TABLE  = module.dynamodb.table_name
    },
    var.custom_environment_variables,
  )
}

module "ecs_service" {
  source = "../../modules/ecs_service"

  name                             = local.name_prefix
  vpc_id                           = module.network.vpc_id
  private_subnet_ids               = module.network.private_subnet_ids
  target_group_arn                 = module.load_balancer.target_group_arn
  container_image                  = local.container_image
  container_port                   = var.container_port
  desired_count                    = var.desired_count
  cpu                              = var.task_cpu
  memory                           = var.task_memory
  environment                      = local.container_environment
  cloudwatch_log_retention_in_days = var.log_retention_in_days
  enable_execute_command           = var.enable_execute_command
  allowed_security_group_ids       = [module.load_balancer.security_group_id]
  allowed_cidr_blocks              = []
  capacity_providers               = var.ecs_capacity_providers
  capacity_provider_strategy       = var.ecs_capacity_provider_strategy
  task_role_policy_statements = [
    {
      sid    = "AllowTableRW"
      effect = "Allow"
      actions = [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Query",
        "dynamodb:Scan"
      ]
      resources = [
        module.dynamodb.table_arn,
        "${module.dynamodb.table_arn}/index/*"
      ]
    }
  ]
  tags = local.tags
}

module "api_gateway" {
  source = "../../modules/api_gateway"

  name                        = "${local.name_prefix}-api"
  integration_uri             = module.load_balancer.listener_arn
  vpc_link_subnet_ids         = module.network.private_subnet_ids
  vpc_link_security_group_ids = [aws_security_group.apigw_vpc_link.id]
  stage_name                  = var.api_stage_name
  route_key                   = var.api_route_key
  tags                        = local.tags
}
