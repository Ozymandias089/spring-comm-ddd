package com.y11i.springcommddd.iam.dto.request;

import lombok.Getter;

public class ChangeBannerImageRequestDTO {
    @Getter
    private String bannerImageUrl;

    public ChangeBannerImageRequestDTO(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }
}
