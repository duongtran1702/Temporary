package atmin.security.jwt;

import atmin.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private SecretKey key; // Bỏ phần khởi tạo trực tiếp tại đây để tránh NullPointerException

    // Hàm này sẽ tự động chạy NGAY SAU KHI Spring đã tiêm đầy đủ jwtProperties thành công
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long nowMillis = System.currentTimeMillis();
        Date issuedAtDate = new Date(nowMillis);
        Date expirationDate = new Date(nowMillis + jwtProperties.getExpiration());

        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName();
                    return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                }).toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .expiration(expirationDate)
                .issuedAt(issuedAtDate)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // Chỉ cần parse thành công không ném ra Exception nghĩa là:
            // Chữ ký đúng + Cấu trúc chuẩn + Còn hạn sử dụng.
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn sử dụng
            throw new JwtException("Token is expired");
        } catch (SignatureException e) {
            // Token có chữ ký không hợp lệ
            throw new JwtException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            // Token sai cấu trúc
            throw new JwtException("Invalid JWT token");
        } catch (UnsupportedJwtException e) {
            // Token không được hỗ trợ
            throw new JwtException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            // Chuỗi token bị rỗng hoặc null
            throw new JwtException("JWT claims string is empty");
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }
}
