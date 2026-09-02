# Thiết kế Cơ sở Dữ liệu Chi tiết Bookify
![Bookify Database ERD](../images/database-erd.png)
---

## 1. Identity Service (MySQL)
*Công nghệ: Spring Data JPA + MySQL*

### Bảng: `user`
* `id`: VARCHAR(36) [PK, UUID]
* `username`: VARCHAR(255) [UNIQUE, NOT NULL]
* `password`: VARCHAR(255) [NOT NULL]
* `email`: VARCHAR(255) [UNIQUE, NOT NULL]
* `email_verified`: BOOLEAN [DEFAULT false]
* `verification_otp`: VARCHAR(4) [Bổ sung: Mã OTP 4 số dùng cho cả xác thực email và quên mật khẩu]
* `otp_expiry_time`: DATETIME [Bổ sung: Thời hạn mã OTP]
* `last_otp_sent_time`: DATETIME [Bổ sung: Thời gian gửi mã OTP gần nhất]
* `otp_attempt_count`: INT [DEFAULT 0, Bổ sung: Số lần nhập sai OTP]
* `password_changed_at`: DATETIME [Bổ sung: Thời điểm đổi mật khẩu để thu hồi token cũ]

### Bảng: `role`
* `name`: VARCHAR(255) [PK]
* `description`: VARCHAR(255)

### Bảng: `permission`
* `name`: VARCHAR(255) [PK]
* `description`: VARCHAR(255)

### Bảng: `invalidated_token`
* `id`: VARCHAR(255) [PK]
* `expiry_time`: DATETIME

### Bảng trung gian: `user_roles`
* `user_id`: VARCHAR(36) [FK -> user.id]
* `roles_name`: VARCHAR(255) [FK -> role.name]
* PRIMARY KEY (`user_id`, `roles_name`)

### Bảng trung gian: `role_permissions`
* `role_name`: VARCHAR(255) [FK -> role.name]
* `permissions_name`: VARCHAR(255) [FK -> permission.name]
* PRIMARY KEY (`role_name`, `permissions_name`)

---

## 2. Profile Service (Neo4J - Graph DB)
*Công nghệ: Spring Data Neo4j*

### Node: `:user_profile`
> **Nguyên tắc thiết kế:** Node độc lập nhẹ (lightweight), **không** map collection quan hệ (`@Relationship List<...>`) vào entity Java để tránh nạp toàn bộ graph vào bộ nhớ (OOM/Lazy loading overhead). Toàn bộ thao tác kết bạn, truy vấn bạn bè và đếm số lượng được xử lý qua Custom Cypher Queries.

* `id`: String [UUID, Internal ID]
* `userId`: String [Index, UNIQUE - Liên kết trực tiếp với `user.id` bên MySQL/JWT]
* `username`: String
* `email`: String
* `firstName`: String
* `lastName`: String
* `avatar`: String
* `dob`: LocalDate
* `city`: String
* `bio`: String

### Relationships:
* `(:user_profile)-[:FOLLOWS { createdDate: Instant }]->(:user_profile)`
* `(:user_profile)-[:FRIEND_WITH { requestId: String, status: 'PENDING' | 'ACCEPTED', createdDate: Instant }]->(:user_profile)`
  * **Vòng đời quan hệ:**
    * *Gửi lời mời:* Tạo directed relationship `(sender)-[:FRIEND_WITH {status: 'PENDING', ...}]->(recipient)`
    * *Chấp nhận:* Cập nhật `SET r.status = 'ACCEPTED'`
    * *Từ chối / Hủy lời mời / Hủy kết bạn:* Thực hiện **`DELETE r`** (xóa hoàn toàn relationship khỏi đồ thị để giữ graph sạch và câu Cypher đơn giản, nhanh chóng).
    * *Không có relationship giữa 2 node:* Mặc định coi là người lạ (`NONE`).
  * **Truy vấn bạn bè:** Dùng cú pháp vô hướng `(me)-[:FRIEND_WITH {status: 'ACCEPTED'}]-(friend)` để tự động duyệt cả 2 chiều sender/recipient.
* `(:user_profile)-[:BLOCKS { createdDate: Instant }]->(:user_profile)`
  * Khi chặn (Block): Tạo relationship `BLOCKS` và đồng thời xóa quan hệ `FRIEND_WITH` nếu đang có.
  * Khi bỏ chặn: Xóa relationship `BLOCKS`.

