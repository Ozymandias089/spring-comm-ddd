package com.y11i.springcommddd.iam.application.port.out;

import com.y11i.springcommddd.iam.domain.Member;

public interface SaveMemberPort {
    Member save(Member member);
}
