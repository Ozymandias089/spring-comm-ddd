package com.y11i.springcommddd.common.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // --- Posts bounded context
    POST_NOT_FOUND("post.not_found", HttpStatus.NOT_FOUND),
    POST_TITLE_INVALID("post.title_invalid", HttpStatus.BAD_REQUEST),
    POST_CONTENT_INVALID("post.content_invalid", HttpStatus.BAD_REQUEST),
    POST_ARCHIVED_MODIFICATION_FORBIDDEN("post.archived_modification_forbidden", HttpStatus.CONFLICT),
    POST_STATUS_TRANSITION_FORBIDDEN("post.status_transition_forbidden", HttpStatus.CONFLICT),
    MEMBER_BANNED_FROM_COMMUNITY("member_banned.from_community", HttpStatus.UNAUTHORIZED),

    // --- media (posts.media)
    MEDIA_URL_INVALID("media.url_invalid", HttpStatus.BAD_REQUEST),
    MEDIA_DISPLAY_ORDER_INVALID("media.display_order_invalid", HttpStatus.BAD_REQUEST),
    MEDIA_METADATA_INVALID("media.metadata_invalid", HttpStatus.BAD_REQUEST),

    // --- Comment
    COMMENT_BODY_INVALID("comment.body_invalid", HttpStatus.BAD_REQUEST),
    COMMENT_DELETED_MODIFICATION_FORBIDDEN("comment.deleted_modification_forbidden", HttpStatus.CONFLICT),
    COMMENT_DEPTH_INVALID("comment.depth_invalid", HttpStatus.BAD_REQUEST),
    COMMENT_UNAVAILABLE("comment.unavailable", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND("comment.not_found", HttpStatus.NOT_FOUND),

    // --- Community
    COMMUNITY_NAME_INVALID("community.name_invalid", HttpStatus.BAD_REQUEST),
    COMMUNITY_NAME_KEY_INVALID("community.name_key_invalid", HttpStatus.BAD_REQUEST),
    COMMUNITY_ARCHIVED_MODIFICATION_FORBIDDEN("community.archived_modification_forbidden", HttpStatus.CONFLICT),
    COMMUNITY_STATUS_TRANSITION_FORBIDDEN("community.status_transition_forbidden", HttpStatus.CONFLICT),
    ID_FORMAT_INVALID("id_format_invalid", HttpStatus.BAD_REQUEST),
    COMMUNITY_NOT_FOUND("community.not_found", HttpStatus.NOT_FOUND),
    DUPLICATE_COMMUNITY_NAME_KEY("duplicate.community.name_key", HttpStatus.CONFLICT),

    IMAGE_URL_INVALID("image.url_invalid", HttpStatus.BAD_REQUEST),

    // community.moderator
    MODERATOR_DUPLICATE("community.moderator_duplicate", HttpStatus.CONFLICT),
    MODERATOR_NOT_FOUND("community.moderator_not_found", HttpStatus.NOT_FOUND),
    MODERATOR_INVALID("community.moderator_invalid", HttpStatus.BAD_REQUEST),

    // --- IAM
    MEMBER_DELETED_MODIFICATION_FORBIDDEN("member.deleted_modification_forbidden", HttpStatus.CONFLICT),
    MEMBER_STATUS_TRANSITION_FORBIDDEN("member.status_transition_forbidden", HttpStatus.CONFLICT),
    EMAIL_INVALID("member.email_invalid", HttpStatus.BAD_REQUEST),
    DISPLAY_NAME_INVALID("member.display_name_invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_HASH_INVALID("member.password_hash_invalid", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND("member.not_found", HttpStatus.NOT_FOUND),
    MEMBER_ACTION_UNAUTHORIZED("member.action_unauthorized", HttpStatus.UNAUTHORIZED),

    // --- Vote
    VOTE_VALUE_INVALID("vote.value_invalid", HttpStatus.BAD_REQUEST),
    VOTE_DUPLICATE("vote.duplicate", HttpStatus.CONFLICT),
    VOTE_NOT_FOUND("vote.not_found", HttpStatus.NOT_FOUND),
    POST_VOTE_UNAVAILABLE("post.vote_unavailable", HttpStatus.BAD_REQUEST),

    // --- common/app/infra
    BAD_REQUEST("generic.bad_request", HttpStatus.BAD_REQUEST),
    PERMISSION_DENIED("app.permission_denied", HttpStatus.FORBIDDEN),
    INTERNAL_ERROR("generic.internal_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;
    ErrorCode(String code, HttpStatus status) { this.code = code; this.status = status; }
    public String code() { return code; }
    public HttpStatus status() { return status; }
}
