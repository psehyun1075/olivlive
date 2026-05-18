output "vpc_id" {
  value = module.network.vpc_id
}

output "public_subnet_ids" {
  value = module.network.public_subnet_ids
}

output "private_subnet_ids" {
  value = module.network.private_subnet_ids
}

output "db_private_subnet_ids" {
  value = module.network.db_private_subnet_ids
}

output "db_private_subnet_names" {
  value = module.network.db_private_subnet_names
}

output "db_private_subnet_cidrs" {
  value = module.network.db_private_subnet_cidrs
}

output "db_private_route_table_id" {
  value = module.network.db_private_route_table_id
}