package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 아직 구현되지 않은 2차 기능들(검색, 북마크, 신고/차단 등)을 위한 스텁 컨트롤러.
 *
 * <p>
 * 모든 엔드포인트는 현재 501 NOT_IMPLEMENTED 를 반환하며,<br>
 * 실제 유스케이스/서비스 구현 시 이 컨트롤러를 확장하거나 분리하면 된다.
 * </p>
 */
@RestController
@RequestMapping("/api")
@Validated
public class PostExtraFeatureController {
    // ---------------------------------------------------------------------
    // 1. 키워드 기반 게시글 검색
    // ---------------------------------------------------------------------

    /**
     * 키워드 기반 게시글 검색.
     * <p>
     * 예: GET /api/search/posts?q=java&page=0&size=20
     */
    @GetMapping(path = "/search/posts", produces = "application/json")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void searchPosts(
            @RequestParam("q") @NotBlank @Size(min = 1, max = 100) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        throw new UnsupportedOperationException("post.search: Not supported yet.");
    }

    // ---------------------------------------------------------------------
    // 2. 사용자별 게시글 목록 (사용자 페이지용)
    // ---------------------------------------------------------------------

    /**
     * 특정 사용자의 게시글 목록 조회.
     * <p>
     * 예: GET /api/members/{memberId}/posts?page=0&size=20
     * (실제 구현 시 status 필터, 정렬 옵션 등이 추가될 수 있다.)
     */
    @GetMapping(path = "/members/{memberId}/posts", produces = "application/json")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void listMemberPosts(
            @PathVariable String memberId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        throw new UnsupportedOperationException("post.listMemberPosts: Not supported yet.");
    }

    // ---------------------------------------------------------------------
    // 3. 게시글 북마크
    // ---------------------------------------------------------------------

    /**
     * 게시글 북마크 추가.
     * <p>
     * 예: POST /api/posts/{postId}/bookmark
     */
    @PostMapping(path = "/posts/{postId}/bookmark")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void addBookmark(
            @AuthenticatedMember MemberId memberId,
            @PathVariable String postId
    ) {
        throw new UnsupportedOperationException("post.bookmark.add: Not supported yet.");
    }

    /**
     * 게시글 북마크 제거.
     * <p>
     * 예: DELETE /api/posts/{postId}/bookmark
     */
    @DeleteMapping(path = "/posts/{postId}/bookmark")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void removeBookmark(
            @AuthenticatedMember MemberId memberId,
            @PathVariable String postId
    ) {
        throw new UnsupportedOperationException("post.bookmark.remove: Not supported yet.");
    }

    /**
     * 내 북마크 목록 조회.
     * <p>
     * 예: GET /api/me/bookmarks?page=0&size=20
     */
    @GetMapping(path = "/me/bookmarks", produces = "application/json")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void listMyBookmarks(
            @AuthenticatedMember MemberId memberId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        throw new UnsupportedOperationException("post.bookmark.list: Not supported yet.");
    }
}
