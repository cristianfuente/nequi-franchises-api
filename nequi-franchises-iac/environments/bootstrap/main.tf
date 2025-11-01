module "remote_state" {
  source = "../../modules/state_backend"

  bucket_name          = var.bucket_name
  dynamodb_table_name  = var.dynamodb_table_name
  tags                 = merge(
    {
      Name        = var.bucket_name
      Environment = "bootstrap"
      Terraform   = "true"
    },
    var.tags,
  )
}
