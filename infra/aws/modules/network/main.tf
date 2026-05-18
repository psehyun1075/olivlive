data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  azs = slice(data.aws_availability_zones.available.names, 0, var.az_count)

  public_subnets     = [for i in range(var.az_count) : cidrsubnet(var.vpc_cidr, 8, i)]
  private_subnets    = [for i in range(var.az_count) : cidrsubnet(var.vpc_cidr, 8, i + 10)]
  db_private_subnets = [for i in range(var.az_count) : cidrsubnet(var.vpc_cidr, 8, i + var.db_subnet_offset)]
}

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "olive-${var.env}-vpc"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "olive-${var.env}-igw"
  }
}

resource "aws_subnet" "public" {
  for_each = { for idx, az in local.azs : tostring(idx) => az }

  vpc_id                  = aws_vpc.this.id
  availability_zone       = each.value
  cidr_block              = local.public_subnets[tonumber(each.key)]
  map_public_ip_on_launch = true

  tags = merge(
    {
      Name = "olive-${var.env}-public-${each.value}"
      Tier = "public"
    },
    var.cluster_name != null ? {
      "kubernetes.io/cluster/${var.cluster_name}" = "shared"
      "kubernetes.io/role/elb"                    = "1"
    } : {}
  )
}

resource "aws_subnet" "private" {
  for_each = { for idx, az in local.azs : tostring(idx) => az }

  vpc_id            = aws_vpc.this.id
  availability_zone = each.value
  cidr_block        = local.private_subnets[tonumber(each.key)]

  tags = merge(
    {
      Name = "olive-${var.env}-private-${each.value}"
      Tier = "private"
    },
    var.cluster_name != null ? {
      "kubernetes.io/cluster/${var.cluster_name}" = "shared"
      "kubernetes.io/role/internal-elb"           = "1"
    } : {}
  )
}

resource "aws_subnet" "db_private" {
  for_each = var.create_db_private_subnets ? { for idx, az in local.azs : tostring(idx) => az } : {}

  vpc_id            = aws_vpc.this.id
  availability_zone = each.value
  cidr_block        = local.db_private_subnets[tonumber(each.key)]

  map_public_ip_on_launch = false

  tags = {
    Name = "olive-${var.env}-db-private-${each.value}"
    Tier = "db"
  }
}

# Public RT
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "olive-${var.env}-rt-public"
  }
}

resource "aws_route" "public_inet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.igw.id
}

resource "aws_route_table_association" "public" {
  for_each = aws_subnet.public

  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}

# App Private RT (AZ별 하나씩)
resource "aws_route_table" "private" {
  for_each = aws_subnet.private
  vpc_id   = aws_vpc.this.id

  tags = {
    Name = "olive-${var.env}-rt-private-${each.value.availability_zone}"
  }
}

resource "aws_route_table_association" "private" {
  for_each = aws_subnet.private

  subnet_id      = each.value.id
  route_table_id = aws_route_table.private[each.key].id
}

# DB Private RT (하나)
resource "aws_route_table" "db_private" {
  count  = var.create_db_private_subnets ? 1 : 0
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "olive-${var.env}-rt-db-private"
  }
}

resource "aws_route_table_association" "db_private" {
  for_each = aws_subnet.db_private

  subnet_id      = each.value.id
  route_table_id = aws_route_table.db_private[0].id
}

# =========================
# NAT: single(1개) / per_az(az_count개)
# =========================
locals {
  nat_targets = var.nat_mode == "per_az" ? aws_subnet.public : { "0" = aws_subnet.public["0"] }
}

resource "aws_eip" "nat" {
  for_each = local.nat_targets
  domain   = "vpc"

  tags = {
    Name = "olive-${var.env}-nat-eip-${each.value.availability_zone}"
  }
}

resource "aws_nat_gateway" "nat" {
  for_each = local.nat_targets

  allocation_id = aws_eip.nat[each.key].id
  subnet_id     = each.value.id

  tags = {
    Name = "olive-${var.env}-nat-${each.value.availability_zone}"
  }

  depends_on = [aws_internet_gateway.igw]
}

# 각 app private route table(AZ별)에 NAT 라우팅
resource "aws_route" "private_nat" {
  for_each = aws_route_table.private

  route_table_id         = each.value.id
  destination_cidr_block = "0.0.0.0/0"

  # per_az면 같은 key의 NAT, single이면 "0" NAT로 통일
  nat_gateway_id = var.nat_mode == "per_az" ? aws_nat_gateway.nat[each.key].id : aws_nat_gateway.nat["0"].id
}