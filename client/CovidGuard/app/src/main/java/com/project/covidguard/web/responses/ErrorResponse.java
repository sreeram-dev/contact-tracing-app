package com.project.covidguard.web.responses;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okio.BufferedSource;

public class ErrorResponse {

    @Json(name = "code")
    public String code;

    @Json(name = "name")
    public String name;

    @Json(name = "description")
    public String description;

    @Json(name = "traceback")
    public String traceback;

    public ErrorResponse(String code, String name, String description, String traceback) {
        this.code =  code;
        this.name = name;
        this.description = description;
        this.traceback = traceback;
    }

    public static JsonAdapter<ErrorResponse> getAdapter() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<ErrorResponse> errorResponseJsonAdapter = moshi.adapter(ErrorResponse.class);
        return errorResponseJsonAdapter;
    }

    public static ErrorResponse buildFromSource(BufferedSource source) throws IOException {
        JsonAdapter<ErrorResponse> responseJsonAdapter = getAdapter();
        return responseJsonAdapter.fromJson(source);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("code: " + code + "\n");
        sb.append("name: " + name + "\n");
        sb.append("description: " + description + "\n");
        sb.append("traceback: " + traceback + "\n");
        return sb.toString();
    }
}
