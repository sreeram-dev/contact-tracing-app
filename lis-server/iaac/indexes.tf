
resource "google_firestore_index" "dev-consent-form" {
  project     = google_project.covidguard_lis_server.project_id

  collection = "dev_consent"

  fields {
    field_path = "host"
    order      = "ASCENDING"
  }

  fields {
    field_path = "revoked_at"
    order = "ASCENDING"
  }


  fields {
    field_path = "uuid"
    order      = "ASCENDING"
  }


  fields {
    field_path = "expired_at"
    order      = "ASCENDING"
  }

}


resource "google_firestore_index" "consent-form" {
  project     = google_project.covidguard_lis_server.project_id

  collection = "consent"

  fields {
    field_path = "host"
    order      = "ASCENDING"
  }

  fields {
    field_path = "revoked_at"
    order = "ASCENDING"
  }


  fields {
    field_path = "uuid"
    order      = "ASCENDING"
  }

  fields {
    field_path = "expired_at"
    order      = "ASCENDING"
  }
}

