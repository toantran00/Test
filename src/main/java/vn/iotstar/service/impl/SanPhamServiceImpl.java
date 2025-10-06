package vn.iotstar.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.SanPham;
import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.DanhMuc;
import vn.iotstar.repository.SanPhamRepository;
import vn.iotstar.service.SanPhamService;

@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Override
    public List<SanPham> findTop4ByDanhMucOrderByNgayNhapDesc(DanhMuc danhMuc) {
        Pageable pageable = PageRequest.of(0, 4);  
        return sanPhamRepository.findTop4ByDanhMucOrderByNgayNhapDesc(danhMuc, pageable);
    }

    @Override
    public SanPham findByMaSanPham(Integer maSanPham) {
        return sanPhamRepository.findByMaSanPham(maSanPham); 
    }

    @Override
    public List<SanPham> findRelatedProductsByCategoryExcludingCurrent(DanhMuc danhMuc, Integer maSanPham) {
        return sanPhamRepository.findByDanhMucAndNotMaSanPham(danhMuc, maSanPham);
    }
    
    // THÊM METHOD MỚI: Lấy sản phẩm cùng LoaiSanPham
    @Override
    public List<SanPham> findRelatedProductsByLoaiSanPhamExcludingCurrent(String loaiSanPham, Integer maSanPham) {
        return sanPhamRepository.findByLoaiSanPhamAndNotMaSanPham(loaiSanPham, maSanPham);
    }
    
    @Override
    public List<SanPham> findByDanhMuc(DanhMuc danhMuc) {
        return sanPhamRepository.findByDanhMuc(danhMuc);
    }

    @Override
    public Page<SanPham> findByDanhMuc(DanhMuc danhMuc, Pageable pageable) {
        return sanPhamRepository.findByDanhMuc(danhMuc, pageable);
    }

    @Override
    public Page<SanPham> findAll(Specification<SanPham> spec, Pageable pageable) {
        return sanPhamRepository.findAll(spec, pageable);
    }
    
    @Override
    public List<SanPham> findAll() {
        return sanPhamRepository.findAll();
    }
    
    @Override
    public Page<SanPham> findByCuaHang(CuaHang cuaHang, Pageable pageable) {
        return sanPhamRepository.findByCuaHang(cuaHang, pageable);
    }

    @Override
    public List<SanPham> findByCuaHang(CuaHang cuaHang) {
        return sanPhamRepository.findByCuaHang(cuaHang);
    }
}