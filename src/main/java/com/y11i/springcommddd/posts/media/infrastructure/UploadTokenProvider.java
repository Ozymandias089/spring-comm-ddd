package com.y11i.springcommddd.posts.media.infrastructure;

import java.time.Duration;

public interface UploadTokenProvider {
    /** 예: POST용 프리사인 URL/폼필드 */
    UploadToken issueUpload(String keyHint, String mimeType, Long sizeLimitBytes, Duration ttl);
}
