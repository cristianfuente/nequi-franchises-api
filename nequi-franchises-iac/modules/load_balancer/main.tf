locals {
  common_tags = merge(
    {
      Terraform = "true"
      Component = var.name
    },
    var.tags,
  )
}

resource "aws_security_group" "alb" {
  name        = "${var.name}-alb-sg"
  description = "Security group for ${var.name} internal load balancer"
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
      Name = "${var.name}-alb-sg"
    },
  )
}

resource "aws_security_group_rule" "alb_ingress" {
  for_each = toset(var.allowed_cidr_blocks)

  type              = "ingress"
  from_port         = var.listener_port
  to_port           = var.listener_port
  protocol          = "tcp"
  cidr_blocks       = [each.value]
  security_group_id = aws_security_group.alb.id
  description       = "Allow listener traffic from ${each.value}"
}

resource "aws_lb" "this" {
  name               = "${var.name}-alb"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.subnet_ids

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-alb"
    },
  )
}

resource "aws_lb_target_group" "this" {
  name        = "${var.name}-tg"
  port        = var.target_group_port
  protocol    = var.target_group_protocol
  target_type = "ip"
  vpc_id      = var.vpc_id

  health_check {
    path                = var.health_check.path
    matcher             = var.health_check.matcher
    healthy_threshold   = var.health_check.healthy_threshold
    unhealthy_threshold = var.health_check.unhealthy_threshold
    interval            = var.health_check.interval
    timeout             = var.health_check.timeout
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-tg"
    },
  )
}

resource "aws_lb_listener" "this" {
  load_balancer_arn = aws_lb.this.arn
  port              = var.listener_port
  protocol          = var.listener_protocol

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}
