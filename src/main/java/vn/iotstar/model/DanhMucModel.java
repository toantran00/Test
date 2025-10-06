package vn.iotstar.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucModel {
    private Integer maDanhMuc;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String tenDanhMuc;
}