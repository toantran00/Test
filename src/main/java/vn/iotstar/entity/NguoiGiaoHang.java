package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "NguoiGiaoHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiGiaoHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiGiaoHang")
    private Integer maNguoiGiaoHang;
    
    @NotBlank(message = "Tên người giao hàng không được để trống")
    @Size(min = 2, max = 100, message = "Tên người giao hàng phải từ 2 đến 100 ký tự")
    @Column(name = "TenNguoiGiaoHang", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenNguoiGiaoHang;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại không đúng định dạng")
    @Column(name = "SDTNguoiGiaoHang", columnDefinition = "VARCHAR(20)")
    private String sdtNguoiGiaoHang;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 10, max = 255, message = "Địa chỉ phải từ 10 đến 255 ký tự")
    @Column(name = "DiaChiNguoiGiaoHang", columnDefinition = "NVARCHAR(255)")
    private String diaChiNguoiGiaoHang;
    
    @OneToMany(mappedBy = "nguoiGiaoHang", cascade = CascadeType.ALL)
    private List<VanChuyen> vanChuyens;
}