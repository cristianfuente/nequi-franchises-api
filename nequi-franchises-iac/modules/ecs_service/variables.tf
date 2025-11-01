variable "name" {
  description = "Base name for ECS resources"
  type        = string
}

variable "vpc_id" {
  description = "VPC identifier used for the service security group"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnets where the service will run"
  type        = list(string)
}

variable "target_group_arn" {
  description = "Target group ARN where tasks will be registered"
  type        = string
}

variable "container_image" {
  description = "Container image (including tag) deployed to ECS"
  type        = string
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
  default     = 8080
}

variable "desired_count" {
  description = "Desired number of running tasks"
  type        = number
  default     = 3
}

variable "cpu" {
  description = "CPU units reserved for the task definition"
  type        = number
  default     = 512
}

variable "memory" {
  description = "Memory (MiB) reserved for the task definition"
  type        = number
  default     = 1024
}

variable "command" {
  description = "Optional command override for the container"
  type        = list(string)
  default     = []
}

variable "environment" {
  description = "Environment variables for the container"
  type        = map(string)
  default     = {}
}

variable "assign_public_ip" {
  description = "Assign a public IP to Fargate tasks"
  type        = bool
  default     = false
}

variable "enable_execute_command" {
  description = "Enable ECS exec for the service"
  type        = bool
  default     = false
}

variable "health_check_grace_period_seconds" {
  description = "Grace period before starting health checks"
  type        = number
  default     = 60
}

variable "cloudwatch_log_retention_in_days" {
  description = "Retention in days for the CloudWatch log group"
  type        = number
  default     = 731
}

variable "allowed_security_group_ids" {
  description = "Security groups allowed to reach the service"
  type        = list(string)
  default     = []
}

variable "allowed_cidr_blocks" {
  description = "CIDR blocks allowed to reach the service"
  type        = list(string)
  default     = []
}

variable "task_role_policy_statements" {
  description = "Additional IAM policy statements for the task role"
  type = list(object({
    sid       = string
    effect    = string
    actions   = list(string)
    resources = list(string)
  }))
  default = []
}

variable "capacity_providers" {
  description = "ECS capacity providers to register in the cluster"
  type        = list(string)
  default     = ["FARGATE", "FARGATE_SPOT"]
}

variable "capacity_provider_strategy" {
  description = "Capacity provider strategy used by the ECS service"
  type = list(object({
    name   = string
    weight = optional(number)
    base   = optional(number)
  }))
  default = [
    {
      name   = "FARGATE_SPOT"
      weight = 5
      base   = 5
    }
  ]
}

variable "tags" {
  description = "Tags applied to ECS resources"
  type        = map(string)
  default     = {}
}
