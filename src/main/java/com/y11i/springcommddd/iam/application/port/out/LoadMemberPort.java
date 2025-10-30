package com.y11i.springcommddd.iam.application.port.out;

import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

/**
 * 회원 조회용 출력 포트.
 *
 * <p>
 * 애플리케이션 서비스(유스케이스)는 이 포트를 통해
 * 도메인 엔티티 {@link Member}를 로드한다.
 * </p>
 *
 * <p>
 * 이 포트는 저장소(JPA, Redis 캐시, 외부 서비스 등)에 대한 구체적인 접근 방식을
 * 추상화한다. 즉, 인프라스트럭처 계층이 이 인터페이스를 구현하고,
 * 서비스 계층은 구현 세부사항을 모른 채 회원 정보를 조회할 수 있다.
 * </p>
 *
 * <p>
 * 호출자는 읽기 전용 조회인지, 후속으로 수정/저장을 할 것인지에 따라
 * 적절한 트랜잭션 경계를 잡아야 한다.
 * </p>
 */
public interface LoadMemberPort {

    /**
     * 고유 회원 식별자({@link MemberId})로 회원을 조회한다.
     *
     * <p>
     * 이 식별자는 시스템 내부에서 불변한 1차 식별자이며,
     * 로그인 이메일 변경 등의 이벤트에도 영향을 받지 않는다.
     * </p>
     *
     * @param id 불변 회원 식별자
     * @return 해당 {@link Member}가 존재하면 Optional.of(member), 없으면 Optional.empty()
     */
    Optional<Member> loadById(MemberId id);

    /**
     * 이메일 주소로 회원을 조회한다.
     *
     * <p>
     * 이 메서드는 주로 "로그인 자격 증명 확인", "비밀번호 재설정 요청" 같은 시나리오에서 사용되며,
     * 보안/복구 흐름에서 사용자의 계정을 찾아야 할 때 유용하다.
     * </p>
     *
     * <p>
     * 단, 이메일은 사용자가 나중에 변경할 수 있으므로
     * <b>권한 체크나 계정 식별의 근거(=내가 이 계정의 주인이다)</b>로 쓰는 것은 바람직하지 않다.
     * 인증 이후의 내부 로직은 가급적 {@link #loadById(MemberId)} 기반으로 동작해야 한다.
     * </p>
     *
     * @param email 로그인 ID나 연락처 역할을 하는 이메일 값 객체
     * @return 해당 {@link Member}가 존재하면 Optional.of(member), 없으면 Optional.empty()
     */
    Optional<Member> loadByEmail(Email email);
}
