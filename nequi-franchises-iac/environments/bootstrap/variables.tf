variable "aws_region" {
  description = "AWS region where the backend resources will be created"
  type        = string
}

variable "bucket_name" {
  description = "Name of the S3 bucket for Terraform remote state"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table used for Terraform state locking"
  type        = string
}

variable "tags" {
  description = "Tags applied to backend resources"
  type        = map(string)
  default     = {}
}
