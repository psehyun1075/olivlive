module "network" {
  source = "../../modules/network"

  env          = var.env
  vpc_cidr     = var.vpc_cidr
  az_count     = var.az_count
  nat_mode     = var.nat_mode
  cluster_name = var.cluster_name

  create_db_private_subnets = var.create_db_private_subnets
  db_subnet_offset          = var.db_subnet_offset
}

module "eks" {
  source = "../../modules/eks"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids

  eks_cluster_role_name = var.eks_cluster_role_name
  eks_node_role_name    = var.eks_node_role_name

  instance_types = var.instance_types
  desired_size   = var.desired_size
  min_size       = var.min_size
  max_size       = var.max_size
  disk_size      = var.disk_size

  endpoint_public_access  = var.endpoint_public_access
  endpoint_private_access = var.endpoint_private_access
}