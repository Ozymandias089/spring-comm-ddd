package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.response.PostDetailResponseDTO;

/**
 * 게시글 상세 조회 유스케이스.
 *
 * <p>
 * 댓글 목록은 포함하지 않고, 게시글 본문 + 커뮤니티/작성자 정보 +
 * 투표/댓글 집계 + (MEDIA일 경우) 미디어 자산 목록을 반환한다.
 * </p>
 */
public interface GetPostDetailUseCase {
    /**
     * 게시글 상세 정보를 조회한다.
     *
     * @param postId   게시글 ID
     * @param viewerId 조회자 ID (로그인 사용자, 비로그인 열고 싶으면 null 허용하도록 확장 가능)
     * @return 게시글 상세 응답 DTO
     */
    PostDetailResponseDTO getPostDetail(PostId postId, MemberId viewerId);
}
