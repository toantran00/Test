// Global variables
let currentUser = null;
let isEditMode = false;

// Load user profile when page loads
document.addEventListener('DOMContentLoaded', function() {
	loadUserProfile();
});

// Load user profile from API
async function loadUserProfile() {
	const token = localStorage.getItem('jwtToken');

	// Show loading state
	document.getElementById('loadingState').style.display = 'block';
	document.getElementById('errorState').style.display = 'none';
	document.getElementById('profileContent').style.display = 'none';

	if (!token) {
		showError('Vui lòng đăng nhập để xem trang cá nhân');
		setTimeout(() => {
			window.location.href = '/login';
		}, 2000);
		return;
	}

	try {
		const response = await fetch('/profile/api/user/profile', {
			headers: {
				'Authorization': 'Bearer ' + token,
				'Content-Type': 'application/json'
			}
		});

		if (response.status === 401) {
			localStorage.removeItem('jwtToken');
			localStorage.removeItem('user');
			showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
			setTimeout(() => {
				window.location.href = '/login';
			}, 3000);
			return;
		}

		const result = await response.json();

		if (result.success) {
			currentUser = result.data;
			displayUserProfile(currentUser);
		} else {
			showError('Lỗi: ' + result.message);
		}

	} catch (error) {
		showError('Lỗi kết nối: ' + error.message);
	}
}

