package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.DanhMuc;
import vn.iotstar.entity.SanPham;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer>, 
                                           JpaSpecificationExecutor<SanPham> {
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc ORDER BY s.ngayNhap DESC")
    List<SanPham> findTop4ByDanhMucOrderByNgayNhapDesc(DanhMuc danhMuc, Pageable pageable);
    
    SanPham findByMaSanPham(Integer maSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc AND s.maSanPham != :maSanPham")
    List<SanPham> findByDanhMucAndNotMaSanPham(DanhMuc danhMuc, Integer maSanPham);
    
    // THÊM METHOD MỚI: Lấy sản phẩm cùng LoaiSanPham (trừ sản phẩm hiện tại)
    @Query("SELECT s FROM SanPham s WHERE s.loaiSanPham = :loaiSanPham AND s.maSanPham != :maSanPham")
    List<SanPham> findByLoaiSanPhamAndNotMaSanPham(String loaiSanPham, Integer maSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc")
    List<SanPham> findByDanhMuc(DanhMuc danhMuc);
    
    Page<SanPham> findByDanhMuc(DanhMuc danhMuc, Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE s.cuaHang = :cuaHang")
    Page<SanPham> findByCuaHang(CuaHang cuaHang, Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.cuaHang = :cuaHang")
    List<SanPham> findByCuaHang(CuaHang cuaHang);
    List<SanPham> findTop5ByOrderByNgayNhapDesc();

    // THÊM METHOD MỚI
    long count(); // Đếm tổng số sản phẩm
    
    // Lấy tất cả sản phẩm (cho admin)
    List<SanPham> findAll();
}