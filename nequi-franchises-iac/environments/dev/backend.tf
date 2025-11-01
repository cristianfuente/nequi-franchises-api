terraform {
  backend "s3" {
    bucket         = "nequi-franchises-terraform-state-698322417763"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "nequi-franchises-terraform-locks"
    encrypt        = true
  }
}
