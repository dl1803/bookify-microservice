package com.dl1803.profile.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.dl1803.profile.dto.request.ReportRequest;
import com.dl1803.profile.dto.response.ApiResponse;
import com.dl1803.profile.dto.response.PageResponse;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.service.BlockService;
import com.dl1803.profile.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BlockController {
    BlockService blockService;
    ReportService reportService;

    // Chặn user
    @PostMapping("/users/{targetUserId}/block")
    public ApiResponse<Void> blockUser(@PathVariable String targetUserId) {
        blockService.blockUser(targetUserId);
        return ApiResponse.<Void>builder().message("User blocked").build();
    }

    // Bỏ chặn user
    @DeleteMapping("/users/{targetUserId}/block")
    public ApiResponse<Void> unblockUser(@PathVariable String targetUserId) {
        blockService.unblockUser(targetUserId);
        return ApiResponse.<Void>builder().message("User unblocked").build();
    }

    // Ds những người mà mình đã chặn
    @GetMapping("/users/blocked")
    public ApiResponse<PageResponse<UserProfileResponse>> getBlockedUsers(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ApiResponse.<PageResponse<UserProfileResponse>>builder()
                .result(blockService.getBlockedUsers(pageable))
                .build();
    }

    // Báo cáo tài khoản
    @PostMapping("/users/{targetUserId}/report")
    public ApiResponse<Void> reportUser(@PathVariable String targetUserId, @Valid @RequestBody ReportRequest request) {
        reportService.reportUser(targetUserId, request);
        return ApiResponse.<Void>builder().message("Report submitted").build();
    }
}
