package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.SanPham;
import vn.iotstar.repository.CuaHangRepository;
import vn.iotstar.service.CuaHangService;
import vn.iotstar.service.SanPhamService;

@Controller
public class CuaHangController {

    @Autowired
    private CuaHangService cuaHangService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private CuaHangRepository cuaHangRepository;
    
    private static final int PAGE_SIZE = 12;

    @GetMapping("/store/{MaCuaHang}")
    public String viewStoreDetail(
            @PathVariable("MaCuaHang") Integer maCuaHang,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "default") String sort,
            Model model) {
        
        CuaHang cuaHang = cuaHangService.findByMaCuaHang(maCuaHang);
        
        if (cuaHang == null) {
            model.addAttribute("errorMessage", "Không tìm thấy cửa hàng");
            return "web/error/404";
        }
        
        Sort sortOrder = getSortOrder(sort);
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, sortOrder);
        Page<SanPham> sanPhamPage = sanPhamService.findByCuaHang(cuaHang, pageable);
        long tongSanPham = sanPhamPage.getTotalElements();
        
        model.addAttribute("cuaHang", cuaHang);
        model.addAttribute("sanPhams", sanPhamPage.getContent());
        model.addAttribute("tongSanPham", tongSanPham);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sanPhamPage.getTotalPages());
        model.addAttribute("selectedSort", sort);
        
        return "web/storeDetail";
    }
    
    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "asc-name":
                return Sort.by(Sort.Direction.ASC, "tenSanPham");
            case "dsc-name":
                return Sort.by(Sort.Direction.DESC, "tenSanPham");
            case "asc-price":
                return Sort.by(Sort.Direction.ASC, "giaBan");
            case "dsc-price":
                return Sort.by(Sort.Direction.DESC, "giaBan");
            case "asc-like":
                return Sort.by(Sort.Direction.ASC, "luotThich");
            case "dsc-like":
                return Sort.by(Sort.Direction.DESC, "luotThich");
            case "default":
            default:
                return Sort.by(Sort.Direction.DESC, "ngayNhap"); // Mặc định: sản phẩm mới nhất
        }
    }
    
    
    @GetMapping("/stores")
    public String listAllStores(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "4") int size,
            @RequestParam(name = "search", required = false) String search,
            Model model) {
        
        try {
            // Tạo Pageable
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
            
            Page<CuaHang> storePage;
            
            // Xử lý tìm kiếm nếu có
            if (search != null && !search.trim().isEmpty()) {
                // Tìm kiếm cửa hàng theo tên hoặc địa chỉ
                storePage = cuaHangRepository.findByTenCuaHangContainingIgnoreCaseOrDiaChiContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
            } else {
                // Lấy tất cả cửa hàng có phân trang
                storePage = cuaHangService.findAll(pageable);
            }
            
            // Thêm attributes vào model
            model.addAttribute("cuaHangs", storePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", storePage.getTotalPages());
            model.addAttribute("totalElements", storePage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("searchKeyword", search != null ? search : "");
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách cửa hàng");
        }
        
        return "web/stores";
    }
}