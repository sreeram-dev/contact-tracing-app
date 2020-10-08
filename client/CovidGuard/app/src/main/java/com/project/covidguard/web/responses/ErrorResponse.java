package com.project.covidguard.web.responses;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okio.BufferedSource;

public class ErrorResponse {
    public String code;
    public String name;
    public String description;

    public ErrorResponse(String code, String name, String description) {
        this.code =  code;
        this.name = name;
        this.description = description;
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
        return sb.toString();
    }
}
