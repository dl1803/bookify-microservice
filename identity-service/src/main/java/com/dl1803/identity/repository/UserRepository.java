package com.dl1803.identity.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dl1803.identity.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Lock(
            LockModeType
                    .PESSIMISTIC_WRITE) // khóa bi quan (khả năng cao có trans khác xử lí trn data này -> khóa trước) (
    // "Query" FOR UPDATE)
    // Yêu cầu DB : khi tìm thấy target -> khóa row User này (kh cho các transaction khác đồng thời SỬA nó cho đến khi
    // trans hiện tại kết thúc)
    // cần tốn tại trong 1 transaction để có phạm vi tồn tại rõ ràng
    // có thể aaay tốn hiệu năng lúc đụng độ
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findAndLockByEmail(@Param("email") String email);

    Optional<User> findByGoogleId(String googleId);
}
