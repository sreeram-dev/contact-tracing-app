package com.project.covidguard.web.services;

import com.project.covidguard.web.responses.verification.RegisterUUIDResponse;
import com.project.covidguard.web.responses.verification.RequestTANResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface VerificationEndpointInterface {

    String BASE_URL_PROD = "https://covidgaurd-285412.ts.r.appspot.com";

    @FormUrlEncoded
    @POST("register-uuid")
    Call<RegisterUUIDResponse> registerUUID(@Field("uuid") String uuid);

    @FormUrlEncoded
    @POST("request-tan")
    Call<RequestTANResponse> requestTAN(@Field("token") String token);
}
