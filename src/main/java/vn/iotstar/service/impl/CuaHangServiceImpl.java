package vn.iotstar.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.CuaHang;
import vn.iotstar.repository.CuaHangRepository;
import vn.iotstar.service.CuaHangService;

@Service
public class CuaHangServiceImpl implements CuaHangService {

    @Autowired
    private CuaHangRepository cuaHangRepository;

    @Override
    public List<CuaHang> findAll() {
        return cuaHangRepository.findAll();
    }

    @Override
    public CuaHang findByMaCuaHang(Integer maCuaHang) {
        return cuaHangRepository.findByMaCuaHang(maCuaHang);
    }

    @Override
    public CuaHang findByTenCuaHang(String tenCuaHang) {
        return cuaHangRepository.findByTenCuaHang(tenCuaHang);
    }
    
    public List<CuaHang> findTop3NewestStores() {
        return cuaHangRepository.findTop3ByOrderByNgayTaoDesc();
    }
    
    @Override
    public Page<CuaHang> findAll(Pageable pageable) {
        return cuaHangRepository.findAll(pageable);
    }
    
    @Override
    public Page<CuaHang> searchStores(String keyword, Pageable pageable) {
        return cuaHangRepository.findByTenCuaHangContainingIgnoreCaseOrDiaChiContainingIgnoreCase(
            keyword, keyword, pageable);
    }
    
    @Override
    public Page<CuaHang> findActiveStores(Pageable pageable) {
        return cuaHangRepository.findByTrangThaiTrue(pageable);
    }
}