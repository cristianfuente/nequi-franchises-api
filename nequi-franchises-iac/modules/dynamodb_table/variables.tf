variable "name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "billing_mode" {
  description = "Billing mode for the table (PAY_PER_REQUEST or PROVISIONED)"
  type        = string
  default     = "PAY_PER_REQUEST"
}

variable "read_capacity" {
  description = "Read capacity units when using PROVISIONED billing"
  type        = number
  default     = 0
}

variable "write_capacity" {
  description = "Write capacity units when using PROVISIONED billing"
  type        = number
  default     = 0
}

variable "hash_key" {
  description = "Primary partition key name"
  type        = string
}

variable "hash_key_type" {
  description = "Attribute type for the primary partition key (S, N, or B)"
  type        = string
  default     = "S"
}

variable "range_key" {
  description = "Optional sort key name"
  type        = string
  default     = ""
}

variable "range_key_type" {
  description = "Attribute type for the sort key"
  type        = string
  default     = "S"
}

variable "ttl_attribute" {
  description = "TTL attribute name, leave empty to disable TTL"
  type        = string
  default     = ""
}

variable "point_in_time_recovery" {
  description = "Enable point in time recovery"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags applied to DynamoDB resources"
  type        = map(string)
  default     = {}
}

variable "global_secondary_indexes" {
  description = "Definitions for global secondary indexes"
  type = list(object({
    name               = string
    hash_key           = string
    hash_key_type      = string
    range_key          = optional(string)
    range_key_type     = optional(string)
    projection_type    = string
    non_key_attributes = optional(list(string))
    read_capacity      = optional(number)
    write_capacity     = optional(number)
  }))
  default = []
}
