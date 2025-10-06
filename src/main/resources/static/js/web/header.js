// Header JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    
    // Variables
    let mobileMenu = false;
    let showCategory = false;
    let selectedIndex = -1;
    let searchTimeout;

    // DOM Elements
    const searchBox = document.querySelector('.search-box');
    const modalSearchElement = document.querySelector('.modal-search');
    const toggleButton = document.querySelector('.toggle');
    const navMenu = document.querySelector('.nav_link ul');
    const categoriesButton = document.querySelector('.categories.d_flex');
    const modalCategory = document.querySelector('.modal-category');
    const searchSection = document.querySelector('.search');
    const searchInput = document.getElementById('search-input');
    const searchForm = searchInput?.closest('form');

    // Initialize
    checkAuthStatus();
    initSearch();
    initCategories();
    initMobileMenu();
    initActiveMenu();

    // Authentication Functions
    function checkAuthStatus() {
        const token = localStorage.getItem('jwtToken');
        const userData = localStorage.getItem('user');
        const authButtons = document.getElementById('authButtons');
        const userMenu = document.getElementById('userMenu');
        const userAccountLink = document.getElementById('userAccountLink');
        
        console.log('Auth check - Token:', !!token, 'UserData:', !!userData);
        
        if (token && userData) {
            // Đã đăng nhập
            authButtons.style.display = 'none';
            userMenu.style.display = 'flex';
            userAccountLink.style.display = 'block';
            
            try {
                const user = JSON.parse(userData);
                document.getElementById('userName').textContent = user.username || 'User';
                
                // Kiểm tra và hiển thị menu ADMIN nếu có quyền
                if (user.role === 'ADMIN' || (user.vaiTro && user.vaiTro.maVaiTro === 'ADMIN')) {
                    showAdminMenu();
                }
            } catch (e) {
                console.error('Error parsing user data:', e);
            }
            
            // Load cart quantity
            loadCartQuantity();
        } else {
            // Chưa đăng nhập
            authButtons.style.display = 'flex';
            userMenu.style.display = 'none';
            userAccountLink.style.display = 'none';
            hideAdminMenu();
        }
    }

    function showAdminMenu() {
        // Kiểm tra xem đã có menu admin chưa
        let adminMenu = document.querySelector('.admin-menu-item');
        
        if (!adminMenu) {
            // Tạo menu item cho admin
            adminMenu = document.createElement('li');
            adminMenu.className = 'admin-menu-item';   
            // Thêm vào trước menu user
            const navMenu = document.querySelector('.nav_link ul');
            const userMenu = document.querySelector('.user-menu-item');
            if (userMenu) {
                navMenu.insertBefore(adminMenu, userMenu);
            } else {
                navMenu.appendChild(adminMenu);
            }
            
            // Thêm style cho menu admin
            const style = document.createElement('style');
            style.textContent = `
                .admin-menu-item .nav_link_item {
                    color: #ff6b6b !important;
                    font-weight: bold;
                }
                .admin-menu-item .nav_link_item:hover {
                    color: #ff5252 !important;
                }
                .admin-menu-item .nav_link_item.active {
                    color: #ff5252 !important;
                }
                .admin-menu-item .nav-indicator {
                    background-color: #ff6b6b !important;
                }
            `;
            document.head.appendChild(style);
        }
        
        adminMenu.style.display = 'block';
    }

    function hideAdminMenu() {
        const adminMenu = document.querySelector('.admin-menu-item');
        if (adminMenu) {
            adminMenu.style.display = 'none';
        }
    }

    function loadCartQuantity() {
        const token = localStorage.getItem('jwtToken');
        if (!token) return;

        fetch('/api/cart/quantity', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('cartQty').textContent = data.data;
            }
        })
        .catch(error => {
            console.error('Error loading cart quantity:', error);
        });
    }

    // Active Menu Management
    function initActiveMenu() {
        // Get current page or set default
        const currentPath = window.location.pathname;
        let activeMenu = 'home'; // default
        
        // Determine active menu based on current path
        if (currentPath === '/' || currentPath === '/home') {
            activeMenu = 'home';
        } else if (currentPath.includes('/profile') || currentPath.includes('/user')) {
            activeMenu = 'user';
        } else if (currentPath.includes('/track-order')) {
            activeMenu = 'track-order';
        } else if (currentPath.includes('/admin')) {
            activeMenu = 'admin';
        }
        
        // Set active menu
        setActiveMenu(activeMenu);
        
        // Add click handlers for menu items
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                const menuType = this.getAttribute('data-menu');
                setActiveMenu(menuType);
                
                // For user account link, prevent default and use our function
                if (menuType === 'user') {
                    e.preventDefault();
                    goToProfile();
                }
            });
        });
    }

    function setActiveMenu(menuType) {
        // Remove active class from all menu items
        const navItems = document.querySelectorAll('.nav_link_item');
        const navIndicators = document.querySelectorAll('.nav-indicator');
        
        navItems.forEach(item => {
            item.classList.remove('active');
        });
        
        navIndicators.forEach(indicator => {
            indicator.style.transform = 'scaleX(0)';
        });
        
        // Add active class to current menu item
        const activeLink = document.querySelector(`.nav-link[data-menu="${menuType}"]`);
        if (activeLink) {
            const navItem = activeLink.querySelector('.nav_link_item');
            const indicator = activeLink.querySelector('.nav-indicator');
            
            if (navItem) {
                navItem.classList.add('active');
            }
            if (indicator) {
                indicator.style.transform = 'scaleX(1)';
            }
        }
    }

    // Global functions for onclick events
    window.logout = function() {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('user');
        // Xóa cookie JWT token
        document.cookie = 'jwtToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        window.location.href = '/';
    }

    window.goToProfile = function() {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            // Chưa đăng nhập, chuyển đến trang đăng nhập
            window.location.href = '/login';
            return;
        }

        // Đã đăng nhập, chuyển đến trang hồ sơ
        window.location.href = '/profile';
    }

    window.goToAdminDashboard = function() {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // Kiểm tra quyền ADMIN trước khi chuyển hướng
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                const user = JSON.parse(userData);
                if (user.role === 'ADMIN' || (user.vaiTro && user.vaiTro.maVaiTro === 'ADMIN')) {
                    window.location.href = '/admin/dashboard';
                } else {
                    alert('Bạn không có quyền truy cập trang quản trị');
                }
            } catch (e) {
                console.error('Error parsing user data:', e);
                window.location.href = '/login';
            }
        }
    }

    // Dropdown user menu
    document.addEventListener('click', function(e) {
        const userDropdown = document.querySelector('.user-dropdown');
        if (userDropdown && !userDropdown.contains(e.target)) {
            const dropdownContent = userDropdown.querySelector('.dropdown-content');
            dropdownContent.style.display = 'none';
        }
    });

    document.querySelector('.user-info')?.addEventListener('click', function(e) {
        e.preventDefault();
        const dropdownContent = this.parentElement.querySelector('.dropdown-content');
        dropdownContent.style.display = dropdownContent.style.display === 'block' ? 'none' : 'block';
    });

    // Search functionality
    function initSearch() {
        if (!searchInput || !modalSearchElement) return;

        // Prevent form submission when empty
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                const searchValue = searchInput.value.trim();
                if (!searchValue) {
                    e.preventDefault();
                }
            });
        }

        // Real-time search on input
        searchInput.addEventListener('input', function(e) {
            clearTimeout(searchTimeout);
            const searchValue = e.target.value.trim();
            
            if (searchValue.length >= 1) {
                searchTimeout = setTimeout(() => {
                    fetchSearchResults(searchValue);
                }, 300);
            } else {
                hideSearchModal();
            }
        });

        // Handle Enter key
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const searchValue = searchInput.value.trim();
                if (searchValue) {
                    hideSearchModal();
                    // Submit form or redirect
                    if (searchForm) {
                        searchForm.submit();
                    }
                }
            }

            // Keyboard navigation in search results
            if (modalSearchElement && !modalSearchElement.classList.contains('hide')) {
                const items = modalSearchElement.querySelectorAll('.modal-search-item');
                if (items.length === 0) return;

                switch(e.key) {
                    case 'ArrowDown':
                        e.preventDefault();
                        selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                        updateSelectedItem(items);
                        break;
                    case 'ArrowUp':
                        e.preventDefault();
                        selectedIndex = Math.max(selectedIndex - 1, -1);
                        updateSelectedItem(items);
                        break;
                    case 'Escape':
                        e.preventDefault();
                        hideSearchModal();
                        break;
                }
            }
        });

        // Show modal on focus if there's text
        searchInput.addEventListener('focus', function() {
            const searchValue = searchInput.value.trim();
            if (searchValue.length >= 1) {
                fetchSearchResults(searchValue);
            }
        });

        // Mouse events for search items
        modalSearchElement.addEventListener('mouseover', function(e) {
            const item = e.target.closest('.modal-search-item');
            if (item) {
                const items = modalSearchElement.querySelectorAll('.modal-search-item');
                selectedIndex = Array.from(items).indexOf(item);
                updateSelectedItem(items);
            }
        });

        modalSearchElement.addEventListener('click', function(e) {
            const item = e.target.closest('.modal-search-item');
            if (item && item.dataset.productId) {
                window.location.href = `/view/${item.dataset.productId}`;
            }
        });
    }

    // Fetch search results from API
    function fetchSearchResults(keyword) {
        fetch(`/api/search-suggestions?keyword=${encodeURIComponent(keyword)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                updateSearchModal(data, keyword);
            })
            .catch(error => {
                console.error('Search error:', error);
                showErrorState();
            });
    }

    // Update search modal with results
    function updateSearchModal(data, keyword) {
        if (!modalSearchElement) return;
        
        let html = '';

        // Search results section
        if (data.products && data.products.length > 0) {
            data.products.forEach(product => {
                html += `
                    <div class="modal-search-item" data-product-id="${product.maSanPham}">
                        <div class="product-info">
                            <img src="${product.hinhAnh || '/images/default-product.jpg'}" 
                                 alt="${escapeHtml(product.tenSanPham)}" 
                                 class="product-thumb">
                            <div class="product-details">
                                <span class="name">${escapeHtml(product.tenSanPham)}</span>
                                <span class="price">₫${formatPrice(product.giaBan)}</span>
                            </div>
                        </div>
                        <i class="fa-solid fa-arrow-right"></i>
                    </div>
                `;
            });
        } else {
            html += `
                <div class="no-results">
                    <i class="fa-solid fa-search"></i>
                    <span>Không tìm thấy sản phẩm cho "${escapeHtml(keyword)}"</span>
                </div>
            `;
        }

        // Recommended section
        if (data.recommended && data.recommended.length > 0) {
            html += `
                <div class="recommended-section">
                    <h3><i class="fa-solid fa-fire"></i> Sản phẩm gợi ý</h3>
                    <div class="recommended-container">
            `;
            
            data.recommended.forEach(item => {
                html += `
                    <div class="recommended-item" onclick="window.location.href='/view/${item.maSanPham}'">
                        <img src="${item.hinhAnh || '/images/default-product.jpg'}" 
                             alt="${escapeHtml(item.tenSanPham)}" 
                             class="recommended-image">
                        <div class="recommended-info">
                            <h4>${escapeHtml(item.tenSanPham)}</h4>
                            <p class="price">₫${formatPrice(item.giaBan)}</p>
                        </div>
                    </div>
                `;
            });
            
            html += `
                    </div>
                </div>
            `;
        }

        modalSearchElement.innerHTML = html;
        showSearchModal();
        selectedIndex = -1;
    }

    // Show search modal
    function showSearchModal() {
        if (modalSearchElement) {
            modalSearchElement.classList.remove('hide');
            modalSearchElement.classList.add('active');
        }
    }

    // Hide search modal
    function hideSearchModal() {
        if (modalSearchElement) {
            modalSearchElement.classList.add('hide');
            modalSearchElement.classList.remove('active');
        }
        selectedIndex = -1;
    }

    // Show error state
    function showErrorState() {
        if (!modalSearchElement) return;
        
        modalSearchElement.innerHTML = `
            <div class="error-state">
                <i class="fa-solid fa-exclamation-triangle"></i>
                <span>Đã có lỗi xảy ra khi tìm kiếm</span>
            </div>
        `;
        showSearchModal();
    }

    // Update selected item for keyboard navigation
    function updateSelectedItem(items) {
        items.forEach((item, index) => {
            item.classList.toggle('selected', index === selectedIndex);
            
            // Scroll into view if needed
            if (index === selectedIndex) {
                item.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
            }
        });
    }

    // Categories functionality
    function initCategories() {
        if (!categoriesButton || !modalCategory) return;

        categoriesButton.addEventListener('click', function(e) {
            e.stopPropagation();
            showCategory = !showCategory;
            modalCategory.classList.toggle('show', showCategory);
        });

        // Close categories when clicking outside
        document.addEventListener('click', function() {
            if (showCategory) {
                showCategory = false;
                modalCategory.classList.remove('show');
            }
        });

        // Prevent categories from closing when clicking inside
        modalCategory.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    // Mobile menu functionality
    function initMobileMenu() {
        if (!toggleButton) return;

        toggleButton.addEventListener('click', function() {
            mobileMenu = !mobileMenu;
            
            if (mobileMenu) {
                navMenu.classList.remove('link', 'f_flex');
                navMenu.classList.add('nav-link-mobileMenu');
                toggleButton.querySelector('i').classList.remove('fas', 'fa-bars', 'open');
                toggleButton.querySelector('i').classList.add('fa', 'fa-times', 'close', 'home-bth');
            } else {
                navMenu.classList.remove('nav-link-mobileMenu');
                navMenu.classList.add('link', 'f_flex');
                toggleButton.querySelector('i').classList.remove('fa', 'fa-times', 'close', 'home-bth');
                toggleButton.querySelector('i').classList.add('fas', 'fa-bars', 'open');
            }
        });

        // Close mobile menu when clicking on menu items
        const navLinks = document.querySelectorAll('.nav_link ul a');
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (mobileMenu) {
                    mobileMenu = false;
                    navMenu.classList.remove('nav-link-mobileMenu');
                    navMenu.classList.add('link', 'f_flex');
                    if (toggleButton) {
                        toggleButton.querySelector('i').classList.remove('fa', 'fa-times', 'close', 'home-bth');
                        toggleButton.querySelector('i').classList.add('fas', 'fa-bars', 'open');
                    }
                }
            });
        });
    }

    // Scroll Effect - Add active class to search bar
    window.addEventListener('scroll', function() {
        if (window.scrollY > 100) {
            searchSection.classList.add('active');
        } else {
            searchSection.classList.remove('active');
        }
    });

    // Close modal when clicking outside
    document.addEventListener('click', function(e) {
        if (modalSearchElement && 
            !modalSearchElement.classList.contains('hide') &&
            !e.target.closest('.search-box') &&
            !e.target.closest('.modal-search')) {
            hideSearchModal();
        }
    });

    // Utility functions
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatPrice(price) {
        if (!price) return '0';
        return new Intl.NumberFormat('vi-VN').format(price);
    }

    // Auto check auth status when page loads
    window.addEventListener('load', function() {
        checkAuthStatus();
    });

    // Export functions if needed
    window.headerFunctions = {
        showSearchModal,
        hideSearchModal,
        formatPrice,
        setActiveMenu,
        checkAuthStatus,
        goToAdminDashboard
    };
});