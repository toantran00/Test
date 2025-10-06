package vn.iotstar.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.DanhGia;
import vn.iotstar.entity.DanhMuc;
import vn.iotstar.entity.SanPham;
import vn.iotstar.repository.DanhGiaRepository;
import vn.iotstar.repository.DanhMucRepository;
import vn.iotstar.service.CuaHangService;
import vn.iotstar.service.DanhGiaService;
import vn.iotstar.service.DanhMucService;
import vn.iotstar.service.SanPhamService;
import vn.iotstar.specification.SanPhamSpecification;

@Controller
public class HomeController {
    
    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucRepository danhMucRepository;
    
    @Autowired
    private DanhMucService danhMucService;
    
    @Autowired
    private CuaHangService cuaHangRepository;
    

    @Autowired
    private DanhGiaService danhGiaService;
    
    @Autowired
    private DanhGiaRepository danhGiaRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<DanhMuc> danhMucs = danhMucRepository.findAll();
        List<CuaHang> cuaHangs = cuaHangRepository.findTop3NewestStores();

        for (DanhMuc danhMuc : danhMucs) {
            List<SanPham> sanPhams = sanPhamService.findTop4ByDanhMucOrderByNgayNhapDesc(danhMuc);
            
            // Xử lý ảnh sản phẩm
            processProductImages(sanPhams);
            
            model.addAttribute("sanPhams_" + danhMuc.getMaDanhMuc(), sanPhams);
        }

        // Xử lý ảnh cửa hàng
        processStoreImages(cuaHangs);

