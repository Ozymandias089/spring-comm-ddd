package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.GetPostDetailUseCase;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.response.PostDetailResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.y11i.springcommddd.posts.api.support.CurrentMemberResolver.resolveCurrentMemberIdOrNull;

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
}
