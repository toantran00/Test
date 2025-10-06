package vn.iotstar.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.iotstar.entity.CuaHang;

public interface CuaHangService {
    // Lấy tất cả các cửa hàng
    List<CuaHang> findAll();
    
    // Lấy cửa hàng theo mã
    CuaHang findByMaCuaHang(Integer maCuaHang);
    
    // Lấy cửa hàng theo tên
    CuaHang findByTenCuaHang(String tenCuaHang);
     
    List<CuaHang> findTop3NewestStores();
    
    Page<CuaHang> findAll(Pageable pageable);
    
    // Tìm kiếm cửa hàng
    Page<CuaHang> searchStores(String keyword, Pageable pageable);
    
    // Lấy cửa hàng đang hoạt động
    Page<CuaHang> findActiveStores(Pageable pageable);
}