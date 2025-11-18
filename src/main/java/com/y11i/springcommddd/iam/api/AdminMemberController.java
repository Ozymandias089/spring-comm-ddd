package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.request.admin.CreateAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.GrantAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.RevokeAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.SetStatusRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 관리자 전용 회원 관리 엔드포인트.
 *
 * <p>
 * 이 컨트롤러는 일반 사용자가 아니라 "이미 관리자 권한을 가진 주체"가
 * 다른 회원 계정에 대해 관리 작업을 수행할 수 있도록 하는 API를 제공한다.
 * 즉, 호출자는 어드민 콘솔/백오피스 UI 같은 신뢰된 클라이언트라고 가정한다.
 * </p>
 *
 * <p>
 * 제공 기능:
 * </p>
 * <ul>
 *     <li><b>관리자 권한 부여</b>: 특정 회원에게 관리자 권한을 부여 (ex. ROLE_ADMIN 추가)</li>
 *     <li><b>관리자 권한 회수</b>: 기존 관리자의 권한을 회수</li>
 *     <li><b>회원 상태 변경</b>: ACTIVE / SUSPENDED / DELETED 같은 계정 상태를 관리자가 강제로 바꿈</li>
 *     <li><b>신규 관리자 계정 생성</b>: 운영용 계정을 직접 생성 (일반 회원가입 경로를 타지 않음)</li>
 * </ul>
 *
 * <p>
 * 중요한 점은 이 API가 "대상 회원"을 인자로 받는다는 것:
 * </p>
 * <ul>
 *     <li>요청 바디에는 보통 <code>memberId</code> (UUID)와 함께 필요한 정보가 담긴다.</li>
 *     <li>즉, 여기서는 현재 로그인한 사용자 자신의 정보만 다루는 것이 아니라,
 *         <b>관리자가 다른 계정에 대해 조치</b>를 내리는 구조다.</li>
 * </ul>
 *
 * <p>
 * 이 컨트롤러는 도메인 규칙/검증 자체를 직접 수행하지 않는다.
 * 실제 비즈니스 검증(예: 이미 DELETED인 계정은 다시 ACTIVE로 못 돌린다든가,
 * 자기 자신을 강등할 수 있는지 여부 등)은
 * {@link com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase} 구현체에서 처리한다.
 * </p>
 *
 * <p>
 * 응답 코드는 다음과 같이 설계된다:
 * </p>
 * <ul>
 *     <li>204 No Content: 작업 성공, 바디 없음 (권한 부여/회수, 상태 변경 등)</li>
 *     <li>201 Created: 새 관리 계정 생성 완료. Location 헤더에 생성된 계정의 리소스 URI를 담는다.</li>
 * </ul>
 *
 * <p>
 * 보안적으로 이 엔드포인트 전체는 <b>관리자 권한이 있는 인증된 요청만 허용</b>된다는
 * 전제가 반드시 필요하다. 즉, Spring Security 설정(예: hasRole('ADMIN'))으로
 * 접근 차단이 걸려 있어야 하며, 컨트롤러 자체는 그 전제 하에 동작한다.
 * </p>
 */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Validated
public class AdminMemberController {
    private final AdminMemberUseCase adminMemberUseCase;

    /**
     * 특정 회원에게 관리자 권한을 부여한다.
     *
     * <p>
     * 예를 들어 일반 사용자에게 ROLE_ADMIN을 추가하는 용도.
     * 동일한 권한을 중복 부여하려 할 경우의 처리(무시 / 에러)는
     * 유스케이스 구현체의 정책에 따른다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/grant-admin")
    public ResponseEntity<Void> grantAdmin(@Valid @RequestBody GrantAdminRequestDTO req) {
        adminMemberUseCase.grantAdmin(new AdminMemberUseCase.GrantAdminCommand(MemberId.objectify(req.getMemberId())));
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 회원으로부터 관리자 권한을 회수한다.
     *
     * <p>
     * 일반적으로 ROLE_ADMIN을 제거하는 시나리오를 의미한다.
     * 자기 자신(admin) 계정의 권한을 회수할 수 있는지 여부 등은
     * 도메인 규칙/비즈니스 정책에 따라 유스케이스에서 검증되어야 한다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/revoke-admin")
    public ResponseEntity<Void> revokeAdmin(@Valid @RequestBody RevokeAdminRequestDTO req) {
        adminMemberUseCase.revokeAdmin(new AdminMemberUseCase.RevokeAdminCommand(MemberId.objectify(req.getMemberId())));
        return ResponseEntity.noContent().build();
    }

    /**
     * 회원의 계정 상태(예: ACTIVE, SUSPENDED, DELETED 등)를 강제로 변경한다.
     *
     * <p>
     * 이 기능은 운영/제재 시나리오에 해당한다.
     * 예를 들어 규칙 위반 사용자를 SUSPENDED로 전환하거나,
     * 완전히 퇴출시키는(DELETED 표기) 등의 조치가 가능하다.
     * </p>
     *
     * <p>
     * 상태 전이 규칙(예: DELETED → ACTIVE 복구 허용 여부 등),
     * SUSPENDED 사용자가 로그인은 가능하지만 게시글 작성은 막는지 여부 등은
     * 도메인 서비스/유스케이스에서 책임지고 판단해야 한다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/set-status")
    public ResponseEntity<Void> setStatus(@Valid @RequestBody SetStatusRequestDTO req) {
        adminMemberUseCase.setStatus(new AdminMemberUseCase.SetStatusCommand(MemberId.objectify(req.getMemberId()), req.getStatus()));
        return ResponseEntity.noContent().build();
    }

    /**
     * 신규 관리자 계정을 생성한다.
     *
     * <p>
     * 일반 사용자 가입 플로우(공개 register API)를 거치지 않고,
     * 운영자가 직접 관리 계정을 발급해야 할 때 사용된다.
     * 이 과정에서 생성된 계정은 곧바로 관리자 권한을 가질 수 있으며,
     * 별도의 이메일 인증 절차를 건너뛸 수 있다.
     * (정확한 정책은 유스케이스 구현에 따른다.)
     * </p>
     *
     * <p>
     * 성공 시 201 Created와 함께 Location 헤더에
     * 새로 생성된 관리자 계정의 식별자 URI를 포함한다.
     * 응답 바디는 비워둘 수 있다.
     * </p>
     */
    @PostMapping("/create-admin")
    public ResponseEntity<Void> createAdmin(@Valid @RequestBody CreateAdminRequestDTO req) {
        var id = adminMemberUseCase.createAdminAccount(
                new AdminMemberUseCase.CreateAdminCommand(req.getEmail(), req.getDisplayName(), req.getPassword())
        );
        return ResponseEntity.created(URI.create("/api/admin/members/" + id)).build();
    }
}
