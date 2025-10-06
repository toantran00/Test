package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.DatHang;

import java.util.List;

@Repository
public interface DatHangRepository extends JpaRepository<DatHang, Integer> {
    
    // Lấy 5 đơn hàng mới nhất
    List<DatHang> findTop5ByOrderByNgayDatDesc();
    
    // Đếm tổng số đơn hàng
    long count();
    
    // Tìm đơn hàng theo trạng thái
    List<DatHang> findByTrangThai(String trangThai);
    
    // Tìm đơn hàng theo người dùng
    List<DatHang> findByNguoiDung_MaNguoiDung(Integer maNguoiDung);
    
    // Thống kê đơn hàng theo tháng
    @Query("SELECT MONTH(d.ngayDat), COUNT(d) FROM DatHang d WHERE YEAR(d.ngayDat) = YEAR(CURRENT_DATE) GROUP BY MONTH(d.ngayDat) ORDER BY MONTH(d.ngayDat)")
    List<Object[]> getOrderStatsByMonth();
}