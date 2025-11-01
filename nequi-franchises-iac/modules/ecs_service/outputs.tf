output "cluster_id" {
  description = "ID of the ECS cluster"
  value       = aws_ecs_cluster.this.id
}

output "service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.this.name
}

output "task_definition_arn" {
  description = "ARN for the created task definition"
  value       = aws_ecs_task_definition.this.arn
}

output "security_group_id" {
  description = "Security group attached to the ECS tasks"
  value       = aws_security_group.service.id
}

output "log_group_name" {
  description = "CloudWatch Logs group used by the service"
  value       = aws_cloudwatch_log_group.this.name
}
