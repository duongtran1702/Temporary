package atmin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BỔ SUNG TỪ DỰ ÁN TEACHER: Controller chứa các API kiểm tra phân quyền dành riêng cho quản trị viên (Admin).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String test(){
        return "test successfully";
    }
}
