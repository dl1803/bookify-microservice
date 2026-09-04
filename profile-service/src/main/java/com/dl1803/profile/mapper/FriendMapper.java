package com.dl1803.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dl1803.profile.dto.response.FriendRequestResponse;
import com.dl1803.profile.projection.FriendRequestProjection;

@Mapper(
        componentModel = "spring",
        uses = {UserProfileMapper.class})
// Khai báo cho MapStruct biết khi mapping FriendMapper, nếu gặp chuyển đổi giữa UserProfile (entity trong projection)
// sang UserProfileResponse (DTO), hãy tái sử dụng bean UserProfileMapper.
public interface FriendMapper {

    // Map cho lời mời nhận được: sender -> fromProfile
    @Mapping(target = "id", source = "requestId")
    @Mapping(target = "fromProfile", source = "sender")
    @Mapping(target = "toProfile", ignore = true)
    FriendRequestResponse toIncomingResponse(FriendRequestProjection projection);

    // Map cho lời mời đã gửi: recipient -> toProfile
    @Mapping(target = "id", source = "requestId")
    @Mapping(target = "toProfile", source = "recipient")
    @Mapping(target = "fromProfile", ignore = true)
    FriendRequestResponse toOutgoingResponse(FriendRequestProjection projection);
}
