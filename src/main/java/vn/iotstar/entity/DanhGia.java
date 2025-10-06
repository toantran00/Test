package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Date;

@Entity
@Table(name = "DanhGia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhGia")
    private Integer maDanhGia;
    
    @NotNull(message = "Sản phẩm không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaSanPham", nullable = false)
    private SanPham sanPham;
    
    @NotNull(message = "Người dùng không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;
    
    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao phải từ 1 đến 5")
    @Max(value = 5, message = "Số sao phải từ 1 đến 5")
    @Column(name = "SoSao")
    private Integer soSao;
    
    @NotBlank(message = "Bình luận không được để trống")
    @Size(min = 10, max = 1000, message = "Bình luận phải từ 10 đến 1000 ký tự")
    @Column(name = "BinhLuan", columnDefinition = "NVARCHAR(MAX)")
    private String binhLuan;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayDanhGia")
    @Builder.Default
    private Date ngayDanhGia = new Date();
    
    @Column(name = "Anh_Video", columnDefinition = "NVARCHAR(255)")
    private String anhVideo;
}