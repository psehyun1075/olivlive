terraform {
  backend "s3" {
    bucket         = "olive-live-tfstate-890742607521-apne2"
    key            = "envs/prod/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "olive-live-tflock"
    encrypt        = true
  }
}