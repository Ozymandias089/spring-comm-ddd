package com.y11i.springcommddd.iam.application.port.out;

import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

public interface LoadMemberPort {
    Optional<Member> loadById(MemberId id);
    Optional<Member> loadByEmail(Email email);
}
