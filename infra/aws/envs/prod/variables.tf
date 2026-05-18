variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "env" {
  type    = string
  default = "prod"
}

variable "owner" {
  type    = string
  default = "psehyun"
}

variable "project" {
  type    = string
  default = "olive-live"
}

variable "az_count" {
  type    = number
  default = 2
  validation {
    condition     = var.az_count >= 2
    error_message = "EKS 때문에 az_count는 2 이상이어야 합니다."
  }
}

variable "vpc_cidr" {
  type    = string
  default = "10.10.0.0/16"
}

variable "nat_mode" {
  type    = string
  default = "single"
  validation {
    condition     = contains(["single", "per_az"], var.nat_mode)
    error_message = "nat_mode must be one of: single, per_az"
  }
}

variable "cluster_name" {
  type    = string
  default = "olive-prod-eks"
}

variable "cluster_version" {
  type    = string
  default = "1.33"
}

variable "eks_cluster_role_name" {
  type    = string
  default = "olive-prod-eks-cluster-role"
}

variable "eks_node_role_name" {
  type    = string
  default = "olive-prod-eks-node-role"
}

variable "instance_types" {
  type    = list(string)
  default = ["t3.medium"]
}

variable "desired_size" {
  type    = number
  default = 2
}

variable "min_size" {
  type    = number
  default = 2
}

variable "max_size" {
  type    = number
  default = 3
}

variable "disk_size" {
  type    = number
  default = 20
}

variable "endpoint_public_access" {
  type    = bool
  default = true
}

variable "endpoint_private_access" {
  type    = bool
  default = true
}

variable "create_db_private_subnets" {
  type    = bool
  default = true
}

variable "db_subnet_offset" {
  type    = number
  default = 20
}