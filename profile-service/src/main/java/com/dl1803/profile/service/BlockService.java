package com.dl1803.profile.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dl1803.profile.dto.response.PageResponse;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.entity.UserProfile;
import com.dl1803.profile.exception.AppException;
import com.dl1803.profile.exception.ErrorCode;
import com.dl1803.profile.mapper.UserProfileMapper;
import com.dl1803.profile.repository.BlockRepository;
import com.dl1803.profile.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BlockService {
    BlockRepository blockRepository;
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public void blockUser(String targetUserId) {
        String currentUserId = getCurrentUserId();
        if (userProfileRepository.findByUserId(targetUserId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        if (targetUserId.equals(currentUserId)) {
            throw new AppException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
        }

        if (blockRepository.existsBlockRelationship(currentUserId, targetUserId)) {
            throw new AppException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        Instant createdDate = Instant.now();
        blockRepository.blockUser(currentUserId, targetUserId, createdDate);

        blockRepository.deleteFriendshipIfExists(currentUserId, targetUserId);
    }

    public void unblockUser(String targetUserId) {
        String currentUserId = getCurrentUserId();
        if (!userProfileRepository.existsById(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        if (targetUserId.equals(currentUserId)) {
            throw new AppException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
        }

        // chỉ người chủ động chặn mới cho phép gỡ -> else lỗi
        if (!blockRepository.hasBlocked(currentUserId, targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_BLOCKED);
        }

        blockRepository.unblockUser(currentUserId, targetUserId);
    }

    public PageResponse<UserProfileResponse> getBlockedUsers(Pageable pageable) {
        String currentUserId = getCurrentUserId();
        Page<UserProfile> pageData = blockRepository.findBlockedUsers(currentUserId, pageable);
        List<UserProfileResponse> responseList = pageData.getContent().stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(pageData.getNumber() + 1)
                .pageSize(pageData.getSize())
                .totalPage(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(responseList)
                .build();
    }
}
