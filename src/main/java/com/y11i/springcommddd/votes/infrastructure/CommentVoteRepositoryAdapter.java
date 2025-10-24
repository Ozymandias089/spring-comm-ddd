package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.domain.CommentVote;
import com.y11i.springcommddd.votes.domain.CommentVoteRepository;
import com.y11i.springcommddd.votes.domain.MyCommentVote;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link CommentVoteRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 도메인 계층의 {@link CommentVoteRepository}를
 * JPA 기반 리포지토리 {@link JpaCommentVoteRepository}로 어댑팅하여
 * 실제 데이터베이스 접근을 수행합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층이 JPA 구현 세부사항에 직접 의존하지 않도록 분리</li>
 *     <li>읽기 작업에는 {@code readOnly = true} 트랜잭션 적용</li>
 *     <li>쓰기 작업(저장, 삭제)은 별도의 트랜잭션으로 처리</li>
 * </ul>
 */
@Repository
@Transactional(readOnly = true)
public class CommentVoteRepositoryAdapter implements CommentVoteRepository {

    private final JpaCommentVoteRepository jpaCommentVoteRepository;

    /**
     * JPA 리포지토리를 주입받습니다.
     *
     * @param jpaCommentVoteRepository JPA 기반 댓글 투표 리포지토리
     */
    public CommentVoteRepositoryAdapter(JpaCommentVoteRepository jpaCommentVoteRepository) {
        this.jpaCommentVoteRepository = jpaCommentVoteRepository;
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public CommentVote save(CommentVote v) {
        return jpaCommentVoteRepository.save(v);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<CommentVote> findByCommentIdAndVoterId(CommentId commentId, MemberId voterId) {
        return jpaCommentVoteRepository.findByCommentIdAndVoterId(commentId, voterId);
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public void delete(CommentVote v) {
        jpaCommentVoteRepository.delete(v);
    }

    /** {@inheritDoc} */
    @Override
    public List<MyCommentVote> findMyVotesByCommentIds(MemberId voterId, Collection<CommentId> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return List.of();

        var uuids = commentIds.stream().map(CommentId::id).collect(Collectors.toSet());
        var rows = jpaCommentVoteRepository.findValuesByVoterAndCommentIds(voterId.id(), uuids);

        return rows.stream()
                .map(r -> new MyCommentVote(new CommentId(r.getId()), r.getValue()))
                .toList();
    }
}
