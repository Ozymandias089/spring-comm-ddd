package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.CreateCommunityUseCase;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.exception.DuplicateCommunityNameKey;
import com.y11i.springcommddd.communities.dto.internal.CommunityRulesDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityCreateResponseDTO;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberStatus;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 생성 유즈케이스의 애플리케이션 서비스 구현.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>커뮤니티 생성 요청을 처리하는 애플리케이션 계층 컴포넌트</li>
 *     <li>도메인 애그리게잇인 {@link Community} 생성 및 초기 규칙 설정</li>
 *     <li>액터(member)의 권한 및 상태 검증</li>
 *     <li>커뮤니티 이름 키(slug)의 형식 및 유일성 보장</li>
 *     <li>도메인 모델을 영속화 포트({@link SaveCommunityPort})를 통해 저장</li>
 * </ul>
 *
 * <p><b>트랜잭션</b></p>
 * <ul>
 *     <li>메서드 단위로 @Transactional 적용하여 쓰기 작업을 보장</li>
 *     <li>읽기 작업은 포트 단에서 처리되며, 별도 read-only 트랜잭션이 필요 없음</li>
 * </ul>
 *
 * <p><b>헥사고날 아키텍처 관점</b></p>
 * 본 서비스는 애플리케이션 계층에 위치하며, 내부적으로 도메인 로직을 조율하고,
 * 외부 인프라에 접근하는 모든 작업은 포트 인터페이스를 통해 추상화합니다.
 */
@Service
@RequiredArgsConstructor
public class CreateCommunityService implements CreateCommunityUseCase {
    private final SaveCommunityPort saveCommunityPort;
    private final LoadCommunityPort loadCommunityPort;
    private final LoadMemberForCommunityPort loadMemberForCommunityPort;

    /**
     * 새 커뮤니티를 생성합니다.
     *
     * <p><b>처리 과정</b></p>
     * <ol>
     *     <li>액터(member)의 존재 및 상태 검증</li>
     *     <li>커뮤니티 이름(name)으로 생성되는 nameKey(slug)의 유일성 검사</li>
     *     <li>도메인 팩토리를 통해 {@link Community} 애그리게잇 생성</li>
     *     <li>요청된 규칙(rule)들을 도메인 메서드를 통해 추가</li>
     *     <li>포트({@link SaveCommunityPort})를 이용해 애그리게잇 영속화</li>
     *     <li>생성된 커뮤니티 정보를 DTO로 변환하여 반환</li>
     * </ol>
     *
     * @param cmd 커뮤니티 생성 요청 데이터(액터, 이름, 설명, 규칙)
     * @return 생성된 커뮤니티 정보 응답 DTO
     *
     * @throws MemberNotFound 요청자의 멤버 정보가 존재하지 않을 때
     * @throws UnauthorizedMemberAction 이메일 미인증 또는 비활성 멤버인 경우
     * @throws DuplicateCommunityNameKey nameKey가 이미 존재하는 경우
     */
    @Override
    @Transactional
    public CommunityCreateResponseDTO createCommunity(CreateCommunityCommand cmd) {
        // 1. 액터 검증
        validateMember(cmd.actorId());
        // 2. nameKey 중복 검증
        ensureNameKeyUnique(cmd.name());

        // 3. 도메인 agg 생성
        Community community = Community.create(cmd.name(), cmd.description());

        // 4. 규칙 추가
        if (cmd.rules() != null)
            for (CommunityRulesDTO rule : cmd.rules()) {
                community.addRule(rule.title(), rule.description(), rule.displayOrder());
            }

        // 5. 저장
        Community saved = saveCommunityPort.save(community);

        // 6. DTO 매핑
        return CommunityCreateResponseDTO.builder()
                .communityId(saved.communityId().stringify())
                .name(saved.communityName().value())
                .nameKey(saved.nameKey().value())
                .status(saved.status().toString())
                .createdAt(saved.createdAt())
                .build();
    }

    /**
     * 액터(member)의 권한을 검증합니다.
     *
     * <p><b>검증 규칙</b></p>
     * <ul>
     *     <li>멤버가 존재해야 함</li>
     *     <li>이메일 인증 완료 상태여야 함</li>
     *     <li>멤버 상태가 {@link MemberStatus#ACTIVE} 이어야 함</li>
     * </ul>
     *
     * @param memberId 요청자 식별자
     *
     * @throws MemberNotFound 존재하지 않는 멤버
     * @throws UnauthorizedMemberAction 활성 상태가 아니거나 이메일 미인증
     */
    private void validateMember(MemberId memberId) {
        Member member = loadMemberForCommunityPort.loadById(memberId).orElseThrow(() -> new MemberNotFound("Member not found"));
        if (!member.emailVerified() || member.status() != MemberStatus.ACTIVE) throw new UnauthorizedMemberAction("Member not active");
    }

    /**
     * 커뮤니티 이름을 기반으로 생성되는 nameKey(slug)의 유일성을 검사합니다.
     *
     * <p><b>검증 이유</b></p>
     * <ul>
     *     <li>nameKey는 URL 접근용 slug 역할을 하므로 중복이 허용되지 않음</li>
     *     <li>도메인 제약을 서비스 계층에서 선검증하여 사용자 친화적 에러 제공</li>
     *     <li>최종적 중복 방어는 DB 유니크 제약으로도 보장됨</li>
     * </ul>
     *
     * @param rawName 커뮤니티 표시명(이름)
     *
     * @throws DuplicateCommunityNameKey nameKey가 이미 존재하는 경우
     */
    private void ensureNameKeyUnique(String rawName) {
        var nk = new CommunityNameKey(rawName);
        boolean exists = loadCommunityPort.loadByNameKey(nk).isPresent();
        if (exists) throw new DuplicateCommunityNameKey("Community name key already exists: " + nk.value());
    }
}
