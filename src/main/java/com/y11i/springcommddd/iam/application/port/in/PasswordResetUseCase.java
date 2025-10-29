package com.y11i.springcommddd.iam.application.port.in;

public interface PasswordResetUseCase {
    void request(String email);
    void confirm(String token, String newPassword);
}
