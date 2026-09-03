package com.dl1803.profile.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dl1803.profile.entity.UserProfile;
import com.dl1803.profile.projection.FriendRequestProjection;

@Repository
public interface FriendRepository extends Neo4jRepository<UserProfile, String> {

    // Tạo lời mời kết bạn (status: PENDING)
    @Query("MATCH (sender:user_profile {userId: $senderId}), (recipient:user_profile {userId: $recipientId}) "
            + "MERGE (sender)-[r:FRIEND_WITH]->(recipient) "
            + "ON CREATE SET r.requestId = $requestId, "
            + "r.status = 'PENDING', "
            + "r.createdDate = $createdDate")
    void createFriendRequest(
            @Param("senderId") String senderId,
            @Param("recipientId") String recipientId,
            @Param("requestId") String requestId,
            @Param("createdDate") Instant createdDate);

    // Chấp nhận lời mời (SET status = 'ACCEPTED')
    @Query("MATCH ()-[r:FRIEND_WITH {requestId: $requestId}]->() "
            + "SET r.status = 'ACCEPTED', r.updatedDate = datetime()")
    void acceptFriendRequest(@Param("requestId") String requestId);

    // Xóa relationship theo requestId (Clean Graph: Dùng cho Reject & Cancel)
    @Query("MATCH ()-[r:FRIEND_WITH {requestId: $requestId}]->() " + "DELETE r")
    void deleteFriendRequestByRequestId(@Param("requestId") String requestId);

    // Hủy kết bạn (Unfriend: Xóa quan hệ ACCEPTED 2 chiều)
    @Query(
            "MATCH (u1:user_profile {userId: $userId1})-[r:FRIEND_WITH {status: 'ACCEPTED'}]-(u2:user_profile {userId: $userId2}) "
                    + "DELETE r")
    void deleteFriendship(@Param("userId1") String userId1, @Param("userId2") String userId2);

    // Kiểm tra quan hệ FRIEND_WITH giữa 2 user (bất kể chiều nào)
    @Query("MATCH (u1:user_profile {userId: $userId1})-[r:FRIEND_WITH]-(u2:user_profile {userId: $userId2}) "
            + "RETURN COUNT(r) > 0")
    boolean existsFriendRelationship(@Param("userId1") String userId1, @Param("userId2") String userId2);

    // Tìm chi tiết lời mời theo requestId để validate quyền duyệt/hủy
    @Query("MATCH (sender:user_profile)-[r:FRIEND_WITH {requestId: $requestId}]->(recipient:user_profile) "
            + "RETURN r.requestId AS requestId, "
            + "r.status AS status, "
            + "r.createdDate AS createdDate, "
            + "sender AS sender, "
            + "recipient AS recipient")
    Optional<FriendRequestProjection> findFriendRequestByRequestId(@Param("requestId") String requestId);

    // Lấy danh sách bạn bè chính thức (Vô hướng, có Pageable)
    @Query( // value : câu lệnh lấy dữ liệu, countQuery : câu lệnh tính tổng số trang
            value =
                    "MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'ACCEPTED'}]-(friend:user_profile) "
                            + "RETURN friend SKIP $skip LIMIT $limit",
            countQuery =
                    "MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'ACCEPTED'}]-(friend:user_profile) "
                            + "RETURN count(friend)")
    Page<UserProfile> findFriends(@Param("userId") String userId, Pageable pageable);

    // Đếm số lượng bạn bè
    @Query("MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'ACCEPTED'}]-(friend:user_profile) "
            + "RETURN count(friend)")
    int countFriends(@Param("userId") String userId);

    // Lấy toàn bộ userId của bạn bè (dành cho Feed / Internal API)
    @Query("MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'ACCEPTED'}]-(friend:user_profile) "
            + "RETURN friend.userId")
    List<String> findAllFriendUserIds(@Param("userId") String userId);

    // Danh sách lời mời nhận được (Incoming Requests)
    @Query(
            value =
                    "MATCH (sender:user_profile)-[r:FRIEND_WITH {status: 'PENDING'}]->(me:user_profile {userId: $userId}) "
                            + "RETURN r.requestId AS requestId, "
                            + "r.status AS status, "
                            + "r.createdDate AS createdDate, "
                            + "sender AS sender, "
                            + "me AS recipient "
                            + "SKIP $skip LIMIT $limit",
            countQuery =
                    "MATCH (sender:user_profile)-[r:FRIEND_WITH {status: 'PENDING'}]->(me:user_profile {userId: $userId}) "
                            + "RETURN count(r)")
    Page<FriendRequestProjection> findIncomingRequests(@Param("userId") String userId, Pageable pageable);

    // Danh sách lời mời đã gửi (Outgoing Requests)
    @Query(
            value =
                    "MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'PENDING'}]->(recipient:user_profile) "
                            + "RETURN r.requestId AS requestId, "
                            + "       r.status AS status, "
                            + "       r.createdDate AS createdDate, "
                            + "       me AS sender, "
                            + "       recipient AS recipient "
                            + "SKIP $skip LIMIT $limit",
            countQuery =
                    "MATCH (me:user_profile {userId: $userId})-[r:FRIEND_WITH {status: 'PENDING'}]->(recipient:user_profile) "
                            + "RETURN count(r)")
    Page<FriendRequestProjection> findOutgoingRequests(@Param("userId") String userId, Pageable pageable);
}
