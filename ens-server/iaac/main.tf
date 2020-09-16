provider "google" {
  project = "ens-server"
  region  = var.prod_gcloud_region
  zone    = var.prod_gcloud_zone
}

resource "google_project" "covidguard_ens_server" {
  name = "ENS Server"
  project_id = "ens-server"
  billing_account = "012ADE-0F81EE-F7AEC5"
}


module "covidguard-ens-project-services" {
  source  = "terraform-google-modules/project-factory/google//modules/project_services"
  version = "9.0.0"
  project_id = google_project.covidguard_ens_server.project_id
  # insert the 2 required variables here
  activate_apis = [
    "appengine.googleapis.com", # App Engine API
    "cloudapis.googleapis.com",         # Google Cloud APIs
    "iam.googleapis.com",               # Identity and Access Management (IAM) API
    "iamcredentials.googleapis.com",    # IAM Service Account Credentials API
    "logging.googleapis.com",           # Stackdriver Logging API
    "monitoring.googleapis.com",        # Stackdriver Monitoring API
    "servicemanagement.googleapis.com", # Service Management API
    "serviceusage.googleapis.com",      # Service Usage API
    "sql-component.googleapis.com",     # Cloud SQL
    "storage-api.googleapis.com",       # Google Cloud Storage JSON API
    "storage-component.googleapis.com", # Cloud Storage
    "cloudbilling.googleapis.com",
    "cloudbuild.googleapis.com",
    "secretmanager.googleapis.com",
  ]

  disable_dependent_services = true
  disable_services_on_destroy = true
  enable_apis = true
}
