package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 해시된 비밀번호 값 객체.
 *
 * <p><b>의도</b><br>
 * 비밀번호의 원문을 저장하지 않고, 해시(인코딩)된 문자열만 보관합니다.
 * bcrypt/argon2 등 PHC 포맷 전체 문자열을 저장하는 것을 전제로 합니다.
 * </p>
 */
@Embeddable
public class PasswordHash implements ValueObject {
    @Column(name = "password_hash", nullable = false, length = 255)
    private String encoded; // bcrypt/argon2 PHC 문자열 전체

    protected PasswordHash() { }

    private PasswordHash(String encoded) {
        if (encoded == null || encoded.isBlank())
            throw new IllegalArgumentException("encoded password cannot be empty");
        this.encoded = encoded;
    }

    /**
     * 이미 해시(인코딩)된 문자열에서 값 객체를 생성합니다.
     *
     * @param encoded 해시 문자열
     * @return {@link PasswordHash}
     * @throws IllegalArgumentException 비어 있거나 null인 경우
     */
    public static PasswordHash fromEncoded(String encoded) {
        return new PasswordHash(encoded);
    }

    /** 해시 문자열 */
    public String encoded() { return encoded; }
}
