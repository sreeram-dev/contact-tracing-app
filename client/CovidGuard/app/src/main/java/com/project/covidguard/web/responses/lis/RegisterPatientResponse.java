package com.project.covidguard.web.responses.lis;

import com.project.covidguard.web.dto.PatientDTO;
import com.squareup.moshi.Json;

public class RegisterPatientResponse {

    @Json(name = "success")
    Boolean success;

    @Json(name = "message")
    String message;

    @Json(name = "profile")
    PatientDTO patient;

    public RegisterPatientResponse(Boolean success, String message, PatientDTO profile) {
        this.success = success;
        this.message = message;
        this.patient = profile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("success: " + this.success + "\n");
        sb.append("message: " + this.message + "\n");
        sb.append("profile: " + this.patient.toString() + "\n");
        return sb.toString();
    }

    public String getId() {
        return this.patient.getId();
    }
}
