package com.dl1803.profile.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dl1803.profile.entity.UserProfile;

@Repository
public interface BlockRepository extends Neo4jRepository<UserProfile, String> {
    // Chặn user: Tạo quan hệ [:BLOCKED]
    @Query("MATCH (blocker:user_profile {userId: $blockerId}), (blocked: user_profile {userId: $blockedId})"
            + "MERGE (blocker)-[r:BLOCKED]->(blocked) "
            + "ON CREATE SET r.createdDate = $createdDate")
    void blockUser(
            @Param("blockerId") String blockerId,
            @Param("blockedId") String blockedId,
            @Param("createdDate") Instant createdDate);

    // Tự động xóa quan hệ bạn bè (nếu có) khi chặn
    @Query("MATCH (u1:user_profile {userId: $userId1}), (u2:user_profile {userId: $userId2}) "
            + "OPTIONAL MATCH (u1)-[r:FRIEND_WITH]-(u2) "
            + "DELETE r") // OPTIONAL MATCH tương tự với left join , tránh gây null lỗi khi 2 user kh có quan hệ
    // left join giữ dl trái - dl phải can null, optional match giữ kq bước match trước(u1,u2) mqh ở bước sau k có ->
    // null
    void deleteFriendshipIfExists(@Param("userId1") String userId1, @Param("userId2") String userId2);

    // Bỏ chặn: Xóa quan hệ [:BLOCKED]
    @Query("MATCH (blocker:user_profile {userId: $blockerId})-[r:BLOCKED]->(blocked:user_profile {userId: $blockedId}) "
            + "DELETE r")
    boolean unblockUser(@Param("blockerId") String blockerId, @Param("blockedId") String blockedId);

    // Kiểm tra user A có đang chặn user B hay không (Có hướng) - ng chủ động mới có quyền thực thi
    @Query("MATCH (blocker:user_profile {userId: $blockerId})-[r:BLOCKED]->(blocked:user_profile {userId: $blockedId}) "
            + "RETURN count(r) > 0")
    boolean hasBlocked(@Param("blockerId") String blockerId, @Param("blockedId") String blockedId);

    // Kiểm tra có quan hệ chặn nào giữa 2 người không (Vô hướng 2 chiều) dùng cho kiểm tra gửi kb, tin nhắn , xem
    // profile
    @Query("MATCH (u1:user_profile {userId: $userId1})-[r:BLOCKED]-(u2:user_profile {userId: $userId2}) "
            + "RETURN count(r) > 0")
    boolean existsBlockRelationship(@Param("userId1") String userId1, @Param("userId2") String userId2);

    // Lấy danh sách những người mình đã chặn (Có phân trang)
    @Query(
            value = "MATCH (me:user_profile {userId: $userId})-[:BLOCKED]->(blocked:user_profile) "
                    + "RETURN blocked SKIP $skip LIMIT $limit",
            countQuery = "MATCH (me:user_profile {userId: $userId})-[:BLOCKED]->(blocked:user_profile) "
                    + "RETURN count(blocked)")
    Page<UserProfile> findBlockedUsers(@Param("userId") String userId, Pageable pageable);
}
