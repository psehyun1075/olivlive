output "vpc_id" {
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  value = [for s in aws_subnet.public : s.id]
}

output "private_subnet_ids" {
  value = [for s in aws_subnet.private : s.id]
}

output "db_private_subnet_ids" {
  value = [for s in aws_subnet.db_private : s.id]
}

output "db_private_subnet_names" {
  value = [for s in aws_subnet.db_private : s.tags["Name"]]
}

output "db_private_subnet_cidrs" {
  value = [for s in aws_subnet.db_private : s.cidr_block]
}

output "db_private_route_table_id" {
  value = try(aws_route_table.db_private[0].id, null)
}

output "azs" {
  value = local.azs
}