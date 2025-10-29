package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

public interface RegisterMemberUseCase {
    record Command(String email, String displayName, String rawPassword) {}
    MemberDTO register(Command cmd);
}

