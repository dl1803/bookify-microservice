package com.dl1803.profile.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.dl1803.profile.dto.request.ProfileCreationRequest;
import com.dl1803.profile.dto.response.ApiResponse;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.service.FriendService;
import com.dl1803.profile.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {
    UserProfileService userProfileService;
    FriendService friendService;

    @PostMapping("/internal/users")
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.createProfile(request))
                .build();
    }

    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getByUserId(userId))
                .build();
    }

    @GetMapping("/internal/users/bulk")
    ApiResponse<List<UserProfileResponse>> getProfilesByUserIdIn(@RequestParam("userIds") List<String> userIds) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.getProfilesByUserIdIn(userIds))
                .build();
    }

    // Lấy ds id bạn bè
    @GetMapping("/internal/users/{userId}/friend-ids")
    ApiResponse<List<String>> getFriendIds(@PathVariable String userId) {
        return ApiResponse.<List<String>>builder()
                .result(friendService.getAllFriendUserIds(userId))
                .build();
    }
}
