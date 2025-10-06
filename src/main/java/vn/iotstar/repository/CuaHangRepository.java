package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.CuaHang;

import java.util.List;

@Repository
public interface CuaHangRepository extends JpaRepository<CuaHang, Integer> {
    
    // Lấy tất cả các cửa hàng
    List<CuaHang> findAll();
    
    // Lấy cửa hàng theo mã
    CuaHang findByMaCuaHang(Integer maCuaHang);
    
    // Lấy cửa hàng theo tên
    CuaHang findByTenCuaHang(String tenCuaHang);
    
    List<CuaHang> findTop3ByOrderByNgayTaoDesc();
    
    Page<CuaHang> findAll(Pageable pageable);
    
    // Tìm kiếm cửa hàng theo tên hoặc địa chỉ
    Page<CuaHang> findByTenCuaHangContainingIgnoreCaseOrDiaChiContainingIgnoreCase(
        String tenCuaHang, String diaChi, Pageable pageable);
    
    // Lấy cửa hàng đang hoạt động
    Page<CuaHang> findByTrangThaiTrue(Pageable pageable);
    
    // Lấy cửa hàng có đánh giá cao
    Page<CuaHang> findByTrangThaiTrueOrderByDanhGiaTrungBinhDesc(Pageable pageable);
    List<CuaHang> findTop5ByOrderByNgayTaoDesc();
    
    // THÊM METHOD MỚI
    long count(); // Đếm tổng số cửa hàng
}