
resource "google_app_engine_application" "lisserver_appengine" {
  project     = google_project.covidguard_lis_server.project_id
  location_id = var.prod_gcloud_region
  database_type = "CLOUD_FIRESTORE"
}
