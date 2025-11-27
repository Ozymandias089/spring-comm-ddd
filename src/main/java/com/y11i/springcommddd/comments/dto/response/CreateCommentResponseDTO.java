package com.y11i.springcommddd.comments.dto.response;

import lombok.Builder;

public record CreateCommentResponseDTO(String commentId) {
    @Builder
    public CreateCommentResponseDTO {}
}
