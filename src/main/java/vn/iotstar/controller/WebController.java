package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        
        if (error != null) {
            switch (error) {
                case "unauthorized":
                    model.addAttribute("error", "Bạn cần đăng nhập để truy cập trang này!");
                    break;
                case "invalid_token":
                    model.addAttribute("error", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                    break;
                case "access_denied":
                    model.addAttribute("error", "Bạn không có quyền truy cập!");
                    break;
                case "system_error":
                    model.addAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại!");
                    break;
                default:
                    model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            }
        }
        
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công! Hẹn gặp lại bạn.");
        }
        
        return "web/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "web/register";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage() {
        return "web/verify-otp";
    }
}