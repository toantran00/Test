package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.NguoiDung;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<NguoiDung> findTop5ByOrderByMaNguoiDungDesc();
    
    // THÊM METHOD MỚI
    long count(); // Đếm tổng số người dùng
    
    // Tìm người dùng theo vai trò
    List<NguoiDung> findByVaiTro_MaVaiTro(String maVaiTro);
    
    // Tìm kiếm người dùng theo tên hoặc email
    @Query("SELECT n FROM NguoiDung n WHERE LOWER(n.tenNguoiDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NguoiDung> searchByTenNguoiDungOrEmail(String keyword);
}