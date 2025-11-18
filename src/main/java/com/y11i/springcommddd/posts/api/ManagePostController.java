package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase.*;
import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.Title;
import com.y11i.springcommddd.posts.dto.request.EditPostRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글(Post) 상태 전이 및 수정 API 컨트롤러.
 *
 * <p><b>책임</b></p>
 * <ul>
 *     <li>초안(DRAFT) → 게시(PUBLISHED) 게시</li>
 *     <li>게시(PUBLISHED) → 보관(ARCHIVED) 전환</li>
 *     <li>보관(ARCHIVED) → 게시(PUBLISHED) 복구</li>
 *     <li>게시글 제목/본문 수정</li>
 * </ul>
 *
 * <p>
 * 도메인 규칙 및 권한 검증은 {@link ManagePostUseCase} 가 담당하며,<br>
 * 이 컨트롤러는 HTTP 계층과 유스케이스를 연결하는 역할만 수행한다.
 * </p>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class ManagePostController {
    private final ManagePostUseCase managePostUseCase;

    // ---------------------------------------------------------------------
    // 게시 (PUBLISH)
    // ---------------------------------------------------------------------

    /**
     * 초안(DRAFT) 상태의 게시글을 게시(PUBLISHED) 상태로 전환한다.
     *
     * <p>
     * - 작성자만 게시할 수 있다. (권한 검증은 서비스에서 수행)
     * - MEDIA 게시글인 경우, 최소 1개 이상의 자산이 있어야 한다.
     * </p>
     *
     * @param actorId 인증된 회원 ID
     * @param postId  게시할 게시글 ID (path variable)
     */
    @PostMapping(path = "/{postId}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishPost(
            @AuthenticatedMember MemberId actorId,
            @PathVariable String postId
    ) {
        managePostUseCase.publish(
                new PublishPostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
    }

    // ---------------------------------------------------------------------
    // 보관 (ARCHIVE)
    // ---------------------------------------------------------------------

    /**
     * 게시(PUBLISHED) 상태의 게시글을 보관(ARCHIVED) 상태로 전환한다.
     *
     * <p>
     * - 작성자 / 모더레이터 / 관리자만 호출 가능
     * </p>
     */
    @PostMapping(path = "/{postId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivePost(
            @AuthenticatedMember MemberId actorId,
            @PathVariable String postId
    ) {
        managePostUseCase.archive(
                new ArchivePostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
    }

    // ---------------------------------------------------------------------
    // 복구 (RESTORE)
    // ---------------------------------------------------------------------

    /**
     * 보관(ARCHIVED) 상태의 게시글을 다시 게시(PUBLISHED) 상태로 복구한다.
     *
     * <p>
     * - 모더레이터 / 관리자만 호출 가능
     * </p>
     */
    @PostMapping(path = "/{postId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restorePost(
            @AuthenticatedMember MemberId actorId,
            @PathVariable String postId
    ) {
        managePostUseCase.restore(
                new RestorePostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
    }

    // ---------------------------------------------------------------------
    // 수정 (EDIT)
    // ---------------------------------------------------------------------

    /**
     * 게시글 제목/본문을 수정한다.
     *
     * <p>
     * - 작성자만 수정 가능
     * - TEXT / MEDIA: title, content 모두 수정 가능
     * - LINK: title만 수정 가능 (content는 무시)
     * </p>
     */
    @PatchMapping(path = "/{postId}/edit", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editPost(
            @AuthenticatedMember MemberId actorId,
            @PathVariable String postId,
            @Valid @RequestBody EditPostRequestDTO requestDTO
    ) {
        managePostUseCase.editPost(
                new EditPostCommand(
                        PostId.objectify(postId),
                        actorId,
                        new Title(requestDTO.title()),
                        new Content(requestDTO.content())
                )
        );
    }
}
