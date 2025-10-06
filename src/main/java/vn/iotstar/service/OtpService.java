package vn.iotstar.service;

import vn.iotstar.model.ApiResponse;
import vn.iotstar.model.OtpRequest;

public interface OtpService {
    ApiResponse<String> generateAndSendOtp(OtpRequest otpRequest);
    ApiResponse<String> verifyOtpAndRegister(String email, String otpCode);
    void cleanupExpiredTokens();
}