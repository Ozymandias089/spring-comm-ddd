package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFoundException;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFoundException;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadCommunityForPostPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostAssetsPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatePostDraftService implements CreatePostDraftUseCase {
    private final SavePostPort savePostPort;
    private final SavePostAssetsPort savePostAssetsPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;

    @Override
    @Transactional
    public PostId createTextDraft(CreateTextDraftCommand cmd) {
        validateCommunity(cmd.communityId());
        validateAuthor(cmd.authorId());

        // 도메인 정적 팩토리 (예: PostKind.TEXT + status = DRAFT)
        Post post = Post.createText(cmd.communityId(), cmd.authorId(), cmd.title(), cmd.content());

        Post saved = savePostPort.save(post);
        return saved.postId();
    }

    @Override
    @Transactional
    public PostId createLinkDraft(CreateLinkDraftCommand cmd) {
        validateCommunity(cmd.communityId());
        validateAuthor(cmd.authorId());

        Post post = Post.createLink(cmd.communityId(), cmd.authorId(), cmd.title(), cmd.link(), null);

        Post saved = savePostPort.save(post);
        return saved.postId();
    }

    @Override
    @Transactional
    public PostId createMediaDraft(CreateMediaDraftCommand cmd) {
        validateCommunity(cmd.communityId());
        validateAuthor(cmd.authorId());

        // 1) 미디어 타입 게시글 초안 생성 (본문/캡션 content 포함)
        Post post = Post.createMedia(cmd.communityId(), cmd.authorId(), cmd.title(), cmd.content());

        Post savedPost = savePostPort.save(post);
        PostId postId = savedPost.postId();

        // 2) 첨부 자산(PostAsset) 초안 생성
        List<AssetMeta> metas = cmd.assets();
        if (metas != null && !metas.isEmpty()) {
            List<PostAsset> assets = metas.stream()
                    .map(meta -> toPostAsset(postId, meta))
                    .toList();

            savePostAssetsPort.saveAll(assets);
        }

        return postId;
    }

    // ----------------------------------------------------------------------
    // 내부 헬퍼
    // ----------------------------------------------------------------------

    private PostAsset toPostAsset(PostId postId, AssetMeta meta) {
        // 여기서는 파일이 이미 업로드되어 있고,
        // fileName이 스토리지 key라고 가정하고 URL을 "스텁"으로 만든다.
        // 나중에 실제 업로드/URL 생성 파이프라인이 정해지면 이 부분만 교체하면 됨.
        String srcUrl = buildSrcUrlFromFileName(meta.fileName());

        if (meta.mediaType() == MediaType.IMAGE) {
            return PostAsset.image(
                    postId,
                    meta.fileSize(),        // sizeBytes
                    meta.fileName(),        // originalFilename
                    meta.displayOrder(),
                    srcUrl,
                    meta.mimeType(),
                    null,                   // width
                    null,                   // height
                    null,                   // altText
                    null                    // caption
            );

        } else if (meta.mediaType() == MediaType.VIDEO) {
            // ✅ 비디오 팩토리 시그니처에 맞게 인자 11개 전달
            return PostAsset.video(
                    postId,
                    meta.fileSize(),        // sizeBytes
                    meta.fileName(),        // originalFilename
                    meta.displayOrder(),
                    srcUrl,
                    meta.mimeType(),
                    null,                   // width
                    null,                   // height
                    null,                   // durationSec
                    null,                   // altText
                    null                    // caption
            );

        } else {
            throw new IllegalArgumentException("Unsupported mediaType: " + meta.mediaType());
        }
    }

    // --------- 검증 헬퍼 ---------

    private void validateCommunity(CommunityId communityId) {
        loadCommunityForPostPort.loadById(communityId).orElseThrow(() -> new CommunityNotFoundException(communityId));
    }

    private void validateAuthor(MemberId authorId) {
        loadAuthorForPostPort.loadById(authorId).orElseThrow(() -> new MemberNotFoundException(authorId));
    }

    /**
     * TODO 스텁: 현재는 단순히 파일명을 그대로 URL로 쓴다.
     * 나중에 스토리지/도메인 정책이 정해지면 여기서
     *  - CDN base URL
     *  - 버킷/폴더
     * 등을 조합해서 실제 접근 URL을 만들어주면 됨.
     */
    private String buildSrcUrlFromFileName(String fileName) {
        return fileName;
        // 예: return "https://cdn.springcomm.app/media/" + fileName;
    }
}
