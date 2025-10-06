package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import vn.iotstar.entity.NguoiDung;
import vn.iotstar.model.ApiResponse;
import vn.iotstar.model.JwtResponse;
import vn.iotstar.model.LoginModel;
import vn.iotstar.model.NguoiDungModel;
import vn.iotstar.model.OtpRequest;
import vn.iotstar.model.OtpVerifyRequest;
import vn.iotstar.repository.NguoiDungRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.UserDetailsImpl;
import vn.iotstar.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginModel loginModel) {
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginModel.getEmail(), loginModel.getMatKhau())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT
            String jwt = jwtUtil.generateJwtToken(authentication);
            
            // Get user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Lấy thông tin người dùng từ database
            NguoiDung nguoiDung = nguoiDungRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            // Get role (remove ROLE_ prefix if exists)
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .orElse("USER");
            
            // Create response
            JwtResponse jwtResponse = JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .username(nguoiDung.getTenNguoiDung())
                .email(userDetails.getEmail())
                .role(role)
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(jwtResponse));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email hoặc mật khẩu không đúng"));
        }
    }

    // API gửi OTP
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {
        ApiResponse<String> result = otpService.generateAndSendOtp(otpRequest);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // API xác thực OTP và hoàn tất đăng ký
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        ApiResponse<String> result = otpService.verifyOtpAndRegister(
            request.getEmail(), 
            request.getOtpCode()
        );
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // API đăng ký cũ (giữ lại để tương thích ngược)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody NguoiDungModel signUpModel) {
        ApiResponse<String> result = authService.registerUser(signUpModel);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
 // Thêm phương thức này vào AuthController
    @GetMapping("/login-success")
    public RedirectView handleLoginSuccess(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Lấy thông tin user từ SecurityContext
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                
                // Lấy thông tin user từ database
                NguoiDung user = nguoiDungRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                // Phân quyền redirect - KHÔNG CẦN TOKEN TRONG URL
                if ("ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                    return new RedirectView("/admin/dashboard");
                } else {
                    return new RedirectView("/");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new RedirectView("/login?error=auth_failed");
    }
    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. Thử lấy từ URL parameter
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }
        
        // 2. Thử lấy từ header Authorization
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 3. Thử lấy từ session
        String token = (String) request.getSession().getAttribute("jwtToken");
        if (token != null) {
            return token;
        }
        
        // 4. Thử lấy từ cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}