### Node: `:report` *(Báo cáo người dùng)*
* `id`: String [UUID]
* `reporterId`: String [Index - userId người gửi báo cáo]
* `targetUserId`: String [Index - userId người bị báo cáo]
* `reason`: String [Enum: `SPAM`, `HARASSMENT`, `FAKE_ACCOUNT`, `INAPPROPRIATE_CONTENT`, `OTHER`]
* `description`: String [Nội dung mô tả chi tiết]
* `createdDate`: Instant [Thời điểm tạo báo cáo, mặc định `now()`]

### Khuyến nghị Cypher Indexes:
```cypher
CREATE CONSTRAINT user_profile_userId_unique IF NOT EXISTS FOR (u:user_profile) REQUIRE u.userId IS UNIQUE;
CREATE INDEX report_target_idx IF NOT EXISTS FOR (r:report) ON (r.targetUserId);
```

---

## 3. Post Service (MongoDB)
*Công nghệ: Spring Data MongoDB*

### Collection: `post`
* `id`: String [PK, MongoId]
* `userId`: String [Index - Người đăng bài]
* `postType`: String [Index - Loại bài viết: `REVIEW` / `PROGRESS` / `QUOTE` / `GENERAL`]
* `bookId`: String [Index, Nullable - ID sách gắn kèm (bắt buộc cho REVIEW/PROGRESS/QUOTE)]
* `content`: String [Nội dung bài viết / review]
* `images`: Array[String] [Danh sách URL ảnh đính kèm]
* `rating`: Double [Nullable - Điểm đánh giá sao 0.5–5.0, dùng cho postType REVIEW]
* `progressPercent`: Integer [Nullable - Tiến độ đọc %, dùng cho postType PROGRESS]
* `pagesRead`: Integer [Nullable - Số trang đã đọc, dùng cho postType PROGRESS]
* `totalPages`: Integer [Nullable - Tổng số trang, dùng cho postType PROGRESS]
* `quoteText`: String [Nullable - Nội dung trích dẫn, dùng cho postType QUOTE]
* `quoteTheme`: String [Nullable - Theme hiển thị: `WARM` / `OCEAN` / `FOREST` / `MIDNIGHT`]
* `likesCount`: Long [DEFAULT 0 - Đếm nhanh số lượt like]
* `commentsCount`: Long [DEFAULT 0 - Đếm nhanh số comment]
* `createdDate`: Instant
* `modifiedDate`: Instant

### Collection: `post_comment`
* `id`: String [PK, MongoId]
* `postId`: String [Index]
* `userId`: String [Index]
* `parentCommentId`: String [Nullable - Dùng khi reply comment]
* `content`: String
* `createdDate`: Instant

### Collection: `post_like`
* `id`: String [PK, MongoId]
* `postId`: String [Index]
* `userId`: String [Index]
* `createdDate`: Instant

### Collection: `post_report`
* `id`: String [PK, MongoId]
* `postId`: String [Index]
* `userId`: String [Index - Người báo cáo]
* `reason`: String
* `description`: String
* `createdDate`: Instant

---

## 4. File Service (MongoDB)
*Công nghệ: Spring Data MongoDB*

### Collection: `file_mgmt`
* `id`: String [PK, MongoId]
* `ownerId`: String [Index]
* `originalFileName`: String
* `contentType`: String
* `size`: Long
* `path`: String
* `md5Checksum`: String

---

## 5. Chat Service (MongoDB)
*Công nghệ: Spring Data MongoDB*

### Collection: `conversation`
* `id`: String [PK, MongoId]
* `type`: String (`direct` / `group`)
* `name`: String [Nullable - Cho group]
* `avatar`: String [Nullable - Cho group]
* `participantsHash`: String [UNIQUE, Index]
* `participants`: Array[Embedded Document `ParticipantInfo`]
  * `userId`: String
  * `username`: String
  * `nickname`: String
  * `firstName`: String
  * `lastName`: String
  * `avatar`: String
* `createdDate`: Instant
* `modifiedDate`: Instant

