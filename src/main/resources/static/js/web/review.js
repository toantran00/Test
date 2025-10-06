let currentPage = 0;
let productId = null;

// Khởi tạo khi DOM loaded
document.addEventListener('DOMContentLoaded', function() {
    // Lấy productId từ URL hoặc từ thẻ meta
    productId = getProductIdFromUrl();
    
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');
    
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', loadMoreReviews);
    }
});

// Lấy productId từ URL
function getProductIdFromUrl() {
    const pathSegments = window.location.pathname.split('/');
    for (let i = 0; i < pathSegments.length; i++) {
        if (pathSegments[i] === 'view' && i + 1 < pathSegments.length) {
            const id = pathSegments[i + 1];
            return !isNaN(id) ? parseInt(id) : null;
        }
    }
    return null;
}

async function loadMoreReviews() {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const reviewsList = document.getElementById('reviewsList');
    const loadMoreContainer = document.getElementById('loadMoreContainer');

    if (!productId) {
        console.error('Không tìm thấy productId');
        return;
    }

    loadMoreBtn.style.display = 'none';
    loadingSpinner.style.display = 'block';

    try {
        // SỬA URL Ở ĐÂY - thay /api/products/ bằng /view/
        const response = await fetch(`/view/${productId}/reviews?page=${currentPage}&size=6`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();

        if (data.danhGias && data.danhGias.length > 0) {
            // Thêm bình luận mới vào danh sách
            data.danhGias.forEach(review => {
                const reviewItem = createReviewItem(review);
                reviewsList.appendChild(reviewItem);
            });

            currentPage++;

            // Ẩn nút nếu không còn bình luận nào nữa
            if (!data.hasNext) {
                loadMoreContainer.style.display = 'none';
            } else {
                loadMoreBtn.style.display = 'block';
            }
        } else {
            // Không còn bình luận nào nữa
            loadMoreContainer.style.display = 'none';
        }
    } catch (error) {
        console.error('Lỗi khi tải bình luận:', error);
        loadMoreBtn.style.display = 'block';
        
        // Hiển thị thông báo lỗi
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = 'Có lỗi xảy ra khi tải bình luận. Vui lòng thử lại.';
        loadMoreContainer.appendChild(errorDiv);
    } finally {
        loadingSpinner.style.display = 'none';
    }
}

function createReviewItem(review) {
    const reviewItem = document.createElement('div');
    reviewItem.className = 'review-item';
    reviewItem.setAttribute('data-aos', 'fade-up');

    // FIX: Kiểm tra kỹ hơn đường dẫn hình ảnh
    let userAvatar = '';
    if (review.nguoiDung && review.nguoiDung.hinhAnh) {
        // Đảm bảo đường dẫn đầy đủ
        let avatarSrc = review.nguoiDung.hinhAnh;
        if (!avatarSrc.startsWith('http') && !avatarSrc.startsWith('/')) {
            avatarSrc = '/' + avatarSrc;
        }
        userAvatar = `<img src="${avatarSrc}" alt="${review.nguoiDung.tenNguoiDung || 'Người dùng'}" class="user-avatar-img" onerror="this.style.display='none'; this.parentNode.innerHTML='<i class=\\'fa-solid fa-user user-avatar-icon\\'></i>'">`;
    } else {
        userAvatar = `<i class="fa-solid fa-user user-avatar-icon"></i>`;
    }

    const stars = Array.from({ length: 5 }, (_, i) =>
        `<i class="fa fa-star ${i < review.soSao ? 'filled' : ''}"></i>`
    ).join('');

    const reviewMedia = review.anhVideo ?
        `<div class="review-media">
            <img src="/images/uploads/reviews/${review.anhVideo}" alt="Media đánh giá" class="review-image">
        </div>` : '';

    const userName = review.nguoiDung && review.nguoiDung.tenNguoiDung ? 
        review.nguoiDung.tenNguoiDung : 'Người dùng ẩn danh';
    
    const reviewDate = review.ngayDanhGia ? 
        new Date(review.ngayDanhGia).toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }) : '';

    reviewItem.innerHTML = `
        <div class="review-header">
            <div class="user-info">
                <div class="user-avatar">
                    ${userAvatar}
                </div>
                <div class="user-details">
                    <span class="user-name">${userName}</span>
                    <div class="review-rating">
                        <div class="stars-small">
                            ${stars}
                        </div>
                        <span class="review-date">${reviewDate}</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="review-content">
            <p class="review-comment">${review.binhLuan || 'Không có bình luận'}</p>
            ${reviewMedia}
        </div>
    `;

    return reviewItem;
}

// Export functions for global access
window.reviewFunctions = {
    loadMoreReviews,
    getProductIdFromUrl
};