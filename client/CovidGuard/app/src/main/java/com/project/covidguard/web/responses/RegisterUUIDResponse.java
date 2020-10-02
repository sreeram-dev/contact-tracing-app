package com.project.covidguard.web.responses;


public class RegisterUUIDResponse {

    public Boolean success;
    public String token;

    public RegisterUUIDResponse(Boolean success, String token) {
        this.success = success;
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
