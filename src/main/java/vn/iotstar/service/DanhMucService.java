package vn.iotstar.service;

import java.util.List;

import vn.iotstar.entity.DanhMuc;

public interface DanhMucService {
	List<DanhMuc> findAll();
    DanhMuc findByMaDanhMuc(Integer maDanhMuc);
    DanhMuc findByTenDanhMuc(String tenDanhMuc);
}
