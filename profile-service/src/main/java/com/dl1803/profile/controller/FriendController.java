package com.dl1803.profile.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.dl1803.profile.dto.response.ApiResponse;
import com.dl1803.profile.dto.response.FriendRequestResponse;
import com.dl1803.profile.dto.response.PageResponse;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.service.FriendService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {
    FriendService friendService;

    // Gửi lời mời kết bạn
    @PostMapping("/users/{targetUserId}/friend-request")
    public ApiResponse<Void> sendFriendRequest(@PathVariable String targetUserId) {
        friendService.sendFriendRequest(targetUserId);
        return ApiResponse.<Void>builder().message("Friend request sent").build();
    }

    // Ds lời mời nhận được
    @GetMapping("/users/friend-requests/incoming")
    public ApiResponse<PageResponse<FriendRequestResponse>> getIncomingFriendRequests(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PageResponse<FriendRequestResponse> response = friendService.getIncomingRequests(pageable);
        return ApiResponse.<PageResponse<FriendRequestResponse>>builder()
                .result(response)
                .build();
    }

    // Ds lời mời đã gửi
    @GetMapping("/users/friend-requests/outgoing")
    public ApiResponse<PageResponse<FriendRequestResponse>> getOutgoingRequests(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ApiResponse.<PageResponse<FriendRequestResponse>>builder()
                .result(friendService.getOutgoingRequests(pageable))
                .build();
    }

    // Chấp nận lời mời
    @PutMapping("/users/friend-requests/{requestId}/accept")
    public ApiResponse<Void> acceptFriendRequest(@PathVariable String requestId) {
        friendService.acceptFriendRequest(requestId);
        return ApiResponse.<Void>builder().message("Friend request accepted").build();
    }

    // Từ chối lời mời
    @PutMapping("/users/friend-requests/{requestId}/reject")
    public ApiResponse<Void> rejectFriendRequest(@PathVariable String requestId) {
        friendService.rejectFriendRequest(requestId);
        return ApiResponse.<Void>builder().message("Friend request rejected").build();
    }

    // Hủy lời mời đã gửi
    @DeleteMapping("/users/friend-requests/{requestId}")
    public ApiResponse<Void> cancelFriendRequest(@PathVariable String requestId) {
        friendService.cancelFriendRequest(requestId);
        return ApiResponse.<Void>builder().message("Friend request cancelled").build();
    }

    // Lấy ds bbe chính thức
    @GetMapping("/users/friends")
    public ApiResponse<PageResponse<UserProfileResponse>> getFriends(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ApiResponse.<PageResponse<UserProfileResponse>>builder()
                .result(friendService.getFriends(pageable))
                .build();
    }

    // hủy kết bạn
    @DeleteMapping("/users/friends/{friendUserId}")
    public ApiResponse<Void> unfriend(@PathVariable String friendUserId) {
        friendService.unfriend(friendUserId);
        return ApiResponse.<Void>builder().message("Unfriended successfully").build();
    }
}
