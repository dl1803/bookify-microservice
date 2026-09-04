package com.dl1803.profile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dl1803.profile.dto.request.ProfileCreationRequest;
import com.dl1803.profile.dto.request.SearchUserRequest;
import com.dl1803.profile.dto.request.UpdateProfileRequest;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.entity.UserProfile;
import com.dl1803.profile.enums.FriendViewStatus;
import com.dl1803.profile.exception.AppException;
import com.dl1803.profile.exception.ErrorCode;
import com.dl1803.profile.mapper.UserProfileMapper;
import com.dl1803.profile.projection.FriendRequestProjection;
import com.dl1803.profile.repository.BlockRepository;
import com.dl1803.profile.repository.FriendRepository;
import com.dl1803.profile.repository.UserProfileRepository;
import com.dl1803.profile.repository.httpClient.FileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    FileClient fileClient;
    FriendRepository friendRepository;
    BlockRepository blockRepository;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);

        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getByUserId(String userId) {
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getProfile(String id) {
        UserProfile userProfile = userProfileRepository
                .findById(id) // tìm bằng id của profile
                .or(() -> userProfileRepository.findByUserId(id)) // ...id của user
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse response = userProfileMapper.toUserProfileResponse(userProfile);
        String targetUserId = userProfile.getUserId();

        // Đếm số lượng bạn bè của targetUser
        response.setFriendsCount(friendRepository.countFriends(targetUserId));

        // Tính trạng thái nút bấm giữa người xem (current) và người được xem (target)
        response.setFriendViewStatus(calculateFriendViewStatus(getCurrentUserId(), targetUserId));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> getAllProfiles() {
        return userProfileMapper.toListUserProfileResponse(userProfileRepository.findAll());
    }

    public UserProfileResponse getMyProfile() {
        String userId = getCurrentUserId();
        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse response = userProfileMapper.toUserProfileResponse(profile);
        response.setFriendsCount(friendRepository.countFriends(userId));
        response.setFriendViewStatus(null); // trang của chính mình kh cần nút kết bạn

        return response;
    }

    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        ;
        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfileMapper.update(profile, request);
        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }

    public UserProfileResponse updateAvatar(MultipartFile file) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        ;
        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Upload file - invoke file service api
        var response = fileClient.uploadMedia(file);

        profile.setAvatar(response.getResult().getUrl());

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }

    public List<UserProfileResponse> search(SearchUserRequest request) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();

        // Xử lý từ khóa để tìm kiếm, không phân biệt hoa thường
        String keyword = (request.getKeyword() != null) ? request.getKeyword().trim() : "";

        // Lọc chính mình + Tìm kiếm
        List<UserProfile> userProfiles =
                userProfileRepository.findByUsernameContainingIgnoreCaseAndUserIdNot(keyword, currentUserId);

        return userProfileMapper.toListUserProfileResponse(userProfiles);
    }

    public List<UserProfileResponse> getProfilesByUserIdIn(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        // Tìm tất cả profile có userId nằm trong danh sách userIds
        List<UserProfile> profiles = userProfileRepository.findAllByUserIdIn(userIds);

        return userProfileMapper.toListUserProfileResponse(profiles);
    }

    private FriendViewStatus calculateFriendViewStatus(String currentUserId, String targetUserId) {
        // Nếu chưa đăng nhập hoặc đang xem profile của chính mình -> null
        if (currentUserId == null || currentUserId.equals(targetUserId)) {
            return null;
        }

        // Nếu 1 trong 2 người đã chặn nhau -> BLOCKED
        if (blockRepository.existsBlockRelationship(currentUserId, targetUserId)) {
            return FriendViewStatus.BLOCKED;
        }

        // Tìm quan hệ bạn bè trong Neo4j
        Optional<FriendRequestProjection> friendshipOpt =
                friendRepository.findFriendshipBetweenUsers(currentUserId, targetUserId);
        if (friendshipOpt.isEmpty()) {
            return FriendViewStatus.NONE;
        }
        FriendRequestProjection friendship = friendshipOpt.get();

        // Nếu đã chấp nhận -> ACCEPTED
        if ("ACCEPTED".equalsIgnoreCase(friendship.status())) {
            return FriendViewStatus.ACCEPTED;
        }

        // Nếu đang chờ duyệt (PENDING) -> phân biệt người gửi và người nhận
        if ("PENDING".equalsIgnoreCase(friendship.status())) {
            if (currentUserId.equals(friendship.getSenderUserId())) {
                return FriendViewStatus.PENDING_SENT; // Mình gửi cho họ
            } else if (currentUserId.equals(friendship.getRecipientUserId())) {
                return FriendViewStatus.PENDING_RECEIVED; // Họ gửi cho mình
            }
        }

        return FriendViewStatus.NONE;
    }
}
