package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "DanhMuc")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhMuc")
    private Integer maDanhMuc;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    @Column(name = "TenDanhMuc", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenDanhMuc;
    
    @OneToMany(mappedBy = "danhMuc", cascade = CascadeType.ALL)
    private List<SanPham> sanPhams;
}