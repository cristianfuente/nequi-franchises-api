output "security_group_id" {
  description = "Security group protecting the load balancer"
  value       = aws_security_group.alb.id
}

output "load_balancer_arn" {
  description = "ARN of the created load balancer"
  value       = aws_lb.this.arn
}

output "load_balancer_dns_name" {
  description = "Internal DNS name for the load balancer"
  value       = aws_lb.this.dns_name
}

output "target_group_arn" {
  description = "Target group ARN attached to the listener"
  value       = aws_lb_target_group.this.arn
}

output "listener_arn" {
  description = "Listener ARN for API Gateway integration"
  value       = aws_lb_listener.this.arn
}
