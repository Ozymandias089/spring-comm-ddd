package com.y11i.springcommddd.iam.application.port.out;

public interface MailPort {
    void send(String to, String subject, String body);
}