// Display user profile
function displayUserProfile(user) {
	// Determine status badge color
	const statusColor = user.trangThai === 'Hoạt động' ? '#28a745' :
		user.trangThai === 'Tạm khóa' ? '#ffc107' : '#dc3545';

	// Construct avatar image path
	const avatarPath = user.hinhAnh ? `/uploads/images/${user.hinhAnh}` : '/images/default-avatar.jpg';

	const profileHTML = `
                <div class="profile-content">
                    <!-- Sidebar -->
                    <div class="profile-sidebar">
                        <div class="user-avatar">
                            <img src="${avatarPath}" 
                                 alt="Avatar" class="avatar-image" id="avatarPreview">
                            <div class="avatar-upload">
                                <label for="avatarInput" class="avatar-upload-label">
                                    <i class="fas fa-camera"></i> Đổi ảnh
                                </label>
                                <input type="file" id="avatarInput" accept="image/*" style="display: none;">
                            </div>
                        </div>
                        
                        <div class="user-info-sidebar">
                            <h3>${user.tenNguoiDung || 'Chưa có tên'}</h3>
                            <p><i class="fas fa-envelope"></i> ${user.email}</p>
                            <p><i class="fas fa-phone"></i> ${user.sdt || 'Chưa cập nhật'}</p>
                            <div class="user-role">${user.vaiTro?.tenVaiTro || 'USER'}</div>
                            <div class="user-status" style="background-color: ${statusColor}">
                                <i class="fas fa-circle"></i> ${user.trangThai || 'Hoạt động'}
                            </div>
                        </div>

                        <div class="profile-nav">
                            <div class="nav-item active" data-tab="profile">
                                <i class="fas fa-user"></i>
                                <span>Thông tin cá nhân</span>
                            </div>
                            <div class="nav-item" data-tab="security">
                                <i class="fas fa-shield-alt"></i>
                                <span>Đổi mật khẩu</span>
                            </div>
                        </div>
                    </div>

                    <!-- Main Content -->
                    <div class="profile-main">
                        <!-- Profile Tab -->
                        <div class="profile-tab active" id="profileTab">
                            <h2 class="section-title">Thông tin cá nhân</h2>
                            <form id="profileForm">
                                <div class="form-grid">
                                    <div class="form-group">
                                        <label class="form-label">Họ và tên *</label>
                                        <input type="text" class="form-input" id="tenNguoiDung" name="tenNguoiDung" 
                                               value="${user.tenNguoiDung || ''}" disabled required>
                                        <div class="form-error" id="tenNguoiDungError">Vui lòng nhập họ và tên</div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="form-label">Email *</label>
                                        <input type="email" class="form-input" value="${user.email}" readonly>
                                        <small style="color: #666; font-size: 12px;">Email không thể thay đổi</small>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="form-label">Số điện thoại</label>
                                        <input type="tel" class="form-input" id="sdt" name="sdt" 
                                               value="${user.sdt || ''}" 
                                               pattern="^0[0-9]{9}$"
                                               placeholder="Ví dụ: 0123456789" disabled>
                                        <div class="form-error" id="sdtError">Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số</div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="form-label">Vai trò</label>
                                        <input type="text" class="form-input" 
                                               value="${user.vaiTro?.tenVaiTro || 'USER'}" readonly>
                                        <small style="color: #666; font-size: 12px;">Vai trò được quản lý bởi hệ thống</small>
                                    </div>

                                    <div class="form-group full-width">
                                        <label class="form-label">Địa chỉ</label>
                                        <input type="text" class="form-input" id="diaChi" name="diaChi" 
                                               value="${user.diaChi || ''}" 
                                               placeholder="Nhập địa chỉ của bạn" disabled>
                                    </div>

                                    <div class="form-group full-width">
                                        <label class="form-label">Trạng thái tài khoản</label>
                                        <input type="text" class="form-input" 
                                               value="${user.trangThai || 'Hoạt động'}" readonly>
                                        <small style="color: #666; font-size: 12px;">Trạng thái được quản lý bởi hệ thống</small>
                                    </div>
                                </div>
                                
                                <div class="form-actions">
                                    <button type="button" class="btn btn-primary" id="editProfileBtn" onclick="toggleEditMode()">
                                        <i class="fas fa-edit"></i> Cập nhật thông tin
                                    </button>
                                    <button type="submit" class="btn btn-success" id="saveProfileBtn" style="display: none;">
                                        <i class="fas fa-save"></i> Lưu
                                    </button>
                                    <button type="button" class="btn btn-secondary" id="cancelProfileBtn" style="display: none;" onclick="cancelEdit()">
                                        <i class="fas fa-times"></i> Hủy
                                    </button>
                                </div>
                            </form>
                        </div>

                        <!-- Security Tab -->
                        <div class="profile-tab" id="securityTab">
                            <h2 class="section-title">Đổi mật khẩu</h2>
                            <form id="passwordForm">
                                <div class="form-grid">
                                    <div class="form-group full-width">
                                        <label class="form-label">Mật khẩu hiện tại *</label>
                                        <div class="password-toggle">
                                            <input type="password" class="form-input" id="currentPassword" name="currentPassword" required>
                                            <i class="fas fa-eye toggle-password"></i>
                                        </div>
                                        <div class="form-error" id="currentPasswordError">Vui lòng nhập mật khẩu hiện tại</div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="form-label">Mật khẩu mới *</label>
                                        <div class="password-toggle">
                                            <input type="password" class="form-input" id="newPassword" name="newPassword" 
                                                   minlength="6" required>
                                            <i class="fas fa-eye toggle-password"></i>
                                        </div>
                                        <div class="form-error" id="newPasswordError">Mật khẩu phải có ít nhất 6 ký tự</div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="form-label">Xác nhận mật khẩu *</label>
                                        <div class="password-toggle">
                                            <input type="password" class="form-input" id="confirmPassword" name="confirmPassword" required>
                                            <i class="fas fa-eye toggle-password"></i>
                                        </div>
                                        <div class="form-error" id="confirmPasswordError">Mật khẩu xác nhận không khớp</div>
                                    </div>
                                </div>
                                
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary" id="changePasswordBtn">
                                        <i class="fas fa-key"></i> Đổi mật khẩu
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            `;

	document.getElementById('profileContent').innerHTML = profileHTML;
	document.getElementById('loadingState').style.display = 'none';
	document.getElementById('profileContent').style.display = 'block';

	// Initialize event listeners
	initializeEventListeners();
}

// Initialize event listeners
function initializeEventListeners() {
	// Tab Navigation
	document.querySelectorAll('.nav-item').forEach(item => {
		item.addEventListener('click', function() {
			document.querySelectorAll('.nav-item').forEach(nav => {
				nav.classList.remove('active');
			});
			this.classList.add('active');

			document.querySelectorAll('.profile-tab').forEach(tab => {
				tab.classList.remove('active');
			});

			const tabId = this.getAttribute('data-tab') + 'Tab';
			document.getElementById(tabId).classList.add('active');
			
			// Reset edit mode when switching tabs
			if (isEditMode) {
				cancelEdit();
			}
		});
	});

	// Password Toggle
	document.querySelectorAll('.toggle-password').forEach(toggle => {
		toggle.addEventListener('click', function() {
			const input = this.previousElementSibling;
			if (input.type === 'password') {
				input.type = 'text';
				this.classList.remove('fa-eye');
				this.classList.add('fa-eye-slash');
			} else {
				input.type = 'password';
				this.classList.remove('fa-eye-slash');
				this.classList.add('fa-eye');
			}
		});
	});

	// Profile Form Submission
	document.getElementById('profileForm').addEventListener('submit', updateProfile);

	// Password Form Submission
	document.getElementById('passwordForm').addEventListener('submit', changePassword);

	// Avatar Upload
	document.getElementById('avatarInput').addEventListener('change', handleAvatarUpload);
	
	// Real-time validation for profile form
	document.getElementById('tenNguoiDung').addEventListener('input', validateTenNguoiDung);
	document.getElementById('sdt').addEventListener('input', validateSdt);
	
	// Real-time validation for password form
	document.getElementById('newPassword').addEventListener('input', validateNewPassword);
	document.getElementById('confirmPassword').addEventListener('input', validateConfirmPassword);
}

