package com.y11i.springcommddd.iam.application.port.out;

/**
 * 아웃바운드 메일 발송 포트.
 *
 * <p>
 * 애플리케이션 서비스는 이 포트를 통해
 * 이메일 인증, 비밀번호 재설정 안내, 알림 등 메시지를 외부로 발송한다.
 * </p>
 *
 * <p>
 * 구현체는 SMTP, 콘솔 로깅, 외부 메일 서비스(API) 등 어떤 방식이든 가능하며,
 * 이 인터페이스는 전송 채널의 세부사항을 추상화한다.
 * </p>
 */
public interface MailPort {

    /**
     * 간단한 텍스트 이메일을 전송한다.
     *
     * <p>
     * 포맷팅(템플릿 처리 등)은 호출자가 이미 완료한 상태라고 가정한다.
     * </p>
     *
     * @param to      수신자 이메일 주소
     * @param subject 메일 제목
     * @param body    메일 본문(텍스트)
     */
    void send(String to, String subject, String body);
}
