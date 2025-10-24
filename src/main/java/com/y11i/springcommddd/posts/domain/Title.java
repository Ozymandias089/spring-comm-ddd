package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 게시글 제목 값 객체.
 *
 * <p><b>검증 규칙</b></p>
 * <ul>
 *   <li>null/공백 금지</li>
 *   <li>최대 길이 200</li>
 * </ul>
 */
@Embeddable
public class Title implements ValueObject {
    @Column(name = "title", nullable = false, length = 200)
    private String value;

    /** For JPA Auditing */
    protected Title() {}

    /**
     * 제목으로 값 객체를 생성합니다.
     *
     * @param value 제목 문자열
     * @throws IllegalArgumentException null/공백 또는 길이 초과(200)인 경우
     */
    public Title(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("title cannot be null or blank");
        if (value.length() > 200) throw new IllegalArgumentException("Title cannot be longer than 200 characters");
        this.value = value.trim();
    }

    /** 제목 문자열 */
    public String value() { return value; }
}
