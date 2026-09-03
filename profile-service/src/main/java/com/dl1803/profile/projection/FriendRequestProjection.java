package com.dl1803.profile.projection;

import com.dl1803.profile.entity.UserProfile;

// Entity ảo chỉ-đọc (Read-only DTO/Projection).
// Sinh ra chỉ để làm một việc: Hứng kết quả snapshot từ câu lệnh Cypher.
public interface FriendRequestProjection {
    String getRequestId();

    String getStatus();

    String getCreatedDate();

    UserProfile getSender();

    UserProfile getRecipient();
}
