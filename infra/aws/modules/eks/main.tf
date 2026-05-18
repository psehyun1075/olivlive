data "aws_iam_role" "cluster" {
  name = var.eks_cluster_role_name
}

data "aws_iam_role" "node" {
  name = var.eks_node_role_name
}

resource "aws_eks_cluster" "this" {
  name     = var.cluster_name
  role_arn = data.aws_iam_role.cluster.arn
  version  = var.cluster_version

  vpc_config {
    subnet_ids              = var.private_subnet_ids
    endpoint_public_access  = var.endpoint_public_access
    endpoint_private_access = var.endpoint_private_access
  }
}

resource "aws_eks_node_group" "default" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.cluster_name}-ng"
  node_role_arn   = data.aws_iam_role.node.arn

  subnet_ids = var.private_subnet_ids

  instance_types = var.instance_types
  disk_size      = var.disk_size
  capacity_type  = "ON_DEMAND"

  scaling_config {
    desired_size = var.desired_size
    min_size     = var.min_size
    max_size     = var.max_size
  }

  update_config {
    max_unavailable = 1
  }

  depends_on = [aws_eks_cluster.this]
}

