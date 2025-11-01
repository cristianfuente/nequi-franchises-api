variable "name" {
  description = "Base name for load balancer resources"
  type        = string
}

variable "vpc_id" {
  description = "Identifier of the VPC where resources will be deployed"
  type        = string
}

variable "subnet_ids" {
  description = "Subnets used by the load balancer"
  type        = list(string)
}

variable "listener_port" {
  description = "Port exposed by the listener"
  type        = number
  default     = 80
}

variable "listener_protocol" {
  description = "Protocol handled by the listener"
  type        = string
  default     = "HTTP"
}

variable "target_group_port" {
  description = "Port where the ECS tasks listen"
  type        = number
  default     = 8080
}

variable "target_group_protocol" {
  description = "Protocol used by the target group"
  type        = string
  default     = "HTTP"
}

variable "health_check" {
  description = "Health check configuration applied to the target group"
  type = object({
    path                = string
    matcher             = string
    healthy_threshold   = number
    unhealthy_threshold = number
    interval            = number
    timeout             = number
  })
  default = {
    path                = "/health"
    matcher             = "200-399"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    interval            = 30
    timeout             = 5
  }
}

variable "allowed_cidr_blocks" {
  description = "CIDR ranges permitted to reach the load balancer"
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Tags applied to load balancer resources"
  type        = map(string)
  default     = {}
}
