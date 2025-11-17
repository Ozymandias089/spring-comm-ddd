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

/**
 * 게시글 초안(DRAFT) 생성 유스케이스의 애플리케이션 서비스 구현체.
 *
 * <p><b>책임</b></p>
 * <ul>
 *     <li>TEXT / LINK / MEDIA 게시글의 초안을 생성하여 저장</li>
 *     <li>커뮤니티 ID / 작성자 ID 유효성 검증</li>
 *     <li>MEDIA 게시글의 자산(PostAsset) 저장</li>
 * </ul>
 *
 * <p>
 * 게시글의 실제 생성 규칙(상태 = DRAFT 부여, kind 결정)은
 * 도메인 애그리게잇 {@link Post} 의 정적 팩토리 메서드가 담당한다.
 * </p>
 *
 * <p>
 * 이 서비스는 트랜잭션 경계를 관리하며, DDD의 애플리케이션 계층 책임에 따라
 * <b>도메인 객체 생성 → 영속화 포트(savePort) → 외부 시스템(스토리지) 연결 로직 연결</b>
 * 의 흐름을 담당한다.
 * </p>
 *
 * @see com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase
 * @see Post
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatePostDraftService implements CreatePostDraftUseCase {
    private final SavePostPort savePostPort;
    private final SavePostAssetsPort savePostAssetsPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;

    // ----------------------------------------------------------------------
    // TEXT Draft
    // ----------------------------------------------------------------------

    /**
     * 텍스트 게시글 초안을 생성한다.
     *
     * <p><b>유효성 검증</b></p>
     * <ul>
     *     <li>커뮤니티 존재 여부</li>
     *     <li>작성자(Member) 존재 여부</li>
     * </ul>
     *
     * <p><b>도메인 규칙</b></p>
     * <ul>
     *     <li>Post.createText(...)를 통해 PostKind = TEXT, Status = DRAFT 로 생성</li>
     * </ul>
     *
     * @param cmd communityId, authorId, title, content
     * @return 생성된 게시글의 {@link PostId}
     *
     * @throws CommunityNotFoundException 커뮤니티 존재 X
     * @throws MemberNotFoundException 작성자 존재 X
     */
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

    // ----------------------------------------------------------------------
    // LINK Draft
    // ----------------------------------------------------------------------

    /**
     * 링크 게시글 초안을 생성한다.
     *
     * <p><b>유효성 검증</b></p>
     * 동일하게 커뮤니티와 작성자 존재 여부를 확인한다.
     *
     * <p><b>도메인 규칙</b></p>
     * <ul>
     *     <li>Post.createLink(...) 호출 → PostKind = LINK, Status = DRAFT</li>
     *     <li>링크 URL(LinkUrl)은 도메인 객체가 검증한다</li>
     * </ul>
     *
     * @param cmd communityId, authorId, title, link
     * @return 생성된 게시글의 {@link PostId}
     *
     * @throws CommunityNotFoundException 커뮤니티 존재 X
     * @throws MemberNotFoundException 작성자 존재 X
     */
    @Override
    @Transactional
    public PostId createLinkDraft(CreateLinkDraftCommand cmd) {
        validateCommunity(cmd.communityId());
        validateAuthor(cmd.authorId());

        Post post = Post.createLink(cmd.communityId(), cmd.authorId(), cmd.title(), cmd.link(), null);

        Post saved = savePostPort.save(post);
        return saved.postId();
    }

    // ----------------------------------------------------------------------
    // MEDIA Draft
    // ----------------------------------------------------------------------

    /**
     * 미디어 게시글 초안을 생성한다.
     *
     * <p><b>동작 흐름</b></p>
     * <ol>
     *     <li>커뮤니티 / 작성자 유효성 검증</li>
     *     <li>Post.createMedia(...) 호출 → PostKind = MEDIA, Status = DRAFT</li>
     *     <li>Post 저장</li>
     *     <li>첨부 미디어 자산(PostAsset) 생성 및 저장</li>
     * </ol>
     *
     * <p><b>주의</b></p>
     * <ul>
     *     <li>여기서는 “초안”을 만드는 것이므로 자산 개수 제약은 두지 않는다</li>
     *     <li>게시(PUBLISH) 시점에 MEDIA 게시글은 최소 1개의 자산이 있어야 한다</li>
     *     <li>파일 업로드는 이미 완료되었다고 가정하고 fileName을 스토리지 key로 사용</li>
     * </ul>
     *
     * @param cmd communityId, authorId, title, content, assets
     * @return 생성된 게시글의 {@link PostId}
     *
     * @throws CommunityNotFoundException 커뮤니티 존재 X
     * @throws MemberNotFoundException 작성자 존재 X
     */
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

    /**
     * AssetMeta → PostAsset 변환.
     *
     * <p>
     * 파일 업로드는 이미 완료되었다고 가정하고 있으며,
     * fileName(=스토리지 key)을 기반으로 접근 URL을 생성한다.
     * 현재는 스텁으로 fileName을 그대로 URL로 사용한다.
     * </p>
     *
     * @param postId 게시글 ID
     * @param meta AssetMeta (이미지/영상)
     * @return 변환된 PostAsset
     *
     * @throws IllegalArgumentException 지원되지 않는 미디어 타입
     */
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

    // ----------------------------------------------------------------------
    // 검증
    // ----------------------------------------------------------------------

    /**
     * 커뮤니티 존재 여부 검증.
     *
     * @param communityId 커뮤니티 ID
     * @throws CommunityNotFoundException 존재하지 않으면 발생
     */
    private void validateCommunity(CommunityId communityId) {
        loadCommunityForPostPort.loadById(communityId).orElseThrow(() -> new CommunityNotFoundException(communityId.stringify()));
    }

    /**
     * 작성자 존재 여부 검증.
     *
     * @param authorId 작성자 ID
     * @throws MemberNotFoundException 존재하지 않으면 발생
     */
    private void validateAuthor(MemberId authorId) {
        loadAuthorForPostPort.loadById(authorId).orElseThrow(() -> new MemberNotFoundException(authorId.stringify()));
    }

    /**
     * 현재는 fileName을 그대로 URL로 사용하는 스텁 메서드.
     *
     * <p>나중에 CDN 도입 시 다음과 같이 교체 가능:</p>
     * <pre>
     * <code>https://cdn.example.com/media/{fileName}</code>
     * </pre>
     */
    private String buildSrcUrlFromFileName(String fileName) {
        return fileName;
        // 예: return "https://cdn.springcomm.app/media/" + fileName;
    }
}
