aws_region            = "us-east-1"
project_name          = "nequi-franchises"
environment           = "dev"
availability_zones    = ["us-east-1a", "us-east-1b"]
public_subnet_cidrs   = ["10.0.0.0/24", "10.0.1.0/24"]
private_subnet_cidrs  = ["10.0.10.0/24", "10.0.11.0/24"]
repository_name       = "nequi-franchises-api"
container_image_tag   = "latest"
dynamodb_table_name   = "core_table"
dynamodb_partition_key = "id"
api_stage_name        = "$default"
tags = {
  Owner = "platform-team"
}
