package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.Optional;
import java.util.UUID;

public interface FindMemberUseCase {
    Optional<MemberDTO> findByEmail(String email);
    Optional<MemberDTO> findById(UUID memberId);
}
