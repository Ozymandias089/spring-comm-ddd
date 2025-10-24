package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 이메일 값 객체.
 *
 * <p><b>검증 규칙</b></p>
 * <ul>
 *   <li>null/공백 금지</li>
 *   <li>트림 후 소문자화</li>
 *   <li>간단한 형식 검사(@ 포함, 양끝 @ 금지)</li>
 * </ul>
 *
 * <p>필요 시 정규식 기반의 강화된 형식 검증으로 확장하세요.</p>
 */
@Embeddable
public class Email implements ValueObject {
    @Column(name = "email", nullable = false, length = 255)
    private String value;

    protected Email() {}

    /**
     * 이메일 문자열로 값 객체를 생성합니다.
     *
     * @param value 이메일 문자열
     * @throws IllegalArgumentException null/공백 또는 형식 위반
     */
    public Email(String value) {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Email value cannot be null or blank");
        String trimmedValue = value.trim().toLowerCase();
        // 아주 간단한 형식 검사 (필요시 강화)
        if (!trimmedValue.contains("@") || trimmedValue.startsWith("@") || trimmedValue.endsWith("@"))
            throw new IllegalArgumentException("email format invalid");
        this.value = trimmedValue;
    }

    /** 이메일 문자열 값 */
    public String value() { return value; }
}
