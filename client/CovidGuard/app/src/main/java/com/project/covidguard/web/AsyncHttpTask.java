package com.project.covidguard.web;

import com.squareup.moshi.Moshi;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Async HttpHandler that executes networks operations in an androidThread
 *
 * @param: Request OkHttpClient Request
 * @return: Future<Response> Observer to HttpResponse from okhttpClient
 */
public class AsyncHttpClient implements Callable {

    private final OkHttpClient client;
    private final Moshi moshi;

    @Override
    public Response call() throws Exception {

        return null;
    }
}
