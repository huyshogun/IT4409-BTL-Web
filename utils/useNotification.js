// File: src/hooks/useNotification.js
import { useState } from 'react';

export const useNotification = () => {
    const [notification, setNotification] = useState({
        isOpen: false,
        type: 'info', 
        message: '',
        title: ''
    });

    // Hàm gọi thông báo thường
    const showFeature = (featureName) => {
        setNotification({
            isOpen: true,
            type: 'info',
            title: 'Thông báo hệ thống',
            message: `Tính năng ${featureName} đang phát triển! 🛠️`
        });
    };

    // Hàm gọi báo lỗi 
    const showError = (errorMessage) => {
        setNotification({
            isOpen: true,
            type: 'error',
            title: 'Đã xảy ra lỗi',
            message: errorMessage
        });
    };

    const showSuccess = (message) => {
        setNotification({
            isOpen: true,
            type: 'success', // Loại là success
            title: 'Thành công! 🎉',
            message: message
        });
    };

    // Hàm đóng thông báo
    const closeNotification = () => {
        setNotification(prev => ({ ...prev, isOpen: false }));
    };

    return {
        notification,      
        showFeature,       
        showError,         
        showSuccess,
        closeNotification  
    };
};