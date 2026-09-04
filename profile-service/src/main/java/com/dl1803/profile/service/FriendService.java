package com.dl1803.profile.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dl1803.event.dto.NotificationEvent;
import com.dl1803.profile.dto.response.FriendRequestResponse;
import com.dl1803.profile.dto.response.PageResponse;
import com.dl1803.profile.dto.response.UserProfileResponse;
import com.dl1803.profile.entity.UserProfile;
import com.dl1803.profile.exception.AppException;
import com.dl1803.profile.exception.ErrorCode;
import com.dl1803.profile.mapper.FriendMapper;
import com.dl1803.profile.mapper.UserProfileMapper;
import com.dl1803.profile.projection.FriendRequestProjection;
import com.dl1803.profile.repository.BlockRepository;
import com.dl1803.profile.repository.FriendRepository;
import com.dl1803.profile.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FriendService {
    FriendRepository friendRepository;
    BlockRepository blockRepository;
    UserProfileRepository userProfileRepository;
    FriendMapper friendMapper;
    UserProfileMapper userProfileMapper;
    KafkaTemplate<String, Object> kafkaTemplate;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public void sendFriendRequest(String targetUserId) {
        if (Objects.equals(targetUserId, this.getCurrentUserId())) {
            throw new AppException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
        }

        if (userProfileRepository.findByUserId(targetUserId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (blockRepository.existsBlockRelationship(this.getCurrentUserId(), targetUserId)) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (friendRepository.existsFriendRelationship(this.getCurrentUserId(), targetUserId)) {
            throw new AppException(ErrorCode.FRIEND_REQUEST_EXISTED);
        }

        String requestId = UUID.randomUUID().toString();
        Instant createdDate = Instant.now();
        friendRepository.createFriendRequest(this.getCurrentUserId(), targetUserId, requestId, createdDate);

        // send notification to target user
        NotificationEvent event = NotificationEvent.builder()
                .channel("FRIEND_REQUEST")
                .recipient(targetUserId)
                .templateCode("FRIEND_REQUEST")
                .param(Map.of("senderId", getCurrentUserId(), "requestId", requestId))
                .subject("Invitation to make new friends")
                .body("You have just received a friend request.")
                .build();
        kafkaTemplate.send("notification-delivery", event);
    }

    public void acceptFriendRequest(String requestId) {
        FriendRequestProjection friendRequestProjection = friendRepository
                .findFriendRequestByRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!getCurrentUserId().equals(friendRequestProjection.getRecipientUserId())) {
            throw new AppException(ErrorCode.NOT_FRIEND_REQUEST_RECIPIENT);
        }

        friendRepository.acceptFriendRequest(requestId);

        NotificationEvent event = NotificationEvent.builder()
                .channel("FRIEND_REQUEST_ACCEPTED")
                .recipient(friendRequestProjection.getSenderUserId())
                .templateCode("FRIEND_REQUEST_ACCEPTED")
                .param(Map.of("recipientId", getCurrentUserId(), "requestId", requestId))
                .subject("Friend request accepted")
                .body("Your friend request has been accepted.")
                .build();

        kafkaTemplate.send("notification-delivery", event);
    }

    public void rejectFriendRequest(String requestId) {
        FriendRequestProjection friendRequestProjection = friendRepository
                .findFriendRequestByRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!getCurrentUserId().equals(friendRequestProjection.getRecipientUserId())) {
            throw new AppException(ErrorCode.NOT_FRIEND_REQUEST_RECIPIENT);
        }

        friendRepository.deleteFriendRequestByRequestId(requestId);
    }

    public void cancelFriendRequest(String requestId) {
        FriendRequestProjection friendRequestProjection = friendRepository
                .findFriendRequestByRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!getCurrentUserId().equals(friendRequestProjection.getSenderUserId())) {
            throw new AppException(ErrorCode.NOT_FRIEND_REQUEST_SENDER);
        }

        friendRepository.deleteFriendRequestByRequestId(requestId);
    }

    public void unfriend(String friendUserId) {
        if (!friendRepository.existsFriendRelationship(this.getCurrentUserId(), friendUserId)) {
            throw new AppException(ErrorCode.NOT_FRIEND);
        }

        friendRepository.deleteFriendship(this.getCurrentUserId(), friendUserId);
    }

    // Pageable (interface) : đóng gói yêu cầu phân trang từ client -> gửi xuống db (chứa các attr: pageNumber,
    // pageSize,
    // sort, offset)
    // Page<T> (interface) : đóng gói dữ liệu trả về từ db(metadata sau 2 câu lệnh query value và count)
    // -> gửi lên server xử lí (chứa các attr: content, totalElements, totalPages, pageNumber, pageSize,..) -> getX()
    // PageResponse<T> : đóng gói dữ liệu cần thiết trả về từ server -> gửi lên client
    //  (chứa các attr: ListData, totalElements, totalPages, currentPage, pageSize)

    public PageResponse<FriendRequestResponse> getIncomingRequests(Pageable pageable) {
        Page<FriendRequestProjection> pageData = friendRepository.findIncomingRequests(getCurrentUserId(), pageable);

        List<FriendRequestResponse> responseList = pageData.getContent().stream()
                .map(friendMapper::toIncomingResponse)
                .toList();

        return PageResponse.<FriendRequestResponse>builder()
                .currentPage(pageData.getNumber() + 1) // Spring tính từ O, trả về client + 1
                .pageSize(pageData.getSize())
                .totalPage(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(responseList)
                .build();
    }

    public PageResponse<FriendRequestResponse> getOutgoingRequests(Pageable pageable) {
        Page<FriendRequestProjection> pageData = friendRepository.findOutgoingRequests(getCurrentUserId(), pageable);

        List<FriendRequestResponse> responseList = pageData.getContent().stream()
                .map(friendMapper::toOutgoingResponse)
                .toList();

        return PageResponse.<FriendRequestResponse>builder()
                .currentPage(pageData.getNumber() + 1)
                .pageSize(pageData.getSize())
                .totalPage(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(responseList)
                .build();
    }

    // Lấy ds toàn bộ ID bạn bè (dành cho Internal API)
    public List<String> getAllFriendUserIds(String userId) {

        return friendRepository.findAllFriendUserIds(userId);
    }

    // lấy ds bạn bè chính thức
    public PageResponse<UserProfileResponse> getFriends(Pageable pageable) {
        Page<UserProfile> pageData = friendRepository.findFriends(getCurrentUserId(), pageable);

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
