variable "name" {
  description = "Name of the API Gateway HTTP API"
  type        = string
}

variable "integration_uri" {
  description = "ARN for the ALB listener used as integration endpoint"
  type        = string
}

variable "vpc_link_subnet_ids" {
  description = "Subnets where the VPC link will create ENIs"
  type        = list(string)
}

variable "vpc_link_security_group_ids" {
  description = "Security groups attached to the VPC link"
  type        = list(string)
}

variable "stage_name" {
  description = "Stage name exposed by API Gateway"
  type        = string
  default     = "prod"
}

variable "route_key" {
  description = "Route key for the default proxy integration"
  type        = string
  default     = "ANY /{proxy+}"
}

variable "tags" {
  description = "Tags applied to API Gateway resources"
  type        = map(string)
  default     = {}
}
