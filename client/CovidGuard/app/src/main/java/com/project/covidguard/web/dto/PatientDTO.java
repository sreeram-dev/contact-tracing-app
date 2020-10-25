package com.project.covidguard.web.dto;

import com.squareup.moshi.Json;

import java.util.Date;

public class PatientDTO {

    @Json(name = "id")
    String id;

    @Json(name = "created_at")
    Date createdAt;

    @Json(name = "is_positive")
    Boolean isPositive;

    @Json(name = "is_recovered")
    Boolean isRecovered;

    @Json(name = "positive_at")
    Date positiveAt;

    @Json(name = "recovered_at")
    Date recoveredAt;

    @Json(name = "uuid")
    String uuid;

    public PatientDTO(String id, Date createdAt, Boolean isPositive, Boolean isRecovered,
                      Date positiveAt, Date recoveredAt, String uuid) {
        this.id = id;
        this.createdAt = createdAt;
        this.isPositive = isPositive;
        this.isRecovered = isRecovered;
        this.positiveAt = positiveAt;
        this.recoveredAt = recoveredAt;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id + "\n");
        sb.append("isPositive: " + isPositive + "\n");
        sb.append("isRecovered: " + isRecovered + "\n");
        return sb.toString();
    }

    public String getId() {
        return this.id;
    }

    public Boolean getIsPositive() {
        return this.isPositive;
    }

    public Boolean getIsRecovered() {
        return this.isRecovered;
    }
}
