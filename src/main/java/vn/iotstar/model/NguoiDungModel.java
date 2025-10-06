package vn.iotstar.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDungModel {
    private Integer maNguoiDung;
    
    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 2, max = 100, message = "Tên người dùng phải từ 2 đến 100 ký tự")
    private String tenNguoiDung;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;
    
    @Pattern(regexp = "^(|\\+?[0-9]{10,15})$", message = "Số điện thoại không đúng định dạng")
    private String sdt;
    
    private String diaChi;
    
    @NotBlank(message = "Vai trò không được để trống")
    private String maVaiTro;
    
    private String trangThai;
    
    // For response
    private String tenVaiTro;
}