package com.project.covidguard.web.services;

import com.project.covidguard.web.responses.RegisterUUIDResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface VerificationEndpointInterface {

    String BASE_URL_PROD = "https://covidgaurd-285412.ts.r.appspot.com";

    @FormUrlEncoded
    @POST("register-uuid")
    Call<RegisterUUIDResponse> registerUUID(@Field("uuid") String uuid);
}
