output "vpc_id" {
  description = "Identifier of the created VPC"
  value       = aws_vpc.this.id
}

output "cidr_block" {
  description = "CIDR block configured on the VPC"
  value       = aws_vpc.this.cidr_block
}

output "public_subnet_ids" {
  description = "Identifiers of the public subnets"
  value       = values(aws_subnet.public)[*].id
}

output "private_subnet_ids" {
  description = "Identifiers of the private subnets"
  value       = values(aws_subnet.private)[*].id
}

output "public_subnet_cidr_blocks" {
  description = "CIDR blocks for the public subnets"
  value       = values(aws_subnet.public)[*].cidr_block
}

output "private_subnet_cidr_blocks" {
  description = "CIDR blocks for the private subnets"
  value       = values(aws_subnet.private)[*].cidr_block
}

output "nat_gateway_ids" {
  description = "Identifiers for created NAT gateways"
  value       = aws_nat_gateway.this[*].id
}
