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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("code: " + code + "\n");
        sb.append("name: " + name + "\n");
        sb.append("description: " + description + "\n");
        return sb.toString();
    }
}
