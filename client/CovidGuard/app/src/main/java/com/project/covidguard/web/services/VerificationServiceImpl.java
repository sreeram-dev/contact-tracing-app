package com.project.covidguard.web.services;


import com.project.covidguard.web.constants.VerificationServer;
import com.project.covidguard.web.requests.RegisterUUIDRequest;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RegisterUUIDResponse;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;

public class VerificationServiceImpl implements VerificationService {

    private final OkHttpClient client;
    private final Moshi moshi;
    private final JsonAdapter<RegisterUUIDResponse> responseJsonAdapter;
    private final JsonAdapter<ErrorResponse> errorResponseJsonAdapter;

    // TODO: Make json requests
    private static final MediaType JSONMediaType = MediaType.parse("application/json; charset=utf-8");


    public VerificationServiceImpl() {

        this.client = new OkHttpClient();
        this.moshi = new Moshi.Builder().build();
        this.responseJsonAdapter = moshi.adapter(RegisterUUIDResponse.class);
        this.errorResponseJsonAdapter = moshi.adapter(ErrorResponse.class);
    }

    public String registerUUIDAndGetToken(String uuid) throws IOException {

        RequestBody formBody = new FormBody.Builder()
                .add("uuid", uuid)
                .build();

        Request request  = new Request.Builder()
                .url(VerificationServer.SERVER_URL_PROD)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            ErrorResponse err = this.errorResponseJsonAdapter.fromJson(response.body().source());
            throw new IOException("Request Failed: " + err);
        }

        RegisterUUIDResponse resp = this.responseJsonAdapter.fromJson(response.body().source());

        return resp.getToken();
    }


}
