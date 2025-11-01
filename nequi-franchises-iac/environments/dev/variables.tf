variable "aws_region" {
  description = "AWS region where the stack will be deployed"
  type        = string
}

variable "project_name" {
  description = "Project identifier used for naming"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g. dev, qa, prod)"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block assigned to the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones leveraged by the deployment"
  type        = list(string)
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
}

variable "enable_nat_gateway" {
  description = "Provision NAT gateway for private subnet egress"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use a single shared NAT gateway"
  type        = bool
  default     = true
}

variable "repository_name" {
  description = "Name for the ECR repository"
  type        = string
}

variable "container_image_tag" {
  description = "Tag used when referencing the container image"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Application port exposed by the container"
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "Task level CPU reservation"
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Task level memory reservation"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of ECS task replicas"
  type        = number
  default     = 3
}

variable "custom_environment_variables" {
  description = "Additional environment variables injected into the container"
  type        = map(string)
  default     = {}
}

variable "log_retention_in_days" {
  description = "Retention applied to application CloudWatch log groups"
  type        = number
  default     = 731
}

variable "enable_execute_command" {
  description = "Enable ECS exec for troubleshooting"
  type        = bool
  default     = false
}

variable "ecs_capacity_providers" {
  description = "Capacity providers registered on the ECS cluster"
  type        = list(string)
  default     = ["FARGATE", "FARGATE_SPOT"]
}

variable "ecs_capacity_provider_strategy" {
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

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "dynamodb_partition_key" {
  description = "Partition key name"
  type        = string
}

variable "dynamodb_partition_key_type" {
  description = "Partition key type"
  type        = string
  default     = "S"
}

variable "dynamodb_sort_key" {
  description = "Optional sort key name"
  type        = string
  default     = ""
}

variable "dynamodb_sort_key_type" {
  description = "Sort key type"
  type        = string
  default     = "S"
}

variable "dynamodb_billing_mode" {
  description = "Billing mode for DynamoDB"
  type        = string
  default     = "PAY_PER_REQUEST"
}

variable "dynamodb_read_capacity" {
  description = "Read capacity units when using PROVISIONED"
  type        = number
  default     = 0
}

variable "dynamodb_write_capacity" {
  description = "Write capacity units when using PROVISIONED"
  type        = number
  default     = 0
}

variable "dynamodb_ttl_attribute" {
  description = "TTL attribute name"
  type        = string
  default     = ""
}

variable "dynamodb_point_in_time_recovery" {
  description = "Enable point in time recovery for DynamoDB"
  type        = bool
  default     = true
}

variable "api_stage_name" {
  description = "Stage name exposed by API Gateway"
  type        = string
  default     = "prod"
}

variable "api_route_key" {
  description = "Route key for the HTTP API"
  type        = string
  default     = "ANY /{proxy+}"
}

variable "tags" {
  description = "Extra tags propagated to every resource"
  type        = map(string)
  default     = {}
}

variable "ecr_image_retention" {
  description = "Number of images to retain in the ECR repository lifecycle policy"
  type        = number
  default     = 5
}
