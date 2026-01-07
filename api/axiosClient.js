import axios from 'axios';

// 1. Khởi tạo instance
const axiosClient = axios.create({
  baseURL: 'https://banking.duchuysaidepchieu.id.vn', // URL Backend của bạn
  headers: {
    'Content-Type': 'application/json',
  },
  // Timeout: 10 giây (Tùy chọn, tránh treo app nếu mạng lag)
  timeout: 10000, 
});

// --- 2. REQUEST INTERCEPTOR (GỬI TOKEN ĐI) ---
axiosClient.interceptors.request.use(
  (config) => {
    // Lấy token từ LocalStorage
    const token = localStorage.getItem('jwtToken');

    const isPublicUrl = 
        config.url.includes('/auth/signin') || 
        config.url.includes('/auth/login') ||  // <--- Thêm dòng này cho chắc
        config.url.includes('/auth/signup') || 
        config.url.includes('/auth/register');

    if (token && !isPublicUrl) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// --- 3. RESPONSE INTERCEPTOR (XỬ LÝ KẾT QUẢ TRẢ VỀ) ---
axiosClient.interceptors.response.use(
  (response) => {
    // Nếu API trả về thành công (Status 200-299) -> Trả về data
    // Bạn có thể return response.data ở đây để code gọi API ngắn gọn hơn (tùy chọn)
    return response;
  },
  (error) => {
    // Lấy thông tin lỗi
    const { response, config } = error;

    // Nếu có phản hồi từ Server (lỗi logic, lỗi auth...)
    if (response) {
      
      // 🔥 XỬ LÝ LỖI 401 (UNAUTHORIZED) - TOKEN HẾT HẠN 🔥
      if (response.status === 401) {
        
        // QUAN TRỌNG: Kiểm tra xem lỗi này có phải do đang Đăng nhập sai không?
        // Nếu đang ở trang login mà nhập sai pass thì cũng ra 401, lúc đó KHÔNG ĐƯỢC reload trang.
        const isLoginRequest = config.url && config.url.includes('/auth/signin');

        if (!isLoginRequest) {
            console.warn("⚠️ Phiên đăng nhập hết hạn. Đang đăng xuất...");

            // 1. Xóa sạch mọi dữ liệu liên quan đến user cũ
            localStorage.clear(); 
            // Hoặc xóa từng cái nếu muốn giữ lại setting ngôn ngữ/theme:
            // localStorage.removeItem('jwtToken');
            // localStorage.removeItem('user');
            // localStorage.removeItem('user_avatar');

            // 2. Chuyển hướng về trang Login
            // Dùng window.location để ép tải lại trang, đảm bảo xóa sạch state của React cũ
            window.location.href = '/';
            
            return Promise.reject(error);
        }
      }

      // Xử lý lỗi 403 (Forbidden) - Không có quyền truy cập
      if (response.status === 403) {
        console.error("⛔ Bạn không có quyền thực hiện chức năng này!");
        // Có thể navigate sang trang 'Access Denied' hoặc chỉ alert
      }
    }

    // Trả lỗi về để các hàm gọi API (try-catch) ở giao diện xử lý tiếp (hiện thông báo đỏ...)
    return Promise.reject(error);
  }
);

export default axiosClient;