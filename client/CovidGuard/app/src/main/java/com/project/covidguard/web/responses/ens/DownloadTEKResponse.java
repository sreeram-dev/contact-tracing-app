package com.project.covidguard.web.responses.ens;

import androidx.core.util.Pair;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class DownloadTEKResponse {

    @Json(name = "teks")
    List<TEKDTO> teks;

    public DownloadTEKResponse(List<DownloadTEKResponse.TEKDTO> teks) {
        this.teks = teks;
    }

    public List<Pair<String, Long>> getTEKsWithENIN() {
        List<Pair<String, Long>> result = new ArrayList<>();

        for (TEKDTO tek: teks) {
            result.add(tek.getPair());
        }

        return result;
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

        public Pair<String, Long> getPair() {
            return new Pair(this.tek, this.en_interval_number);
        }
    }
}
