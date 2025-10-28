package com.y11i.springcommddd.common.api;

import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class ProblemFactory {
    private static final String TYPE_BASE = "https://api.springcomm.app/errors/";
    private ProblemFactory() {}

    public static ProblemDetail of(ErrorCode code, String title, String detail, String instance) {
        var pd = ProblemDetail.forStatus(code.status());
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setType(URI.create(TYPE_BASE + code.code()));
        if (instance != null) pd.setInstance(URI.create(instance));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", code.code());
        return pd;
    }
}
