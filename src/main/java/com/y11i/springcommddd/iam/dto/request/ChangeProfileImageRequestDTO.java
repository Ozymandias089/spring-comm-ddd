package com.y11i.springcommddd.iam.dto.request;

import lombok.Getter;

public class ChangeProfileImageRequestDTO {
    @Getter
    private String profileImageUrl;

    public ChangeProfileImageRequestDTO(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
