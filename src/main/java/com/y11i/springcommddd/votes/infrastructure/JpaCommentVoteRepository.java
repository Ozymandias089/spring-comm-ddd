package com.y11i.springcommddd.votes.infrastructure;


import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.domain.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA 기반의 댓글 투표 리포지토리.
 * <p>
 * {@link CommentVote} 엔티티를 데이터베이스에 영속화하며,
 * 댓글({@link CommentId})과 투표자({@link MemberId})를 기준으로
 * 단일 투표를 조회하는 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 구현체를 자동 생성</li>
 *     <li>메서드 이름 기반 쿼리 파생으로 단건 조회 제공</li>
 *     <li>{@link CommentVoteRepositoryAdapter}에서 주입받아 사용됨</li>
 * </ul>
 */
@Repository
public interface JpaCommentVoteRepository extends JpaRepository<CommentVote, CommentId> {

    /**
     * 댓글 ID와 투표자 ID를 기준으로 투표를 조회합니다.
     *
     * @param commentId 댓글 식별자
     * @param voterId   투표자 식별자
     * @return 일치하는 {@link CommentVote}가 존재하면 반환, 없으면 빈 {@link Optional}
     */
    Optional<CommentVote>  findByCommentIdAndVoterId(CommentId commentId, MemberId voterId);
}
