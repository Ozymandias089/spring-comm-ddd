package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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

    /** 이미 해싱된(인코딩된) 문자열에서 생성 */
    public static PasswordHash fromEncoded(String encoded) {
        return new PasswordHash(encoded);
    }

    public String encoded() { return encoded; }
}
