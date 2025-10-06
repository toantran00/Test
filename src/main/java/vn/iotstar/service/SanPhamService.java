package vn.iotstar.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import vn.iotstar.entity.SanPham;
import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.DanhMuc;

public interface SanPhamService {
    List<SanPham> findTop4ByDanhMucOrderByNgayNhapDesc(DanhMuc danhMuc);
    SanPham findByMaSanPham(Integer maSanPham);
    List<SanPham> findRelatedProductsByCategoryExcludingCurrent(DanhMuc danhMuc, Integer maSanPham);
    
    // THÊM METHOD MỚI: Lấy sản phẩm cùng LoaiSanPham
    List<SanPham> findRelatedProductsByLoaiSanPhamExcludingCurrent(String loaiSanPham, Integer maSanPham);
    
    List<SanPham> findByDanhMuc(DanhMuc danhMuc);
    Page<SanPham> findByDanhMuc(DanhMuc danhMuc, Pageable pageable);
    
    // Method cho Specification
    Page<SanPham> findAll(Specification<SanPham> spec, Pageable pageable);
    
    List<SanPham> findAll();
    
    Page<SanPham> findByCuaHang(CuaHang cuaHang, Pageable pageable);
    List<SanPham> findByCuaHang(CuaHang cuaHang);
}