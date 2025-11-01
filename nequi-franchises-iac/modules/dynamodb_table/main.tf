locals {
  common_tags = merge(
    {
      Terraform = "true"
      Component = var.name
    },
    var.tags,
  )

  gsi_attributes = flatten([
    for index in var.global_secondary_indexes : concat(
      [
        {
          name = index.hash_key
          type = index.hash_key_type
        }
      ],
      try(index.range_key, null) != null ? [
        {
          name = index.range_key
          type = try(index.range_key_type, null)
        }
      ] : []
    )
  ])

  attributes = distinct(concat(
    [
      {
        name = var.hash_key
        type = var.hash_key_type
      }
    ],
    var.range_key != "" ? [
      {
        name = var.range_key
        type = var.range_key_type
      }
    ] : [],
    local.gsi_attributes,
  ))
}

resource "aws_dynamodb_table" "this" {
  name         = var.name
  billing_mode = var.billing_mode
  hash_key     = var.hash_key
  range_key    = var.range_key != "" ? var.range_key : null

  dynamic "attribute" {
    for_each = { for attr in local.attributes : attr.name => attr }

    content {
      name = attribute.value.name
      type = attribute.value.type
    }
  }

  read_capacity  = var.billing_mode == "PROVISIONED" ? var.read_capacity : null
  write_capacity = var.billing_mode == "PROVISIONED" ? var.write_capacity : null

  dynamic "point_in_time_recovery" {
    for_each = var.point_in_time_recovery ? [true] : []

    content {
      enabled = true
    }
  }

  dynamic "ttl" {
    for_each = var.ttl_attribute != "" ? [var.ttl_attribute] : []

    content {
      attribute_name = ttl.value
      enabled        = true
    }
  }

  dynamic "global_secondary_index" {
    for_each = { for index in var.global_secondary_indexes : index.name => index }

    content {
      name               = global_secondary_index.value.name
      hash_key           = global_secondary_index.value.hash_key
      range_key          = try(global_secondary_index.value.range_key, null)
      projection_type    = global_secondary_index.value.projection_type
      non_key_attributes = try(global_secondary_index.value.non_key_attributes, null)
      read_capacity      = var.billing_mode == "PROVISIONED" ? try(global_secondary_index.value.read_capacity, null) : null
      write_capacity     = var.billing_mode == "PROVISIONED" ? try(global_secondary_index.value.write_capacity, null) : null
    }
  }

  tags = merge(
    local.common_tags,
    {
      Name = var.name
    },
  )
}
