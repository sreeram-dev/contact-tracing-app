package com.project.covidguard.web.services;

import com.project.covidguard.web.responses.lis.PatientStatusResponse;
import com.project.covidguard.web.responses.lis.RegisterPatientResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LISServerInterface {
    String BASE_URL_PROD = "https://lis-server-289906.ts.r.appspot.com";

    @FormUrlEncoded
    @POST("register-patient")
    Call<RegisterPatientResponse> registerPatient(@Field("uuid") String uuid);

    @FormUrlEncoded
    @GET("get-patient-status")
    Call<PatientStatusResponse> getPatientStatus(@Query("uuid") String uuid);
}
