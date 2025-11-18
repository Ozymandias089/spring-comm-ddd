package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.ListCommunityPostsUseCase;
import com.y11i.springcommddd.posts.application.port.in.ListHomeFeedPostsUseCase;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.y11i.springcommddd.posts.api.support.CurrentMemberResolver.resolveCurrentMemberIdOrNull;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class PostFeedController {
    private final ListHomeFeedPostsUseCase listHomeFeedPostsUseCase;
    private final ListCommunityPostsUseCase listCommunityPostsUseCase;

    // ----------------------------------------------------
    // 홈 피드
    // ----------------------------------------------------

    /**
     * 홈 피드용 게시글 리스트를 조회한다.
     * <p>
     * 예:
     * GET /api/posts/feed?sort=new&page=0&size=20
     *
     * @param sort 정렬 기준 ("new", "top", "hot" 등 – 미지정 시 "new")
     * @param page 페이지 번호 (0-base)
     * @param size 페이지 크기
     */
    @GetMapping(path = "/posts/feed", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PageResultDTO<PostSummaryResponseDTO> getHomeFeed(
            @RequestParam(name = "sort", defaultValue = "new") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        MemberId viewerId = resolveCurrentMemberIdOrNull();

        var query = new ListHomeFeedPostsUseCase.Query(
                viewerId,
                sort,
                page,
                size
        );

        return listHomeFeedPostsUseCase.listHomeFeed(query);
    }

    // ----------------------------------------------------
    // 특정 커뮤니티 피드
    // ----------------------------------------------------

    /**
     * 특정 커뮤니티 내 게시글 리스트를 조회한다.
     * <p>
     * 예:
     * GET /api/communities/{communityId}/posts?sort=new&page=0&size=20
     *
     * @param communityId 커뮤니티 ID (UUID 문자열)
     * @param sort 정렬 기준 ("new", "top", "hot" 등 – 미지정 시 "new")
     * @param page 페이지 번호 (0-base)
     * @param size 페이지 크기
     */
    @GetMapping(path = "/communities/{communityId}/posts", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PageResultDTO<PostSummaryResponseDTO> getCommunityFeed(
            @PathVariable String communityId,
            @RequestParam(name = "sort", defaultValue = "new") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        MemberId viewerId = resolveCurrentMemberIdOrNull();

        var query = new ListCommunityPostsUseCase.Query(
                communityId,
                viewerId,
                sort,
                page,
                size
        );

        return listCommunityPostsUseCase.listCommunityPosts(query);
    }
}
