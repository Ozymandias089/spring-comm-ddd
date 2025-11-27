package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.comments.domain.exception.InvalidCommentBody;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

/**
 * 댓글 본문 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>불변(Immutable)</li>
 *   <li>값 동등성 비교({@link #equals(Object)}) 제공</li>
 *   <li>DB에는 {@code @Lob}으로 저장 (DB 벤더에 따라 CLOB/TEXT)</li>
 * </ul>
 *
 * <p><b>제약</b></p>
 * <ul>
 *   <li>null 또는 공백 문자열 금지</li>
 * </ul>
 */
@Embeddable
public class CommentBody implements ValueObject {
    @Lob
    private String value;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
    protected CommentBody() {}

    /**
     * 본문 값을 받아 값 객체를 생성합니다.
     *
     * @param value 본문 문자열
     * @throws InvalidCommentBody null 또는 공백인 경우
     */
    public CommentBody(String value) {
        if (value == null || value.isBlank()) throw new InvalidCommentBody("Comment body cannot be null or blank");
        this.value = value;
    }

    /** 본문 문자열 값 */
    public String value() {
        return value;
    }

    @Override public boolean equals(Object o) { return o instanceof CommentBody b && value.equals(b.value); }
    @Override public int hashCode() { return value.hashCode(); }
}
