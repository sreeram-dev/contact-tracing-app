terraform {
    backend "gcs" {
        bucket = "tf-state-prod-covidguard"
        prefix = "terraform/state"
    }
}
