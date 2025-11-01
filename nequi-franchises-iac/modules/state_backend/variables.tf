variable "bucket_name" {
  description = "Name of the S3 bucket used to store Terraform state"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table used for Terraform state locking"
  type        = string
}

variable "tags" {
  description = "Tags applied to the state backend resources"
  type        = map(string)
  default     = {}
}
