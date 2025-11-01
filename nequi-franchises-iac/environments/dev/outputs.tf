output "api_endpoint" {
  description = "Invoke URL for the API Gateway stage"
  value       = module.api_gateway.api_endpoint
}

output "ecr_repository_url" {
  description = "ECR repository URL used by the service"
  value       = module.container_registry.repository_url
}

output "load_balancer_dns_name" {
  description = "Internal DNS name for the application load balancer"
  value       = module.load_balancer.load_balancer_dns_name
}

output "ecs_service_name" {
  description = "Deployed ECS service name"
  value       = module.ecs_service.service_name
}

output "dynamodb_table_name" {
  description = "Provisioned DynamoDB table"
  value       = module.dynamodb.table_name
}
