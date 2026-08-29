# 📖 Bookify — API Specification

> **Version:** 1.0.0  
> **Base URL:** `http://localhost:8888/api/v1`  
> **Updated:** 2026-08-16  
> **Tài liệu thiết kế API chính thức cho toàn bộ hệ thống Bookify**

---

## Mục lục

- [1. Tổng quan](#1-tổng-quan)
  - [1.1. Kiến trúc & Routing](#11-kiến-trúc--routing)
  - [1.2. Response Format](#12-response-format)
  - [1.3. Xác thực & Phân quyền](#13-xác-thực--phân-quyền)
  - [1.4. Bảng mã lỗi](#14-bảng-mã-lỗi)
- [2. Identity Service — Xác thực & Quản lý tài khoản](#2-identity-service)
  - [2.1. Authentication](#21-authentication)
  - [2.2. User Management](#22-user-management)
  - [2.3. Role Management](#23-role-management)
  - [2.4. Permission Management](#24-permission-management)
- [3. Profile Service — Hồ sơ & Mạng xã hội](#3-profile-service)
  - [3.1. User Profile](#31-user-profile)
  - [3.2. Friend](#32-friend)
  - [3.3. Block & Report](#33-block--report)
  - [3.4. Internal APIs](#34-internal-apis)
- [4. Post Service — Bài viết & Tương tác](#4-post-service)
  - [4.1. Post CRUD](#41-post-crud)
  - [4.2. Post Feed](#42-post-feed)
  - [4.3. Comments](#43-comments)
  - [4.4. Likes](#44-likes)
  - [4.5. Report Post](#45-report-post)
- [5. Chat Service — Nhắn tin](#5-chat-service)
  - [5.1. Conversations](#51-conversations)
  - [5.2. Messages](#52-messages)
  - [5.3. Message Reactions](#53-message-reactions)
  - [5.4. WebSocket Realtime](#54-websocket-realtime)
- [6. File Service — Quản lý tập tin](#6-file-service)
- [7. Notification Service — Thông báo](#7-notification-service)
  - [7.1. Email](#71-email)
  - [7.2. In-App Notifications](#72-in-app-notifications)
- [8. Book Service — Catalog & Bookshelf](#8-book-service)
  - [8.1. Book Catalog](#81-book-catalog)
  - [8.2. Book Rating & Review](#82-book-rating--review)
  - [8.3. Bookshelf (Tủ sách cá nhân)](#83-bookshelf-tủ-sách-cá-nhân)
- [9. Search Service](#9-search-service)
- [10. Recommendation Service](#10-recommendation-service)
- [Phụ lục A — Bảng tổng hợp toàn bộ API](#phụ-lục-a--bảng-tổng-hợp-toàn-bộ-api)
- [Phụ lục B — Sơ đồ luồng xác thực](#phụ-lục-b--sơ-đồ-luồng-xác-thực)

---

## 1. Tổng quan

### 1.1. Kiến trúc & Routing

Bookify sử dụng kiến trúc **Microservices** với **API Gateway** (Spring Cloud Gateway, port `8888`) làm điểm vào duy nhất. Gateway thực hiện `StripPrefix=2` — client gọi `/api/v1/identity/users` thì service nhận `/users`.

| Service | Gateway Prefix | Internal Port | Database |
|---|---|---|---|
| Identity Service | `/api/v1/identity/**` | 8080 | MySQL |
| Profile Service | `/api/v1/profile/**` | 8081 | Neo4j |
| Notification Service | `/api/v1/notification/**` | 8082 | MongoDB |
| Post Service | `/api/v1/post/**` | 8083 | MongoDB |
| File Service | `/api/v1/file/**` | 8084 | MongoDB + Storage |
| Chat Service | `/api/v1/chat/**` | 8085 | MongoDB |
| Book Service | `/api/v1/book/**` | 8086 | MongoDB |
| Search Service | `/api/v1/search/**` | 8087 | Elasticsearch |
| Recommendation Service | `/api/v1/recommend/**` | 8088 | Milvus |

### 1.2. Response Format

Tất cả API response đều dùng cấu trúc `ApiResponse<T>`:

```json
{
  "code": 1000,
  "error": "OPTIONAL_ERROR_KEY",
  "message": "Optional message",
  "result": { }
}
```

| Field | Type | Mô tả |
|---|---|---|
| `code` | `int` | `1000` = thành công. Khác = lỗi (xem [1.4](#14-bảng-mã-lỗi)). |
| `error` | `string?` | Machine-readable key của lỗi (VD: `INVALID_CREDENTIALS`). |
| `message` | `string?` | Thông báo bổ sung (nullable). |
| `result` | `T?` | Dữ liệu trả về (nullable). |

**Paginated Response** — dùng cho mọi danh sách có phân trang:

```json
{
  "code": 1000,
  "result": {
    "currentPage": 1,
    "totalPages": 10,
    "pageSize": 20,
    "totalElements": 195,
    "data": [ ]
  }
}
```

### 1.3. Xác thực & Phân quyền

- **Cơ chế:** JWT Bearer Token (Nimbus JOSE)
- **Header:** `Authorization: Bearer <token>`
- **Introspect:** API Gateway gọi nội bộ tới Identity Service kiểm tra token trước khi forward.
- Endpoint đánh dấu 🔒 yêu cầu token. Không có 🔒 = public.
- Một số endpoint yêu cầu role cụ thể (ADMIN, MODERATOR) — ghi rõ tại từng endpoint.

### 1.4. Bảng mã lỗi

| Code | Name | HTTP | Description |
|---|---|---|---|
| `1000` | SUCCESS | 200 | Success |
| `1001` | INVALID_KEY | 400 | Invalid message key |
| `1002` | USER_EXISTED | 400 | Username or email is already taken |
| `1003` | USERNAME_INVALID | 400 | Username must be at least 4 characters |
| `1004` | PASSWORD_INVALID | 400 | Password must be at least 6 characters |
| `1005` | USER_NOT_EXISTED | 404 | User not found |
| `1006` | UNAUTHENTICATED | 401 | Authentication is required |
| `1007` | UNAUTHORIZED | 403 | You do not have permission to access this resource |
| `1008` | INVALID_DOB | 400 | Your age must be at least 18 |
| `1009` | INVALID_EMAIL | 400 | Please enter a valid email address |
| `1010` | FILE_NOT_FOUND | 404 | File not found |
| `1011` | CONVERSATION_NOT_FOUND | 404 | Conversation not found |
| `1012` | POST_NOT_FOUND | 404 | Post not found |
| `1013` | COMMENT_NOT_FOUND | 404 | Comment not found |
| `1014` | BOOK_NOT_FOUND | 404 | Book not found |
| `1015` | ALREADY_LIKED | 400 | Already liked |
| `1016` | NOT_LIKED | 400 | Not liked |
| `1017` | SELF_ACTION_NOT_ALLOWED | 400 | Cannot perform action on yourself |
| `1018` | FRIEND_REQUEST_EXISTED | 400 | Friend request already exists |
| `1019` | FRIEND_REQUEST_NOT_FOUND | 404 | Friend request not found |
| `1020` | ALREADY_FRIENDS | 400 | Already friends |
| `1021` | CANNOT_SEND_EMAIL | 400 | Cannot send email |
| `1024` | ISBN_EXISTED | 400 | ISBN already exists |
| `1025` | ALREADY_ON_SHELF | 400 | Book already on shelf |
| `1026` | NOTIFICATION_NOT_FOUND | 404 | Notification not found |
| `1027` | USER_BLOCKED | 403 | User has been blocked |
| `1028` | INVALID_FILE_TYPE | 400 | Unsupported file type |
| `1029` | FILE_TOO_LARGE | 400 | File size exceeds limit |
| `1031` | MESSAGE_NOT_FOUND | 404 | Message not found |
| `1032` | MESSAGE_EDIT_EXPIRED | 400 | Message edit time expired |
| `1033` | INVALID_OTP | 400 | Invalid OTP code |
| `1034` | OTP_EXPIRED | 400 | OTP has expired. Please request a new one |
| `1035` | USER_NOT_VERIFIED | 403 | Email is not verified. Please check your inbox |
| `1036` | USER_ALREADY_VERIFIED | 400 | Email is already verified |
| `1037` | OTP_SEND_COOLDOWN | 429 | Please wait before requesting a new OTP |
| `1038` | TOO_MANY_OTP_ATTEMPTS | 400 | Too many failed attempts. Please request a new OTP |
| `1039` | INVALID_GOOGLE_TOKEN | 400 | Google authentication failed. Please try again |
| `1040` | GOOGLE_AUTH_FAILED | 500 | Unable to connect to Google. Please try again later |
| `1041` | ACCOUNT_LINKED_TO_DIFFERENT_PROVIDER | 409 | This email is already registered with a different login method |
| `1042` | INVALID_CREDENTIALS | 401 | Username or password is incorrect |
| `1043` | TOKEN_EXPIRED | 401 | Your session has expired. Please login again |
| `1044` | TOKEN_REVOKED | 401 | Your session has been revoked. Please login again |
| `1045` | INVALID_REFRESH_TOKEN | 401 | Invalid refresh token |
| `1046` | WRONG_CURRENT_PASSWORD | 400 | Current password is incorrect |
| `1047` | PASSWORD_CHANGED_RELOGIN | 401 | Password has been changed. Please login again |
| `1048` | EMAIL_IS_REQUIRED | 400 | Email address is required |

| `9999` | UNCATEGORIZED_EXCEPTION | 500 | An unexpected error occurred. Please try again later |

---

## 2. Identity Service

> **Prefix:** `/api/v1/identity` → `http://localhost:8080`  
> **Database:** MySQL | **Package:** `com.dl1803.identity`

### 2.1. Authentication

---

#### POST `/auth/token` — Đăng nhập

**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiryTime": "2026-08-17T14:30:00.000+00:00"
  }
}
```

**Errors:** `1042` INVALID_CREDENTIALS, `1035` USER_NOT_VERIFIED, `1041` ACCOUNT_LINKED_TO_DIFFERENT_PROVIDER

---

#### POST `/auth/introspect` — Kiểm tra token

Dùng nội bộ bởi API Gateway.

**Request:**
```json
{
  "token": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "valid": true
  }
}
```

---

#### POST `/auth/refresh` — Làm mới token

**Request:**
```json
{
  "token": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "token": "eyJ...(new)",
    "expiryTime": "2026-08-17T15:00:00.000+00:00"
  }
}
```

---

#### POST `/auth/logout` — Đăng xuất

Vô hiệu hóa token hiện tại (lưu vào `invalidated_token`).

**Request:**
```json
{
  "token": "string"
}
```

**Response:**
```json
{ "code": 1000 }
```

---

#### POST `/auth/verify-email` — Xác thực email

Xác nhận email qua mã OTP 4 số gửi trong email.

**Request:**
```json
{
  "email": "user@example.com",
  "otp": "1234"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Email verified successfully"
}
```

---

#### POST `/auth/resend-verification` — Gửi lại email xác thực

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "Verification email sent"
}
```

---

#### POST `/auth/forgot-password` — Quên mật khẩu

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "If the email exists in our system, an OTP has been sent."
}
```



#### POST `/auth/reset-password` — Đặt lại mật khẩu

**Request:**
```json
{
  "email": "user@example.com",
  "otp": "1234",
  "newPassword": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Password has been reset"
}
```

**Errors:** `1033` INVALID_OTP, `1034` OTP_EXPIRED, `1004` PASSWORD_INVALID

---

#### POST `/auth/change-password` — Đổi mật khẩu

🔒 Bearer Token

**Request:**
```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Password changed successfully",
  "result": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiryTime": "2026-08-19T23:59:59.000+00:00"
  }
}
```

**Errors:** `1006` UNAUTHENTICATED (mật khẩu hiện tại sai), `1004` PASSWORD_INVALID

---

### 2.2. User Management

---

#### POST `/users/registration` — Đăng ký tài khoản

Tạo user mới. Tự động gọi nội bộ Profile Service tạo profile và gửi email xác thực qua Kafka.

**Request:**
```json
{
  "username": "bookworm42",
  "password": "securePass123",
  "firstName": "Nguyen",
  "lastName": "Van A",
  "dob": "2000-05-15",
  "email": "vana@example.com",
  "city": "Ho Chi Minh"
}
```

**Validation:**

| Trường | Ràng buộc |
|---|---|
| `username` | ≥ 4 ký tự, unique |
| `password` | ≥ 6 ký tự |
| `email` | Email hợp lệ, unique, bắt buộc |
| `dob` | User ≥ 18 tuổi |
| `firstName`, `lastName`, `city` | Tùy chọn |

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "bookworm42",
    "email": "vana@example.com",
    "emailVerified": false,
    "roles": []
  }
}
```

**Errors:** `1002`, `1003`, `1004`, `1008`, `1009`

---

#### GET `/users` — Danh sách user

🔒 Bearer Token — **ADMIN**

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "id": "uuid",
      "username": "bookworm42",
      "email": "vana@example.com",
      "emailVerified": true,
      "roles": [
        {
          "name": "USER",
          "description": "Default user role",
          "permissions": [
            { "name": "READ_POST", "description": "Read posts" }
          ]
        }
      ]
    }
  ]
}
```

---

#### GET `/users/{userId}` — Thông tin user theo ID

🔒 Bearer Token

| Param | Type | Mô tả |
|---|---|---|
| `userId` | `string (UUID)` | ID user |

**Response:** `UserResponse`

**Errors:** `1005` USER_NOT_EXISTED

---

#### GET `/users/myInfo` — Thông tin bản thân

🔒 Bearer Token — Lấy từ SecurityContext.

**Response:** `UserResponse`

---

#### PUT `/users/{userId}` — Cập nhật user

🔒 Bearer Token

**Request:**
```json
{
  "password": "newSecurePass",
  "firstName": "Nguyen Updated",
  "lastName": "Van B",
  "dob": "2000-06-20",
  "roles": ["USER", "MODERATOR"]
}
```

> Chỉ ADMIN mới thay đổi được `roles`.

**Response:** `UserResponse`

---

#### DELETE `/users/{userId}` — Xóa user

🔒 Bearer Token — **ADMIN**

**Response:**
```json
{
  "code": 1000,
  "result": "User has been deleted!"
}
```

---

### 2.3. Role Management

> 🔒 Tất cả endpoint yêu cầu Bearer Token — **ADMIN**

---

#### POST `/roles` — Tạo role

**Request:**
```json
{
  "name": "MODERATOR",
  "description": "Can moderate posts and comments",
  "permissions": ["READ_POST", "DELETE_POST", "DELETE_COMMENT"]
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "name": "MODERATOR",
    "description": "Can moderate posts and comments",
    "permissions": [
      { "name": "READ_POST", "description": "Read posts" }
    ]
  }
}
```

---

#### GET `/roles` — Danh sách role

**Response:** `ApiResponse<List<RoleResponse>>`

---

#### DELETE `/roles/{role}` — Xóa role

| Param | Mô tả |
|---|---|
| `role` | Tên role (VD: `MODERATOR`) |

**Response:** `{ "code": 1000 }`

---

### 2.4. Permission Management

> 🔒 Tất cả endpoint yêu cầu Bearer Token — **ADMIN**

---

#### POST `/permissions` — Tạo permission

**Request:**
```json
{
  "name": "DELETE_POST",
  "description": "Allows deleting any post"
}
```

**Response:** `PermissionResponse`

---

#### GET `/permissions` — Danh sách permission

**Response:** `ApiResponse<List<PermissionResponse>>`

---

#### DELETE `/permissions/{permission}` — Xóa permission

**Response:** `{ "code": 1000 }`

---

## 3. Profile Service

> **Prefix:** `/api/v1/profile` → `http://localhost:8081`  
> **Database:** Neo4j (Graph DB) | **Package:** `com.dl1803.profile`

### 3.1. User Profile

---

#### GET `/users/{profileId}` — Xem profile theo ID

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "neo4j-uuid",
    "userId": "550e8400-...",
    "username": "bookworm42",
    "avatar": "http://localhost:8888/api/v1/file/media/download/abc.jpg",
    "email": "vana@example.com",
    "firstName": "Nguyen",
    "lastName": "Van A",
    "dob": "2000-05-15",
    "city": "Ho Chi Minh",
    "bio": "I love reading books 📚",
    "friendsCount": 42,
    "friendStatus": "NONE"
  }
}
```

| Trường | Mô tả |
|---|---|
| `friendsCount` | Tổng số bạn bè |
| `friendStatus` | Quan hệ bạn bè giữa viewer và owner (xem enum bên dưới) |

**`friendStatus` enum:**

| Giá trị | Mô tả |
|---|---|
| `NONE` | Không có quan hệ |
| `PENDING_SENT` | Viewer đã gửi lời mời, đang chờ owner chấp nhận |
| `PENDING_RECEIVED` | Owner đã gửi lời mời, đang chờ viewer chấp nhận |
| `ACCEPTED` | Đã là bạn bè |
| `BLOCKED` | Viewer đã chặn owner |

---

#### GET `/users` — Danh sách tất cả profile

🔒 Bearer Token

**Response:** `ApiResponse<List<UserProfileResponse>>`

---

#### GET `/users/my-profile` — Xem profile bản thân

🔒 Bearer Token

**Response:** `UserProfileResponse`

---

#### PUT `/users/my-profile` — Cập nhật profile

🔒 Bearer Token

**Request:**
```json
{
  "email": "newemail@example.com",
  "firstName": "Nguyen Updated",
  "lastName": "Van B",
  "dob": "2000-06-20",
  "city": "Ha Noi",
  "bio": "New bio here 🌟"
}
```

> Tất cả trường đều tùy chọn — chỉ gửi trường muốn thay đổi.

**Response:** `UserProfileResponse` đã cập nhật

---

#### PUT `/users/avatar` — Cập nhật ảnh đại diện

🔒 Bearer Token  
**Content-Type:** `multipart/form-data`

| Param | Type | Mô tả |
|---|---|---|
| `file` | `MultipartFile` | Ảnh JPG/PNG/WEBP |

**Response:** `UserProfileResponse` với `avatar` mới

---

#### POST `/users/search` — Tìm kiếm user

🔒 Bearer Token

**Request:**
```json
{
  "keyword": "nguyen"
}
```

> Tìm theo `username`, `firstName`, `lastName`.

**Response:** `ApiResponse<List<UserProfileResponse>>`

---

### 3.2. Friend

> Neo4j Relationship: `(:user_profile)-[:FRIEND_WITH { status }]->(:user_profile)`

**`FriendRequestStatus` enum — Vòng đời kết bạn:**

| Giá trị | Mô tả | Chuyển tiếp được sang |
|---|---|---|
| `PENDING` | Đã gửi lời mời, chờ phản hồi | `ACCEPTED`, `REJECTED`, `CANCELLED` |
| `ACCEPTED` | Đã chấp nhận — là bạn bè | `UNFRIENDED` |
| `REJECTED` | Đã từ chối lời mời | `PENDING` (gửi lại) |
| `CANCELLED` | Người gửi đã hủy lời mời | `PENDING` (gửi lại) |
| `UNFRIENDED` | Đã hủy kết bạn | `PENDING` (gửi lại) |

---

#### POST `/users/{profileId}/friend-request` — Gửi lời mời kết bạn

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "Friend request sent"
}
```

**Errors:** `1017` SELF_ACTION_NOT_ALLOWED, `1018` FRIEND_REQUEST_EXISTED, `1020` ALREADY_FRIENDS, `1027` USER_BLOCKED

---

#### GET `/users/friend-requests/incoming` — Lời mời kết bạn nhận được

🔒 Bearer Token

**Query:** `page`, `size`

**Response:**
```json
{
  "code": 1000,
  "result": {
    "currentPage": 1, "totalPages": 2, "pageSize": 20, "totalElements": 25,
    "data": [
      {
        "id": "request-uuid",
        "fromProfile": { /* UserProfileResponse */ },
        "status": "PENDING",
        "createdDate": "2026-08-16T12:00:00Z"
      }
    ]
  }
}
```

---

#### GET `/users/friend-requests/outgoing` — Lời mời đã gửi

🔒 Bearer Token

**Query:** `page`, `size`

**Response:** Paginated — cấu trúc giống incoming, nhưng thay `fromProfile` bằng `toProfile`.

---

#### PUT `/users/friend-requests/{requestId}/accept` — Chấp nhận

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "Friend request accepted"
}
```

**Errors:** `1019` FRIEND_REQUEST_NOT_FOUND

---

#### PUT `/users/friend-requests/{requestId}/reject` — Từ chối

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "Friend request rejected"
}
```

---

#### DELETE `/users/friend-requests/{requestId}` — Hủy lời mời đã gửi

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

#### GET `/users/friends` — Danh sách bạn bè

🔒 Bearer Token

**Query:** `page`, `size`

**Response:** Paginated `UserProfileResponse`

---

#### DELETE `/users/friends/{friendProfileId}` — Hủy kết bạn

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "Unfriended successfully"
}
```

---

### 3.3. Block & Report

---

#### POST `/users/{profileId}/block` — Chặn user

🔒 Bearer Token

Chặn user sẽ tự động hủy kết bạn nếu có.

**Response:**
```json
{
  "code": 1000,
  "message": "User blocked"
}
```

---

#### DELETE `/users/{profileId}/block` — Bỏ chặn user

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "message": "User unblocked"
}
```

---

#### GET `/users/blocked` — Danh sách đã chặn

🔒 Bearer Token

**Response:** `ApiResponse<List<UserProfileResponse>>`

---

#### POST `/users/{profileId}/report` — Báo cáo user

🔒 Bearer Token

**Request:**
```json
{
  "reason": "SPAM",
  "description": "Sending spam messages"
}
```

> `reason` enum: `SPAM`, `HARASSMENT`, `FAKE_ACCOUNT`, `INAPPROPRIATE_CONTENT`, `OTHER`

**Response:**
```json
{
  "code": 1000,
  "message": "Report submitted"
}
```

---

### 3.4. Internal APIs

> ⚠️ Chỉ giao tiếp nội bộ giữa services — không expose qua Gateway.

---

#### POST `/internal/users` — Tạo profile (nội bộ)

Gọi tự động bởi Identity Service khi user đăng ký.

**Request:**
```json
{
  "userId": "uuid",
  "username": "bookworm42",
  "email": "vana@example.com",
  "firstName": "Nguyen",
  "lastName": "Van A",
  "dob": "2000-05-15",
  "city": "Ho Chi Minh"
}
```

**Response:** `UserProfileResponse`

---

#### GET `/internal/users/{userId}` — Lấy profile theo userId (nội bộ)

---

#### GET `/internal/users/bulk?userIds=id1,id2,...` — Lấy nhiều profile (nội bộ)

**Response:** `ApiResponse<List<UserProfileResponse>>`

---

## 4. Post Service

> **Prefix:** `/api/v1/post` → `http://localhost:8083`  
> **Database:** MongoDB | **Package:** `com.dl1803.post`

### 4.1. Post CRUD

---

#### POST `/posts` — Tạo bài viết

🔒 Bearer Token

**Request:**
```json
{
  "content": "Just finished reading 'Clean Code'. Amazing book! 📖",
  "images": ["http://...image1.jpg", "http://...image2.jpg"],
  "bookId": "book-mongo-id"
}
```

> `images` và `bookId` đều tùy chọn.

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "mongo-id",
    "content": "Just finished reading 'Clean Code'. Amazing book! 📖",
    "userId": "uuid",
    "username": "bookworm42",
    "userAvatar": "http://...",
    "bookId": "book-mongo-id",
    "images": ["http://...image1.jpg"],
    "likesCount": 0,
    "commentsCount": 0,
    "isLiked": false,
    "createdDate": "2026-08-16T14:30:00Z",
    "modifiedDate": "2026-08-16T14:30:00Z"
  }
}
```

---

#### GET `/posts/{postId}` — Chi tiết bài viết

🔒 Bearer Token

**Response:** `PostResponse` đầy đủ (như trên)

**Errors:** `1012` POST_NOT_FOUND

---

#### PUT `/posts/{postId}` — Chỉnh sửa bài viết

🔒 Bearer Token — Chỉ chủ bài viết

**Request:**
```json
{
  "content": "Updated content ✏️",
  "images": ["http://...new-image.jpg"],
  "bookId": "book-mongo-id"
}
```

**Response:** `PostResponse` đã cập nhật

---

#### DELETE `/posts/{postId}` — Xóa bài viết

🔒 Bearer Token — Chủ bài viết hoặc ADMIN/MODERATOR

**Response:**
```json
{
  "code": 1000,
  "message": "Post deleted"
}
```

---

#### GET `/my-posts` — Bài viết của bản thân

🔒 Bearer Token

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `page` | `int` | `1` | Trang |
| `size` | `int` | `10` | Số bài/trang |

**Response:** Paginated `PostResponse`

---

#### GET `/users/{userId}/posts` — Bài viết của user cụ thể

🔒 Bearer Token

**Query:** `page`, `size`

**Response:** Paginated `PostResponse`

---

### 4.2. Post Feed

---

#### GET `/feed` — Newsfeed

🔒 Bearer Token

Lấy bài viết từ bạn bè của user, sắp xếp theo thời gian mới nhất.

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Bài/trang |

**Response:** Paginated `PostResponse`

---

### 4.3. Comments

---

#### POST `/posts/{postId}/comments` — Viết comment

🔒 Bearer Token

**Request:**
```json
{
  "content": "Great review! I agree.",
  "parentCommentId": null
}
```

> `parentCommentId = null` → comment gốc. Có giá trị → reply comment.

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "comment-mongo-id",
    "postId": "post-mongo-id",
    "userId": "uuid",
    "username": "reader99",
    "userAvatar": "http://...",
    "parentCommentId": null,
    "content": "Great review! I agree.",
    "createdDate": "2026-08-16T15:00:00Z"
  }
}
```

---

#### GET `/posts/{postId}/comments` — Danh sách comment

🔒 Bearer Token

**Query:** `page`, `size`

**Response:** Paginated `CommentResponse`

---

#### PUT `/comments/{commentId}` — Sửa comment

🔒 Bearer Token — Chỉ chủ comment

**Request:**
```json
{
  "content": "Updated comment"
}
```

**Response:** `CommentResponse`

---

#### DELETE `/comments/{commentId}` — Xóa comment

🔒 Bearer Token — Chủ comment hoặc ADMIN

**Response:** `{ "code": 1000 }`

**Errors:** `1013` COMMENT_NOT_FOUND

---

### 4.4. Likes

---

#### POST `/posts/{postId}/like` — Thích bài viết

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "postId": "post-mongo-id",
    "likesCount": 43
  }
}
```

**Errors:** `1015` ALREADY_LIKED

---

#### DELETE `/posts/{postId}/like` — Bỏ thích

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "postId": "post-mongo-id",
    "likesCount": 42
  }
}
```

**Errors:** `1016` NOT_LIKED

---

#### GET `/posts/{postId}/likes` — Danh sách người thích

🔒 Bearer Token

**Query:** `page`, `size`

**Response:** Paginated `UserProfileResponse`

---

### 4.5. Report Post

---

#### POST `/posts/{postId}/report` — Báo cáo bài viết

🔒 Bearer Token

**Request:**
```json
{
  "reason": "INAPPROPRIATE_CONTENT",
  "description": "Contains offensive language"
}
```

> `reason` enum: `SPAM`, `HARASSMENT`, `INAPPROPRIATE_CONTENT`, `MISINFORMATION`, `COPYRIGHT`, `OTHER`

**Response:**
```json
{
  "code": 1000,
  "message": "Report submitted"
}
```

---

## 5. Chat Service

> **Prefix:** `/api/v1/chat` → `http://localhost:8085`  
> **Database:** MongoDB | **Package:** `com.dl1803.chat`

### 5.1. Conversations

---

#### POST `/conversations/create` — Tạo cuộc hội thoại

🔒 Bearer Token

**Request:**
```json
{
  "type": "direct",
  "participantIds": ["user-uuid-2"]
}
```

| Trường | Type | Mô tả |
|---|---|---|
| `type` | `string` | `"direct"` (1-1) hoặc `"group"` |
| `participantIds` | `string[]` | userId người tham gia (≥ 1, không bao gồm bản thân) |

> Với `direct`, nếu conversation đã tồn tại (dựa trên `participantsHash`) → trả về conversation cũ.

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "conv-mongo-id",
    "type": "direct",
    "participantsHash": "hash-abc123",
    "conversationAvatar": "http://...",
    "conversationName": "Van B",
    "participants": [
      {
        "userId": "uuid-1",
        "username": "bookworm42",
        "nickname": null,
        "firstName": "Nguyen",
        "lastName": "Van A",
        "avatar": "http://..."
      },
      {
        "userId": "uuid-2",
        "username": "reader99",
        "nickname": "Bestie",
        "firstName": "Tran",
        "lastName": "Van B",
        "avatar": "http://..."
      }
    ],
    "createdDate": "2026-08-16T10:00:00Z",
    "modifiedDate": "2026-08-16T14:30:00Z"
  }
}
```

---

#### GET `/conversations/my-conversations` — Danh sách hội thoại

🔒 Bearer Token

**Response:** `ApiResponse<List<ConversationResponse>>`

---

#### GET `/conversations/{conversationId}` — Chi tiết hội thoại

🔒 Bearer Token

**Response:** `ConversationResponse`

**Errors:** `1011` CONVERSATION_NOT_FOUND

---

#### PUT `/conversations/{conversationId}/nickname` — Đặt biệt danh

🔒 Bearer Token

**Request:**
```json
{
  "targetUserId": "uuid-of-target",
  "nickname": "Bestie 📖"
}
```

> `nickname: null` hoặc `""` → gỡ nickname.

**Response:** `ConversationResponse` đã cập nhật

---

#### PUT `/conversations/{conversationId}/group-name` — Đổi tên nhóm

🔒 Bearer Token — Chỉ group conversation

**Request:**
```json
{
  "groupName": "Book Club 📚"
}
```

**Response:** `ConversationResponse`

---

#### PUT `/conversations/{conversationId}/group-avatar` — Đổi ảnh nhóm

🔒 Bearer Token — Chỉ group conversation  
**Content-Type:** `multipart/form-data`

| Param | Type |
|---|---|
| `file` | `MultipartFile` |

**Response:** `ConversationResponse`

---

#### POST `/conversations/{conversationId}/participants` — Thêm thành viên nhóm

🔒 Bearer Token

**Request:**
```json
{
  "userIds": ["uuid-3", "uuid-4"]
}
```

**Response:** `ConversationResponse`

---

#### DELETE `/conversations/{conversationId}/participants/{userId}` — Xóa thành viên

🔒 Bearer Token — Chỉ người tạo nhóm

**Response:** `ConversationResponse`

---

#### DELETE `/conversations/{conversationId}/leave` — Rời nhóm

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

### 5.2. Messages

---

#### POST `/messages/create` — Gửi tin nhắn

🔒 Bearer Token

**Request:**
```json
{
  "conversationId": "conv-mongo-id",
  "message": "Hey! Have you read this book?"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "msg-mongo-id",
    "conversationId": "conv-mongo-id",
    "me": true,
    "message": "Hey! Have you read this book?",
    "isEdited": false,
    "reactions": [],
    "sender": {
      "userId": "uuid-1",
      "username": "bookworm42",
      "nickname": null,
      "firstName": "Nguyen",
      "lastName": "Van A",
      "avatar": "http://..."
    },
    "createdDate": "2026-08-16T14:35:00Z",
    "editedDate": null
  }
}
```

| Trường | Mô tả |
|---|---|
| `me` | `true` nếu sender là user hiện tại — FE dùng để vẽ hướng tin nhắn |
| `isEdited` | `true` nếu tin nhắn đã bị chỉnh sửa |
| `reactions` | Danh sách reaction trên tin nhắn (xem [5.3](#53-message-reactions)) |
| `editedDate` | Thời điểm chỉnh sửa gần nhất, `null` nếu chưa sửa |

---

#### GET `/messages` — Lịch sử tin nhắn

🔒 Bearer Token

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `conversationId` | `string` | *required* | ID cuộc hội thoại |
| `before` | `string` | *latest* | Message ID cursor — lấy tin trước nó |
| `size` | `int` | `50` | Số tin nhắn |

**Response:** `ApiResponse<List<ChatMessageResponse>>`

---

#### PUT `/messages/{messageId}` — Chỉnh sửa tin nhắn

🔒 Bearer Token — Chỉ sender, trong vòng 15 phút sau khi gửi

**Request:**
```json
{
  "message": "Updated message content"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "msg-mongo-id",
    "conversationId": "conv-mongo-id",
    "me": true,
    "message": "Updated message content",
    "isEdited": true,
    "reactions": [ ],
    "sender": { /* ParticipantInfo */ },
    "createdDate": "2026-08-16T14:35:00Z",
    "editedDate": "2026-08-16T14:40:00Z"
  }
}
```

**Errors:** `1031` MESSAGE_NOT_FOUND, `1032` MESSAGE_EDIT_EXPIRED, `1007` UNAUTHORIZED (không phải sender)

---

#### DELETE `/messages/{messageId}` — Thu hồi tin nhắn

🔒 Bearer Token — Chỉ sender

**Response:** `{ "code": 1000 }`

**Errors:** `1031` MESSAGE_NOT_FOUND

---

### 5.3. Message Reactions

> Thả cảm xúc cho tin nhắn. Mỗi user chỉ được 1 reaction/message; gửi lại = thay đổi reaction.

**`ReactionType` enum:**

| Giá trị | Emoji |
|---|---|
| `LIKE` | 👍 |
| `LOVE` | ❤️ |
| `HAHA` | 😂 |
| `WOW` | 😮 |
| `SAD` | 😢 |
| `ANGRY` | 😡 |

---

#### POST `/messages/{messageId}/reactions` — Thả / Đổi reaction

🔒 Bearer Token

**Request:**
```json
{
  "type": "LOVE"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "messageId": "msg-mongo-id",
    "reactions": [
      {
        "userId": "uuid-1",
        "username": "bookworm42",
        "avatar": "http://...",
        "type": "LOVE",
        "createdDate": "2026-08-16T14:36:00Z"
      },
      {
        "userId": "uuid-2",
        "username": "reader99",
        "avatar": "http://...",
        "type": "HAHA",
        "createdDate": "2026-08-16T14:37:00Z"
      }
    ],
    "reactionSummary": [
      { "type": "LOVE", "count": 1 },
      { "type": "HAHA", "count": 1 }
    ]
  }
}
```

**Errors:** `1031` MESSAGE_NOT_FOUND

---

#### DELETE `/messages/{messageId}/reactions` — Gỡ reaction

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "messageId": "msg-mongo-id",
    "reactions": [ /* updated list */ ],
    "reactionSummary": [ /* updated summary */ ]
  }
}
```

---

#### GET `/messages/{messageId}/reactions` — Xem danh sách reactions

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "messageId": "msg-mongo-id",
    "reactions": [
      {
        "userId": "uuid-1",
        "username": "bookworm42",
        "avatar": "http://...",
        "type": "LOVE",
        "createdDate": "2026-08-16T14:36:00Z"
      }
    ],
    "reactionSummary": [
      { "type": "LOVE", "count": 1 }
    ]
  }
}
```

---

### 5.4. WebSocket Realtime

**Connection:** `ws://localhost:8888/ws/chat?token={JWT}`

**SUBSCRIBE — Nhận dữ liệu:**

| Destination | Mô tả |
|---|---|
| `/topic/conversations/{conversationId}` | Tin nhắn realtime của conversation |
| `/topic/conversations/{conversationId}/typing` | Trạng thái đang gõ |
| `/topic/conversations/{conversationId}/reactions` | Cập nhật reaction realtime |
| `/user/queue/notifications` | Thông báo cá nhân |
| `/user/queue/conversations` | Cập nhật danh sách conversation |

**SEND — Gửi dữ liệu:**

| Destination | Body | Mô tả |
|---|---|---|
| `/app/chat.send` | `{ "conversationId": "...", "message": "..." }` | Gửi tin nhắn |
| `/app/chat.typing` | `{ "conversationId": "..." }` | Báo đang gõ |
| `/app/chat.read` | `{ "conversationId": "...", "messageId": "..." }` | Đánh dấu đã đọc |
| `/app/chat.react` | `{ "messageId": "...", "type": "LOVE" }` | Thả reaction |

---

## 6. File Service

> **Prefix:** `/api/v1/file` → `http://localhost:8084`  
> **Database:** MongoDB + Local Storage (→ S3 tương lai)  
> **Package:** `com.dl1803.file`

---

#### POST `/media/upload` — Upload file

🔒 Bearer Token  
**Content-Type:** `multipart/form-data`

| Param | Type | Mô tả |
|---|---|---|
| `file` | `MultipartFile` | File cần upload |

**Response:**
```json
{
  "code": 1000,
  "result": {
    "originalFileName": "my-book-cover.jpg",
    "url": "http://localhost:8888/api/v1/file/media/download/1723812345_abc.jpg"
  }
}
```

**Errors:** `1028` INVALID_FILE_TYPE, `1029` FILE_TOO_LARGE

---

#### GET `/media/download/{fileName}` — Download / Xem file

🔓 **Public** — Không cần token

**Response:** Binary stream với `Content-Type` header phù hợp.

**Errors:** `1010` FILE_NOT_FOUND

---

#### GET `/media/my-files` — Danh sách file đã upload

🔒 Bearer Token

**Query:** `page`, `size`

**Response:**
```json
{
  "code": 1000,
  "result": {
    "currentPage": 1, "totalPages": 3, "pageSize": 20, "totalElements": 52,
    "data": [
      {
        "id": "file-mongo-id",
        "originalFileName": "book-cover.jpg",
        "url": "http://...",
        "contentType": "image/jpeg",
        "size": 245760,
        "createdDate": "2026-08-16T10:00:00Z"
      }
    ]
  }
}
```

---

#### DELETE `/media/{fileId}` — Xóa file

🔒 Bearer Token — Chỉ owner

**Response:** `{ "code": 1000 }`

**Errors:** `1010` FILE_NOT_FOUND

---

## 7. Notification Service

> **Prefix:** `/api/v1/notification` → `http://localhost:8082`  
> **Database:** MongoDB | **Message Broker:** Apache Kafka  
> **Package:** `com.dl1803.notification`

### 7.1. Email

---

#### POST `/email/send` — Gửi email

🔒 Bearer Token — Internal/ADMIN

**Request:**
```json
{
  "to": {
    "email": "user@example.com",
    "name": "Nguyen Van A"
  },
  "subject": "Welcome to Bookify!",
  "htmlContent": "<h1>Welcome!</h1><p>Thank you for joining.</p>"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "messageId": "brevo-msg-id-12345"
  }
}
```

**Errors:** `1023` CANNOT_SEND_EMAIL

---

#### Kafka Consumer: topic `notification-delivery`

> Không phải REST API. Identity Service publish event khi user đăng ký → Notification Service tự động gửi email.

**Message Schema:**
```json
{
  "channel": "EMAIL",
  "recipient": "user@example.com",
  "subject": "Welcome to Bookify",
  "body": "<html>...</html>"
}
```

---

### 7.2. In-App Notifications

---

#### GET `/notifications` — Danh sách thông báo

🔒 Bearer Token

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Số thông báo/trang |
| `unreadOnly` | `boolean` | `false` | Chỉ lấy chưa đọc |

**Response:**
```json
{
  "code": 1000,
  "result": {
    "currentPage": 1, "totalPages": 5, "pageSize": 20, "totalElements": 98,
    "data": [
      {
        "id": "notif-mongo-id",
        "senderId": "uuid-sender",
        "senderName": "reader99",
        "senderAvatar": "http://...",
        "type": "POST_LIKE",
        "targetId": "post-mongo-id",
        "content": "reader99 đã thích bài viết của bạn",
        "isRead": false,
        "createdDate": "2026-08-16T14:00:00Z"
      }
    ]
  }
}
```

**`NotificationType` enum:**

| Giá trị | Mô tả | targetId |
|---|---|---|
| `POST_LIKE` | Thích bài viết | Post ID |
| `NEW_COMMENT` | Comment bài viết | Post ID |
| `COMMENT_REPLY` | Reply comment | Comment ID |
| `FRIEND_REQUEST` | Lời mời kết bạn | Profile ID |
| `FRIEND_ACCEPTED` | Chấp nhận kết bạn | Profile ID |
| `MESSAGE` | Tin nhắn mới | Conversation ID |
| `MESSAGE_REACTION` | Reaction trên tin nhắn | Message ID |
| `BOOK_RECOMMENDATION` | Gợi ý sách | Book ID |
| `SYSTEM` | Thông báo hệ thống | null |

---

#### GET `/notifications/unread-count` — Đếm chưa đọc

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "count": 12
  }
}
```

---

#### PUT `/notifications/{notificationId}/read` — Đánh dấu đã đọc

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

**Errors:** `1026` NOTIFICATION_NOT_FOUND

---

#### PUT `/notifications/read-all` — Đánh dấu tất cả đã đọc

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

#### DELETE `/notifications/{notificationId}` — Xóa thông báo

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

## 8. Book Service

> **Prefix:** `/api/v1/book` → `http://localhost:8086`  
> **Database:** MongoDB | **Package:** `com.dl1803.book`

### 8.1. Book Catalog

---

#### POST `/books` — Thêm sách

🔒 Bearer Token — **ADMIN / MODERATOR**

**Request:**
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "coverUrl": "http://localhost:8888/api/v1/file/media/download/cover.jpg",
  "synopsis": "A handbook of agile software craftsmanship...",
  "genres": ["Technology", "Software Engineering"],
  "publishedYear": 2008
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "book-mongo-id",
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "coverUrl": "http://...",
    "synopsis": "...",
    "genres": ["Technology", "Software Engineering"],
    "publishedYear": 2008,
    "ratingAvg": 0.0,
    "ratingCount": 0,
    "createdDate": "2026-08-16T10:00:00Z"
  }
}
```

**Errors:** `1024` ISBN_EXISTED

---

#### GET `/books` — Danh sách sách

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Sách/trang |
| `genre` | `string` | *all* | Lọc thể loại |
| `sortBy` | `string` | `createdDate` | `createdDate` / `ratingAvg` / `title` |
| `order` | `string` | `desc` | `asc` / `desc` |

**Response:** Paginated `BookResponse`

---

#### GET `/books/{bookId}` — Chi tiết sách

**Response:** `BookResponse`

**Errors:** `1014` BOOK_NOT_FOUND

---

#### PUT `/books/{bookId}` — Cập nhật sách

🔒 Bearer Token — **ADMIN / MODERATOR**

**Request:** Giống POST `/books` (tất cả trường tùy chọn)

**Response:** `BookResponse`

---

#### DELETE `/books/{bookId}` — Xóa sách

🔒 Bearer Token — **ADMIN**

**Response:** `{ "code": 1000 }`

---

#### GET `/books/genres` — Danh sách thể loại

**Response:**
```json
{
  "code": 1000,
  "result": [
    "Fiction", "Non-Fiction", "Technology", "Science",
    "Fantasy", "Romance", "History", "Self-Help", "Biography"
  ]
}
```

---

#### GET `/books/search` — Tìm sách

**Query:**

| Param | Type | Mô tả |
|---|---|---|
| `q` | `string` | Từ khóa (title, author) |
| `genre` | `string` | Lọc thể loại |
| `page` | `int` | Trang |
| `size` | `int` | Kết quả/trang |

**Response:** Paginated `BookResponse`

---

### 8.2. Book Rating & Review

---

#### POST `/books/{bookId}/rating` — Đánh giá sách

🔒 Bearer Token

**Request:**
```json
{
  "rating": 4.5
}
```

> `rating`: 0.5 → 5.0, bước 0.5. Mỗi user chỉ đánh giá 1 lần/sách; gọi lại = cập nhật.

**Response:**
```json
{
  "code": 1000,
  "result": {
    "bookId": "book-mongo-id",
    "userRating": 4.5,
    "ratingAvg": 4.2,
    "ratingCount": 156
  }
}
```

---

#### GET `/books/{bookId}/rating/me` — Xem đánh giá của mình

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "rating": 4.5,
    "createdDate": "2026-08-10T10:00:00Z"
  }
}
```

> Trả `result: null` nếu chưa đánh giá.

---

#### DELETE `/books/{bookId}/rating` — Xóa đánh giá

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

#### GET `/books/{bookId}/reviews` — Các bài review về sách

🔒 Bearer Token

Lấy các post có `bookId` tương ứng.

**Query:** `page`, `size`, `sortBy` (`createdDate` / `likesCount`)

**Response:** Paginated `PostResponse`

---

### 8.3. Bookshelf (Tủ sách cá nhân)

> Mỗi user có tủ sách với 3 kệ: `WANT_TO_READ`, `READING`, `READ`.

---

#### POST `/bookshelf` — Thêm sách vào tủ

🔒 Bearer Token

**Request:**
```json
{
  "bookId": "book-mongo-id",
  "status": "WANT_TO_READ"
}
```

> `status` enum: `WANT_TO_READ`, `READING`, `READ`

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "shelf-entry-id",
    "bookId": "book-mongo-id",
    "book": { /* BookResponse */ },
    "status": "WANT_TO_READ",
    "progress": 0,
    "startDate": null,
    "finishDate": null,
    "addedDate": "2026-08-16T10:00:00Z"
  }
}
```

**Errors:** `1014` BOOK_NOT_FOUND, `1025` ALREADY_ON_SHELF

---

#### GET `/bookshelf` — Tủ sách của tôi

🔒 Bearer Token

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `status` | `string` | *all* | Lọc: `WANT_TO_READ`, `READING`, `READ` |
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Sách/trang |

**Response:** Paginated `BookshelfEntryResponse`

---

#### GET `/bookshelf/users/{userId}` — Tủ sách của user khác

🔒 Bearer Token

**Query:** `status`, `page`, `size`

**Response:** Paginated `BookshelfEntryResponse`

---

#### PUT `/bookshelf/{entryId}` — Cập nhật trạng thái / tiến độ đọc

🔒 Bearer Token

**Request:**
```json
{
  "status": "READING",
  "progress": 45
}
```

> `progress`: 0–100 (phần trăm). Khi chuyển sang `READ`, `finishDate` tự động set.

**Response:** `BookshelfEntryResponse`

---

#### DELETE `/bookshelf/{entryId}` — Xóa sách khỏi tủ

🔒 Bearer Token

**Response:** `{ "code": 1000 }`

---

#### GET `/bookshelf/stats` — Thống kê đọc sách

🔒 Bearer Token

**Response:**
```json
{
  "code": 1000,
  "result": {
    "totalBooks": 42,
    "wantToRead": 15,
    "reading": 3,
    "read": 24,
    "avgRating": 4.1,
    "topGenres": [
      { "genre": "Fiction", "count": 12 },
      { "genre": "Technology", "count": 8 }
    ]
  }
}
```

---

## 9. Search Service

> **Prefix:** `/api/v1/search` → `http://localhost:8087`  
> **Database:** Elasticsearch | **Package:** `com.dl1803.search`

---

#### GET `/search/books` — Full-text search sách

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `q` | `string` | *required* | Từ khóa |
| `genres` | `string` | *all* | Lọc thể loại (nhiều giá trị phân cách dấu phẩy) |
| `yearFrom` | `int` | — | Năm xuất bản từ |
| `yearTo` | `int` | — | Năm xuất bản đến |
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Kết quả/trang |

**Response:**
```json
{
  "code": 1000,
  "result": {
    "currentPage": 1, "totalPages": 3, "pageSize": 20, "totalElements": 45,
    "data": [
      {
        "id": "book-id",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "coverUrl": "http://...",
        "ratingAvg": 4.5,
        "highlight": {
          "title": "<em>Clean</em> <em>Code</em>",
          "synopsis": "...agile software <em>craftsmanship</em>..."
        }
      }
    ]
  }
}
```

---

#### GET `/search/profiles` — Tìm kiếm user

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `q` | `string` | *required* | Từ khóa (username, firstName, lastName) |
| `city` | `string` | *all* | Lọc thành phố |
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Kết quả/trang |

**Response:** Paginated `UserProfileResponse` với `highlight`

---

#### GET `/search/posts` — Tìm kiếm bài viết

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `q` | `string` | *required* | Từ khóa |
| `userId` | `string` | *all* | Lọc theo user |
| `bookId` | `string` | *all* | Lọc theo sách |
| `page` | `int` | `1` | Trang |
| `size` | `int` | `20` | Kết quả/trang |

**Response:** Paginated `PostResponse` với `highlight`

---

#### GET `/search/autocomplete` — Gợi ý từ khóa khi gõ

**Query:**

| Param | Type | Mô tả |
|---|---|---|
| `q` | `string` | Chuỗi đang gõ |
| `type` | `string` | `book` / `profile` / `all` |
| `limit` | `int` | Số gợi ý (default `5`) |

**Response:**
```json
{
  "code": 1000,
  "result": [
    { "text": "Clean Code", "type": "book", "id": "book-id" },
    { "text": "Code Complete", "type": "book", "id": "book-id-2" }
  ]
}
```

---

## 10. Recommendation Service

> **Prefix:** `/api/v1/recommend` → `http://localhost:8088`  
> **Database:** Milvus (Vector DB) | **Package:** `com.dl1803.recommendation`

---

#### GET `/recommend/books` — Gợi ý sách

🔒 Bearer Token

Dựa trên vector sở thích người dùng (lịch sử đọc, rating, thể loại yêu thích).

**Query:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `limit` | `int` | `10` | Số sách gợi ý |

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "book": { /* BookResponse */ },
      "score": 0.95,
      "reason": "Similar to books you've rated highly"
    }
  ]
}
```

---

#### GET `/recommend/books/{bookId}/similar` — Sách tương tự

Dựa trên book embeddings trong Milvus.

**Query:** `limit` (default `10`)

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "book": { /* BookResponse */ },
      "similarity": 0.89
    }
  ]
}
```

---

#### GET `/recommend/users` — Gợi ý kết bạn

🔒 Bearer Token

Người dùng có sở thích đọc sách tương tự.

**Query:** `limit` (default `10`)

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "profile": { /* UserProfileResponse */ },
      "commonGenres": ["Fiction", "Science"],
      "mutualFriends": 3
    }
  ]
}
```

---

#### GET `/recommend/feed` — Bài viết gợi ý cho Newsfeed

🔒 Bearer Token

Bổ sung cho feed chính — hiển thị bài viết từ người chưa kết bạn nhưng có cùng sở thích.

**Query:** `limit` (default `10`)

**Response:** `ApiResponse<List<PostResponse>>`

---

## Phụ lục A — Bảng tổng hợp toàn bộ API

### Identity Service (16 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/auth/token` | — | Đăng nhập |
| 2 | POST | `/auth/introspect` | — | Kiểm tra token |
| 3 | POST | `/auth/refresh` | — | Làm mới token |
| 4 | POST | `/auth/logout` | — | Đăng xuất |
| 5 | POST | `/auth/verify-email` | — | Xác thực email |
| 6 | POST | `/auth/resend-verification` | 🔒 | Gửi lại email xác thực |
| 7 | POST | `/auth/forgot-password` | — | Quên mật khẩu |
| 8 | POST | `/auth/reset-password` | — | Đặt lại mật khẩu |
| 9 | POST | `/auth/change-password` | 🔒 | Đổi mật khẩu |
| 10 | POST | `/users/registration` | — | Đăng ký |
| 11 | GET | `/users` | 🔒 ADMIN | Danh sách user |
| 12 | GET | `/users/{userId}` | 🔒 | Thông tin user |
| 13 | GET | `/users/myInfo` | 🔒 | Thông tin bản thân |
| 14 | PUT | `/users/{userId}` | 🔒 | Cập nhật user |
| 15 | DELETE | `/users/{userId}` | 🔒 ADMIN | Xóa user |
| 16 | POST | `/roles` | 🔒 ADMIN | Tạo role |
| 17 | GET | `/roles` | 🔒 ADMIN | Danh sách role |
| 18 | DELETE | `/roles/{role}` | 🔒 ADMIN | Xóa role |
| 19 | POST | `/permissions` | 🔒 ADMIN | Tạo permission |
| 20 | GET | `/permissions` | 🔒 ADMIN | Danh sách permission |
| 21 | DELETE | `/permissions/{permission}` | 🔒 ADMIN | Xóa permission |

### Profile Service (18 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | GET | `/users/{profileId}` | 🔒 | Xem profile |
| 2 | GET | `/users` | 🔒 | Tất cả profile |
| 3 | GET | `/users/my-profile` | 🔒 | Profile bản thân |
| 4 | PUT | `/users/my-profile` | 🔒 | Cập nhật profile |
| 5 | PUT | `/users/avatar` | 🔒 | Cập nhật avatar |
| 6 | POST | `/users/search` | 🔒 | Tìm kiếm user |
| 7 | POST | `/users/{profileId}/friend-request` | 🔒 | Gửi kết bạn |
| 8 | GET | `/users/friend-requests/incoming` | 🔒 | Lời mời nhận |
| 9 | GET | `/users/friend-requests/outgoing` | 🔒 | Lời mời gửi |
| 10 | PUT | `/users/friend-requests/{id}/accept` | 🔒 | Chấp nhận |
| 11 | PUT | `/users/friend-requests/{id}/reject` | 🔒 | Từ chối |
| 12 | DELETE | `/users/friend-requests/{id}` | 🔒 | Hủy lời mời |
| 13 | GET | `/users/friends` | 🔒 | Danh sách bạn |
| 14 | DELETE | `/users/friends/{friendProfileId}` | 🔒 | Hủy kết bạn |
| 15 | POST | `/users/{profileId}/block` | 🔒 | Chặn user |
| 16 | DELETE | `/users/{profileId}/block` | 🔒 | Bỏ chặn |
| 17 | GET | `/users/blocked` | 🔒 | DS đã chặn |
| 18 | POST | `/users/{profileId}/report` | 🔒 | Báo cáo user |

### Profile Internal (3 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/internal/users` | Internal | Tạo profile |
| 2 | GET | `/internal/users/{userId}` | Internal | Profile theo userId |
| 3 | GET | `/internal/users/bulk` | Internal | Nhiều profile |

### Post Service (16 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/posts` | 🔒 | Tạo bài viết |
| 2 | GET | `/posts/{postId}` | 🔒 | Chi tiết bài viết |
| 3 | PUT | `/posts/{postId}` | 🔒 Owner | Sửa bài viết |
| 4 | DELETE | `/posts/{postId}` | 🔒 Owner/ADMIN | Xóa bài viết |
| 5 | GET | `/my-posts` | 🔒 | Bài viết bản thân |
| 6 | GET | `/users/{userId}/posts` | 🔒 | Bài viết user |
| 7 | GET | `/feed` | 🔒 | Newsfeed |
| 8 | POST | `/posts/{postId}/comments` | 🔒 | Viết comment |
| 9 | GET | `/posts/{postId}/comments` | 🔒 | DS comment |
| 10 | PUT | `/comments/{commentId}` | 🔒 Owner | Sửa comment |
| 11 | DELETE | `/comments/{commentId}` | 🔒 Owner/ADMIN | Xóa comment |
| 12 | POST | `/posts/{postId}/like` | 🔒 | Thích |
| 13 | DELETE | `/posts/{postId}/like` | 🔒 | Bỏ thích |
| 14 | GET | `/posts/{postId}/likes` | 🔒 | DS người thích |
| 15 | POST | `/posts/{postId}/report` | 🔒 | Báo cáo bài viết |

### Chat Service (16 endpoints + WebSocket)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/conversations/create` | 🔒 | Tạo hội thoại |
| 2 | GET | `/conversations/my-conversations` | 🔒 | DS hội thoại |
| 3 | GET | `/conversations/{id}` | 🔒 | Chi tiết |
| 4 | PUT | `/conversations/{id}/nickname` | 🔒 | Đặt biệt danh |
| 5 | PUT | `/conversations/{id}/group-name` | 🔒 | Đổi tên nhóm |
| 6 | PUT | `/conversations/{id}/group-avatar` | 🔒 | Đổi ảnh nhóm |
| 7 | POST | `/conversations/{id}/participants` | 🔒 | Thêm thành viên |
| 8 | DELETE | `/conversations/{id}/participants/{uid}` | 🔒 | Xóa thành viên |
| 9 | DELETE | `/conversations/{id}/leave` | 🔒 | Rời nhóm |
| 10 | POST | `/messages/create` | 🔒 | Gửi tin nhắn |
| 11 | GET | `/messages` | 🔒 | Lịch sử tin |
| 12 | PUT | `/messages/{messageId}` | 🔒 Owner | Sửa tin nhắn |
| 13 | DELETE | `/messages/{messageId}` | 🔒 Owner | Thu hồi tin |
| 14 | POST | `/messages/{messageId}/reactions` | 🔒 | Thả reaction |
| 15 | DELETE | `/messages/{messageId}/reactions` | 🔒 | Gỡ reaction |
| 16 | GET | `/messages/{messageId}/reactions` | 🔒 | DS reactions |
| — | WS | `/ws/chat` | Token | WebSocket |

### File Service (4 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/media/upload` | 🔒 | Upload file |
| 2 | GET | `/media/download/{fileName}` | — | Download file |
| 3 | GET | `/media/my-files` | 🔒 | DS file đã upload |
| 4 | DELETE | `/media/{fileId}` | 🔒 Owner | Xóa file |

### Notification Service (6 endpoints + Kafka)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/email/send` | 🔒 ADMIN | Gửi email |
| 2 | GET | `/notifications` | 🔒 | DS thông báo |
| 3 | GET | `/notifications/unread-count` | 🔒 | Đếm chưa đọc |
| 4 | PUT | `/notifications/{id}/read` | 🔒 | Đánh dấu đã đọc |
| 5 | PUT | `/notifications/read-all` | 🔒 | Đọc tất cả |
| 6 | DELETE | `/notifications/{id}` | 🔒 | Xóa thông báo |

### Book Service (15 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/books` | 🔒 ADMIN | Thêm sách |
| 2 | GET | `/books` | — | Danh sách sách |
| 3 | GET | `/books/{bookId}` | — | Chi tiết sách |
| 4 | PUT | `/books/{bookId}` | 🔒 ADMIN | Cập nhật sách |
| 5 | DELETE | `/books/{bookId}` | 🔒 ADMIN | Xóa sách |
| 6 | GET | `/books/genres` | — | DS thể loại |
| 7 | GET | `/books/search` | — | Tìm sách |
| 8 | POST | `/books/{bookId}/rating` | 🔒 | Đánh giá |
| 9 | GET | `/books/{bookId}/rating/me` | 🔒 | Rating của tôi |
| 10 | DELETE | `/books/{bookId}/rating` | 🔒 | Xóa rating |
| 11 | GET | `/books/{bookId}/reviews` | 🔒 | Các bài review |
| 12 | POST | `/bookshelf` | 🔒 | Thêm vào tủ |
| 13 | GET | `/bookshelf` | 🔒 | Tủ sách của tôi |
| 14 | GET | `/bookshelf/users/{userId}` | 🔒 | Tủ sách user |
| 15 | PUT | `/bookshelf/{entryId}` | 🔒 | Cập nhật tủ |
| 16 | DELETE | `/bookshelf/{entryId}` | 🔒 | Xóa khỏi tủ |
| 17 | GET | `/bookshelf/stats` | 🔒 | Thống kê đọc |

### Search Service (4 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | GET | `/search/books` | — | Tìm sách full-text |
| 2 | GET | `/search/profiles` | — | Tìm user |
| 3 | GET | `/search/posts` | — | Tìm bài viết |
| 4 | GET | `/search/autocomplete` | — | Gợi ý khi gõ |

### Recommendation Service (4 endpoints)

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 1 | GET | `/recommend/books` | 🔒 | Gợi ý sách |
| 2 | GET | `/recommend/books/{bookId}/similar` | — | Sách tương tự |
| 3 | GET | `/recommend/users` | 🔒 | Gợi ý kết bạn |
| 4 | GET | `/recommend/feed` | 🔒 | Bài viết gợi ý |

---

**Tổng: ~104 REST endpoints + 1 WebSocket + 1 Kafka consumer**

---

## Phụ lục B — Sơ đồ luồng xác thực

```
┌──────────┐     ┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Client  │────▶│ API Gateway │────▶│ Identity Service │     │ Target Service  │
│ (Mobile) │     │  :8888      │     │ /auth/introspect │     │                 │
└──────────┘     └──────┬──────┘     └────────┬─────────┘     └────────┬────────┘
                        │                     │                        │
                   1. Request           2. Introspect             4. Forward
                   + Bearer Token       token valid?              (nếu valid)
                        │                     │                        │
                        │◀────────────────────│                        │
                        │    3. {valid: true}                          │
                        │─────────────────────────────────────────────▶│
                        │                                              │
                        │◀─────────────────────────────────────────────│
                        │             5. Response                      │
```

---