import React, { useState, useEffect } from 'react';
import { bankingService } from '../services/bankingService';
import { useNavigate, useLocation } from 'react-router-dom';
import { commonStyles } from '../styles/commonStyles';
import GlobalModal from '../components/GlobalModal'; // Import Component
import { useNotification } from '../utils/useNotification'; // Import Hook
import { useGlobalLoading } from '../context/LoadingContext'; // Import Hook

const CreateAccount = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // --- STATE QUẢN LÝ ---
  const [formData, setFormData] = useState({
    userId: '',         
    accountName: '',
    accountType: 'SAVINGS',
    currency: 'VND',
    initialDeposit: 0
  });

  const [result, setResult] = useState(null);
  const { notification, showFeature, showError, showSuccess, closeNotification } = useNotification();
  const { showLoading, hideLoading, isLoading } = useGlobalLoading();
  const [currentUser, setCurrentUser] = useState(null);
  const [isDirty, setIsDirty] = useState(false);

  // --- LOGIC (GIỮ NGUYÊN) ---
  useEffect(() => {
    const storedUserStr = localStorage.getItem('user');
    const storedUser = storedUserStr ? JSON.parse(storedUserStr) : null;
    setCurrentUser(storedUser);

    let targetUserId = null;
    if (location.state && location.state.userId) {
      targetUserId = location.state.userId;
    } else if (storedUser && storedUser.id) {
      targetUserId = storedUser.id;
    }

    if (targetUserId) {
      setFormData(prev => ({ ...prev, userId: targetUserId }));
    } else {
      showError("⚠️ Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
      navigate('/');
    }
  }, [location, navigate]);

  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (isDirty) { e.preventDefault(); e.returnValue = ''; }
    };
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [isDirty]);

  const handleCancel = () => {
    if (isDirty) {
      if (window.confirm("⚠️ Hủy kích hoạt sẽ đăng xuất. Bạn chắc chứ?")) {
        bankingService.logout(); navigate('/');
      }
    } else {
      bankingService.logout(); navigate('/');
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (!isDirty) setIsDirty(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    showLoading("Đang xử lý...");
    if (!formData.userId) { showError("Lỗi ID"); navigate('/'); return; }

    try {
      const response = await bankingService.createAccount({
        ...formData,
        initialDeposit: parseFloat(formData.initialDeposit)
      });
      setIsDirty(false); 
      setResult(response.data);
      showSuccess('✅ Kích hoạt tài khoản thành công!');
    } catch (error) {
      showError('❌ Lỗi: ' + (error.response?.data?.message || 'Không thể tạo tài khoản'));
    } finally {
      hideLoading();
    }
  };

  // --- STYLES "RỰC RỠ" ---
  const styles = {
    wrapper: {
      minHeight: '100vh', width: '100%',
      // Gradient Xanh-Tím hiện đại cho Tài chính
      background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      fontFamily: "'Segoe UI', Roboto, sans-serif", padding: '20px'
    },
    glassCard: {
      width: '100%', maxWidth: '550px',
      background: 'rgba(255, 255, 255, 0.9)',
      backdropFilter: 'blur(20px)',
      borderRadius: '20px',
      boxShadow: '0 20px 50px rgba(0,0,0,0.2)',
      padding: '40px 30px',
      position: 'relative', overflow: 'hidden'
    },
    header: {
      textAlign: 'center', marginBottom: '30px'
    },
    title: {
      fontSize: '26px', fontWeight: '800', margin: '0',
      background: 'linear-gradient(to right, #00c6ff, #0072ff)',
      WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
      textTransform: 'uppercase'
    },
    welcomeBox: {
      background: '#e3f2fd', padding: '15px', borderRadius: '12px',
      marginBottom: '25px', color: '#0d47a1', fontSize: '14px',
      borderLeft: '5px solid #2196f3', display: 'flex', alignItems: 'center', gap: '10px'
    },
    label: {
      fontSize: '13px', fontWeight: '600', color: '#555',
      marginBottom: '8px', display: 'block', textTransform: 'uppercase'
    },
    input: {
      width: '100%', padding: '12px 15px', borderRadius: '10px',
      border: '2px solid #eee', fontSize: '15px', outline: 'none',
      transition: 'all 0.3s', backgroundColor: '#f9f9f9', boxSizing: 'border-box'
    },
    select: {
      width: '100%', padding: '12px 15px', borderRadius: '10px',
      border: '2px solid #eee', fontSize: '15px', outline: 'none',
      backgroundColor: 'white', cursor: 'pointer'
    },
    btnSubmit: {
      width: '100%', padding: '15px', borderRadius: '12px',
      border: 'none', 
      background: 'linear-gradient(to right, #00c6ff, #0072ff)', // Gradient nút
      color: 'white', fontSize: '16px', fontWeight: 'bold',
      cursor: isLoading ? 'not-allowed' : 'pointer',
      boxShadow: '0 8px 20px rgba(0, 114, 255, 0.3)',
      marginTop: '10px', transition: 'transform 0.2s'
    },
    btnCancel: {
      width: '100%', padding: '12px', marginTop: '15px',
      background: 'white', border: '1px solid #ddd', borderRadius: '12px',
      color: '#666', fontWeight: '600', cursor: 'pointer'
    },
    // RESULT CARD
    resultCard: {
      textAlign: 'center', animation: 'fadeIn 0.6s ease'
    },
    successIcon: {
      width: '70px', height: '70px', background: '#d4edda',
      color: '#155724', borderRadius: '50%', fontSize: '35px',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      margin: '0 auto 20px auto', boxShadow: '0 5px 15px rgba(21, 87, 36, 0.2)'
    },
    infoRow: {
        display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid #eee'
    }
  };

  return (
    <>
      <style>{`
        input:focus, select:focus { border-color: #0072ff !important; background: white !important; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
      `}</style>

      <div style={styles.wrapper}>
        <div style={styles.glassCard}>
          
          {/* Nút Back nhỏ ở góc */}
          <button onClick={handleCancel} style={{position: 'absolute', top: '20px', left: '20px', border: 'none', background: 'none', fontSize: '20px', cursor: 'pointer', color: '#999'}}>⬅</button>

          <div style={styles.header}>
             <div style={{fontSize: '40px', marginBottom: '10px'}}>🏦</div>
             <h2 style={styles.title}>{!result ? 'Kích Hoạt Tài Khoản' : 'Mở Tài Khoản Thành Công'}</h2>
          </div>

          {currentUser && !result && (
            <div style={styles.welcomeBox}>
               <span style={{fontSize: '24px'}}>👋</span>
               <div>
                  Xin chào <strong>{currentUser.username || currentUser.accountName}</strong>!
                  <div style={{fontSize: '12px', opacity: 0.8}}>Hoàn tất thiết lập bên dưới để bắt đầu giao dịch.</div>
               </div>
            </div>
          )}

          {/* --- FORM NHẬP LIỆU --- */}
          {!result ? (
            <form onSubmit={handleSubmit}>
              
              <div style={{marginBottom: '20px'}}>
                <label style={styles.label}>Tên tài khoản (Alias)</label>
                <input 
                  name="accountName" 
                  value={formData.accountName} 
                  onChange={handleChange} 
                  style={styles.input}
                  placeholder="Ví dụ: Tài khoản chính, Quỹ đen..." 
                  required
                />
              </div>

              <div style={{display: 'flex', gap: '20px', marginBottom: '20px'}}>
                 <div style={{flex: 1}}>
                    <label style={styles.label}>Loại TK</label>
                    <select name="accountType" value={formData.accountType} onChange={handleChange} style={styles.select}>
                       <option value="SAVINGS">Tiết kiệm (Savings)</option>
                       <option value="CHECKING">Thanh toán (Checking)</option>
                       <option value="CREDIT">Tín dụng (Credit)</option>
                    </select>
                 </div>
                 <div style={{flex: 1}}>
                    <label style={styles.label}>Tiền tệ</label>
                    <select name="currency" value={formData.currency} onChange={handleChange} style={styles.select}>
                       <option value="VND">🇻🇳 VND</option>
                       <option value="USD">🇺🇸 USD</option>
                    </select>
                 </div>
              </div>

              <div style={{marginBottom: '30px'}}>
                <label style={styles.label}>Nạp tiền lần đầu</label>
                <div style={{position: 'relative'}}>
                    <input 
                    type="number" 
                    name="initialDeposit" 
                    value={formData.initialDeposit} 
                    onChange={handleChange} 
                    min="0"
                    style={{...styles.input, paddingRight: '60px', color: '#0072ff', fontWeight: 'bold', fontSize: '18px'}}
                    required
                    />
                    <span style={{position: 'absolute', right: '15px', top: '50%', transform: 'translateY(-50%)', fontWeight: 'bold', color: '#999'}}>{formData.currency}</span>
                </div>
                <div style={{fontSize: '11px', color: '#888', marginTop: '5px'}}>Số tiền này sẽ được cộng vào số dư khả dụng.</div>
              </div>

              <button type="submit" disabled={isLoading} style={styles.btnSubmit}>
                {isLoading ? 'Đang khởi tạo hệ thống...' : '🚀 KÍCH HOẠT NGAY'}
              </button>

              <button type="button" onClick={handleCancel} style={styles.btnCancel}>
                 Hủy bỏ & Đăng xuất
              </button>
            </form>
          ) : (
            /* --- KẾT QUẢ THÀNH CÔNG --- */
            <div style={styles.resultCard}>
                <div style={styles.successIcon}>✓</div>
                <p style={{color: '#666', marginBottom: '25px'}}>Tài khoản của bạn đã sẵn sàng sử dụng!</p>

                <div style={{background: '#f8f9fa', padding: '20px', borderRadius: '15px', border: '1px dashed #ccc', marginBottom: '20px'}}>
                    <div style={styles.infoRow}>
                        <span style={{color: '#888'}}>Số tài khoản</span>
                        <span style={{fontWeight: 'bold', color: '#0072ff', fontSize: '18px'}}>{result.accountNumber}</span>
                    </div>
                    <div style={styles.infoRow}>
                        <span style={{color: '#888'}}>Chủ tài khoản</span>
                        <span style={{fontWeight: 'bold'}}>{result.accountName.toUpperCase()}</span>
                    </div>
                    <div style={{...styles.infoRow, borderBottom: 'none'}}>
                        <span style={{color: '#888'}}>Số dư ban đầu</span>
                        <span style={{fontWeight: 'bold', color: '#28a745'}}>{result.balance.toLocaleString()} {result.currency}</span>
                    </div>
                    
                    {/* QR Code */}
                    <div style={{marginTop: '15px', paddingTop: '15px', borderTop: '1px dashed #ccc'}}>
                        <img 
                            src={result.qrCode.startsWith('data:image') ? result.qrCode : `data:image/png;base64,${result.qrCode}`} 
                            alt="QR" width="120"
                            style={{borderRadius: '10px', border: '1px solid #eee'}}
                        />
                        <div style={{fontSize: '10px', color: '#999', marginTop: '5px'}}>Quét để chuyển tiền</div>
                    </div>
                </div>

                <button 
                    onClick={() => navigate('/dashboard')} 
                    style={{...styles.btnSubmit, background: '#28a745', boxShadow: '0 8px 20px rgba(40, 167, 69, 0.3)'}}
                >
                    TRUY CẬP DASHBOARD ➡
                </button>
            </div>
          )}
        </div>
        {/* Đặt GlobalModal ở cuối cùng */}
        <GlobalModal 
            config={notification} 
            onClose={closeNotification} 
            styles={commonStyles} 
        />
      </div>
    </>
  );
};

export default CreateAccount;