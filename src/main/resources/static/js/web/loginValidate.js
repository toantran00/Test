// Clear error khi người dùng nhập
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('input', function() {
        clearError(this.id);
    });
    
    input.addEventListener('focus', function() {
        this.parentElement.classList.add('focused');
    });
    
    input.addEventListener('blur', function() {
        if (!this.value) {
            this.parentElement.classList.remove('focused');
        }
    });
});

function clearError(fieldId) {
    const input = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    if (input) {
        input.classList.remove('error');
        input.parentElement.classList.remove('error');
    }
    if (errorDiv) {
        errorDiv.textContent = '';
        errorDiv.style.display = 'none';
    }
}

function showError(fieldId, message) {
    const input = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    if (input) {
        input.classList.add('error');
        input.parentElement.classList.add('error');
    }
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

function validateForm() {
    let isValid = true;
    
    // Clear all errors
    document.querySelectorAll('.error-text').forEach(el => {
        el.style.display = 'none';
        el.textContent = '';
    });
    document.querySelectorAll('input').forEach(el => {
        el.classList.remove('error');
        el.parentElement.classList.remove('error');
    });
    
    // Validate Email
    const email = document.getElementById('email').value.trim();
    if (!email) {
        showError('email', 'Email không được để trống');
        isValid = false;
    } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showError('email', 'Email không đúng định dạng');
            isValid = false;
        }
    }
    
    // Validate Mật khẩu
    const matKhau = document.getElementById('matKhau').value;
    if (!matKhau) {
        showError('matKhau', 'Mật khẩu không được để trống');
        isValid = false;
    } else if (matKhau.length < 6) {
        showError('matKhau', 'Mật khẩu phải có ít nhất 6 ký tự');
        isValid = false;
    }
    
    return isValid;
}

function togglePassword() {
    const passwordInput = document.getElementById('matKhau');
    const toggleIcon = document.querySelector('.toggle-password i');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.className = 'fas fa-eye-slash';
    } else {
        passwordInput.type = 'password';
        toggleIcon.className = 'fas fa-eye';
    }
}
// Thêm vào file loginValidate.js hoặc trong thẻ script của login.html
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const logoutParam = urlParams.get('logout');
    
    if (logoutParam === 'true') {
        showLogoutSuccess();
    }
});

function showLogoutSuccess() {
    const errorDiv = document.getElementById('errorMessage');
    errorDiv.innerHTML = '<i class="fas fa-check-circle"></i> Đăng xuất thành công!';
    errorDiv.style.display = 'block';
    errorDiv.style.backgroundColor = '#d4edda';
    errorDiv.style.color = '#155724';
    errorDiv.style.border = '1px solid #c3e6cb';
    
    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    if (!validateForm()) {
        return;
    }

    const email = document.getElementById('email').value;
    const matKhau = document.getElementById('matKhau').value;
    const errorMessage = document.getElementById('errorMessage');
    
    // Reset error message
    errorMessage.style.display = 'none';
    errorMessage.textContent = '';
    
    // Hiệu ứng loading
    const submitBtn = document.querySelector('.btn-login');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
    submitBtn.disabled = true;

    fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: email,
            matKhau: matKhau
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Lưu token vào localStorage
            localStorage.setItem('jwtToken', data.data.token);
            localStorage.setItem('user', JSON.stringify(data.data));
            
            // Lưu token vào cookie để server có thể truy cập
            document.cookie = `jwtToken=${data.data.token}; path=/; max-age=86400; SameSite=Lax`;
            
            // Hiệu ứng thành công
            submitBtn.innerHTML = '<i class="fas fa-check"></i> Đăng nhập thành công!';
            
            // QUAN TRỌNG: Kiểm tra role và chuyển hướng
            setTimeout(() => {
                if (data.data.role === 'ADMIN') {
                    // Nếu là ADMIN, chuyển đến dashboard admin
                    window.location.href = '/admin/dashboard';
                } else {
                    // Nếu là USER thông thường, chuyển đến trang chủ
                    window.location.href = '/';
                }
            }, 1000);
            
        } else {
            errorMessage.textContent = data.message;
            errorMessage.style.display = 'block';
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    })
    .catch(error => {
        errorMessage.textContent = 'Đã xảy ra lỗi khi đăng nhập. Vui lòng thử lại.';
        errorMessage.style.display = 'block';
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
        console.error('Error:', error);
    });
});

// Kiểm tra nếu đã đăng nhập thì chuyển hướng
if (localStorage.getItem('jwtToken')) {
    // Có thể thêm logic kiểm tra role ở đây nếu muốn
    window.location.href = '/';
}