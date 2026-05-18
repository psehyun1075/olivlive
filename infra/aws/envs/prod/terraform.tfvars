env        = "prod"
aws_region = "ap-northeast-2"
owner      = "psehyun"
project    = "olive-live"

az_count = 2
vpc_cidr = "10.10.0.0/16"

nat_mode = "single"

cluster_name    = "olive-prod-eks"
cluster_version = "1.33"

eks_cluster_role_name = "olive-prod-eks-cluster-role"
eks_node_role_name    = "olive-prod-eks-node-role"

instance_types = ["t3.medium"]
desired_size   = 2
min_size       = 2
max_size       = 3
disk_size      = 20

endpoint_public_access  = true
endpoint_private_access = true

create_db_private_subnets = true
db_subnet_offset          = 20