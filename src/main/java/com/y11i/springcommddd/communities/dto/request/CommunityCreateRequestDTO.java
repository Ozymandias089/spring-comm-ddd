package com.y11i.springcommddd.communities.dto.request;

import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CommunityCreateRequestDTO(
        @NotBlank String name,
        @Size(max = 500) String description,
        @Valid List<CommunityRuleDTO> rules
) {}
