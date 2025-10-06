package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.util.JwtUtil;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private CuaHangRepository cuaHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DatHangRepository datHangRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        try {
            // Sử dụng SecurityContext để lấy thông tin authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof AnonymousAuthenticationToken) {
                return "redirect:/login?error=unauthorized";
            }
            
            // Lấy thông tin user từ principal
            String email = authentication.getName();
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Kiểm tra role ADMIN
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy dữ liệu thống kê từ database
            long totalUsers = nguoiDungRepository.count();
            long totalStores = cuaHangRepository.count();
            long totalProducts = sanPhamRepository.count();
            long totalOrders = datHangRepository.count();
            
            // Lấy danh sách mới nhất
            List<NguoiDung> recentUsers = nguoiDungRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "maNguoiDung"))).getContent();
            List<CuaHang> recentStores = cuaHangRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "ngayTao"))).getContent();
            List<SanPham> recentProducts = sanPhamRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "ngayNhap"))).getContent();
            List<DatHang> recentOrders = datHangRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "ngayDat"))).getContent();

            // Thêm dữ liệu vào model
            model.addAttribute("user", user);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalStores", totalStores);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("recentUsers", recentUsers);
            model.addAttribute("recentStores", recentStores);
            model.addAttribute("recentProducts", recentProducts);
            model.addAttribute("recentOrders", recentOrders);
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=system_error";
        }
    }

    @GetMapping("/users")
    public String showUserManagement(HttpServletRequest request, Model model) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return "redirect:/login?error=unauthorized";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy tất cả người dùng từ database
            List<NguoiDung> users = nguoiDungRepository.findAll();
            
            model.addAttribute("user", user);
            model.addAttribute("users", users);
            
            return "admin/users";
            
        } catch (Exception e) {
            System.err.println("Error loading user management: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
    }

    @GetMapping("/stores")
    public String showStoreManagement(HttpServletRequest request, Model model) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return "redirect:/login?error=unauthorized";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy tất cả cửa hàng từ database
            List<CuaHang> stores = cuaHangRepository.findAll();
            
            model.addAttribute("user", user);
            model.addAttribute("stores", stores);
            
            return "admin/stores";
            
        } catch (Exception e) {
            System.err.println("Error loading store management: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
    }

    @GetMapping("/products")
    public String showProductManagement(HttpServletRequest request, Model model) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return "redirect:/login?error=unauthorized";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy tất cả sản phẩm từ database
            List<SanPham> products = sanPhamRepository.findAll();
            
            model.addAttribute("user", user);
            model.addAttribute("products", products);
            
            return "admin/products";
            
        } catch (Exception e) {
            System.err.println("Error loading product management: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
    }

    @GetMapping("/categories")
    public String showCategoryManagement(HttpServletRequest request, Model model) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return "redirect:/login?error=unauthorized";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy tất cả danh mục từ database
            List<DanhMuc> categories = danhMucRepository.findAll();
            
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            
            return "admin/categories";
            
        } catch (Exception e) {
            System.err.println("Error loading category management: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
    }

    @GetMapping("/orders")
    public String showOrderManagement(HttpServletRequest request, Model model) {
        try {
            String token = extractJwtFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                return "redirect:/login?error=unauthorized";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!"ADMIN".equals(user.getVaiTro().getMaVaiTro())) {
                return "redirect:/login?error=access_denied";
            }

            // Lấy tất cả đơn hàng từ database
            List<DatHang> orders = datHangRepository.findAll();
            
            model.addAttribute("user", user);
            model.addAttribute("orders", orders);
            
            return "admin/orders";
            
        } catch (Exception e) {
            System.err.println("Error loading order management: " + e.getMessage());
            return "redirect:/login?error=system_error";
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }
        
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        String token = (String) request.getSession().getAttribute("jwtToken");
        if (token != null) {
            return token;
        }
        
        return null;
    }
}
