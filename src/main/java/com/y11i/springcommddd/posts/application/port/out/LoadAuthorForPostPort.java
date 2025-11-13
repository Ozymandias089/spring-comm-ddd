package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

/**
 * 게시글 작성 시 작성자(Member)를 검증/조회하기 위한 포트.
 */
public interface LoadAuthorForPostPort {
    Optional<Member> loadById(MemberId authorId);
}
