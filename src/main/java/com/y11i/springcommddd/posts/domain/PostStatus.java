package com.y11i.springcommddd.posts.domain;

/**
 * 게시글 상태.
 *
 * <ul>
 *   <li>{@link #DRAFT}: 작성 중</li>
 *   <li>{@link #PUBLISHED}: 게시됨</li>
 *   <li>{@link #ARCHIVED}: 보관(수정/노출 제한)</li>
 * </ul>
 */
public enum PostStatus {
    DRAFT,      // 작성중
    PUBLISHED,  // 게시됨
    ARCHIVED    // 보관. 수정 및 노출 금지
}
