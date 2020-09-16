terraform {
    backend "gcs" {
        bucket = "tf-state-ens-server-prod-covidguard"
        prefix = "terraform/state"
    }
}
