
resource "google_app_engine_application" "ensserver_appengine" {
  project     = google_project.covidguard_ens_server.project_id
  location_id = var.prod_gcloud_region
  database_type = "CLOUD_FIRESTORE"
}
