package com.y11i.springcommddd.iam.application.port.out;

import com.y11i.springcommddd.iam.domain.Member;

/**
 * 회원 엔티티를 영속화(저장/갱신)하기 위한 출력 포트.
 *
 * <p>
 * 이 포트는 도메인 엔티티 {@link Member}의 현재 상태를
 * 영구 저장소(예: RDBMS)를 통해 반영한다.
 * </p>
 *
 * <p>
 * 일반적으로 "신규 회원 등록"이나 "프로필(닉네임/이메일/비밀번호 등) 변경",
 * "상태(SUSPENDED, DELETED 등) 변경" 등의 시나리오에서 호출된다.
 * </p>
 *
 * <p>
 * 구현체는 다음 정책을 가져야 한다 (권장):
 * </p>
 * <ul>
 *     <li>동일한 {@code Member.memberId()}가 이미 존재하면 업데이트(변경 사항 반영)</li>
 *     <li>존재하지 않으면 신규 생성(insert)</li>
 * </ul>
 *
 * 즉 사실상 <b>upsert</b> 의미로 동작하는 것이 일반적이다.
 *
 * <p>
 * 트랜잭션 경계(예: @Transactional)는 호출 측 서비스 계층에서 관리한다.
 * SaveMemberPort 자체는 "이 호출이 성공적으로 끝났다면 DB/저장소 반영이 성공했다"
 * 라는 계약만 보장하면 된다.
 * </p>
 */
public interface SaveMemberPort {
    /**
     * 주어진 {@link Member}의 현재 상태를 영속 계층에 반영한다.
     *
     * @param member 저장할 도메인 엔티티
     * @return 저장 후(혹은 갱신 후)의 엔티티 스냅샷.
     *         JPA 구현체라면 DB에서 flush/merge된 엔티티를 반환할 수 있다.
     */
    Member save(Member member);
}
