package com.y11i.springcommddd.comments.domain;

/**
 * 댓글의 표시 상태.
 *
 * <ul>
 *   <li>{@link #VISIBLE}: 표시됨</li>
 *   <li>{@link #DELETED}: 소프트 삭제됨(내용 노출 제한 등 정책 적용 가능)</li>
 * </ul>
 */
public enum CommentStatus {
    /** 표시됨 */
    VISIBLE,
    /** 소프트 삭제됨 */
    DELETED
}
