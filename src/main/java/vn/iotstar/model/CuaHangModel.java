package vn.iotstar.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuaHangModel {
    private Integer maCuaHang;
    
    @NotNull(message = "Chủ cửa hàng không được để trống")
    private Integer maNguoiDung;
    
    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(min = 2, max = 100, message = "Tên cửa hàng phải từ 2 đến 100 ký tự")
    private String tenCuaHang;
    
    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String moTa;
    
    private Date ngayTao;
    
    private String hinhAnh;
    
    // For response
    private String tenNguoiDung;
    private String emailNguoiDung;
    private String sdtNguoiDung;
    private Integer soLuongSanPham;
    private Integer soLuongKhuyenMai;
}