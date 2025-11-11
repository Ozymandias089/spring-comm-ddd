package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.UUID;

public interface PostAuditPort {
    void record(String action, UUID postId, MemberId actor, String details);
}
