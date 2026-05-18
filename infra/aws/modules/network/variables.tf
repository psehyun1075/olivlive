variable "env" {
  type = string
}

variable "vpc_cidr" {
  type = string
}

variable "az_count" {
  type = number
}

variable "nat_mode" {
  type    = string
  default = "single" # single | per_az
  validation {
    condition     = contains(["single", "per_az"], var.nat_mode)
    error_message = "nat_mode must be one of: single, per_az"
  }
}

# EKS 서브넷 태그용(선택)
variable "cluster_name" {
  type    = string
  default = null
}

# DB 전용 private subnet 생성 여부
variable "create_db_private_subnets" {
  type    = bool
  default = false
}

# /16 VPC를 /24로 나눌 때 DB subnet 시작 오프셋
# 예: 20 -> 10.10.20.0/24, 10.10.21.0/24
variable "db_subnet_offset" {
  type    = number
  default = 20
}