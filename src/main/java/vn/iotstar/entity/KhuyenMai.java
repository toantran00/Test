package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "KhuyenMai")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKhuyenMai")
    private Integer maKhuyenMai;
    
    @NotNull(message = "Cửa hàng không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaCuaHang", nullable = false)
    private CuaHang cuaHang;
    
    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(min = 3, max = 50, message = "Mã giảm giá phải từ 3 đến 50 ký tự")
    @Column(name = "MaGiamGia", nullable = false, columnDefinition = "VARCHAR(50)")
    private String maGiamGia;
    
    @NotNull(message = "Discount không được để trống")
    @DecimalMin(value = "0.0", message = "Discount không được âm")
    @DecimalMax(value = "100.0", message = "Discount không được vượt quá 100")
    @Column(name = "Discount", nullable = false, precision = 5, scale = 2)
    private BigDecimal discount;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là hiện tại hoặc tương lai")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayBatDau", nullable = false)
    private Date ngayBatDau;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là tương lai")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayKetThuc", nullable = false)
    private Date ngayKetThuc;
    
    @NotNull(message = "Số lượng mã giảm giá không được để trống")
    @Min(value = 0, message = "Số lượng mã giảm giá không được âm")
    @Column(name = "SoLuongMaGiamGia")
    private Integer soLuongMaGiamGia;
}