// Toggle edit mode for profile form
function toggleEditMode() {
	isEditMode = true;
	
	// Enable editable fields
	document.getElementById('tenNguoiDung').disabled = false;
	document.getElementById('sdt').disabled = false;
	document.getElementById('diaChi').disabled = false;
	
	// Toggle buttons
	document.getElementById('editProfileBtn').style.display = 'none';
	document.getElementById('saveProfileBtn').style.display = 'inline-flex';
	document.getElementById('cancelProfileBtn').style.display = 'inline-flex';
}

// Cancel edit mode
function cancelEdit() {
	isEditMode = false;
	
	// Disable editable fields and restore original values
	document.getElementById('tenNguoiDung').disabled = true;
	document.getElementById('tenNguoiDung').value = currentUser.tenNguoiDung || '';
	document.getElementById('sdt').disabled = true;
	document.getElementById('sdt').value = currentUser.sdt || '';
	document.getElementById('diaChi').disabled = true;
	document.getElementById('diaChi').value = currentUser.diaChi || '';
	
	// Clear errors
	clearFormErrors();
	
	// Toggle buttons
	document.getElementById('editProfileBtn').style.display = 'inline-flex';
	document.getElementById('saveProfileBtn').style.display = 'none';
	document.getElementById('cancelProfileBtn').style.display = 'none';
}

// Validation functions
function validateTenNguoiDung() {
	const input = document.getElementById('tenNguoiDung');
	const error = document.getElementById('tenNguoiDungError');
	
	if (!input.value.trim()) {
		input.classList.add('error');
		error.classList.add('show');
		return false;
	} else {
		input.classList.remove('error');
		error.classList.remove('show');
		return true;
	}
}

function validateSdt() {
	const input = document.getElementById('sdt');
	const error = document.getElementById('sdtError');
	const pattern = /^0[0-9]{9}$/;
	
	if (input.value && !pattern.test(input.value)) {
		input.classList.add('error');
		error.classList.add('show');
		return false;
	} else {
		input.classList.remove('error');
		error.classList.remove('show');
		return true;
	}
}

function validateNewPassword() {
	const input = document.getElementById('newPassword');
	const error = document.getElementById('newPasswordError');
	
	if (input.value && input.value.length < 6) {
		input.classList.add('error');
		error.classList.add('show');
		return false;
	} else {
		input.classList.remove('error');
		error.classList.remove('show');
		return true;
	}
}

function validateConfirmPassword() {
	const newPassword = document.getElementById('newPassword').value;
	const confirmPassword = document.getElementById('confirmPassword');
	const error = document.getElementById('confirmPasswordError');
	
	if (confirmPassword.value && confirmPassword.value !== newPassword) {
		confirmPassword.classList.add('error');
		error.classList.add('show');
		return false;
	} else {
		confirmPassword.classList.remove('error');
		error.classList.remove('show');
		return true;
	}
}

function clearFormErrors() {
	document.querySelectorAll('.form-input').forEach(input => {
		input.classList.remove('error');
	});
	document.querySelectorAll('.form-error').forEach(error => {
		error.classList.remove('show');
	});
}

// Handle avatar upload
async function handleAvatarUpload(e) {
	const file = e.target.files[0];
	if (!file) return;

	const formData = new FormData();
	formData.append('avatar', file);

	const success = await callApi('/profile/api/user/upload-avatar', 'POST', formData, 'Cập nhật ảnh đại diện thành công', true);

	if (success) {
		// Update avatar preview
		const reader = new FileReader();
		reader.onload = function(e) {
			document.getElementById('avatarPreview').src = e.target.result;
		}
		reader.readAsDataURL(file);
	}
}

