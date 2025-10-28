package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.PasswordHash;
import com.y11i.springcommddd.iam.domain.exception.InvalidDisplayName;
import com.y11i.springcommddd.iam.domain.exception.InvalidEmail;
import com.y11i.springcommddd.iam.domain.exception.InvalidPasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ValueObjectsTest {

    // --- Email ---
    @Test
    @DisplayName("Email: Trim/toLowerCase")
    void email_trim_lowerCase() {
        var e = new Email("  USER@Domain.Com   ");
        assertThat(e.value()).isEqualTo("user@domain.com");
    }

    @Test
    @DisplayName("Email: null/blank/형식(@ 없음, @로 시작/끝) → InvalidEmail")
    void email_invalid_cases() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(InvalidEmail.class);
        assertThatThrownBy(() -> new Email("   ")).isInstanceOf(InvalidEmail.class);
        assertThatThrownBy(() -> new Email("no-at-sign")).isInstanceOf(InvalidEmail.class);
        assertThatThrownBy(() -> new Email("@abc")).isInstanceOf(InvalidEmail.class);
        assertThatThrownBy(() -> new Email("abc@")).isInstanceOf(InvalidEmail.class);
    }

    // --- DisplayName ---
    @Test
    @DisplayName("DisplayName: 트림 유지 및 하한(2) 충족")
    void displayName_trim_and_min() {
        var dn = new com.y11i.springcommddd.iam.domain.DisplayName("  Alice  ");
        assertThat(dn.value()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("DisplayName: null/blank/길이<2 → InvalidDisplayName")
    void displayName_invalid_cases() {
        assertThatThrownBy(() -> new com.y11i.springcommddd.iam.domain.DisplayName(null)).isInstanceOf(InvalidDisplayName.class);
        assertThatThrownBy(() -> new com.y11i.springcommddd.iam.domain.DisplayName("   ")).isInstanceOf(InvalidDisplayName.class);
        assertThatThrownBy(() -> new com.y11i.springcommddd.iam.domain.DisplayName("A")).isInstanceOf(InvalidDisplayName.class);
        // 상한(>50)은 VO에서 직접 체크하지 않으므로 여기서 테스트하지 않음 (DB 컬럼 길이로 관리)
    }

    // --- PasswordHash ---

    @Test
    @DisplayName("PasswordHash: 유효한 해시 문자열로 생성")
    void passwordHash_ok() {
        var h = PasswordHash.fromEncoded("$argon2id$v=19$m=65536,t=3,p=1$....");
        assertThat(h.encoded()).startsWith("$argon2id");
    }

    @Test
    @DisplayName("PasswordHash: null/blank → InvalidPasswordHash")
    void passwordHash_invalid_cases() {
        assertThatThrownBy(() -> PasswordHash.fromEncoded(null)).isInstanceOf(InvalidPasswordHash.class);
        assertThatThrownBy(() -> PasswordHash.fromEncoded("   ")).isInstanceOf(InvalidPasswordHash.class);
    }
}
