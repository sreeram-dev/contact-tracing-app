package com.project.covidguard.web.responses;

import com.squareup.moshi.Json;

public class UploadDiagnosisKeyResponse {

    @Json(name = "success")
    Boolean success;

    @Json(name = "message")
    String message;

    public UploadDiagnosisKeyResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("success: " + success + "\n");
        sb.append("message: " + message + "\n");
        return sb.toString();
    }
}
