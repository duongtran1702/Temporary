package atmin.repository;

import atmin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("Select u from User u where u.email = :username or u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    boolean existsByEmail(String email);

    // BỔ SUNG TỪ DỰ ÁN TEACHER: Kiểm tra xem username đã tồn tại chưa khi đăng ký tài khoản mới.
    boolean existsByUsername(String username);
}
