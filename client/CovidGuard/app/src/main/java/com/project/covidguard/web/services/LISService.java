package com.project.covidguard.web.services;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;

import java.util.Date;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class LISService {
    private static Retrofit retrofit = null;

    public static LISServerInterface getService() {
        if (retrofit == null) {
            CertificatePinner pinner = new CertificatePinner.Builder()
                .add("*.appspot.com", "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
                .add("lis-server-289906.ts.r.appspot.com", "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
                .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .certificatePinner(pinner)
                    .addInterceptor(new StethoInterceptor()).build();
            Moshi moshi = new Moshi.Builder()
                .add(Date.class, new Rfc3339DateJsonAdapter())
                .build();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(LISServerInterface.BASE_URL_PROD)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build();
        }

        return retrofit.create(LISServerInterface.class);
    }
}
