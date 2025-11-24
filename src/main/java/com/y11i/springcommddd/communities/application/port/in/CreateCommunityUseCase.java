package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityCreateResponseDTO;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

/**
 * 커뮤니티 생성(Create Community) 유즈케이스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>커뮤니티 생성 요청을 처리하는 애플리케이션 계층의 진입점(Port)</li>
 *     <li>입력(Command)과 출력(ResponseDTO)을 정의하여 외부 어댑터(Controller 등)가 사용할 수 있는 계약(Contract)을 제공</li>
 *     <li>도메인 정책을 트리거하는 사용 시나리오를 명확히 기술</li>
 * </ul>
 *
 * <p><b>관련 도메인 정책</b></p>
 * <ul>
 *     <li>커뮤니티 이름(name) → nameKey(slug)의 형식/정규화 규칙</li>
 *     <li>nameKey는 고유해야 함 (중복 불가)</li>
 *     <li>커뮤니티 생성자는 회원이어야 하며, 이메일 인증 및 ACTIVE 상태여야 함</li>
 *     <li>초기 상태는 {@code PENDING} 으로 생성</li>
 *     <li>초기 규칙(rules)이 제공될 경우 애그리게잇에 추가됨</li>
 * </ul>
 *
 * <p><b>헥사고날 아키텍처 관점</b></p>
 * 본 인터페이스는 <strong>입력 포트(Input Port)</strong>이며,
 * 어댑터(REST 컨트롤러 등)는 이 포트를 호출함으로써 애플리케이션 로직을 수행합니다.
 * 실제 구현은 {@code CreateCommunityService}가 담당합니다.
 */
public interface CreateCommunityUseCase {

    /**
     * 새 커뮤니티를 생성합니다.
     *
     * <p><b>처리 단계</b></p>
     * <ol>
     *     <li>요청자의 멤버 ID로 멤버 상태 검증</li>
     *     <li>커뮤니티 이름(name)으로 생성되는 nameKey(slug)의 유일성 검사</li>
     *     <li>도메인 애그리게잇 {@link com.y11i.springcommddd.communities.domain.Community} 생성</li>
     *     <li>요청된 규칙(rule)들을 애그리게잇에 추가</li>
     *     <li>영속 포트를 통해 저장</li>
     *     <li>생성 결과 DTO 반환</li>
     * </ol>
     *
     * @param cmd 커뮤니티 생성에 필요한 입력 데이터(요청자, 이름, 설명, 규칙 목록)
     * @return 생성된 커뮤니티 정보를 담은 DTO
     */
    CommunityCreateResponseDTO createCommunity(CreateCommunityCommand cmd);

    /**
     * 커뮤니티 생성 요청을 표현하는 Command 객체.
     *
     * <p><b>필드 설명</b></p>
     * <ul>
     *     <li>{@code actorId}: 커뮤니티를 생성하는 사용자 ID</li>
     *     <li>{@code name}: 커뮤니티 표시명 (slug는 이 값을 정규화하여 생성)</li>
     *     <li>{@code description}: 커뮤니티 설명 (nullable)</li>
     *     <li>{@code rules}: 초기 커뮤니티 규칙 목록 (nullable)</li>
     * </ul>
     *
     * <p><b>Command 객체의 목적</b></p>
     * <ul>
     *     <li>유즈케이스 입력값을 명확히 모델링</li>
     *     <li>컨트롤러/프론트 계층에서 전달되는 데이터를 안전하게 캡슐화</li>
     *     <li>테스트 시에도 명확한 API 형태 제공</li>
     * </ul>
     *
     * <p><b>주의</b></p>
     * <ul>
     *     <li>nameKey는 여기서 직접 받지 않음 → 도메인 VO {@code CommunityNameKey} 내부에 정규화 규칙 존재</li>
     *     <li>설명(description)은 nullable → 도메인에서 null 허용</li>
     * </ul>
     *
     * @param actorId 커뮤니티 생성자 멤버 ID
     * @param name 커뮤니티 표시명
     * @param description 커뮤니티 설명 (null 가능)
     * @param rules 커뮤니티 초기 규칙 목록
     */
    record CreateCommunityCommand(MemberId actorId, String name, String description, List<CommunityRuleDTO> rules){}
}
