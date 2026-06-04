package atmin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;

    @ManyToMany(fetch = FetchType.EAGER) // Dùng EAGER để khi lấy User lên thì Hibernate tự động lấy luôn danh sách Roles đi kèm
    @JoinTable(
            name = "user_roles", // Tên bảng trung gian trong Database
            joinColumns = @JoinColumn(name = "user_username"), // Khóa ngoại trỏ về khóa chính của bảng User
            inverseJoinColumns = @JoinColumn(name = "role_id")  // Khóa ngoại trỏ về khóa chính của bảng Role
    )
    private Set<Role> roles;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
