output "api_endpoint" {
  description = "Base invoke URL for the HTTP API"
  value       = aws_apigatewayv2_stage.this.invoke_url
}

output "api_id" {
  description = "Identifier of the HTTP API"
  value       = aws_apigatewayv2_api.this.id
}

output "vpc_link_id" {
  description = "Identifier for the VPC link used by the API"
  value       = aws_apigatewayv2_vpc_link.this.id
}