// Update profile
async function updateProfile(e) {
	e.preventDefault();

	// Validate all fields
	const isTenValid = validateTenNguoiDung();
	const isSdtValid = validateSdt();

	if (!isTenValid || !isSdtValid) {
		showToast('error', 'Lỗi xác thực', 'Vui lòng kiểm tra lại thông tin nhập vào');
		return;
	}

	const btn = document.getElementById('saveProfileBtn');
	const originalText = btn.innerHTML;

	// Show loading state
	btn.disabled = true;
	btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';

	const formData = new FormData(e.target);
	const data = {
		tenNguoiDung: formData.get('tenNguoiDung'),
		sdt: formData.get('sdt'),
		diaChi: formData.get('diaChi')
	};

	const success = await callApi('/profile/api/user/update', 'POST', data, 'Cập nhật thông tin thành công');

	// Reset button
	btn.disabled = false;
	btn.innerHTML = originalText;

	if (success) {
		// Exit edit mode and reload profile data
		isEditMode = false;
		setTimeout(() => {
			loadUserProfile();
		}, 1500);
	}
}

// Change password
async function changePassword(e) {
	e.preventDefault();

	// Validate password fields
	const isNewPasswordValid = validateNewPassword();
	const isConfirmPasswordValid = validateConfirmPassword();

	if (!isNewPasswordValid || !isConfirmPasswordValid) {
		showToast('error', 'Lỗi xác thực', 'Vui lòng kiểm tra lại mật khẩu');
		return;
	}

	const btn = document.getElementById('changePasswordBtn');
	const originalText = btn.innerHTML;

	// Show loading state
	btn.disabled = true;
	btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đổi mật khẩu...';

	const formData = new FormData(e.target);
	const newPassword = formData.get('newPassword');
	const confirmPassword = formData.get('confirmPassword');

	if (newPassword !== confirmPassword) {
		showToast('error', 'Lỗi', 'Mật khẩu xác nhận không khớp!');
		btn.disabled = false;
		btn.innerHTML = originalText;
		return;
	}

	const data = {
		currentPassword: formData.get('currentPassword'),
		newPassword: newPassword,
		confirmPassword: confirmPassword
	};

	const success = await callApi('/profile/api/user/change-password', 'POST', data, 'Đổi mật khẩu thành công');

	// Reset button
	btn.disabled = false;
	btn.innerHTML = originalText;

	if (success) {
		e.target.reset();
		clearFormErrors();
	}
}

// Generic API call function
async function callApi(url, method, data, successMessage, isFormData = false) {
	const token = localStorage.getItem('jwtToken');

	try {
		const response = await fetch(url, {
			method: method,
			headers: {
				'Authorization': 'Bearer ' + token,
				'Accept': 'application/json',
				...(isFormData ? {} : { 'Content-Type': 'application/json' })
			},
			body: isFormData ? data : JSON.stringify(data)
		});

		const result = await response.json();

		if (result.success) {
			showToast('success', 'Thành công', successMessage);
			return true;
		} else {
			showToast('error', 'Lỗi', result.message);
			return false;
		}
	} catch (error) {
		showToast('error', 'Lỗi kết nối', error.message);
		return false;
	}
}

// Show toast notification
function showToast(type, title, message) {
	// Remove existing toasts
	const existingToast = document.querySelector('.toast');
	if (existingToast) {
		existingToast.remove();
	}

	// Create toast element
	const toast = document.createElement('div');
	toast.className = `toast ${type}`;
	
	const icon = type === 'success' ? 'fa-check-circle' : 
	             type === 'error' ? 'fa-exclamation-circle' : 
	             'fa-info-circle';
	
	toast.innerHTML = `
		<div class="toast-icon">
			<i class="fas ${icon}"></i>
		</div>
		<div class="toast-content">
			<div class="toast-title">${title}</div>
			<div class="toast-message">${message}</div>
		</div>
		<button class="toast-close" onclick="this.parentElement.remove()">
			<i class="fas fa-times"></i>
		</button>
	`;
	
	document.body.appendChild(toast);
	
	// Trigger animation
	setTimeout(() => {
		toast.classList.add('show');
	}, 10);
	
	// Auto remove after 5 seconds
	setTimeout(() => {
		toast.classList.add('hide');
		setTimeout(() => {
			toast.remove();
		}, 300);
	}, 5000);
}

// Show main error (for loading errors)
function showError(message) {
	document.getElementById('errorMessage').textContent = message;
	document.getElementById('loadingState').style.display = 'none';
	document.getElementById('errorState').style.display = 'block';
	document.getElementById('profileContent').style.display = 'none';
}