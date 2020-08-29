package com.project.covidguard.web.responses;

public class ErrorResponse {
    public String code;
    public String name;
    public String description;

    public ErrorResponse(String code, String name, String description) {
        this.code =  code;
        this.name = name;
        this.description = description;
    }
}
