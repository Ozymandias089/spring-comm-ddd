package com.y11i.springcommddd.posts.api;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase.CreateTextDraftCommand;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase.CreateLinkDraftCommand;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase.CreateMediaDraftCommand;
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

@RestController
@RequestMapping("/api/posts/drafts")
@RequiredArgsConstructor
@Validated
public class CreatePostDraftController {
    private final CreatePostDraftUseCase createPostDraftUseCase;

    // ----------------------------------------------------------------------
    // TEXT Draft
    // ----------------------------------------------------------------------

    /**
     * 텍스트 게시글 초안을 작성한다.
     *
     * <p>
     * 인증된 사용자(@AuthenticatedMember) 기준으로만 작성할 수 있으며,
     * 요청 바디에는 authorId를 받지 않는다. (남의 계정으로 글 작성 방지)
     * </p>
     *
     * <p>
     * 응답으로는 생성된 초안의 postId만 내려준다.
     * 실제 "게시"는 별도의 상태 관리 API에서 처리한다.
     * </p>
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, content
     */
    @PostMapping(path = "/text", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createTextPostDraft(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreateTextPostRequestDTO requestDTO
    ) {
        PostId postId = createPostDraftUseCase.createTextDraft(
                new CreateTextDraftCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        requestDTO.title(),
                        requestDTO.content()
                )
        );
        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    // ----------------------------------------------------------------------
    // LINK Draft
    // ----------------------------------------------------------------------

    /**
     * 링크 게시글 초안을 작성한다.
     *
     * <p>
     * 인증된 사용자 기준으로만 작성 가능하며,
     * 링크 URL 검증은 도메인 계층(Post.createLink/LinkUrl 값 객체)에서 처리한다.
     * </p>
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, link
     */
    @PostMapping(path = "/link", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createLinkPostDraft(
            @AuthenticatedMember MemberId memberId,
            @Valid @RequestBody CreateLinkPostRequestDTO requestDTO
    ) {
        PostId postId = createPostDraftUseCase.createLinkDraft(
                new CreateLinkDraftCommand(
                        CommunityId.objectify(requestDTO.communityId()),
                        memberId,
                        requestDTO.title(),
                        requestDTO.link()
                )
        );
        return new PostDraftCreatedResponseDTO(postId.stringify());
    }

    // ----------------------------------------------------------------------
    // MEDIA Draft
    // ----------------------------------------------------------------------

    /**
     * 미디어 게시글 초안을 작성한다.
     *
     * <p><b>역할</b></p>
     * <ul>
     *     <li>요청 DTO → CreateMediaDraftCommand + AssetMeta 리스트로 변환</li>
     *     <li>초안(Post)과 자산(PostAsset)을 함께 생성하는 유스케이스 호출</li>
     *     <li>생성된 PostId를 응답으로 반환</li>
     * </ul>
     *
     * <p>
     * 파일 업로드는 이미 끝났다고 가정하고, fileName을 스토리지 key로 사용한다.
     * 실제 URL 생성/썸네일링 등은 애플리케이션/도메인 서비스의 책임이다.
     * </p>
     *
     * @param memberId   인증된 회원 ID
     * @param requestDTO communityId, title, content, assets[]
     */
    @PostMapping(path = "/media", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDraftCreatedResponseDTO createMediaPostDraft(
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

        PostId postId = createPostDraftUseCase.createMediaDraft(
                new CreateMediaDraftCommand(
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
     * <p>
     * - "IMAGE", "image", "Image" 모두 허용하고 싶으면 대문자 변환 후 valueOf 사용
     * - 잘못된 값이면 IllegalArgumentException 대신
     *   네가 정의한 커스텀 예외(예: InvalidMediaTypeException) 던져서
     *   GlobalExceptionHandler에서 400으로 매핑하도록 해도 된다.
     */
    private static MediaType toMediaType(String raw) {
        if (raw == null) throw new IllegalArgumentException("mediaType must not be null");
        return MediaType.valueOf(raw.toUpperCase());  // enum 이름이 IMAGE / VIDEO 라는 전제
    }
}
