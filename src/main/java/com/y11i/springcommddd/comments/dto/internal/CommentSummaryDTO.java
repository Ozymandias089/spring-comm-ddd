package com.y11i.springcommddd.comments.dto.internal;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * 댓글 트리 노드 요약 DTO.
 *
 * <p>
 * - 루트 댓글과 대댓글을 동일한 구조로 표현한다.<br>
 * - children 필드를 통해 레딧 스타일의 스레드 트리를 구성할 수 있다.
 * </p>
 *
 * <p>
 * API 응답에서는 보통 PageResultDTO&lt;CommentSummaryDTO&gt; 형태로,
 * content = 루트 댓글 리스트, 각 루트의 children에 대댓글 트리를 포함시키는 식으로 사용한다.
 * </p>
 */
public record CommentSummaryDTO(
        String commentId,          // 댓글 ID
        String postId,             // 게시글 ID
        String parentCommentId,    // 부모 댓글 ID (루트면 null)
        int depth,                 // 루트 = 0, 자식 = 1 ...

        String authorId,           // 작성자 ID
        String authorDisplayName,  // 작성자 표시 이름(나중에 Member에서 끌어오기)

        boolean mine,              // 현재 로그인 유저가 쓴 댓글인지 여부
        boolean deleted,           // 삭제된 댓글인지 여부
        boolean edited,          // 수정 여부를 반환

        String body,               // 본문 (삭제된 경우 null 이거나 "[deleted]" 등 정책 적용)

        int upCount,               // 추천 수
        int downCount,             // 비추천 수
        int score,                 // upCount - downCount
        Integer myVote,            // 현재 유저의 투표값: -1 / 0 / 1 (미투표면 null)

        Instant createdAt,
        Instant updatedAt,

        List<CommentSummaryDTO> children // 대댓글 트리
) {
    @Builder
    public CommentSummaryDTO{}
}
