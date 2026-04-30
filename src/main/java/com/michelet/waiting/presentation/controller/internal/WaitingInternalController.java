package com.michelet.waiting.presentation.controller.internal;

import com.michelet.common.response.ApiResponse;
import com.michelet.waiting.application.dto.WaitingResult;
import com.michelet.waiting.application.service.WaitingService;
import com.michelet.waiting.presentation.WaitingSuccessCode;
import com.michelet.waiting.presentation.dto.response.WaitingStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/waitings")
@RequiredArgsConstructor
public class WaitingInternalController {

    private final WaitingService waitingService;

    // 입장 가능 여부 검증 - 예약 서비스가 예약 전 호출
    @GetMapping("/verify-token")
    public ResponseEntity<ApiResponse<WaitingStatusResponse>> verifyToekn(
            @RequestParam String token
    ){
        WaitingResult result = waitingService.verifyToken(token);
        return ResponseEntity
                .status(WaitingSuccessCode.GET_SUCCESS.getHttpStatus())
                .body(ApiResponse.ok(
                        WaitingSuccessCode.GET_SUCCESS,
                        WaitingStatusResponse.from(result)
                ));

    }

    // 예약 완료 후 토큰 삭제 - 예약 서비스가 예약 완료 시 호출
    @DeleteMapping("/{waitingId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeWaiting(
            @PathVariable UUID waitingId,
            @RequestHeader("X-User-Id") UUID userId
        ){
        waitingService.completeWaiting(waitingId, userId);
        return ResponseEntity
                .status(WaitingSuccessCode.DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.ok(
                        WaitingSuccessCode.DELETE_SUCCESS,
                        null
                        ));
    }

}
