package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.DanhMuc;
import vn.iotstar.repository.DanhMucRepository;
import vn.iotstar.service.DanhMucService;

import java.util.List;

@Service
public class DanhMucServiceImpl implements DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Override
    public List<DanhMuc> findAll() {
        return danhMucRepository.findAll();
    }

    @Override
    public DanhMuc findByMaDanhMuc(Integer maDanhMuc) {
        return danhMucRepository.findByMaDanhMuc(maDanhMuc);
    }
    
    @Override
    public DanhMuc findByTenDanhMuc(String tenDanhMuc) {
        return danhMucRepository.findByTenDanhMuc(tenDanhMuc);
    }
}