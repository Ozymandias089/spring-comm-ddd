package com.y11i.springcommddd.redis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Testcontainers Redis 컨테이너에 붙는 RedisTemplate 설정.
 * <p>
 * 각 테스트 클래스에서 컨테이너 host/port를 받아서
 * 이 config에 주입할 수 있도록, settable 형태로 만들어준다.
 * <p>
 * 간단하게 하기 위해 여기서는 public 생성자에 host, port를 받도록 하지 않고
 * 테스트 클래스에서 직접 @Bean을 override해도 된다.
 * <p>
 * 여기 기본 버전은 "localhost:port"를 전제로 하고,
 * 테스트 클래스 쪽에서 동적으로 port를 주입해서 Bean을 등록할 거다.
 * <p>
 * 단일 테스트 클래스 내부 @TestConfiguration으로 정의해도 된다.
 */
@TestConfiguration
public class RedisTestConfig {
    public static LettuceConnectionFactory lettuceConnectionFactory(String host, int port) {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);
        // DB index 0 사용
        return new LettuceConnectionFactory(conf);
    }

    public static StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
        cf.afterPropertiesSet(); // 수동 초기화
        return new StringRedisTemplate(cf);
    }
}
