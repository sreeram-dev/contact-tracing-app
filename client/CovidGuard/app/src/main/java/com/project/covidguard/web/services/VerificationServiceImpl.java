package com.project.covidguard.web.services;


import com.project.covidguard.web.AsyncHttpTask;
import com.project.covidguard.web.constants.VerificationServer;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RegisterUUIDResponse;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;

public class VerificationServiceImpl implements VerificationService {


    private final Moshi moshi;
    private final JsonAdapter<RegisterUUIDResponse> responseJsonAdapter;
    private final JsonAdapter<ErrorResponse> errorResponseJsonAdapter;


    // TODO: Make json requests
    private static final MediaType JSONMediaType = MediaType.parse("application/json; charset=utf-8");


    public VerificationServiceImpl() {

        this.moshi = new Moshi.Builder().build();
        this.responseJsonAdapter = moshi.adapter(RegisterUUIDResponse.class);
        this.errorResponseJsonAdapter = moshi.adapter(ErrorResponse.class);
    }

    public String registerUUIDAndGetToken(String uuid) throws IOException {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        RequestBody formBody = new FormBody.Builder()
                .add("uuid", uuid)
                .build();

        Request request  = new Request.Builder()
                .url(VerificationServer.SERVER_URL_PROD + "/" + VerificationServer.REGISTER_UUID_URI)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        ExecutorService service = Executors.newSingleThreadExecutor();
        AsyncHttpTask task = new AsyncHttpTask(request);

        Future<Response> observer = service.submit(task);
        service.shutdown();
        try {
            Response response = observer.get();
            if (response == null || !response.isSuccessful()) {
                ErrorResponse err = this.errorResponseJsonAdapter.fromJson(response.body().source());
                throw new IOException("Request Failed: " + err);
            }

            RegisterUUIDResponse resp = this.responseJsonAdapter.fromJson(response.body().source());
            return resp.getToken();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        throw new IOException("Execution Failed: Internal Client Error");
    }
}
