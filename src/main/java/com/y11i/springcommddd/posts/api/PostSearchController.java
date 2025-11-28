package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.SearchCommunityPostsUseCase;
import com.y11i.springcommddd.posts.application.port.in.SearchHomePostsUseCase;
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
public class PostSearchController {
    private final SearchHomePostsUseCase searchHomePostsUseCase;
    private final SearchCommunityPostsUseCase searchCommunityPostsUseCase;

    // ----------------------------------------------------
    // 전체(홈) 검색
    // ----------------------------------------------------
    /**
     * 전체 게시글 검색 엔드포인트.
     * <p>
     * 예:
     * GET /api/posts/search?q=java&sort=new&page=0&size=20
     */
    @GetMapping(path = "/posts/search", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PageResultDTO<PostSummaryResponseDTO> searchHome(
            @RequestParam(name = "q") String keyword,
            @RequestParam(name = "sort", defaultValue = "new") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
        MemberId viewerId = resolveCurrentMemberIdOrNull();

        var query = new SearchHomePostsUseCase.Query(
                viewerId,
                keyword,
                sort,
                page,
                size
        );

        return searchHomePostsUseCase.search(query);
    }

    // ----------------------------------------------------
    // 특정 커뮤니티 내 검색
    // ----------------------------------------------------
    /**
     * 특정 커뮤니티 내 게시글 검색 엔드포인트.
     * <p>
     * 예:
     * GET /api/communities/{nameKey}/posts/search?q=spring&sort=top&page=0&size=20
     */
    @GetMapping(path = "/c/{nameKey}/posts/search", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PageResultDTO<PostSummaryResponseDTO> searchCommunity(
            @PathVariable String nameKey,
            @RequestParam(name = "q") String keyword,
            @RequestParam(name = "sort", defaultValue = "new") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        MemberId viewerId = resolveCurrentMemberIdOrNull();

        var query = new SearchCommunityPostsUseCase.Query(
                nameKey,
                viewerId,
                keyword,
                sort,
                page,
                size
        );

        return searchCommunityPostsUseCase.search(query);
    }
}
