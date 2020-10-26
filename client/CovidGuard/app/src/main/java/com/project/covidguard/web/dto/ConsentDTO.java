package com.project.covidguard.web.dto;

import com.squareup.moshi.Json;

import java.util.Date;

public class ConsentDTO {

    @Json(name = "id")
    String id;

    @Json(name = "revoked_at")
    Date revokedAt;

    @Json(name = "expired_at")
    Date expiredAt;

    @Json(name = "uuid")
    String uuid;

    @Json(name = "key")
    String key;

    @Json(name = "host")
    String host;

    public ConsentDTO(String id, Date expiredAt,
                      Date revokedAt, String host, String uuid) {
        this.id = id;
        this.expiredAt = expiredAt;
        this.revokedAt = revokedAt;
        this.host = host;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id + "\n");
        sb.append("expiredAt: " + expiredAt + "\n");
        sb.append("revokedAt: " + revokedAt + "\n");
        sb.append("host: " + host + "\n");
        sb.append("uuid: " + uuid + "\n");
        return sb.toString();
    }

    public String getId() {
        return this.id;
    }

    public Date getExpiredAt() {
        return this.expiredAt;
    }

    public Date getRevokedAt() {
        return this.revokedAt;
    }
}