### Collection: `chat_message`
* `id`: String [PK, MongoId]
* `conversationId`: String [Index]
* `message`: String
* `sender`: Embedded Document `ParticipantInfo`
* `isEdited`: Boolean [DEFAULT false]
* `editedDate`: Instant [Nullable]
* `reactions`: Array[Embedded Document `Reaction`]
  * `userId`: String
  * `type`: String
  * `createdDate`: Instant
* `createdDate`: Instant [Index]

---

## 6. Notification Service (MongoDB - Thiết kế mới)
*Công nghệ: Spring Data MongoDB*

### Collection: `notification`
* `id`: String [PK, MongoId]
* `recipientId`: String [Index - Người nhận]
* `senderId`: String [Người gây ra tương tác]
* `type`: String (`POST_LIKE`, `NEW_COMMENT`, `COMMENT_REPLY`, `FRIEND_REQUEST`, `FRIEND_ACCEPTED`, `MESSAGE`, `MESSAGE_REACTION`, `BOOK_RECOMMENDATION`, `SYSTEM`)
* `targetId`: String [ID của post/comment/conversation/book]
* `content`: String [Nội dung hiển thị thông báo]
* `isRead`: Boolean [DEFAULT false]
* `createdDate`: Instant

---

## 7. Book Service (MongoDB - Core Book Engine)
*Công nghệ: Spring Data MongoDB*

### Collection: `book`
* `id`: String [PK, MongoId]
* `title`: String [Index]
* `author`: String [Index]
* `isbn`: String [UNIQUE]
* `coverUrl`: String
* `synopsis`: String
* `genres`: Array[String]
* `publishedYear`: Integer
* `pageCount`: Integer [Tổng số trang]
* `ratingAvg`: Double
* `ratingCount`: Long [DEFAULT 0]
* `createdDate`: Instant

### Collection: `book_rating`
* `id`: String [PK, MongoId]
* `bookId`: String [Index]
* `userId`: String [Index]
* `rating`: Double [0.5–5.0]
* `createdDate`: Instant
* Compound Index: (`bookId`, `userId`) [UNIQUE]

### Collection: `bookshelf_entry`
* `id`: String [PK, MongoId]
* `userId`: String [Index]
* `bookId`: String [Index]
* `status`: String (`WANT_TO_READ` / `READING` / `READ`)
* `progress`: Integer [0-100]
* `currentPage`: Integer [Nullable - Số trang hiện tại đang đọc]
* `startDate`: Instant [Nullable]
* `finishDate`: Instant [Nullable]
* `addedDate`: Instant

### Collection: `custom_shelf` *(Kệ sách tùy chỉnh)*
* `id`: String [PK, MongoId]
* `userId`: String [Index]
* `name`: String [Tên kệ: "Favorites", "Psychology", "Sci-Fi Classics"...]
* `icon`: String [Tên icon: "favorite", "star", "book", "fire", "bookmark", "psychology", "science"]
* `description`: String [Nullable]
* `bookIds`: Array[String] [Danh sách ID sách trong kệ]
* `createdDate`: Instant
* Compound Index: (`userId`, `name`) [UNIQUE - Mỗi user không được trùng tên kệ]

### Collection: `reading_progress_log` *(Nhật ký tiến độ đọc)*
* `id`: String [PK, MongoId]
* `userId`: String [Index]
* `bookId`: String [Index]
* `bookshelfEntryId`: String [Index - Liên kết với bookshelf_entry]
* `pagesRead`: Integer [Số trang đã đọc tại thời điểm ghi log]
* `totalPages`: Integer [Tổng số trang sách]
* `percentage`: Integer [0-100, Tiến độ %]
* `note`: String [Nullable - Ghi chú khi cập nhật tiến độ]
* `logDate`: LocalDate [Index - Ngày ghi log, dùng để tính Reading Streak]
* `createdDate`: Instant

---

## 8. Search & Recommendation Service (Kế hoạch mở rộng)

### Search Service (Elasticsearch)
* **Index `book_search`**: Đánh chỉ mục full-text cho `title`, `author`, `genres`, `synopsis`.
* **Index `profile_search`**: Đánh chỉ mục cho `username`, `firstName`, `lastName`.

### Recommendation Service (Vector DB - Milvus)
* **Collection `book_embeddings`**: `id` (bookId), `vector` (Mô tả ngữ nghĩa của sách).
* **Collection `user_preferences`**: `id` (userId), `vector` (Vector sở thích người dùng).