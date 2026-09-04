package com.dl1803.profile.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // ẩn trường null trong json nếu có
public class FriendRequestResponse {
    String id;
    UserProfileResponse fromProfile; // người gửi lời mời
    UserProfileResponse toProfile; // người nhận lời mời
    String status;
    Instant createdDate;
}
