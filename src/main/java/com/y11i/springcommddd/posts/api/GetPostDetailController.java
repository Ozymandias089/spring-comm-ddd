package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.GetPostDetailUseCase;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.response.PostDetailResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class GetPostDetailController {
    private final GetPostDetailUseCase getPostDetailUseCase;

    /**
     * 게시글 상세 정보를 조회한다.
     *
     * @param postId   게시글 ID (UUID 문자열)
     */
    @GetMapping(path = "/{postId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PostDetailResponseDTO getPostDetail(@PathVariable String postId) {
        MemberId viewerId = resolveCurrentMemberIdOrNull();
        return getPostDetailUseCase.getPostDetail(PostId.objectify(postId), viewerId);
    }

    /**
     * 현재 로그인한 사용자의 MemberId를 조회한다.
     * 로그인하지 않은 경우 null을 반환한다.
     */
    private MemberId resolveCurrentMemberIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedMemberPrincipal memberPrincipal) return memberPrincipal.getMemberId();

        return null;
    }
}
