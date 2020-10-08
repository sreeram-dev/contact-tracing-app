package com.project.covidguard.web.dto;

import com.project.covidguard.data.entities.TEK;
import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class UploadTEKRequest {

    @Json(name = "tan")
    public String tan;

    @Json(name = "teks")
    public List<TEKDTO> teks;

    public UploadTEKRequest(String tan, List<TEKDTO> teks) {
        this.tan = tan;
        this.teks = teks;
    }

    public static UploadTEKRequest buildRequest(String tan, List<TEK> teks) {
        List<TEKDTO> tekDTOs = new ArrayList<>();

        for (TEK tek: teks) {
            tekDTOs.add(new TEKDTO(tek.getTekId(), tek.getEnIntervalNumber()));
        }

        return new UploadTEKRequest(tan, tekDTOs);
    }
}
