package com.project.covidguard.web;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Async HttpTask that executes networks operations in an androidThread
 *
 * @param: Request OkHttpClient Request
 * @return: Future<Response> Observer to HttpResponse from okhttpClient
 */
public class AsyncHttpTask implements Callable {

    private final OkHttpClient client;

    private final Request request;

    public AsyncHttpTask(Request request) {
        this.request = request;
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new StethoInterceptor()).build();
    }

    @Override
    public Response call() throws IOException {
        return client.newCall(request).execute();
    }
}
