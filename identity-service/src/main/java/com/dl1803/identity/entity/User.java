package com.dl1803.identity.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;

import com.dl1803.identity.enums.AuthProvider;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    String password;

    @Column(name = "email", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String email;

    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    boolean emailVerified;

    @Column(name = "verification_otp", columnDefinition = "VARCHAR(4)")
    String verificationOtp;

    @Column(name = "otp_expiry_time")
    LocalDateTime otpExpiryTime;

    @Column(name = "last_otp_sent_time")
    LocalDateTime lastOtpSentTime;

    @Column(name = "otp_attempt_count", columnDefinition = "INT DEFAULT 0")
    int otpAttemptCount;

    @Column(name = "password_change_at")
    LocalDateTime passwordChangeAt;

    @Column(name = "google_id", unique = true)
    String googleId;

    @Enumerated(EnumType.STRING) // yêu cầu DE lưu chữ "LOCAL" hoặc "GOOGLE" thay vì số 0,1 (vì dùng enum)
    @Column(name = "auth_provider", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'LOCAL'")
    // columnDefinition báo cho MYSQL gán default value ở múc DB : khi ins vào DB mà k truyền field này -> tự điền LOCAL
    // Khi thêm column mới vào bảng đang có dữ liệu cũ, DEFAULT 'LOCAL' có thể giúp DB điền giá trị mặc định cho các row
    // cũ (muục đích chính)

    AuthProvider authProvider = AuthProvider.LOCAL; // chỉ ở mức Java , Nó không sửa database cũ

    @ManyToMany
    Set<Role> roles;
}
