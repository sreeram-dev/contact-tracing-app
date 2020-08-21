provider "google" {
  project = "covidgaurd-285412"
  region  = var.prod_gcloud_region
  zone    = var.prod_gcloud_zone
}
