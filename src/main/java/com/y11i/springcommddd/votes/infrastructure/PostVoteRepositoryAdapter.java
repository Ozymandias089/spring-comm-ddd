package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * {@link PostVoteRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 도메인 계층의 {@link PostVoteRepository}를
 * JPA 기반 리포지토리 {@link JpaPostVoteRepository}로 어댑팅하여 실제 데이터베이스 접근을 수행합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층이 JPA 세부 구현에 직접 의존하지 않도록 분리</li>
 *     <li>읽기 작업에는 {@code readOnly = true} 트랜잭션 적용</li>
 *     <li>쓰기 작업(저장, 삭제)은 별도의 트랜잭션으로 처리</li>
 * </ul>
 *
 * @see JpaPostVoteRepository
 * @see PostVoteRepository
 * @see PostVote
 * @see MemberId
 * @see PostId
 * @author y11
 */
@Repository
@Transactional(readOnly = true)
public class PostVoteRepositoryAdapter implements PostVoteRepository {

    private final JpaPostVoteRepository jpaPostVoteRepository;

    /**
     * JPA 리포지토리를 주입받습니다.
     *
     * @param jpaPostVoteRepository JPA 기반 투표 리포지토리
     */
    public PostVoteRepositoryAdapter(JpaPostVoteRepository jpaPostVoteRepository) {
        this.jpaPostVoteRepository = jpaPostVoteRepository;
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public PostVote save(PostVote v) {
        return jpaPostVoteRepository.save(v);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId) {
        return jpaPostVoteRepository.findByPostIdAndVoterId(postId, voterId);
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public void delete(PostVote v) {
        jpaPostVoteRepository.delete(v);
    }
}
