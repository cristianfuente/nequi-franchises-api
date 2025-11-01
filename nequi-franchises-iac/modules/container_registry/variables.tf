variable "name" {
  description = "Name of the ECR repository"
  type        = string
}

variable "image_tag_mutability" {
  description = "Image tag mutability setting for the ECR repository"
  type        = string
  default     = "MUTABLE"
}

variable "scan_on_push" {
  description = "Enable image scanning on push"
  type        = bool
  default     = true
}

variable "lifecycle_policy" {
  description = "JSON lifecycle policy to attach to the repository"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Tags applied to the ECR repository"
  type        = map(string)
  default     = {}
}
