package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase.*;
import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.Title;
import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import com.y11i.springcommddd.posts.dto.request.EditPostRequestDTO;
import com.y11i.springcommddd.posts.dto.request.UpdateDraftPostRequestDTO;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Slf4j
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
        PostId published = managePostUseCase.publish(
                new PublishPostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
        log.info("Published post with id {}", published);
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
        PostId archived = managePostUseCase.archive(
                new ArchivePostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
        log.info("Archived post with id {}", archived);
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
        PostId restored = managePostUseCase.restore(
                new RestorePostCommand(
                        PostId.objectify(postId),
                        actorId
                )
        );
        log.info("Restore post with id {}", restored);
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
        PostId edited = managePostUseCase.editPost(
                new EditPostCommand(
                        PostId.objectify(postId),
                        actorId,
                        new Title(requestDTO.title()),
                        new Content(requestDTO.content())
                )
        );
        log.info("Edited post with id {}", edited);
    }

    @DeleteMapping(path = "/draft/{postId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void scrapDraft(
            @AuthenticatedMember MemberId actorId,
            @PathVariable String postId
    ){
        managePostUseCase.scrapDraft(new ScrapDraftCommand(PostId.objectify(postId), actorId));
    }

    @PatchMapping(path = "/draft/{postId}/edit", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editDraftPost(
            @PathVariable String postId,
            @AuthenticatedMember MemberId memberId,
            @RequestBody @Validated UpdateDraftPostRequestDTO request
    ) {
        List<PostAssetUploadDTO> assetDTOs = request.postAssetUploadDTOs();

        // null -> 첨부 변경 안 함, empty -> 다 지우기
        List<AssetMeta> metas = (assetDTOs == null) ? null
                : assetDTOs.stream()
                .map(this::toAssetMeta)
                .toList();

        PostId edited = managePostUseCase.editDraft(
                new ManagePostUseCase.EditDraftPostCommand(
                        PostId.objectify(postId),
                        memberId,
                        request.communityId(),
                        request.title(),
                        request.content(),
                        request.link(),
                        metas
                )
        );
        log.info("Edited post draft with id {}", edited);
    }

    @PostMapping(path = "/draft/{postId}/publish", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editDraftAndPublish(
            @PathVariable String postId,
            @AuthenticatedMember MemberId memberId,
            @RequestBody @Valid UpdateDraftPostRequestDTO request
    ) {
        List<PostAssetUploadDTO> assetDTOs = request.postAssetUploadDTOs();

        List<AssetMeta> metas = (assetDTOs == null) ? null
                : assetDTOs.stream()
                .map(this::toAssetMeta)
                .toList();

        PostId editAndPosted = managePostUseCase.editDraftAndPublish(
                new EditDraftAndPublishCommand(
                        PostId.objectify(postId),
                        memberId,
                        request.communityId(),
                        request.title(),
                        request.content(),
                        request.link(),
                        metas
                )
        );
        log.info("Edited post draft and publish with id {}", editAndPosted);
    }

    /**
     * 업로드 DTO를 내부 AssetMeta로 변환한다.
     * (초안 생성 시 사용하던 매핑 로직과 동일하게 유지할 것)
     */
    private AssetMeta toAssetMeta(PostAssetUploadDTO dto) {
        return new AssetMeta(
                toMediaType(dto.mediaType()),
                dto.displayOrder(),
                dto.fileSize(),
                dto.fileName(),
                dto.mimeType()
        );
    }

    private MediaType toMediaType(String raw) {
        // 기존에 쓰던 규칙에 맞춰서 구현
        // 예: "IMAGE", "VIDEO" 같은 값을 enum으로 매핑
        return MediaType.valueOf(raw.toUpperCase());
    }
}
