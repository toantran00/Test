package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "DatHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDatHang")
    private Integer maDatHang;
    
    @NotNull(message = "Người dùng không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayDat")
    @Builder.Default
    private Date ngayDat = new Date();
    
    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền không được âm")
    @Column(name = "TongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;
    
    @NotBlank(message = "Trạng thái không được để trống")
    @Column(name = "TrangThai", columnDefinition = "NVARCHAR(50)")
    @Builder.Default
    private String trangThai = "New";
    
    @OneToMany(mappedBy = "datHang", cascade = CascadeType.ALL)
    private List<DatHangChiTiet> datHangChiTiets;
    
    @OneToMany(mappedBy = "datHang", cascade = CascadeType.ALL)
    private List<VanChuyen> vanChuyens;
    
    @OneToMany(mappedBy = "datHang", cascade = CascadeType.ALL)
    private List<ThanhToan> thanhToans;
}