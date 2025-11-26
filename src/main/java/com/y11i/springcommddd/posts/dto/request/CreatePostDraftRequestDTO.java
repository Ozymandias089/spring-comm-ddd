package com.y11i.springcommddd.posts.dto.request;

import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record CreatePostDraftRequestDTO(

        /*
          게시글이 속한 커뮤니티 ID (UUID 문자열).
         */
        @NotBlank
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "communityId must be a UUID string"
        )
        String communityId,

        /*
          게시글 종류: TEXT / MEDIA / LINK
         */
        @NotBlank
        @Pattern(
                // 대소문자 무시하고 TEXT|MEDIA|LINK 허용
                regexp = "(?i)TEXT|MEDIA|LINK",
                message = "type must be one of TEXT, MEDIA, LINK"
        )
        String type,

        /*
          게시글 제목.
         */
        @NotBlank
        @Size(max = 120)
        String title,

        /*
          게시글 본문 또는 캡션.
          TEXT에서는 필수(실제 강제는 도메인에서 수행),
          MEDIA에서는 선택,
          LINK에서는 사용하지 않는다.
         */
        @Size(max = 20000)
        String content,

        /*
          링크 게시글용 URL.
          LINK 타입일 때 유효한 http/https URL 여야 한다.
          다른 타입에서는 null/빈 값이어도 된다.
         */
        @Size(max = 1024)
        @URL(
                regexp = "^(http|https)://.*$",
                message = "link must be a valid http/https URL"
        )
        String link,

        /*
          미디어 게시글 첨부 자산 정보.
          MEDIA 타입일 때만 의미가 있다.
         */
        List<PostAssetUploadDTO> assets
) {
}
