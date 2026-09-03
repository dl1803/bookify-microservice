package com.dl1803.profile.dto.response;

import java.time.LocalDate;

import com.dl1803.profile.enums.FriendViewStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String userId;
    String username;
    String avatar;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    String bio;
    int friendsCount;
    FriendViewStatus friendViewStatus;
}
