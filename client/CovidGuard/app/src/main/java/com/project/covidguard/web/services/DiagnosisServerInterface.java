package com.project.covidguard.web.services;

import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.web.responses.UploadDiagnosisKeyResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DiagnosisServerInterface {

    String BASE_URL_PROD = "https://ens-server.ts.r.appspot.com";

    @Headers({"Content-Type: application/json"})
    @POST("upload-teks")
    Call<UploadDiagnosisKeyResponse> uploadTEKs(@Field("tan") String tan, @Body List<TEK> teks);
}
