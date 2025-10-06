package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.DanhMuc;

import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {

    // Lấy tất cả các danh mục
    List<DanhMuc> findAll();

    // Lấy danh mục theo mã danh mục
    DanhMuc findByMaDanhMuc(Integer maDanhMuc);
    
    DanhMuc findByTenDanhMuc(String tenDanhMuc);
    
    // THÊM METHOD MỚI
    long count(); // Đếm tổng số danh mục
}