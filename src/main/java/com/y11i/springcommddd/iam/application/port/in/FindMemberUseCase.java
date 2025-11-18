package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.Optional;
import java.util.UUID;

/**
 * 회원 조회 유스케이스.
 *
 * <p>주로 현재 로그인한 사용자의 정보 조회나
 * 관리자가 특정 사용자를 점검할 때 사용된다.</p>
 *
 * <p>이 유스케이스는 순수 조회(read-only) 목적이다.</p>
 */
public interface FindMemberUseCase {
    /**
     * @deprecated 이메일은 더 이상 인증 식별자로 사용되지 않습니다.
     *              {@link #findById(MemberId)}를 대신 사용하세요.
     */
    @Deprecated
    Optional<MemberDTO> findByEmail(String email);

    /**
     * 고유 식별자(MemberId)로 회원을 조회한다.
     *
     * @param memberId 불변 회원 식별자 (MemberId 내부 UUID)
     * @return 해당 사용자의 정보를 담은 DTO. 없으면 empty.
     */
    Optional<MemberDTO> findById(MemberId memberId);
}
