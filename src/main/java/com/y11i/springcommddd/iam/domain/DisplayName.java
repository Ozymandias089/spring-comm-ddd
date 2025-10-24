package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 표시명 값 객체.
 *
 * <p><b>검증 규칙</b></p>
 * <ul>
 *   <li>null 금지</li>
 *   <li>트림 후 공백 금지</li>
 *   <li>길이 하한(2) 및 상한(50) 적용</li>
 * </ul>
 */
@Embeddable
public class DisplayName implements ValueObject {
    @Column(name = "display_name", nullable = false, length = 50)
    private String value;

    protected DisplayName() {}

    /**
     * 표시명으로 값 객체를 생성합니다.
     *
     * @param value 표시명 문자열
     * @throws IllegalArgumentException null/공백 또는 길이 규칙 위반
     */
    public DisplayName(String value) {
        if (value == null) throw new IllegalArgumentException("DisplayName value cannot be null");
        String trimmedValue = value.trim();
        if (trimmedValue.isBlank()) throw new IllegalArgumentException("DisplayName value cannot be blank");
        if (trimmedValue.length() < 2) throw new IllegalArgumentException("DisplayName value must be between 2..50");
        this.value = trimmedValue;
    }

    /** 표시명 문자열 */
    public String value() { return value; }
}
