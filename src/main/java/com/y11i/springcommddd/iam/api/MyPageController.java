package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.*;
import com.y11i.springcommddd.iam.dto.response.MyPageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인된 사용자의 "내 정보 / 내 프로필" 영역을 다루는 엔드포인트.
 *
 * <p>
 * 이 컨트롤러는 인증된 사용자 전용이며,
 * 파라미터로 `@AuthenticatedMember MemberId memberId`를 받는다.
 * 이는 임의로 보내는 값이 아니라,
 * SecurityContext의 principal(AuthenticatedMemberPrincipal)에서 안전하게 추출된 MemberId이다.
 * 즉, 클라이언트가 다른 사람의 ID를 임의로 넣을 수 없다.
 * </p>
 *
 * <p>
 * 제공되는 기능:
 * </p>
 * <ul>
 *     <li><b>GET /api/my-page</b> - 내 현재 프로필 조회</li>
 *     <li><b>PATCH /api/my-page/display-name</b> - 닉네임 변경</li>
 *     <li><b>PATCH /api/my-page/email</b> - 이메일 변경(임시 반영 또는 전처리)</li>
 *     <li><b>PATCH /api/my-page/password</b> - 비밀번호 변경(현재 비밀번호 검증 포함)</li>
 *     <li><b>PATCH /api/my-page/profile-image</b> - 프로필 이미지 URL 변경</li>
 *     <li><b>PATCH /api/my-page/banner-image</b> - 배너 이미지 URL 변경</li>
 * </ul>
 *
 * <p>
 * 대부분의 엔드포인트는 변경 후 최신 상태를 {@link MyPageResponseDTO}로 반환한다.
 * 비밀번호 변경은 민감 동작이라 204 No Content만 반환한다.
 * </p>
 */
@RestController
@RequestMapping("/api/my-page")
@RequiredArgsConstructor
@Validated
public class MyPageController {
    private final FindMemberUseCase findMemberUseCase;
    private final ManageProfileUseCase manageProfileUseCase;

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회한다.
     *
     * <p>
     * 반환값은 {@link MyPageResponseDTO}이며,
     * 닉네임, 이미지 URL 등 공개 가능한 자기 정보가 포함된다.
     * </p>
     *
     * <p>
     * 이 엔드포인트는 인증이 필요하다.
     * </p>
     */
    @GetMapping
    public MyPageResponseDTO me(@AuthenticatedMember MemberId memberId) {
        MemberDTO memberDTO = findMemberUseCase.findById(memberId.id()).orElseThrow();
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    /**
     * 표시명(닉네임)을 변경한다.
     *
     * <p>
     * 요청자는 자신의 MemberId로만 변경을 요청할 수 있으며,
     * 남의 계정 닉네임을 변경할 수 없다.
     * </p>
     *
     * <p>
     * 성공 시 갱신된 프로필 정보를 반환한다.
     * </p>
     */
    @PatchMapping(path="/display-name", consumes="application/json", produces="application/json")
    public MyPageResponseDTO rename(@AuthenticatedMember MemberId memberId, @Valid @RequestBody RenameRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.rename(
                new ManageProfileUseCase.RenameCommand(memberId.id(), requestDto.displayName())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    /**
     * 이메일 주소를 변경한다.
     *
     * <p>
     * 시스템 정책에 따라 이 단계에서 즉시 이메일이 바뀔 수도 있고,
     * EmailVerificationUseCase와 연결되어 "변경 요청" 상태로 둘 수도 있다.
     * </p>
     *
     * <p>
     * 성공 시 갱신된 프로필 정보를 반환한다.
     * </p>
     */
    @PatchMapping(path="/email", consumes="application/json", produces="application/json")
    public MyPageResponseDTO changeEmail(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeEmailRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeEmail(
                new ManageProfileUseCase.ChangeEmailCommand(memberId.id(), requestDto.getEmail())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    /**
     * 비밀번호를 변경한다.
     *
     * <p>
     * 현재 비밀번호를 함께 제출해야 하며,
     * 내부에서 이를 검증한 뒤 새 비밀번호(해시 적용)를 저장한다.
     * </p>
     *
     * <p>
     * 민감한 작업이므로 응답은 204 No Content만 반환하고,
     * 사용자 프로필 JSON은 굳이 돌려주지 않는다.
     * </p>
     */
    @PatchMapping(path="/password", consumes="application/json", produces="application/json")
    public ResponseEntity<Void> changePassword(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangePasswordRequestDTO requestDto) {
        manageProfileUseCase.changePassword(
                new ManageProfileUseCase.ChangePasswordCommand(memberId.id(), requestDto.getNewPassword(), requestDto.getCurrentPassword())
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로필 이미지 URL을 변경한다.
     *
     * <p>
     * 저장 방식은 도메인 규칙에 따라 달라질 수 있다.
     * 예: 외부 이미지 CDN URL, 내부 업로드된 리소스의 경로 등.
     * </p>
     *
     * <p>
     * 성공 시 200 OK로 갱신된 프로필 정보를 반환한다.
     * </p>
     */
    @PatchMapping(path="/profile-image", consumes="application/json", produces="application/json")
    public MyPageResponseDTO changeProfileImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeProfileImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeProfileImage(
                new ManageProfileUseCase.ChangeProfileImageCommand(memberId.id(), requestDto.getProfileImageUrl())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    /**
     * 배너 이미지 URL(프로필 상단 커버 이미지 등)을 변경한다.
     *
     * <p>
     * 성공 시 갱신된 프로필 정보를 반환한다.
     * </p>
     */
    @PatchMapping(path="/banner-image", consumes="application/json", produces="application/json")
    public MyPageResponseDTO changeBannerImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeBannerImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeBannerImage(
                new ManageProfileUseCase.ChangeBannerImageCommand(memberId.id(), requestDto.getBannerImageUrl())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }
}
