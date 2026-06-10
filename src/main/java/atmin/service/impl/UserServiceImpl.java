package atmin.service.impl;

import atmin.dto.request.LoginRequest;
import atmin.dto.request.RegisterRequest;
import atmin.dto.response.AuthResponse;
import atmin.model.Role;
import atmin.model.User;
import atmin.model.RefreshToken;
import atmin.repository.RoleRepository;
import atmin.repository.UserRepository;
import atmin.repository.RefreshTokenRepository;
import atmin.security.jwt.JwtProvider;
import atmin.security.jwt.JwtProperties;
import atmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    
    private final JwtProvider jwtProvider;

    private final RoleRepository roleRepository;

    // BỔ SUNG TỪ DỰ ÁN TEACHER: Tiêm repository quản lý Refresh Token
    private final RefreshTokenRepository refreshTokenRepository;

    // BỔ SUNG TỪ DỰ ÁN TEACHER: Tiêm cấu hình JWT properties để lấy thời gian hết hạn của Refresh Token
    private final JwtProperties jwtProperties;

    @Override
    public void register(RegisterRequest request) {

        // BỔ SUNG TỪ DỰ ÁN TEACHER: Kiểm tra trùng lặp username trước khi đăng ký
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new RuntimeException("ROLE_USER not found"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .roles(Set.of(roleUser, adminRole))
                // SECURITY
                .password(passwordEncoder.encode(request.getPassword()))

                .build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // SECURITY
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        System.out.println("Login Successful");

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String accessToken = jwtProvider.generateToken(user);

        // BỔ SUNG TỪ DỰ ÁN TEACHER: Sinh Refresh Token có thời hạn sống dài hơn
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // BỔ SUNG: Thu hồi tất cả các Refresh Token đang hoạt động cũ của người dùng này
        java.util.List<RefreshToken> activeTokens = refreshTokenRepository.findAllActiveByUser(user);
        if (activeTokens != null && !activeTokens.isEmpty()) {
            activeTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(activeTokens);
        }

        // BỔ SUNG TỪ DỰ ÁN TEACHER: Lưu Refresh Token vào database
        RefreshToken refreshEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiredAt(LocalDateTime.now().plusNanos(jwtProperties.getRefreshExpiration() * 1_000_000L))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshEntity);

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    // BỔ SUNG TỪ DỰ ÁN TEACHER: Triển khai xoay vòng Refresh Token (Token Refresh Rotation - RTR)
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. Xác thực tính hợp lệ của Refresh Token (về chữ ký, cấu trúc, thời gian hết hạn của JWT)
        try {
            if (!jwtProvider.validateToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token: " + e.getMessage());
        }

        // 2. Tìm kiếm thực thể Refresh Token trong cơ sở dữ liệu
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // 3. Kiểm tra xem Refresh Token có bị thu hồi (revoked) hoặc hết hạn hay chưa
        if (tokenEntity.isRevoked()) {
            // BỔ SUNG: Phát hiện cuộc tấn công tái sử dụng (Replay Attack Detection).
            // Thu hồi lập tức toàn bộ các Refresh Token khác đang hoạt động của người dùng này để đảm bảo an toàn.
            java.util.List<RefreshToken> activeTokens = refreshTokenRepository.findAllActiveByUser(tokenEntity.getUser());
            if (activeTokens != null && !activeTokens.isEmpty()) {
                activeTokens.forEach(t -> t.setRevoked(true));
                refreshTokenRepository.saveAll(activeTokens);
            }
            throw new RuntimeException("Refresh token has been revoked due to potential reuse attack");
        }
        if (tokenEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token is expired");
        }

        // 4. Sinh cặp Access Token và Refresh Token mới
        User user = tokenEntity.getUser();
        String newAccessToken = jwtProvider.generateToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        // 5. Thu hồi token cũ và lưu token mới
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        RefreshToken newRefreshEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiredAt(LocalDateTime.now().plusNanos(jwtProperties.getRefreshExpiration() * 1_000_000L))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(newRefreshEntity);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}