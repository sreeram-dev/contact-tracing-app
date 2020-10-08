package com.project.covidguard.web.services;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.moshi.Moshi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class ExposureNotificationService {
    private static Retrofit retrofit = null;

    public static DiagnosisServerInterface getService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor()).build();
            Moshi moshi = new Moshi.Builder().build();
            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(DiagnosisServerInterface.BASE_URL_PROD)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build();
        }

        return retrofit.create(DiagnosisServerInterface.class);
    }

}
