package com.y11i.springcommddd.iam.domain;

import java.util.Optional;

public interface MemberRepository {    Member save(Member member);
    Optional<Member> findById(MemberId id);
    Optional<Member> findByEmail(Email email);
}
