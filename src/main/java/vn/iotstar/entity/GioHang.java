package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "GioHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGioHang")
    private Integer maGioHang;
    
    @NotNull(message = "Người dùng không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayTao")
    @Builder.Default
    private Date ngayTao = new Date();
    
    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL)
    private List<MatHang> matHangs;
}