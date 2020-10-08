package com.project.covidguard.web.responses;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonClass;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Date;

public class RequestTANResponse {

    @Json(name = "success")
    public Boolean success;

    @Json(name = "tan")
    public String TAN;

    @Json(name = "expired-at")
    public Date expiredAt;


    public RequestTANResponse(Boolean success, String tan,  Date expiredAt) {
        this.success = success;
        this.TAN = tan;
        this.expiredAt = expiredAt;
    }


    public static JsonAdapter<RequestTANResponse> getAdapter() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<RequestTANResponse> responseJsonAdapter = moshi.adapter(RequestTANResponse.class);
        return responseJsonAdapter;
    }

    public String getTAN() {
        return this.TAN;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("success: " + this.success + "\n");
        sb.append("tan: " + this.TAN + "\n");
        sb.append("expired-at: " + this.expiredAt + "\n");
        return sb.toString();
    }
}
