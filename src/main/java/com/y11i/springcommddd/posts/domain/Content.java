package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.posts.domain.exception.InvalidContent;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

/**
 * 게시글 본문 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>{@code @Lob} 매핑: DB에 TEXT/CLOB 등으로 저장</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 *
 * <p><b>검증 규칙</b></p>
 * <ul>
 *   <li>null/공백 금지</li>
 * </ul>
 */
@Embeddable
public class Content implements ValueObject {
    @Lob
    private String value;

    protected Content() {}

    /**
     * 본문으로 값 객체를 생성합니다.
     *
     * @param value 본문 문자열
     * @throws InvalidContent null/공백인 경우
     */
    public Content(String value) {
        if(value == null || value.isBlank()) throw new InvalidContent("Content value cannot be null or blank");
        this.value = value;
    }

    /** 본문 문자열 */
    public String value() { return value; }
}
