package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

public interface LoadMemberForCommunityPort {
    Optional<Member> loadById(MemberId memberId);
}
