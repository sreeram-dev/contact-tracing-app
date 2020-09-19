terraform {
    backend "gcs" {
        bucket = "tf-state-lis-server-prod-covidguard"
        prefix = "terraform/state"
    }
}
