package com.project.covidguard.web.dto;

import com.squareup.moshi.Json;

public class TEKDTO {

    @Json(name = "tek")
    public String tek;

    @Json(name = "en_interval_number")
    public Long en_interval_number;

    public TEKDTO(String tek, Long en_interval_number) {
        this.tek = tek.trim();
        this.en_interval_number = en_interval_number;
    }

}
