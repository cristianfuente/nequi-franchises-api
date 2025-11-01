variable "name" {
  description = "Short name used to tag and identify networking resources"
  type        = string
}

variable "cidr_block" {
  description = "CIDR block assigned to the VPC"
  type        = string
}

variable "azs" {
  description = "List of availability zones to spread subnets across"
  type        = list(string)
}

variable "public_subnet_cidrs" {
  description = "List of CIDR blocks for the public subnets"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "List of CIDR blocks for the private subnets"
  type        = list(string)
}

variable "enable_nat_gateway" {
  description = "Whether to provision a NAT gateway for private subnet egress"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Create a single NAT gateway instead of one per availability zone"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags to apply to networking resources"
  type        = map(string)
  default     = {}
}
