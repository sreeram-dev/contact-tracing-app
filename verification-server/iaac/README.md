Server Infrastructure for CovidGuard
==================================================

Terraform is being used to create the infrastructure on Google Cloud. All
infrastructure is created using Google Cloud Service Account
(terraform-automation) which is a robot account.

Terraform state is stored remotely and encrypted using symmetric keys which is
rotated every 90 days automatically.  It is stored

To use terraform,

1. Authenticate using gcloud SDK (https://cloud.google.com/sdk)
    `gcloud auth application-default login`
2. Ask owners (Sreeram, Lokesh) to add you as service account user and service
   account token creator
3. Run `terraform init`

Authentication to use the resources is done using Service Account
Impersonation. (https://cloud.google.com/iam/docs/impersonating-service-accounts#iam-service-accounts-grant-role-parent-gcloud)


To deploy,
1. Use `terraform plan` to know what resources are being created
2. Use `terraform apply` to actually apply the resources

If apply fails, let us know. Always create a new branch to make new changes so
`master` branch can contain the stable code.

If any issues, Contact Sreeram (a1775690@student.adelaide.edu.au)
