package com.y11i.springcommddd.iam.domain;

import java.util.Optional;

/**
 * {@link Member} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 회원(Member) 도메인의 영속성과 조회를 담당하는 추상 리포지토리로,
 * 구체적인 구현체는 인프라스트럭처 계층에서 제공합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>회원 정보의 저장 및 수정</li>
 *     <li>회원 식별자({@link MemberId})를 통한 조회</li>
 *     <li>회원 이메일({@link Email})을 통한 조회</li>
 * </ul>
 *
 * <p>
 * 도메인 계층은 이 인터페이스에만 의존하며,
 * JPA 또는 다른 저장 기술에는 의존하지 않습니다.
 * </p>
 *
 * @author y11
 */
public interface MemberRepository {

    /**
     * 회원을 저장하거나 수정합니다.
     *
     * @param member 저장할 {@link Member} 객체
     * @return 저장된 {@link Member} 인스턴스
     */
    Member save(Member member);

    /**
     * 회원 식별자({@link MemberId})를 통해 회원을 조회합니다.
     *
     * @param id 조회할 회원의 식별자
     * @return 존재하면 {@link Member}를 포함하는 {@link Optional}, 없으면 비어 있음
     */
    Optional<Member> findById(MemberId id);

    /**
     * 이메일({@link Email})을 통해 회원을 조회합니다.
     *
     * @param email 조회할 이메일 값
     * @return 존재하면 {@link Member}를 포함하는 {@link Optional}, 없으면 비어 있음
     */
    Optional<Member> findByEmail(Email email);
}
