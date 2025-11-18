package com.y11i.springcommddd.posts.media.infrastructure;

import java.time.Instant;
import java.util.Map;

public record UploadToken(String key, String url, Map<String,String> formFields, Instant expiresAt) {}
