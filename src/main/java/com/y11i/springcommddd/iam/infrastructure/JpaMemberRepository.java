package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA 기반의 회원 리포지토리.
 * <p>
 * {@link Member} 엔티티를 데이터베이스에 영속화하며,
 * 이메일({@link Email})을 기준으로 회원을 조회하는 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 자동으로 구현체를 생성</li>
 *     <li>쿼리 메서드 파생(query derivation)을 통해 간결한 조회 제공</li>
 *     <li>{@link MemberRepositoryAdapter}를 통해 도메인에 연결됨</li>
 * </ul>
 *
 * @see JpaRepository
 * @see MemberRepositoryAdapter
 * @see Member
 * @see Email
 */
@Repository
public interface JpaMemberRepository extends JpaRepository<Member, MemberId> {

    /**
     * 이메일({@link Email})을 기준으로 회원을 조회합니다.
     *
     * @param email 조회할 이메일 값
     * @return 일치하는 {@link Member}가 존재하면 반환, 없으면 빈 {@link Optional}
     */
    Optional<Member> findByEmail(Email email);
}
