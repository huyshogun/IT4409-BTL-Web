// File: src/components/GlobalModal.jsx
import React from 'react';

const GlobalModal = ({ config, onClose, styles }) => {
    if (!config || !config.isOpen) return null;

    // 1. Kiểm tra loại thông báo
    const isError = config.type === 'error';
    const isSuccess = config.type === 'success'; // Mới thêm

    // 2. Chọn Style Content tương ứng
    let contentStyle = styles.modalContent; // Mặc định
    if (isError) contentStyle = styles.modalContentError || styles.modalContent;
    if (isSuccess) contentStyle = styles.modalContentSuccess || styles.modalContent;

    // 3. Cấu hình Theme (Màu sắc & Icon)
    let themeColor = '#007bff'; // Xanh dương (Info)
    let icon = '🛠️';

    if (isError) {
        themeColor = '#dc3545'; // Đỏ
        icon = '⚠️';
    } else if (isSuccess) {
        themeColor = '#28a745'; // Xanh lá
        icon = '✅';
    }

    return (
        <div style={styles.modalOverlay} onClick={onClose}>
            <div style={contentStyle} onClick={e => e.stopPropagation()}>
                
                {/* Icon */}
                <span style={{...styles.modalIcon, color: themeColor}}>{icon}</span>

                {/* Title */}
                <h3 style={{...styles.modalTitle, color: themeColor}}>{config.title}</h3>

                {/* Message */}
                <p style={styles.modalText}>{config.message}</p>

                {/* Button */}
                <button 
                    style={{
                        ...styles.modalButton, 
                        backgroundColor: themeColor,
                        boxShadow: `0 4px 15px ${themeColor}66` // Tạo bóng mờ theo màu nút
                    }} 
                    onClick={onClose}
                >
                    {isSuccess ? 'Tiếp tục ➜' : 'Đóng lại'}
                </button>
            </div>
        </div>
    );
};

export default GlobalModal;