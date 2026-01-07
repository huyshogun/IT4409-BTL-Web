import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { bankingService } from '../services/bankingService';
import { commonStyles } from '../styles/commonStyles';
import GlobalModal from '../components/GlobalModal'; // Import Component
import { useNotification } from '../utils/useNotification'; // Import Hook


const Settings = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  
  // Lấy thông tin user cơ bản
  const user = JSON.parse(localStorage.getItem('user')) || {};
  
  // --- STATE CÀI ĐẶT ---
  // 1. Avatar: Lấy từ LocalStorage nếu có, không thì dùng ảnh mặc định
  const [avatar, setAvatar] = useState(localStorage.getItem('user_avatar') || 'https://via.placeholder.com/150');
  
  // 2. Giao diện (Dark Mode): false = Sáng, true = Tối
  const [isDarkMode, setIsDarkMode] = useState(localStorage.getItem('app_theme') === 'dark');
  
  // 3. Ngôn ngữ
  const [language, setLanguage] = useState(localStorage.getItem('app_language') || 'vi');

  // 4. Các State Mock (Bảo mật)
  const [biometricEnabled, setBiometricEnabled] = useState(true);
  const [fingerprintEnabled, setFingerprintEnabled] = useState(false);
  const { notification, showFeature, showError, showSuccess, closeNotification } = useNotification();

  // --- HÀM XỬ LÝ ---
  
  // Xử lý đổi Avatar
  const handleAvatarChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const newAvatar = e.target.result;
        setAvatar(newAvatar);
        // Lưu vào LocalStorage để các trang khác (như Dashboard) có thể dùng chung
        localStorage.setItem('user_avatar', newAvatar);
        showSuccess("✅ Đổi ảnh đại diện thành công!");
      };
      reader.readAsDataURL(file);
    }
  };

  // Xử lý đổi Giao diện (Theme)
  const toggleTheme = () => {
    const newTheme = !isDarkMode ? 'dark' : 'light';
    setIsDarkMode(!isDarkMode);
    localStorage.setItem('app_theme', newTheme);
    // Thực tế bạn sẽ cần áp dụng class CSS vào body hoặc context, ở đây mình làm demo đổi màu nền
  };

  // Xử lý đổi Ngôn ngữ
  const handleLanguageChange = (e) => {
    setLanguage(e.target.value);
    localStorage.setItem('app_language', e.target.value);
  };

  const handleLogout = () => {
    bankingService.logout();
    navigate('/');
  };

  // Mock toggle
  const toggleMock = (setter, label) => {
    setter(prev => {
        const newVal = !prev;
        showSuccess(`Đã ${newVal ? 'BẬT' : 'TẮT'} tính năng ${label} (Mô phỏng)`);
        return newVal;
    });
  };

  // --- STYLES (Kế thừa phong cách Mobile Center của Dashboard) ---
  const styles = {
    outerWrapper: {
      minHeight: '100vh', width: '100%',
      backgroundColor: '#e4e6eb', // Nền desktop
      display: 'flex', justifyContent: 'center', alignItems: 'flex-start',
      paddingTop: '20px', paddingBottom: '20px', boxSizing: 'border-box',
    },
    container: {
      width: '100%', maxWidth: '480px', minHeight: '90vh',
      backgroundColor: isDarkMode ? '#1e1e1e' : '#ffffff', // Đổi màu theo Theme
      color: isDarkMode ? '#ffffff' : '#333333',
      fontFamily: "'Segoe UI', Roboto, sans-serif",
      position: 'relative', borderRadius: '30px',
      boxShadow: '0 0 20px rgba(0,0,0,0.1)', overflow: 'hidden',
    },
    header: {
      background: 'linear-gradient(135deg, #007bff, #0056b3)',
      padding: '20px', color: 'white', display: 'flex', alignItems: 'center', gap: '15px'
    },
    backBtn: { background: 'none', border: 'none', color: 'white', fontSize: '24px', cursor: 'pointer' },
    
    // Avatar Section
    profileSection: {
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      padding: '30px 20px', borderBottom: isDarkMode ? '1px solid #333' : '1px solid #f0f0f0'
    },
    avatarWrapper: { position: 'relative', marginBottom: '15px' },
    avatarImg: { width: '100px', height: '100px', borderRadius: '50%', objectFit: 'cover', border: '4px solid white', boxShadow: '0 5px 15px rgba(0,0,0,0.1)' },
    cameraBtn: {
      position: 'absolute', bottom: '5px', right: '5px',
      width: '30px', height: '30px', borderRadius: '50%',
      backgroundColor: '#007bff', color: 'white', border: '2px solid white',
      display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', fontSize: '14px'
    },
    
    // List Menu
    sectionTitle: { padding: '20px 20px 10px 20px', fontSize: '14px', fontWeight: 'bold', color: '#888', textTransform: 'uppercase' },
    menuList: { padding: '0 20px' },
    menuItem: {
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: '15px 0', borderBottom: isDarkMode ? '1px solid #333' : '1px solid #f0f0f0'
    },
    menuLabel: { fontSize: '16px', fontWeight: '500' },
    
    // Switch Toggle (CSS thuần)
    switch: { position: 'relative', display: 'inline-block', width: '50px', height: '26px' },
    slider: (checked) => ({
      position: 'absolute', cursor: 'pointer', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: checked ? '#28a745' : '#ccc', borderRadius: '34px', transition: '.4s'
    }),
    sliderBefore: (checked) => ({
      position: 'absolute', content: '""', height: '20px', width: '20px',
      left: checked ? '26px' : '4px', bottom: '3px',
      backgroundColor: 'white', borderRadius: '50%', transition: '.4s'
    }),
    
    select: {
      padding: '8px', borderRadius: '8px', border: '1px solid #ccc',
      backgroundColor: isDarkMode ? '#333' : 'white', color: isDarkMode ? 'white' : 'black'
    }
  };

  // Component Switch nhỏ gọn
  const ToggleSwitch = ({ checked, onChange }) => (
    <div style={styles.switch} onClick={onChange}>
      <span style={styles.slider(checked)}></span>
      <span style={styles.sliderBefore(checked)}></span>
    </div>
  );

  return (
    <div style={styles.outerWrapper}>
      <div style={styles.container}>
        
        {/* Input file ẩn để chọn ảnh */}
        <input 
          type="file" ref={fileInputRef} 
          style={{ display: 'none' }} accept="image/*" 
          onChange={handleAvatarChange} 
        />

        {/* HEADER */}
        <div style={styles.header}>
          <button onClick={() => navigate('/dashboard')} style={styles.backBtn}>⬅</button>
          <h2 style={{ margin: 0, fontSize: '18px' }}>Cài đặt</h2>
        </div>

        {/* PROFILE & AVATAR */}
        <div style={styles.profileSection}>
          <div style={styles.avatarWrapper}>
            <img src={avatar} alt="Avatar" style={styles.avatarImg} />
            <div style={styles.cameraBtn} onClick={() => fileInputRef.current.click()}>📷</div>
          </div>
          <h3 style={{ margin: 0 }}>{user.accountName || user.username}</h3>
          <p style={{ margin: '5px 0 0 0', color: '#888', fontSize: '13px' }}>Thay đổi thông tin cá nhân</p>
        </div>

        {/* NHÓM 1: CÁ NHÂN HÓA */}
        <div style={styles.sectionTitle}>Giao diện & Ngôn ngữ</div>
        <div style={styles.menuList}>
          
          {/* Đổi Giao diện (Dark Mode) */}
          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>🌙 Chế độ tối (Dark Mode)</span>
            <ToggleSwitch checked={isDarkMode} onChange={toggleTheme} />
          </div>

          {/* Đổi Ảnh nền (Demo màu sắc) */}
          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>🖼️ Ảnh nền App</span>
            <div style={{display: 'flex', gap: '5px'}}>
               <div style={{width: '20px', height: '20px', borderRadius: '50%', background: '#fff', border: '1px solid #ccc', cursor: 'pointer'}}></div>
               <div style={{width: '20px', height: '20px', borderRadius: '50%', background: '#ffecd2', cursor: 'pointer'}}></div>
               <div style={{width: '20px', height: '20px', borderRadius: '50%', background: '#d4fc79', cursor: 'pointer'}}></div>
            </div>
          </div>

          {/* Đổi Ngôn ngữ */}
          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>🌐 Ngôn ngữ</span>
            <select value={language} onChange={handleLanguageChange} style={styles.select}>
              <option value="vi">Tiếng Việt</option>
              <option value="en">English</option>
              <option value="jp">日本語</option>
            </select>
          </div>
        </div>

        {/* NHÓM 2: BẢO MẬT (MOCK) */}
        <div style={styles.sectionTitle}>Bảo mật & Đăng nhập</div>
        <div style={styles.menuList}>
          
          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>👁️ Đăng nhập Sinh trắc học</span>
            <ToggleSwitch checked={biometricEnabled} onChange={() => toggleMock(setBiometricEnabled, 'Sinh trắc học')} />
          </div>

          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>👆 Xác thực Vân tay</span>
            <ToggleSwitch checked={fingerprintEnabled} onChange={() => toggleMock(setFingerprintEnabled, 'Vân tay')} />
          </div>

          {/* --- 1. Cập nhật thông tin tài khoản (Đã sửa lại cho đồng bộ) --- */}
          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>📝 Cập nhật thông tin tài khoản</span>
            <button 
              style={{
                padding: '5px 10px', 
                fontSize: '12px',
                backgroundColor: '#007bff', // Thêm màu xanh cho nút này để nổi bật hơn chút (tùy chọn)
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }} 
              onClick={() => navigate('/update-account')}
            >
              Cập nhật
            </button>
          </div>

          <div style={styles.menuItem}>
            <span style={styles.menuLabel}>🔐 Đổi mật khẩu đăng nhập</span>
            <button style={{padding: '5px 10px', fontSize: '12px'}} onClick={() => showSuccess("Chức năng đang phát triển")}>Thay đổi</button>
          </div>
        </div>

        {/* LOGOUT */}
        <div style={{ padding: '30px 20px' }}>
          <button 
            onClick={handleLogout}
            style={{
              width: '100%', padding: '15px', borderRadius: '12px', border: 'none',
              backgroundColor: '#ffebee', color: '#c62828', fontWeight: 'bold', fontSize: '16px', cursor: 'pointer'
            }}
          >
            Đăng xuất
          </button>
          <p style={{textAlign: 'center', color: '#999', fontSize: '11px', marginTop: '10px'}}>Phiên bản: 1.0.5</p>
        </div>

      </div>

      {/* Đặt GlobalModal ở cuối cùng */}
      <GlobalModal 
          config={notification} 
          onClose={closeNotification} 
          styles={commonStyles} 
      />
    </div>
  );
};

export default Settings;