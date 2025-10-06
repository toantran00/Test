package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.NguoiDung;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.VaiTro;
import vn.iotstar.model.ApiResponse;
import vn.iotstar.model.OtpRequest;
import vn.iotstar.repository.NguoiDungRepository;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.VaiTroRepository;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public ApiResponse<String> generateAndSendOtp(OtpRequest otpRequest) {
        // Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.existsByEmail(otpRequest.getEmail())) {
            return ApiResponse.error("Email đã được sử dụng");
        }

        try {
            // Xóa OTP cũ nếu có
            otpTokenRepository.deleteByEmail(otpRequest.getEmail());

            // Tạo mã OTP ngẫu nhiên
            String otpCode = generateOtpCode();

            // Tạo token mới
            OtpToken otpToken = OtpToken.builder()
                    .email(otpRequest.getEmail())
                    .otpCode(otpCode)
                    .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .tenNguoiDung(otpRequest.getTenNguoiDung())
                    .matKhau(passwordEncoder.encode(otpRequest.getMatKhau()))
                    .sdt(otpRequest.getSdt())
                    .diaChi(otpRequest.getDiaChi())
                    .isUsed(false)
                    .attemptCount(0)
                    .build();

            otpTokenRepository.save(otpToken);

            // Gửi email
            emailService.sendOtpEmail(
                    otpRequest.getEmail(),
                    otpCode,
                    otpRequest.getTenNguoiDung()
            );

            return ApiResponse.success("Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");

        } catch (Exception e) {
            return ApiResponse.error("Không thể gửi mã OTP. Vui lòng thử lại sau.");
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyOtpAndRegister(String email, String otpCode) {
        // Tìm OTP token
        Optional<OtpToken> optionalToken = otpTokenRepository
                .findByEmailAndIsUsedFalse(email);

        if (optionalToken.isEmpty()) {
            return ApiResponse.error("Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại.");
        }

        OtpToken otpToken = optionalToken.get();

        // Kiểm tra số lần thử
        if (!otpToken.canAttempt()) {
            otpTokenRepository.delete(otpToken);
            return ApiResponse.error("Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã OTP.");
        }

        // Kiểm tra OTP hết hạn
        if (otpToken.isExpired()) {
            otpTokenRepository.delete(otpToken);
            return ApiResponse.error("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }

        // Kiểm tra mã OTP
        if (!otpToken.getOtpCode().equals(otpCode)) {
            otpToken.incrementAttempt();
            otpTokenRepository.save(otpToken);
            int remainingAttempts = 5 - otpToken.getAttemptCount();
            return ApiResponse.error("Mã OTP không chính xác. Bạn còn " + remainingAttempts + " lần thử.");
        }

        try {
            // Tạo người dùng mới
            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setTenNguoiDung(otpToken.getTenNguoiDung());
            nguoiDung.setEmail(otpToken.getEmail());
            nguoiDung.setMatKhau(otpToken.getMatKhau()); // Đã được mã hóa
            nguoiDung.setSdt(otpToken.getSdt());
            nguoiDung.setDiaChi(otpToken.getDiaChi());
            nguoiDung.setTrangThai("Hoạt động");

            // Gán vai trò USER
            Optional<VaiTro> userRole = vaiTroRepository.findById("USER");
            if (userRole.isEmpty()) {
                return ApiResponse.error("Vai trò mặc định không tồn tại");
            }
            nguoiDung.setVaiTro(userRole.get());

            // Lưu người dùng
            nguoiDungRepository.save(nguoiDung);

            // Đánh dấu OTP đã sử dụng
            otpToken.setIsUsed(true);
            otpTokenRepository.save(otpToken);

            // Gửi email chào mừng
            try {
                emailService.sendWelcomeEmail(nguoiDung.getEmail(), nguoiDung.getTenNguoiDung());
            } catch (Exception e) {
                // Không làm gián đoạn quá trình đăng ký nếu email chào mừng thất bại
            }

            return ApiResponse.success("Đăng ký tài khoản thành công!");

        } catch (Exception e) {
            return ApiResponse.error("Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại.");
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}