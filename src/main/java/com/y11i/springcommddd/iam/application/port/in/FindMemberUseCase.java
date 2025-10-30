package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.Optional;
import java.util.UUID;

public interface FindMemberUseCase {
    /**
     * @deprecated 이메일은 더 이상 인증 식별자로 사용되지 않습니다.
     *              {@link #findById(UUID)}를 대신 사용하세요.
     */
    @Deprecated
    Optional<MemberDTO> findByEmail(String email);
    Optional<MemberDTO> findById(UUID memberId);
}
