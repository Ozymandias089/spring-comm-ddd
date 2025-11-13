package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.posts.domain.PostId;

/**
 * 게시글에 달린 댓글 수를 조회하기 위한 포트.
 * <p>
 * Post 애플리케이션 서비스에서 댓글 수가 필요할 때
 * Comment 도메인/리포지토리 구현에 직접 의존하지 않고
 * 이 포트를 통해 조회한다.
 * </p>
 */
public interface LoadCommentCountPort {

    /**
     * 주어진 게시글에 달린 댓글 수를 반환합니다.
     *
     * @param postId 게시글 식별자
     * @return 댓글 개수
     */
    long countByPostId(PostId postId);
}
