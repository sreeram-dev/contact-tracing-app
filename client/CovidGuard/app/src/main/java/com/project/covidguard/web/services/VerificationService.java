package com.project.covidguard.web.services;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;


import java.util.Date;

import okhttp3.OkHttpClient;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class VerificationService {

    private static Retrofit retrofit = null;

    public static VerificationEndpointInterface getService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor()).build();
            Moshi moshi = new Moshi.Builder()
                    .add(Date.class, new Rfc3339DateJsonAdapter())
                    .build();
            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(VerificationEndpointInterface.BASE_URL_PROD)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build();
        }

        return retrofit.create(VerificationEndpointInterface.class);
    }
}
