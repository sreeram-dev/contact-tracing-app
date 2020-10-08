package com.project.covidguard.web.services;

import com.project.covidguard.web.dto.UploadTEKRequest;
import com.project.covidguard.web.responses.UploadDiagnosisKeyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DiagnosisServerInterface {

    String BASE_URL_PROD = "https://ens-server.ts.r.appspot.com";

    @Headers({"Content-Type: application/json"})
    @POST("upload-diagnosis-keys")
    Call<UploadDiagnosisKeyResponse> uploadTEKs(@Body UploadTEKRequest request);
}
