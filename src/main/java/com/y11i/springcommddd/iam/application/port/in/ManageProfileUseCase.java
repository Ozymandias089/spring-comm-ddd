package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.UUID;

/**
 * 사용자의 자기 프로필(표시명, 이메일, 비밀번호, 이미지 등)을 수정하는 유스케이스.
 *
 * <p>
 * 이 유스케이스는 "본인 계정"에 대한 변경만을 의도한다.
 * 즉, 호출자는 이미 인증된 사용자이며, 대상 {@code memberId}는 본인 ID라고 가정한다.
 * (권한 검증은 상위 계층(API, Security)에서 수행할 수 있다.)
 * </p>
 *
 * <p>모든 메서드는 변경 이후 최신 상태의 {@link MemberDTO}를 반환한다.</p>
 */
public interface ManageProfileUseCase {
    /**
     * 표시명(닉네임 등)을 변경한다.
     *
     * @param cmd 대상 회원과 변경할 표시명을 담은 명령
     * @return 변경 후 회원 정보
     */
    MemberDTO rename(RenameCommand cmd);

    /**
     * 이메일 주소를 변경한다.
     *
     * <p>정책상 이 변경은 이메일 인증 플로우(토큰 확인 등)과 연결될 수 있다.
     * 구현체는 즉시 반영하거나 '미확정 상태'로 둘 수 있다.</p>
     *
     * @param cmd 대상 회원과 새 이메일을 담은 명령
     * @return 변경 후 회원 정보
     */
    MemberDTO changeEmail(ChangeEmailCommand cmd);

    /**
     * 비밀번호를 변경한다.
     *
     * <p>구현체는 {@code currentPassword}를 반드시 검증해야 한다.
     * (즉, 비밀번호 변경은 본인 인증된 사용자만 가능)</p>
     *
     * @param cmd 대상 회원 / 새 비밀번호 / 현재 비밀번호
     * @return 변경 후 회원 정보
     */

    MemberDTO changePassword(ChangePasswordCommand cmd);

    /**
     * 프로필 이미지(아바타 등) URL을 갱신한다.
     *
     * @param command 대상 회원 / 새로운 프로필 이미지 URL
     * @return 변경 후 회원 정보
     */
    MemberDTO changeProfileImage(ChangeProfileImageCommand command);

    /**
     * 배너 이미지 URL(프로필 상단 커버 이미지 등)을 갱신한다.
     *
     * @param command 대상 회원 / 새로운 배너 이미지 URL
     * @return 변경 후 회원 정보
     */
    MemberDTO changeBannerImage(ChangeBannerImageCommand command);

    /**
     * 표시명(닉네임) 변경 명령.
     *
     * @param memberId     자기 자신의 회원 식별자(UUID)
     * @param displayName  새 표시명
     */
    record RenameCommand(UUID memberId, String displayName) {}

    /**
     * 이메일 변경 명령.
     *
     * @param memberId 자기 자신의 회원 식별자(UUID)
     * @param email    새 이메일 주소
     */
    record ChangeEmailCommand(UUID memberId, String email) {}

    /**
     * 비밀번호 변경 명령.
     *
     * @param memberId         자기 자신의 회원 식별자(UUID)
     * @param rawPassword      새 비밀번호(평문)
     * @param currentPassword  기존 비밀번호(검증용)
     */
    record ChangePasswordCommand(UUID memberId, String rawPassword, String currentPassword) {}

    /**
     * 프로필 이미지 URL 변경 명령.
     *
     * @param memberId        자기 자신의 회원 식별자(UUID)
     * @param profileImageUrl 새 프로필 이미지 URL
     */
    record ChangeProfileImageCommand(UUID memberId, String profileImageUrl) {}

    /**
     * 배너 이미지 URL 변경 명령.
     *
     * @param memberId       자기 자신의 회원 식별자(UUID)
     * @param bannerImageUrl 새 배너 이미지 URL
     */
    record ChangeBannerImageCommand(UUID memberId, String bannerImageUrl) {}
}
