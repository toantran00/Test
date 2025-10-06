package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.NguoiDung;
import vn.iotstar.model.ApiResponse;
import vn.iotstar.repository.NguoiDungRepository;
import vn.iotstar.util.JwtUtil;

import java.io.File;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String showProfile(HttpServletRequest request, Model model) {
        try {
            // Lấy token từ request
            String token = extractJwtFromRequest(request);
            
            if (token == null) {
                // Nếu không có token, chuyển hướng đến login
                return "redirect:/login?error=unauthorized";
            }
            
            if (!jwtUtil.validateJwtToken(token)) {
                // Token không hợp lệ
                return "redirect:/login?error=invalid_token";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("user", user);
            model.addAttribute("activeMenu", "user");
            
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
        
        return "web/profile";
    }

    // API endpoint to get user profile data
    @GetMapping("/api/user/profile")
    @ResponseBody
    public ResponseEntity<ApiResponse<NguoiDung>> getUserProfile(HttpServletRequest request) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Unauthorized: Token không hợp lệ"));
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(ApiResponse.success(user));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    // API endpoint to update user profile
    @PostMapping("/api/user/update")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractJwtFromRequest(httpRequest);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Unauthorized: Token không hợp lệ"));
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate số điện thoại nếu có
            if (request.getSdt() != null && !request.getSdt().trim().isEmpty()) {
                String sdt = request.getSdt().trim();
                if (!sdt.matches("^0[0-9]{9}$")) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số"));
                }
                user.setSdt(sdt);
            } else {
                user.setSdt(null);
            }
            
            // Cập nhật thông tin khác
            if (request.getTenNguoiDung() != null && !request.getTenNguoiDung().trim().isEmpty()) {
                user.setTenNguoiDung(request.getTenNguoiDung().trim());
            }
            
            if (request.getDiaChi() != null) {
                user.setDiaChi(request.getDiaChi().trim());
            }
            
            nguoiDungRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    // API endpoint to change password
    @PostMapping("/api/user/change-password")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractJwtFromRequest(httpRequest);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Unauthorized: Token không hợp lệ"));
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getMatKhau())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu hiện tại không đúng"));
            }
            
            // Kiểm tra mật khẩu mới và xác nhận
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu xác nhận không khớp"));
            }
            
            // Kiểm tra độ dài mật khẩu mới
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu mới phải có ít nhất 6 ký tự"));
            }
            
            // Cập nhật mật khẩu
            user.setMatKhau(passwordEncoder.encode(request.getNewPassword()));
            nguoiDungRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    // API endpoint to upload avatar
    @PostMapping("/api/user/upload-avatar")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest httpRequest) {
        try {
            String token = extractJwtFromRequest(httpRequest);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Unauthorized: Token không hợp lệ"));
            }

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn file ảnh"));
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File ảnh không được vượt quá 5MB"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ chấp nhận file ảnh (jpg, png, gif)"));
            }

            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "images" + File.separator;
            
            // Create uploads/images directory if not exists
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "avatar_" + user.getMaNguoiDung() + "_" + System.currentTimeMillis() + fileExtension;
            String fullFilePath = uploadDir + newFilename;

            // Save file
            File destFile = new File(fullFilePath);
            file.transferTo(destFile);

            // Update user avatar path (save only filename in database)
            user.setHinhAnh(newFilename);
            nguoiDungRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật avatar thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. Thử lấy từ URL parameter (dành cho web redirect)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }
        
        // 2. Thử lấy từ header Authorization (dành cho AJAX)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 3. Thử lấy từ cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 4. Thử lấy từ session attribute
        String token = (String) request.getSession().getAttribute("jwtToken");
        if (token != null) {
            return token;
        }
        
        return null;
    }

    // Request DTOs
    public static class UpdateProfileRequest {
        private String tenNguoiDung;
        private String sdt;
        private String diaChi;

        // Getters and setters
        public String getTenNguoiDung() { return tenNguoiDung; }
        public void setTenNguoiDung(String tenNguoiDung) { this.tenNguoiDung = tenNguoiDung; }
        public String getSdt() { return sdt; }
        public void setSdt(String sdt) { this.sdt = sdt; }
        public String getDiaChi() { return diaChi; }
        public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}