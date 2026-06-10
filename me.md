# TỔNG QUAN LUỒNG XÁC THỰC (BASIC AUTH & JWT)

---



## 1. Luồng Đăng Nhập bằng Basic Auth (Mỗi request đều xác thực lại qua DB)

1. **Lấy dữ liệu & Đóng gói:** `BasicAuthenticationFilter` ngầm của Spring Security đọc Header `Authorization: Basic ...` từ request gửi lên, giải mã Base64 lấy `username` + `password` thô, đóng gói thành token tạm thời chưa xác thực (`UsernamePasswordAuthenticationToken`).
2. **Xác thực:**
   - `BasicAuthenticationFilter` gọi `AuthenticationManager` (ở đây được cấu hình qua `AuthenticationConfiguration` tự động gom các Bean).
   - `AuthenticationManager` gọi tiếp `DaoAuthenticationProvider`.
   - `DaoAuthenticationProvider` gọi [UserDetailServiceCustom](file:///d:/IT211/test_ss_13/src/main/java/atmin/security/principal/UserDetailServiceCustom.java) lấy `UserPrincipal` (chứa user và mật khẩu băm từ DB) để trả lại.
   - `DaoAuthenticationProvider` dùng `BCryptPasswordEncoder` để so sánh mật khẩu thô và mật khẩu băm trong DB.
3. **Lưu trạng thái đăng nhập:** Nếu khớp, `DaoAuthenticationProvider` trả về token đã xác thực thành công. Luồng quay lại `BasicAuthenticationFilter`, filter này tự động lưu token vào `SecurityContextHolder` để thiết lập trạng thái đăng nhập.

## 2. Luồng Đăng Nhập bằng JWT (Chỉ kiểm tra Token trên RAM, không query DB)

1. **Kiểm tra Token:** [JwtAuthenticationFilter](file:///d:/IT211/test_ss_13/src/main/java/atmin/security/jwt/JwtAuthenticationFilter.java) hứng request, đọc Header `Authorization: Bearer <token>`.
2. **Giải mã:** Gọi `JwtProvider.validateToken()` để kiểm tra chữ ký và thời hạn Token trực tiếp trên RAM (không query DB).
3. **Thiết lập đăng nhập:** Nếu Token hợp lệ, lấy `username` (kiểu String) và `roles` từ Token, tự đóng gói thành token đã xác thực (`UsernamePasswordAuthenticationToken`) rồi gán thẳng vào `SecurityContextHolder`.

---

# JWT Flow Note

## 1. Các file tham gia

```text
SecurityConfig
│
├── AuthenticationManager
│       │
│       └── UserDetailServiceCustom
│                │
│                └── UserPrincipal
│
├── JwtAuthenticationFilter
│
└── JwtProvider
```

---

# PHẦN 1 - LUỒNG LOGIN

## Request

```http
POST /api/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "123456"
}
```

---

## Bước 1: Controller nhận request login

```
authenticationManager.authenticate(...)
```

Spring sẽ chuyển việc xác thực cho AuthenticationManager.

---

## Bước 2: AuthenticationManager

File:

```
SecurityConfig
```

Bean:

```
@Bean
public AuthenticationManager authenticationManager()
```

Khởi tạo:

```
DaoAuthenticationProvider
```

và đăng ký:

```
UserDetailServiceCustom
```

```
Controller
    ↓
AuthenticationManager
```

---

## Bước 3: UserDetailServiceCustom

Hàm chạy:

```
loadUserByUsername(String username)
```

Mục đích:

* Query database
* Tìm user theo username

```
userRepository.findByUsername(username)
```

Nếu không tìm thấy:

```
throw UsernameNotFoundException
```

```
AuthenticationManager
    ↓
UserDetailServiceCustom
```

---

## Bước 4: Tạo UserPrincipal

Sau khi lấy được User từ DB:

```
return UserPrincipal.builder()
```

Role được convert thành:

```
SimpleGrantedAuthority
```

Ví dụ:

```
ADMIN
```

↓

```
ROLE_ADMIN
```

```
UserDetailServiceCustom
    ↓
UserPrincipal
```

---

## Bước 5: So sánh mật khẩu

Spring tự động gọi:

```
getPassword()
```

trong:

```
UserPrincipal
```

và so sánh với:

```
BCryptPasswordEncoder
```

Nếu sai:

```
BadCredentialsException
```

Nếu đúng:

```
Authenticated = true
```

---

## Bước 6: Tạo JWT

Sau khi đăng nhập thành công:

```
jwtProvider.generateToken(user)
```

File:

```
JwtProvider
```

Hàm:

```
generateToken(User user)
```

JWT chứa:

```
{
  "sub": "admin",
  "roles": [
    "ROLE_ADMIN"
  ],
  "iat": "...",
  "exp": "..."
}
```

```
AuthenticationManager
    ↓
JwtProvider.generateToken()
```

---

## Bước 7: Trả JWT về Client

Response:

```json
{
  "token": "eyJhbGciOi..."
}
```

Frontend lưu:

```javascript
localStorage.setItem("token", token)
```

hoặc

```javascript
Cookie
```

---

# PHẦN 2 - LUỒNG REQUEST SAU LOGIN

Ví dụ:

```http
GET /api/users
Authorization: Bearer eyJhbGciOi...
```

---

## Bước 1: Security Filter Chain

File:

```text
SecurityConfig
```

Cấu hình:

```
.addFilterBefore(
    jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class
)
```

Spring sẽ chạy:

```
JwtAuthenticationFilter
```

trước.

```
Request
    ↓
JwtAuthenticationFilter
```

---

## Bước 2: JwtAuthenticationFilter

Hàm chạy:

```
doFilterInternal(...)
```

Lấy Header:

```
request.getHeader("Authorization")
```

Ví dụ:

```
Bearer abc.xyz.123
```

---

## Bước 3: Tách JWT

```
String token =
        authHeader.substring(7);
```

Kết quả:

```
abc.xyz.123
```

---

## Bước 4: Validate Token

Filter gọi:

```
jwtProvider.validateToken(token)
```

File:

```
JwtProvider
```

Hàm:

```
validateToken()
```

Kiểm tra:

* Signature
* Expiration
* JWT format

Nếu lỗi:

```
ExpiredJwtException
SignatureException
MalformedJwtException
```

Trả:

```
401 Unauthorized
```

---

## Bước 5: Đọc thông tin trong JWT

Nếu hợp lệ:

```
jwtProvider.getUsernameFromToken(token)
```

↓

```
jwtProvider.getRolesFromToken(token)
```

Ví dụ:

```
{
  "sub": "admin",
  "roles": [
    "ROLE_ADMIN"
  ]
}
```

↓

```
username = "admin"
roles = ["ROLE_ADMIN"]
```

---

## Bước 6: Tạo Authentication

```
UsernamePasswordAuthenticationToken
```

```
new UsernamePasswordAuthenticationToken(
    username,
    null,
    authorities
)
```

---

## Bước 7: Đưa vào SecurityContext

```
SecurityContextHolder
    .getContext()
    .setAuthentication(auth);
```

Lúc này Spring hiểu:

```
User hiện tại = admin
Role = ROLE_ADMIN
```

---

## Bước 8: Controller được chạy

Ví dụ:

```
@GetMapping("/users")
```

Hoặc:

```
@PreAuthorize("hasRole('ADMIN')")
```

Spring kiểm tra role.

Nếu đủ quyền:

```
200 OK
```

Nếu không đủ:

```
403 Forbidden
```

---

# PHẦN 3 - THỨ TỰ FILE CHẠY

## Login

```
Controller
    ↓
SecurityConfig
    ↓
AuthenticationManager
    ↓
UserDetailServiceCustom
    ↓
UserPrincipal
    ↓
BCryptPasswordEncoder
    ↓
JwtProvider.generateToken()
    ↓
Client nhận JWT
```

---

## Request Sau Login

```
Request
    ↓
SecurityConfig
    ↓
JwtAuthenticationFilter
    ↓
JwtProvider.validateToken()
    ↓
JwtProvider.getUsernameFromToken()
    ↓
JwtProvider.getRolesFromToken()
    ↓
SecurityContextHolder
    ↓
Controller
```

---

# PHẦN 4 - ĐIỂM QUAN TRỌNG

## Chỉ Login mới query DB

```
Login
    ↓
UserDetailServiceCustom
    ↓
Database
```

---

## Sau Login KHÔNG query DB

```
Request
    ↓
JwtAuthenticationFilter
    ↓
JWT
    ↓
Controller
```

Hệ thống của project hiện tại là:

```
STATELESS JWT AUTHENTICATION
```

Tức là:

* Không dùng Session
* Không lưu User trong Server
* Không query DB mỗi request
* Chỉ đọc dữ liệu từ JWT
