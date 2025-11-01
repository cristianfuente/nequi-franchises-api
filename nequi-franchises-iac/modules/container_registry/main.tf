locals {
  common_tags = merge(
    {
      Terraform = "true"
      Component = var.name
    },
    var.tags,
  )
}

resource "aws_ecr_repository" "this" {
  name                 = var.name
  image_tag_mutability = var.image_tag_mutability

  image_scanning_configuration {
    scan_on_push = var.scan_on_push
  }

  tags = merge(
    local.common_tags,
    {
      Name = var.name
    },
  )
}

resource "aws_ecr_lifecycle_policy" "this" {
  count = var.lifecycle_policy != "" ? 1 : 0

  repository = aws_ecr_repository.this.name
  policy     = var.lifecycle_policy
}
