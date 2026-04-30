package com.michelet.waiting.presentation.controller;


import com.michelet.common.response.ApiResponse;
import com.michelet.waiting.application.dto.GetWaitingStatusQuery;
import com.michelet.waiting.application.dto.WaitingResult;
import com.michelet.waiting.application.service.WaitingService;
import com.michelet.waiting.presentation.WaitingSuccessCode;
import com.michelet.waiting.presentation.dto.request.EnterWaitingRequest;
import com.michelet.waiting.presentation.dto.response.WaitingStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/waitings")
@RequiredArgsConstructor
public class WaitingApiController {

    private final WaitingService waitingService;

    // 대기 등록
    @PostMapping
    public ResponseEntity<ApiResponse<WaitingStatusResponse>>enter(
            @RequestBody @Valid EnterWaitingRequest request
    ){
        WaitingResult result = waitingService.enterWaiting(request.toCommand());
        return ResponseEntity
                .status(WaitingSuccessCode.ENTER_SUCCESS.getHttpStatus())
                .body(ApiResponse.ok(
                        WaitingSuccessCode.ENTER_SUCCESS,
                        WaitingStatusResponse.from(result)
                ));
    }

    // 대기 순번 조회
    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<WaitingStatusResponse>> getStatus(
            @RequestParam String token
    ){
        WaitingResult result = waitingService.getStatus(
                new GetWaitingStatusQuery(token)
        );
        return ResponseEntity
                .status(WaitingSuccessCode.GET_SUCCESS.getHttpStatus())
                .body(ApiResponse.ok(
                        WaitingSuccessCode.GET_SUCCESS,
                        WaitingStatusResponse.from(result)
                ));

    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID waitingId
    ){
        waitingService.cancelWaiting(waitingId);
        return ResponseEntity
                .status(WaitingSuccessCode.CANCEL_SUCCESS.getHttpStatus())
                .body(ApiResponse.ok(WaitingSuccessCode.CANCEL_SUCCESS, null));
    }

}
