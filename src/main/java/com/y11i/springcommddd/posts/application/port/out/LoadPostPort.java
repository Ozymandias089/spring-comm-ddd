package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadPostPort {
    PageSlice findByCriteria(
            Optional<CommunityId> communityId,
            Optional<MemberId> authorId,
            Optional<PostStatus> status,
            int page, int size,
            Sort sort // createdAt desc 등
    );

    record Sort(String property, boolean desc) {
        public static Sort byCreatedAtDesc() { return new Sort("createdAt", true); }
    }

    record Row( // 요약용 투사
                UUID id,
                CommunityId communityId,
                MemberId authorId,
                String title,
                PostStatus status,
                Instant publishedAt,
                Instant createdAt,
                int up, int down
    ) {}

    record PageSlice(int page, int size, long totalElements, int totalPages, List<Row> content) {}

}
