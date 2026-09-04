package com.dl1803.profile.projection;

import java.time.Instant;

import com.dl1803.profile.entity.UserProfile;

// Entity ảo chỉ-đọc (Read-only DTO/Projection).
// Sinh ra chỉ để làm một việc: Hứng kết quả snapshot từ câu lệnh Cypher.

// Khi dùng một interface (như FriendRequestProjection) làm kiểu trả về, Spring Data kích hoạt cơ chế Target-Aware
// Proxy:
// Spring không tự tạo một object để imlp interface bth, mà tạo một "lớp vỏ bọc ảo" (Proxy) trỏ thẳng vào 1 instance của
// UserProfile.
// Khi code gọi getter như getRequestId(), Proxy này gọi thẳng vào object UserProfile để tìm xem có thuộc tính requestId
// hay getRequestId() không.
// Vì requestId là thuộc tính nằm trên đường nối FRIEND_WITH (Relationship), hoàn toàn không nằm trong class
// UserProfile,
// nên Spring không tìm thấy -> quăng lỗi
// Dùng record để khắc phục lỗi (cơ chế Target-Aware Proxy)
// -> Không còn Proxy: Spring Data thấy đây là một Class/Record độc lập (không phải interface),
// ->  tắt hoàn toàn cơ chế Target-Aware Proxy và không còn đi tìm kiếm attr bên trong UserProfile.
public record FriendRequestProjection(
        String requestId,
        String status,
        Instant createdDate,
        UserProfile sender,
        UserProfile recipient,
        String senderUserId,
        String recipientUserId) {
    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public UserProfile getSender() {
        return sender;
    }

    public UserProfile getRecipient() {
        return recipient;
    }

    public String getSenderUserId() {
        if (senderUserId != null) {
            return senderUserId;
        }
        return sender != null ? sender.getUserId() : null;
    }

    public String getRecipientUserId() {
        if (recipientUserId != null) {
            return recipientUserId;
        }
        return recipient != null ? recipient.getUserId() : null;
    }
}
