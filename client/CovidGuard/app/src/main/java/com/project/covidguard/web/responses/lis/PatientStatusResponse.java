package com.project.covidguard.web.responses.lis;

import com.project.covidguard.web.dto.PatientDTO;
import com.squareup.moshi.Json;

public class PatientStatusResponse {

    @Json(name = "success")
    Boolean success;

    @Json(name = "profile")
    PatientDTO patient;

    public PatientStatusResponse(Boolean success, PatientDTO patient) {
        this.success = success;
        this.patient = patient;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("success: " + this.success + "\n");
        sb.append("profile: " + this.patient.toString() + "\n");
        return sb.toString();
    }

    public PatientDTO getPatient() {
        return this.patient;
    }

    public Boolean isPositive() {
        return this.patient.getIsPositive();
    }

    public Boolean isRecovered() {
        return this.patient.getIsRecovered();
    }
}
