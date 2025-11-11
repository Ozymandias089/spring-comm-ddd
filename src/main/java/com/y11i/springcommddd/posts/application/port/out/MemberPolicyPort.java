package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.iam.domain.MemberId;

public interface MemberPolicyPort {
    boolean isEmailVerified(MemberId memberId);
}