        model.addAttribute("danhMucs", danhMucs);
        model.addAttribute("cuaHangs", cuaHangs);
        return "index"; 
    }
    
    @GetMapping("/view/{MaSanPham}/reviews")
    @ResponseBody
    public ResponseEntity<?> loadMoreReviews(
            @PathVariable("MaSanPham") Integer maSanPham,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        
        try {
            List<DanhGia> danhGias = danhGiaService.findDanhGiasWithUserBySanPham(maSanPham, page, size);
            Long totalDanhGias = danhGiaService.countDanhGiasBySanPham(maSanPham);
            
            // Xử lý hình ảnh người dùng - FIX QUAN TRỌNG
            for (DanhGia danhGia : danhGias) {
                if (danhGia.getNguoiDung() != null) {
                    // Xử lý hình ảnh người dùng
                    if (danhGia.getNguoiDung().getHinhAnh() != null && 
                        !danhGia.getNguoiDung().getHinhAnh().isEmpty()) {
                        // Thêm đường dẫn đầy đủ
                        if (!danhGia.getNguoiDung().getHinhAnh().startsWith("/uploads/")) {
                            danhGia.getNguoiDung().setHinhAnh("/uploads/images/" + danhGia.getNguoiDung().getHinhAnh());
                        }
                    }
                }
            }
            
            boolean hasNext = (page + 1) * size < totalDanhGias;
            
            Map<String, Object> response = new HashMap<>();
            response.put("danhGias", danhGias);
            response.put("hasNext", hasNext);
            response.put("currentPage", page);
            response.put("totalDanhGias", totalDanhGias);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi tải đánh giá"));
        }
    }

    @GetMapping("/view/{MaSanPham}")
    public String viewProductDetail(@PathVariable("MaSanPham") Integer maSanPham, Model model) {
        SanPham sanPham = sanPhamService.findByMaSanPham(maSanPham);
        
        if (sanPham == null) {
            return "redirect:/";
        }
        
        // Xử lý ảnh sản phẩm chính
        processProductImage(sanPham);

        // Lấy sản phẩm liên quan
        List<SanPham> relatedProducts = new ArrayList<>();
        if (sanPham.getLoaiSanPham() != null && !sanPham.getLoaiSanPham().isEmpty()) {
            relatedProducts = sanPhamService.findRelatedProductsByLoaiSanPhamExcludingCurrent(
                sanPham.getLoaiSanPham(), maSanPham);
            
            if (relatedProducts.size() > 4) {
                relatedProducts = relatedProducts.subList(0, 4);
            }
        }
        
        // Xử lý ảnh sản phẩm liên quan
        processProductImages(relatedProducts);

        // Lấy tên cửa hàng từ sản phẩm
        String storeName = "";
        if (sanPham.getCuaHang() != null && sanPham.getCuaHang().getTenCuaHang() != null) {
            storeName = sanPham.getCuaHang().getTenCuaHang();
        }

        // SỬ DỤNG NATIVE QUERY để lấy 2 đánh giá đầu tiên
        List<DanhGia> danhGias = danhGiaService.findDanhGiasWithUserBySanPham(maSanPham, 0, 2);
        Long totalDanhGias = danhGiaService.countDanhGiasBySanPham(maSanPham);
        
     // Xử lý hình ảnh người dùng
        for (DanhGia danhGia : danhGias) {
            if (danhGia.getNguoiDung() != null && 
                danhGia.getNguoiDung().getHinhAnh() != null && 
                !danhGia.getNguoiDung().getHinhAnh().isEmpty()) {
                
                if (!danhGia.getNguoiDung().getHinhAnh().startsWith("/uploads/")) {
                    danhGia.getNguoiDung().setHinhAnh("/uploads/images/" + danhGia.getNguoiDung().getHinhAnh());
                }
            }
        }

        Double averageRating = danhGiaService.getAverageRatingBySanPham(sanPham);
        Long totalReviews = danhGiaService.getCountBySanPham(sanPham);

        model.addAttribute("ItemProduct", sanPham);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("categoryName", sanPham.getDanhMuc().getTenDanhMuc());
        model.addAttribute("storeName", storeName);
        model.addAttribute("loaiSanPham", sanPham.getLoaiSanPham());
        
        // Thêm dữ liệu đánh giá vào model
        model.addAttribute("danhGias", danhGias);
        model.addAttribute("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("hasMoreReviews", totalDanhGias > 2); // Có thêm bình luận không
        model.addAttribute("productId", maSanPham); // Thêm productId cho JavaScript

        return "web/productDetail";
    }
    
    @GetMapping("/category/{tenDanhMuc}")
    public String productList(
            @PathVariable("tenDanhMuc") String tenDanhMuc,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) List<String> price,
            @RequestParam(required = false) List<String> store,
            @RequestParam(required = false) List<String> loai,
            @RequestParam(required = false) List<String> star,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        String decodedTenDanhMuc = decodeCategoryName(tenDanhMuc);
        DanhMuc danhMuc = danhMucService.findByTenDanhMuc(decodedTenDanhMuc);
        
        if (danhMuc == null) {
            return "redirect:/";
        }
        
        // Tạo Specification với filters
        Specification<SanPham> spec = SanPhamSpecification.filterProducts(
            danhMuc, price, store, loai, star, search
        );
        
        // Tạo Sort
        Sort sortObj = createSort(sort);
        
        // Tạo Pageable
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        
        // Query với Specification
        Page<SanPham> productPage = sanPhamService.findAll(spec, pageable);
        
        // Xử lý ảnh sản phẩm
        processProductImages(productPage.getContent());
        
        // Lấy danh sách stores và loại cho dropdown
        List<SanPham> allProducts = sanPhamService.findByDanhMuc(danhMuc);
        
        List<String> stores = allProducts.stream()
                .map(sp -> sp.getCuaHang().getTenCuaHang())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> loaiSanPhams = allProducts.stream()
                .map(SanPham::getLoaiSanPham)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        String bannerUrl = getBannerUrlByCategory(danhMuc.getMaDanhMuc());
         
        // Add attributes
        model.addAttribute("categoryName", danhMuc.getTenDanhMuc());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("stores", stores);
        model.addAttribute("loaiSanPhams", loaiSanPhams);
        model.addAttribute("bannerUrl", bannerUrl);
        
        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageUrl", "/category/" + tenDanhMuc);
        
        // Selected filters
        model.addAttribute("selectedPrices", price != null ? price : new ArrayList<>());
        model.addAttribute("selectedStores", store != null ? store : new ArrayList<>());
        model.addAttribute("selectedLoais", loai != null ? loai : new ArrayList<>());
        model.addAttribute("selectedStars", star != null ? star : new ArrayList<>());
        model.addAttribute("selectedSort", sort != null ? sort : "default");
        model.addAttribute("searchKeyword", search != null ? search : "");
        
        return "web/productList";
    }
    
    @GetMapping("/products")
    public String allProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) List<String> price,
            @RequestParam(required = false) List<String> store,
            @RequestParam(required = false) List<String> loai,
            @RequestParam(required = false) List<String> star,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        // Decode UTF-8 cho search
        if (search != null && !search.trim().isEmpty()) {
            try {
                search = java.net.URLDecoder.decode(search, "UTF-8");
                search = search.trim();
            } catch (Exception e) {
                // Nếu đã decoded, tiếp tục
            }
        }
        
        // Tạo Specification (null cho danhMuc = tìm tất cả)
        Specification<SanPham> spec = SanPhamSpecification.filterProducts(
            null, price, store, loai, star, search
        );
        
        // Tạo Sort
        Sort sortObj = createSort(sort);
        
        // Query với Specification
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        Page<SanPham> productPage = sanPhamService.findAll(spec, pageable);
        
        // Xử lý ảnh sản phẩm
        processProductImages(productPage.getContent());
        
        // Lấy danh sách stores và loại cho filter
        List<SanPham> allProducts = sanPhamService.findAll();
        
        List<String> stores = allProducts.stream()
                .map(sp -> sp.getCuaHang().getTenCuaHang())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> loaiSanPhams = allProducts.stream()
                .map(SanPham::getLoaiSanPham)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // Add attributes
        model.addAttribute("categoryName", "Tất cả sản phẩm");
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("stores", stores);
        model.addAttribute("loaiSanPhams", loaiSanPhams);
        model.addAttribute("bannerUrl", "/images/banner-default.jpg");
        
        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageUrl", "/products");
        
        // Selected filters
        model.addAttribute("selectedPrices", price != null ? price : new ArrayList<>());
        model.addAttribute("selectedStores", store != null ? store : new ArrayList<>());
        model.addAttribute("selectedLoais", loai != null ? loai : new ArrayList<>());
        model.addAttribute("selectedStars", star != null ? star : new ArrayList<>());
        model.addAttribute("selectedSort", sort != null ? sort : "default");
        model.addAttribute("searchKeyword", search != null ? search : "");
        
        return "web/products";
    } 
    // ========== CÁC PHƯƠNG THỨC XỬ LÝ ẢNH ==========
    
    /**
     * Xử lý ảnh cho một sản phẩm
     */
    private void processProductImage(SanPham sanPham) {
        if (sanPham != null) {
            if (sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isEmpty()) {
                sanPham.setHinhAnh("/images/" + sanPham.getHinhAnh());
            } else {
                sanPham.setHinhAnh("/images/default-product.jpg");
            }
        }
    }
    
    /**
     * Xử lý ảnh cho danh sách sản phẩm
     */
    private void processProductImages(List<SanPham> sanPhams) {
        if (sanPhams != null) {
            sanPhams.forEach(sp -> {
                if (sp.getHinhAnh() != null && !sp.getHinhAnh().isEmpty()) {
                    sp.setHinhAnh("/images/" + sp.getHinhAnh());
                } else {
                    sp.setHinhAnh("/images/default-product.jpg");
                }
            });
        }
    }
    
    /**
     * Xử lý ảnh cho danh sách cửa hàng
     */
    private void processStoreImages(List<CuaHang> cuaHangs) {
        if (cuaHangs != null) {
            cuaHangs.forEach(ch -> {
                if (ch.getHinhAnh() != null && !ch.getHinhAnh().isEmpty()) {
                    ch.setHinhAnh("/images/" + ch.getHinhAnh());
                } else {
                    ch.setHinhAnh("/images/store-default.jpg");
                }
            });
        }
    }
    
    // ========== CÁC PHƯƠNG THỨC HỖ TRỢ ==========
    
    private Sort createSort(String sortType) {
        if (sortType == null || sortType.equals("default")) {
            return Sort.by(Sort.Direction.DESC, "ngayNhap");
        }
        
        switch (sortType) {
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
            default:
                return Sort.by(Sort.Direction.DESC, "ngayNhap");
        }
    }

    private String decodeCategoryName(String encodedName) {
        switch (encodedName.toLowerCase()) {
            case "cho-canh":
                return "Chó cảnh";
            case "meo-canh":
                return "Mèo cảnh";
            case "phu-kien":
                return "Phụ kiện";
            default:
                return encodedName.replace("-", " ");
        }
    }

    private String getBannerUrlByCategory(Integer maDanhMuc) {
        switch (maDanhMuc) {
            case 1:
                return "/images/banner-cho-canh.jpg";
            case 2:
                return "/images/banner-meo-canh.jpg";
            case 3:
                return "/images/banner-phu-kien.jpg";
            default:
                return "/images/banner-default.jpg";
        }
    }
}