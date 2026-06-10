# Hướng dẫn & Tài liệu Tích hợp các Tính năng từ Teacher vào Me

Tài liệu này tổng hợp toàn bộ các file được thêm mới, chỉnh sửa và hướng dẫn kiểm thử các tính năng được bổ sung từ dự án của giáo viên (Teacher) vào dự án của bạn (Me).

---

## 1. Danh sách các file được Thêm mới & Chỉnh sửa

Tất cả các phần mã nguồn được bổ sung hoặc chỉnh sửa đều được đánh dấu bằng chú thích rõ ràng:

### 📁 Các file thêm mới (New Files)
* **[TokenRefreshRequest.java](file:///d:/IT211/Session_19/me/src/main/dto/request/TokenRefreshRequest.java)**:
  * *Nhiệm vụ*: DTO đóng gói Refresh Token gửi lên từ phía Client khi yêu cầu cấp mới Access Token.
* **[RefreshToken.java](file:///d:/IT211/Session_19/me/src/main/model/RefreshToken.java)**:
  * *Nhiệm vụ*: Entity đại diện cho bảng `refresh_token` trong DB, liên kết Many-to-One với `User`. Lưu trữ thông tin token, thời gian hết hạn (`expiredAt`), và trạng thái bị thu hồi (`isRevoked`).
* **[RefreshTokenRepository.java](file:///d:/IT211/Session_19/me/src/main/repository/RefreshTokenRepository.java)**:
  * *Nhiệm vụ*: JPA Repository giúp truy vấn thực thể `RefreshToken` từ database, tìm các token active của User, và xóa các token đã quá hạn.
* **[AdminController.java](file:///d:/IT211/Session_19/me/src/main/controller/AdminController.java)**:
  * *Nhiệm vụ*: Định tuyến các tác vụ quản trị viên tại `/api/admin/test`, sử dụng `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` để giới hạn quyền truy cập.
* **[RefreshTokenCleanupScheduler.java](file:///d:/IT211/Session_19/me/src/main/scheduler/RefreshTokenCleanupScheduler.java)**:
  * *Nhiệm vụ*: Component lập lịch chạy định kỳ lúc 2h sáng hàng ngày nhằm tự động xóa sạch các Refresh Token quá hạn trong DB để tránh phình to dữ liệu.

### 📝 Các file được cập nhật (Modified Files)
* **[Application.java](file:///d:/IT211/Session_19/me/src/main/Application.java)**:
  * *Nhiệm vụ*: Kích hoạt `@EnableScheduling` để hỗ trợ Scheduler hoạt động.
* **[UserRepository.java](file:///d:/IT211/Session_19/me/src/main/repository/UserRepository.java)**:
  * *Nhiệm vụ*: Bổ sung phương thức `existsByUsername` phục vụ kiểm tra trùng lặp tên đăng nhập.
* **[application.properties](file:///d:/IT211/Session_19/me/src/main/resources/application.properties)**:
  * *Nhiệm vụ*: Bổ sung biến cấu hình `jwt.refresh-expiration=604800000` (độ dài thời gian sống của Refresh Token là 7 ngày).
* **[JwtProperties.java](file:///d:/IT211/Session_19/me/src/main/jwt/JwtProperties.java)**:
  * *Nhiệm vụ*: Ánh xạ thuộc tính cấu hình `refreshExpiration` mới từ properties.
* **[JwtProvider.java](file:///d:/IT211/Session_19/me/src/main/jwt/JwtProvider.java)**:
  * *Nhiệm vụ*: Bổ sung hàm `generateRefreshToken` để tạo mã token có thời hạn dài hơn cho phiên đăng nhập.
* **[UserService.java](file:///d:/IT211/Session_19/me/src/main/service/UserService.java)**:
  * *Nhiệm vụ*: Khai báo phương thức `AuthResponse refreshToken(String refreshToken)`.
* **[UserServiceImpl.java](file:///d:/IT211/Session_19/me/src/main/impl/UserServiceImpl.java)**:
  * *Nhiệm vụ*:
    * Trong `register`: Thêm kiểm tra trùng lặp tên tài khoản (`existsByUsername`).
    * Trong `login`: Thu hồi toàn bộ Refresh Token cũ đang hoạt động (`isRevoked = true`), sau đó tạo và lưu trữ Refresh Token mới vào DB.
    * Trong `refreshToken`: Xác thực token gửi lên, thu hồi token cũ và sinh cặp token mới. Nếu phát hiện gửi lên một token đã bị thu hồi (`isRevoked = true`), lập tức thu hồi toàn bộ các token khác đang hoạt động của user đó để phòng chống tấn công Replay Attack.
* **[AuthController.java](file:///d:/IT211/Session_19/me/src/main/controller/AuthController.java)**:
  * *Nhiệm vụ*: Bổ sung API POST `/api/auth/refresh` nhận yêu cầu refresh token từ client.
* **[Query.sql](file:///d:/IT211/Session_19/me/Query.sql)**:
  * *Nhiệm vụ*: Chèn vai trò chuẩn (`ROLE_USER`, `ROLE_ADMIN`) và chèn 2 tài khoản mẫu `admin01` (Admin) và `user01` (User) có cùng mật khẩu mã hóa là `123456`.
* **[build.gradle](file:///d:/IT211/Session_19/me/build.gradle)**:
  * *Nhiệm vụ*: Loại trừ thư mục test lỗi của thư viện (`com/example/library/**`) để giúp dự án biên dịch thành công.

---

## 2. Quy trình kiểm thử API (Testing Guide)

### Bước 1: Khởi tạo database
Chạy các câu lệnh trong file [Query.sql](file:///d:/IT211/Session_19/me/Query.sql) trên phần mềm quản lý MySQL của bạn để nhập dữ liệu mẫu.

### Bước 2: Chạy ứng dụng
Mở terminal tại thư mục `me` và chạy lệnh:
```bash
.\gradlew bootRun
```

### Bước 3: Sử dụng API Client (Postman) để kiểm tra các API

#### 1. Đăng nhập (Login)
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/login`
* **Body (JSON)**:
  ```json
  {
    "username": "admin01",
    "password": "123456"
  }
  ```
* **Kết quả**: Trả về `200 OK` chứa cặp token (`accessToken` và `refreshToken`). Các token cũ của user trong DB sẽ chuyển sang `is_revoked = 1`.

#### 2. Kiểm thử API Admin (Phân quyền)
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/admin/test`
* **Headers**: Thêm Header `Authorization` có giá trị `Bearer <accessToken>`
* **Kết quả**:
  * Nếu đăng nhập bằng **`admin01`**: Trả về chuỗi `"test successfully"`.
  * Nếu đăng nhập bằng **`user01`**: Trả về lỗi `403 Forbidden` do thiếu quyền.

#### 3. Làm mới Token (Refresh Token)
* **Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/refresh`
* **Body (JSON)**:
  ```json
  {
    "refreshToken": "<chuỗi_refreshToken_nhận_được_khi_login>"
  }
  ```
* **Kết quả**: Trả về `200 OK` chứa cặp Access/Refresh Token mới tinh. Token cũ vừa dùng trong database sẽ chuyển trạng thái thành `isRevoked = true`.

#### 4. Kiểm tra tính năng bảo mật ngăn chặn Token giả mạo/đã cũ
* **Trường hợp gửi token ngẫu nhiên không đúng**: Bị chặn lại bởi `validateToken` của JWT hoặc `findByToken` của DB trả về `404 Not Found`.
* **Trường hợp gửi lại chính token cũ đã dùng ở Bước 3 (Bảo mật nâng cao)**: 
  * Hệ thống phát hiện token gửi lên đã bị thu hồi (`isRevoked = true`).
  * Hệ thống lập tức thu hồi (set `isRevoked = true`) toàn bộ các Refresh Token khác đang hoạt động của User đó.
  * Quăng lỗi: `"Refresh token has been revoked due to potential reuse attack"`.
  * Trình Scheduler chạy lúc 2h sáng hàng ngày sẽ tự động xóa sạch các Refresh Token quá hạn khỏi database.

---

## 3. Phân Tích Cơ Chế Bảo Mật Nâng Cao & Tránh Phình To DB

### 3.1. Lỗ hổng trước khi sửa đổi
Ban đầu, khi người dùng đăng nhập lại (hoặc đăng xuất), hệ thống chỉ sinh thêm một dòng Refresh Token mới trong DB mà không hề đụng chạm hoặc vô hiệu hóa các Refresh Token cũ của phiên làm việc trước. 
Điều này dẫn đến lỗ hổng: **Nhiều Refresh Token của cùng một User đều có hiệu lực song song**. Nếu kẻ tấn công bằng cách nào đó sở hữu một token cũ, chúng vẫn có thể tiếp tục sử dụng để lấy Access Token mới và truy cập trái phép tài nguyên cho đến khi token hết hạn tự nhiên (thường là 7 ngày).

### 3.2. Giải pháp khắc phục (Cách hoạt động chi tiết)

#### A. Khi Đăng Nhập Mới (Login)
1. Xác thực thông tin tài khoản và mật khẩu thành công.
2. Tìm tất cả các Refresh Token đang hoạt động (`isRevoked = false`) gắn với User trong DB.
3. Chuyển trạng thái toàn bộ các token cũ này thành `isRevoked = true` để vô hiệu hóa tức thì.
4. Sinh Refresh Token mới, lưu vào DB dưới trạng thái `isRevoked = false`.

#### B. Cơ Chế Phát Hiện Tái Sử Dụng Token (RTR Reuse Detection)
Nếu kẻ tấn công đánh cắp được một Refresh Token cũ và cố tình gửi lên API `/api/auth/refresh`:
1. Server tìm thấy token này trong DB nhưng trạng thái của nó đã bị đánh dấu là `isRevoked = true`.
2. Hệ thống nhận diện đây là hành vi tái sử dụng trái phép (Replay Attack).
3. Để đảm bảo an toàn tuyệt đối, hệ thống **thu hồi lập tức toàn bộ các Refresh Token đang hoạt động khác** của người dùng đó.
4. Ném ra lỗi và từ chối cấp phép. Kẻ tấn công bị chặn đứng, và người dùng thật cũng sẽ bị đăng xuất để đăng nhập lại an toàn (do toàn bộ token của họ đã bị hủy).

#### C. Giải pháp tránh phình to Database (Spring Scheduler)
Để tránh việc lưu giữ quá nhiều token cũ làm nặng DB:
1. Hệ thống sử dụng một tác vụ tự động chạy ngầm (`@Scheduled(cron = "0 0 2 * * ?")`) lúc **2h sáng hàng ngày**.
2. Bộ dọn dẹp chỉ xóa các token **đã hết hạn tự nhiên** (`expiredAt < LocalDateTime.now()`).
3. **Tại sao không xóa các token bị revoke khi chúng chưa hết hạn?** Vì nếu xóa quá sớm trước thời điểm hết hạn tự nhiên, khi kẻ tấn công gửi token cũ lên, hệ thống sẽ không tìm thấy trong DB và chỉ coi đó là "token không tồn tại" (không kích hoạt được cơ chế thu hồi khẩn cấp RTR để bảo vệ phiên hiện tại của người dùng).

---

### 3.3. Chi tiết Mã Nguồn Triển Khai

#### 1. Các phương thức truy vấn trong [RefreshTokenRepository.java](file:///d:/IT211/Session_19/me/src/main/repository/RefreshTokenRepository.java)
```
// Tìm các token đang active của user để thu hồi khi cần thiết
@Query("SELECT r FROM RefreshToken r WHERE r.user = :user AND r.isRevoked = false")
List<RefreshToken> findAllActiveByUser(@Param("user") User user);

// Xóa các token đã hết hạn tự nhiên ra khỏi DB
@Modifying
@Transactional
@Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now")
void deleteExpiredTokens(@Param("now") LocalDateTime now);
```

#### 2. Logic xử lý trong [UserServiceImpl.java](file:///d:/IT211/Session_19/me/src/main/impl/UserServiceImpl.java)
* **Khi đăng nhập thành công (`login`):**
```
// Thu hồi toàn bộ Refresh Token cũ của User này
List<RefreshToken> activeTokens = refreshTokenRepository.findAllActiveByUser(user);
if (activeTokens != null && !activeTokens.isEmpty()) {
    activeTokens.forEach(t -> t.setRevoked(true));
    refreshTokenRepository.saveAll(activeTokens);
}
```
* **Khi xoay vòng token (`refreshToken`):**
```
// Phát hiện tấn công tái sử dụng (Reuse Detection)
if (tokenEntity.isRevoked()) {
    List<RefreshToken> activeTokens = refreshTokenRepository.findAllActiveByUser(tokenEntity.getUser());
    if (activeTokens != null && !activeTokens.isEmpty()) {
        activeTokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }
    throw new RuntimeException("Refresh token has been revoked due to potential reuse attack");
}
```

#### 3. Bộ dọn dẹp tự động [RefreshTokenCleanupScheduler.java](file:///d:/IT211/Session_19/me/src/main/scheduler/RefreshTokenCleanupScheduler.java)
```
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") // 2h sáng mỗi ngày
    public void cleanExpiredTokens() {
        System.out.println("Scheduler: Running cleanup for expired refresh tokens...");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
```
