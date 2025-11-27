package com.y11i.springcommddd.comments.application.port.out;

import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

public interface LoadAuthorForCommentPort {
    Optional<Member> loadById(MemberId memberId);
}
