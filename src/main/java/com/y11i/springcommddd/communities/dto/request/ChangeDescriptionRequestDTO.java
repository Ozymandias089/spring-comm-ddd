package com.y11i.springcommddd.communities.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeDescriptionRequestDTO(@NotNull @Size(max = 500) String description) {
}
