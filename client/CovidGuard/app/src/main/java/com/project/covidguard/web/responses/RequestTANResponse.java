package com.project.covidguard.web.responses;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.threeten.bp.LocalDateTime;

public class RequestTANResponse {

    @Json(name = "success")
    public Boolean success;

    @Json(name = "tan")
    public String TAN;

    @Json(name = "expired-at")
    public LocalDateTime expiredAt;


    public RequestTANResponse(Boolean success, String tan, LocalDateTime expiredAt) {
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
}
