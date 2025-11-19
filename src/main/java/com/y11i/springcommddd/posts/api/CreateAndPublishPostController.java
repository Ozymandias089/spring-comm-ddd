package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.CreateAndPublishPostUseCase;
import com.y11i.springcommddd.posts.application.port.in.CreateAndPublishPostUseCase.CreateAndPublishLinkCommand;
import com.y11i.springcommddd.posts.application.port.in.CreateAndPublishPostUseCase.CreateAndPublishMediaCommand;
import com.y11i.springcommddd.posts.application.port.in.CreateAndPublishPostUseCase.CreateAndPublishTextCommand;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import com.y11i.springcommddd.posts.dto.request.CreateLinkPostRequestDTO;
import com.y11i.springcommddd.posts.dto.request.CreateMediaPostRequestDTO;
import com.y11i.springcommddd.posts.dto.request.CreateTextPostRequestDTO;
import com.y11i.springcommddd.posts.dto.response.PostDraftCreatedResponseDTO;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글을 "바로 게재" 상태로 생성하는 API 컨트롤러.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>TEXT / LINK / MEDIA 게시글을 즉시 PUBLISHED 상태로 생성</li>
 *     <li>내부적으로는 초안 생성 + 게시 상태 전이를 하나의 트랜잭션으로 수행</li>
 * </ul>
 *
 * <p>
 * 도메인 규칙, 권한 검증, 상태 전이는
 * {@link CreateAndPublishPostUseCase} 가 사용하는 하위 유스케이스
 * ({@link com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase},
 *  {@link com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase})에 위임한다.
 * </p>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class CreateAndPublishPostController {
    private final CreateAndPublishPostUseCase createAndPublishPostUseCase;

    // ----------------------------------------------------------------------
    // TEXT - 바로 게재
    // ----------------------------------------------------------------------

    /**
     * 텍스트 게시글을 생성하고 즉시 게시(PUBLISHED) 상태로 만든다.
     *
     * <p>
     * 인증된 사용자(@AuthenticatedMember) 기준으로만 작성할 수 있으며,
     * 요청 바디에는 authorId를 받지 않는다.
     * </p>
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, content
     */
    @PostMapping(path = "/text", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createAndPublishTextPost(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreateTextPostRequestDTO requestDTO
    ) {
        PostId postId = createAndPublishPostUseCase.createAndPublishText(
                new CreateAndPublishTextCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        requestDTO.title(),
                        requestDTO.content()
                )
        );

        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    // ----------------------------------------------------------------------
    // LINK - 바로 게재
    // ----------------------------------------------------------------------

    /**
     * 링크 게시글을 생성하고 즉시 게시(PUBLISHED) 상태로 만든다.
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, link
     */
    @PostMapping(path = "/link", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createAndPublishLinkPost(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreateLinkPostRequestDTO requestDTO
    ) {
        PostId postId = createAndPublishPostUseCase.createAndPublishLink(
                new CreateAndPublishLinkCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        requestDTO.title(),
                        requestDTO.link()
                )
        );

        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    // ----------------------------------------------------------------------
    // MEDIA - 바로 게재
    // ----------------------------------------------------------------------

    /**
     * 미디어 게시글을 생성하고 즉시 게시(PUBLISHED) 상태로 만든다.
     *
     * <p>
     * 파일 업로드는 이미 완료되었다고 가정하며,
     * {@link PostAssetUploadDTO#fileName()} 을 스토리지 key로 사용한다.
     * </p>
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, content, assets[]
     */
    @PostMapping(path = "/media", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createAndPublishMediaPost(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreateMediaPostRequestDTO requestDTO
    ) {
        List<PostAssetUploadDTO> assetDTOs = requestDTO.postAssetUploadDTOs();
        List<AssetMeta> metas = (assetDTOs == null) ? List.of()
                : assetDTOs.stream()
                .map(dto -> new AssetMeta(
                        toMediaType(dto.mediaType()),
                        dto.displayOrder(),
                        dto.fileSize(),
                        dto.fileName(),
                        dto.mimeType()
                ))
                .toList();

        PostId postId = createAndPublishPostUseCase.createAndPublishMedia(
                new CreateAndPublishMediaCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        requestDTO.title(),
                        requestDTO.content(),
                        metas
                )
        );

        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    /**
     * 요청에서 넘어온 mediaType 문자열을 MediaType enum으로 변환한다.
     *
     * <p>
     * - "IMAGE", "image", "Image" 등 대소문자 무시
     * - 잘못된 값이면 IllegalArgumentException → GlobalExceptionHandler에서 400으로 매핑 가능
     * </p>
     */
    private static MediaType toMediaType(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("mediaType must not be null");
        }
        return MediaType.valueOf(raw.toUpperCase());
    }
}
