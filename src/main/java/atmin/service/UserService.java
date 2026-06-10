package atmin.service;

import atmin.dto.request.LoginRequest;
import atmin.dto.request.RegisterRequest;
import atmin.dto.response.AuthResponse;

public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    // BỔ SUNG TỪ DỰ ÁN TEACHER: Phương thức hỗ trợ refresh token để cấp phát Access Token mới cho client.
    AuthResponse refreshToken(String refreshToken);
}