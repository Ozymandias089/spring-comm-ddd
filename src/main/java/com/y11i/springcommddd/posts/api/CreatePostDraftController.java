package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.domain.PostType;
import com.y11i.springcommddd.posts.dto.request.CreatePostDraftRequestDTO;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import com.y11i.springcommddd.posts.dto.response.PostDraftCreatedResponseDTO;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/drafts")
@RequiredArgsConstructor
@Validated
public class CreatePostDraftController {
    private final CreatePostDraftUseCase createPostDraftUseCase;

    /**
     * 게시글 초안을 생성한다.
     *
     * <p>
     * type 에 따라 TEXT / MEDIA / LINK 중 하나로 동작하며,
     * 인증된 사용자(@AuthenticatedMember)만 초안을 생성할 수 있다.
     * </p>
     *
     * <ul>
     *     <li>TEXT: title + content</li>
     *     <li>LINK: title + link</li>
     *     <li>MEDIA: title + (optional content) + assets[]</li>
     * </ul>
     *
     * 밴된 사용자에 대한 차단은 애플리케이션 서비스
     * ({@link CreatePostDraftUseCase}) 내부의 CheckCommunityBanPort에서 처리한다.
     */
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createPostDraft(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreatePostDraftRequestDTO requestDTO
    ) {
        // 1. 타입 문자열 -> PostType enum 변환
        PostType type = toPostType(requestDTO.type());

        // 2. MEDIA 타입이면 자산 메타로 변환
        List<AssetMeta> metas = List.of();
        if (type == PostType.MEDIA) {
            List<PostAssetUploadDTO> assetDTOs = requestDTO.assets();
            metas = (assetDTOs == null) ? List.of()
                    : assetDTOs.stream()
                    .map(dto -> new AssetMeta(
                            toMediaType(dto.mediaType()),
                            dto.displayOrder(),
                            dto.fileSize(),
                            dto.fileName(),
                            dto.mimeType()
                    ))
                    .toList();
        }

        // 3. 유스케이스 커맨드 생성 및 호출
        PostId postId = createPostDraftUseCase.createDraft(
                new CreatePostDraftUseCase.CreateDraftCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        type,
                        requestDTO.title(),
                        requestDTO.content(), // TEXT, MEDIA에서 사용
                        requestDTO.link(),    // LINK에서 사용
                        metas                 // MEDIA에서 사용
                )
        );

        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    /**
     * 요청에서 넘어온 type 문자열을 PostType enum으로 변환한다.
     * 예: "text", "Text", "TEXT" -> PostType.TEXT
     */
    private static PostType toPostType(String raw) {
        if (raw == null) throw new IllegalArgumentException("type must not be null");
        return PostType.valueOf(raw.toUpperCase());
    }

    /**
     * 요청에서 넘어온 mediaType 문자열을 MediaType enum으로 변환한다.
     * 기존 MEDIA용 엔드포인트에서 사용하던 로직을 그대로 재사용.
     */
    private static MediaType toMediaType(String raw) {
        if (raw == null) throw new IllegalArgumentException("mediaType must not be null");
        return MediaType.valueOf(raw.toUpperCase());
    }
}
