package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * {@link MemberRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 도메인 계층의 {@link MemberRepository}를 JPA 기반 리포지토리인
 * {@link JpaMemberRepository}로 어댑팅하여 실제 데이터베이스 접근을 수행합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층이 JPA 세부 구현에 의존하지 않도록 격리</li>
 *     <li>읽기 작업에는 {@code readOnly = true} 트랜잭션 적용</li>
 *     <li>쓰기 작업(저장)은 별도의 트랜잭션으로 처리</li>
 * </ul>
 *
 * @see JpaMemberRepository
 * @see MemberRepository
 * @see Member
 * @author y11
 */
@Repository
@Transactional(readOnly = true)
public class MemberRepositoryAdapter implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    /**
     * JPA 리포지토리를 주입받습니다.
     *
     * @param jpaMemberRepository JPA 기반 회원 리포지토리
     */
    public MemberRepositoryAdapter(JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> findById(MemberId memberId) {
        return jpaMemberRepository.findById(memberId);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> findByEmail(Email email) {
        return jpaMemberRepository.findByEmail(email);
    }
}
