
resource "google_app_engine_application" "covidguard_appengine" {
  project     = google_project.covidguard.project_id
  location_id = var.prod_gcloud_region
  database_type = "CLOUD_FIRESTORE"
}
