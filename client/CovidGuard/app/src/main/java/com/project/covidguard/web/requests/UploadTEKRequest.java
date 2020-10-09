package com.project.covidguard.web.requests;

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

    static class TEKDTO {

        @Json(name = "tek")
        public String tek;

        @Json(name = "en_interval_number")
        public Long en_interval_number;

        public TEKDTO(String tek, Long en_interval_number) {
            this.tek = tek.trim();
            this.en_interval_number = en_interval_number;
        }
    }

}
