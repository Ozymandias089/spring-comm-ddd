//package com.y11i.springcommddd.iam.dto.request;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.Getter;
//
//public class RenameRequestDTO {
//    @Getter
//    @NotBlank
//    @Size(min = 2, max = 50)
//    private String displayName;
//
//    @JsonCreator
//    public RenameRequestDTO(@JsonProperty("displayName") String displayName) {
//        this.displayName = displayName;
//    }
//}
// src/main/java/com/y11i/springcommddd/iam/dto/request/RenameRequestDTO.java
package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameRequestDTO(
        @NotBlank @Size(min = 2, max = 50) String displayName
) {}
