package com.y11i.springcommddd.communities.bans.api;

import com.y11i.springcommddd.communities.bans.application.port.in.ViewBanUseCase;
import com.y11i.springcommddd.communities.bans.dto.response.CommunityBanPageResponseDTO;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/c/{nameKey}/bans")
@RequiredArgsConstructor
public class ViewBanController {

    private final ViewBanUseCase viewBanUseCase;

    /**
     * 커뮤니티별 밴 목록 조회
     * GET /api/c/{nameKey}/bans?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<CommunityBanPageResponseDTO> listCommunityBans(
            @PathVariable("nameKey") String nameKey,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticatedMember MemberId actorId
    ) {
        var query = new ViewBanUseCase.ListCommunityBansQuery(
                actorId,
                new CommunityNameKey(nameKey),
                page,
                size
        );

        CommunityBanPageResponseDTO result = viewBanUseCase.listCommunityBans(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<CommunityBanPageResponseDTO> listCommunityBanHistory(
            @PathVariable("nameKey") String nameKey,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticatedMember MemberId actorId
    ) {
        var query = new ViewBanUseCase.ListCommunityBanHistoryQuery(
                actorId,
                new CommunityNameKey(nameKey),
                page,
                size
        );

        CommunityBanPageResponseDTO result = viewBanUseCase.listCommunityBanHistory(query);
        return ResponseEntity.ok(result);
    }
}