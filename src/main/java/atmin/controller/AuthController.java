package atmin.controller;

import atmin.dto.request.LoginRequest;
import atmin.dto.request.RegisterRequest;
import atmin.dto.request.TokenRefreshRequest;
import atmin.dto.response.AuthResponse;
import atmin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request){

        userService.register(request);
        return ResponseEntity.ok("Register success");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {

        return ResponseEntity.ok(userService.refreshToken(request.getRefreshToken()));
    }
}