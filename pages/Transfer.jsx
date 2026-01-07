import React, { useState, useEffect, useRef } from 'react';
import { bankingService } from '../services/bankingService';
import { useNavigate, useLocation } from 'react-router-dom';
import html2canvas from 'html2canvas';
import { commonStyles } from '../styles/commonStyles';
import GlobalModal from '../components/GlobalModal'; // Import Component
import { useNotification } from '../utils/useNotification'; // Import Hook
import { useGlobalLoading } from '../context/LoadingContext'; // Import Hook

const Transfer = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('user'));
  const billRef = useRef(null); // Ref để chụp ảnh hóa đơn

  // --- STATE QUẢN LÝ ---
  const [formData, setFormData] = useState({
    sourceAccountNumber: '',
    destinationAccountNumber: '',
    amount: '',
    description: '',
  });

  const [currentBalance, setCurrentBalance] = useState(0);
  const [currency, setCurrency] = useState('VND');
  const [recipientName, setRecipientName] = useState(''); 
  const [isAccountChecked, setIsAccountChecked] = useState(false);
  const [transactionResult, setTransactionResult] = useState(null);
  const [captchaCode, setCaptchaCode] = useState('');
  const [inputCaptcha, setInputCaptcha] = useState('');
  const { notification, showFeature, showError, showSuccess, closeNotification } = useNotification();
  const { showLoading, hideLoading, isLoading } = useGlobalLoading();

  // --- LOGIC (GIỮ NGUYÊN NHƯ CŨ) ---
  useEffect(() => {
    const fetchAccountInfo = async () => {
      if (!user || !user.id) { navigate('/'); return; }
      try {
        const res = await bankingService.getAccountInfo(user.id);
        setFormData(prev => ({ ...prev, sourceAccountNumber: res.data.accountNumber }));
        setCurrentBalance(res.data.balance);
        setCurrency(res.data.currency || 'VND');
      } catch (error) { navigate('/dashboard'); }
    };
    fetchAccountInfo();
    generateCaptcha();
  }, []);

  useEffect(() => {
    if (location.state?.scannedAccount && formData.sourceAccountNumber) {
        const scannedAcc = location.state.scannedAccount;
        if (scannedAcc === formData.sourceAccountNumber) {
            showError("⚠️ Không thể chuyển cho chính mình!");
            navigate('/dashboard');
            return;
        }
        setFormData(prev => ({ ...prev, destinationAccountNumber: scannedAcc }));
        autoCheckAccount(user.id, scannedAcc);
    }
  }, [location.state, formData.sourceAccountNumber]);

  const generateCaptcha = () => {
    const chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    let code = "";
    for (let i = 0; i < 6; i++) { code += chars.charAt(Math.floor(Math.random() * chars.length)); }
    setCaptchaCode(code);
    setInputCaptcha('');
  };

  const autoCheckAccount = async (userId, accNum) => {
    try {
        const res = await bankingService.checkDestinationAccount(userId, accNum);
        setRecipientName(res.data.accountName || res.data);
        setIsAccountChecked(true);
    } catch (error) { console.error(error); }
  };

  const handleCheckAccount = async () => {
    if (!formData.destinationAccountNumber) return showError("Vui lòng nhập số tài khoản!");
    if (formData.destinationAccountNumber === formData.sourceAccountNumber) return showError("⛔ Không thể chuyển cho chính mình!");
    showLoading("Đang xử lý...")
    try {
      const res = await bankingService.checkDestinationAccount(user.id, formData.destinationAccountNumber);
      setRecipientName(res.data.accountName || res.data);
      setIsAccountChecked(true);
    } catch (error) { showError("❌ Không tìm thấy tài khoản!"); setIsAccountChecked(false); setRecipientName(''); } 
    finally { hideLoading(); }
  };

  const handleResetAccount = () => {
    setIsAccountChecked(false); setRecipientName('');
    setFormData(prev => ({ ...prev, destinationAccountNumber: '' }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (inputCaptcha.toUpperCase() !== captchaCode) { showError("❌ Mã Captcha sai!"); generateCaptcha(); return; }
    if (parseFloat(formData.amount) > currentBalance) { showError("❌ Số dư không đủ!"); return; }

    showLoading("Đang xử lý...");
    try {
      const payload = {
        sourceAccountNumber: formData.sourceAccountNumber,
        destinationAccountNumber: formData.destinationAccountNumber,
        amount: parseFloat(formData.amount),
        transactionType: 'TRANSFER', currency: currency, description: formData.description
      };
      const res = await bankingService.createTransaction(user.id, payload);
      setTransactionResult(res.data);
    } catch (error) { showError(`❌ Lỗi: ${error.response?.data?.message || 'Thất bại'}`); generateCaptcha(); } 
    finally { hideLoading(); }
  };

  const handleSaveBill = () => {
    if (billRef.current) {
        html2canvas(billRef.current).then(canvas => {
            const link = document.createElement('a');
            link.download = `Bill_${transactionResult.transactionReference}.png`;
            link.href = canvas.toDataURL();
            link.click();
        });
    }
  };

  // --- STYLES "LỘNG LẪY" ---
  const styles = {
    wrapper: {
        minHeight: '100vh', width: '100%',
        background: '#f0f2f5', // Nền tổng thể sạch sẽ
        display: 'flex', justifyContent: 'center', alignItems: 'center',
        fontFamily: "'Segoe UI', Roboto, sans-serif",
        padding: '20px'
    },
    container: {
        width: '100%', maxWidth: '500px',
        background: '#fff',
        borderRadius: '25px',
        boxShadow: '0 20px 60px rgba(0,0,0,0.15)', // Bóng đổ sâu tạo cảm giác nổi
        overflow: 'hidden',
        position: 'relative'
    },
    header: {
        background: 'linear-gradient(135deg, #0062cc 0%, #00c6ff 100%)',
        padding: '30px 25px 80px 25px', // Padding dưới lớn để thẻ ATM đè lên
        color: 'white', textAlign: 'center',
        position: 'relative'
    },
    // THẺ ATM ẢO (Điểm nhấn rực rỡ)
    atmCard: {
        background: 'linear-gradient(120deg, #89f7fe 0%, #66a6ff 100%)', // Gradient thẻ
        margin: '-60px 25px 25px 25px', // Đẩy lên đè vào header
        borderRadius: '20px',
        padding: '20px',
        color: 'white',
        boxShadow: '0 10px 25px rgba(33, 147, 176, 0.4)', // Bóng màu xanh
        position: 'relative',
        zIndex: 10,
        backdropFilter: 'blur(10px)',
    },
    chip: {
        width: '40px', height: '30px', background: '#ffdb4d', borderRadius: '5px', marginBottom: '15px',
        boxShadow: 'inset 0 0 5px rgba(0,0,0,0.2)'
    },
    formBody: { padding: '0 25px 30px 25px' },
    inputGroup: { marginBottom: '20px' },
    label: { display: 'block', marginBottom: '8px', fontSize: '13px', color: '#666', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' },
    input: {
        width: '100%', padding: '14px', borderRadius: '12px',
        border: '1px solid #e0e0e0', background: '#f9f9f9',
        fontSize: '16px', fontWeight: '500', color: '#333',
        outline: 'none', transition: 'all 0.3s',
        boxSizing: 'border-box'
    },
    btnCheck: {
        position: 'absolute', right: '5px', top: '33px', // Căn chỉnh nút kiểm tra vào trong input
        padding: '8px 15px', borderRadius: '8px',
        border: 'none', background: 'linear-gradient(45deg, #11998e, #38ef7d)',
        color: 'white', fontWeight: 'bold', cursor: 'pointer',
        boxShadow: '0 4px 10px rgba(56, 239, 125, 0.3)'
    },
    recipientBox: {
        marginTop: '10px', padding: '12px', borderRadius: '10px',
        background: 'rgba(56, 239, 125, 0.15)', color: '#007f30',
        display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px'
    },
    btnSubmit: {
        width: '100%', padding: '16px', borderRadius: '15px',
        border: 'none', background: 'linear-gradient(135deg, #0062cc, #00c6ff)',
        color: 'white', fontSize: '18px', fontWeight: 'bold',
        cursor: 'pointer', marginTop: '10px',
        boxShadow: '0 8px 20px rgba(0, 98, 204, 0.4)',
        transition: 'transform 0.2s',
    },
    // BILL STYLES
    billOverlay: {
        position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
        background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(5px)',
        zIndex: 1000, display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px'
    },
    billCard: {
        background: 'white', width: '100%', maxWidth: '380px',
        padding: '0', borderRadius: '0', // Hóa đơn vuông vức hoặc răng cưa
        position: 'relative', overflow: 'hidden',
        boxShadow: '0 25px 50px rgba(0,0,0,0.25)'
    },
    billHeader: {
        background: '#28a745', padding: '30px 20px', textAlign: 'center', color: 'white',
        borderBottom: '5px dashed white' // Giả hiệu ứng xé giấy
    }
  };

  // --- RENDER BILL (HÓA ĐƠN KHI THÀNH CÔNG) ---
  if (transactionResult) {
    return (
        <div style={styles.billOverlay}>
            <div style={styles.billCard} ref={billRef}>
                {/* Phần Xanh lá trên đầu */}
                <div style={styles.billHeader}>
                    <div style={{width: '60px', height: '60px', background: 'white', borderRadius: '50%', margin: '0 auto 15px auto', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
                        <span style={{fontSize: '30px', color: '#28a745'}}>✓</span>
                    </div>
                    <h2 style={{margin: 0, fontSize: '20px'}}>Giao dịch thành công!</h2>
                    <p style={{margin: '10px 0 0 0', opacity: 0.9, fontSize: '14px'}}>{new Date().toLocaleString('vi-VN')}</p>
                    <h1 style={{margin: '15px 0 0 0', fontSize: '32px'}}>{transactionResult.amount.toLocaleString()} VND</h1>
                </div>

                {/* Phần nội dung chi tiết */}
                <div style={{padding: '30px 25px', background: '#fdfdfd'}}>
                    <RowBill label="Mã giao dịch" value={transactionResult.transactionReference} />
                    <RowBill label="Nguồn" value={transactionResult.sourceAccountNumber} />
                    <RowBill label="Người nhận" value={transactionResult.destinationAccountName || transactionResult.destinationAccountNumber} />
                    <RowBill label="Nội dung" value={transactionResult.description} />
                    <RowBill label="Phí GD" value="Miễn phí" isStatus={true} />
                </div>

                {/* Footer Hóa đơn */}
                <div style={{padding: '20px', background: '#f5f5f5', textAlign: 'center'}}>
                    <img src="https://upload.wikimedia.org/wikipedia/commons/d/d0/QR_code_for_mobile_English_Wikipedia.svg" alt="QR Verify" width="60" style={{opacity: 0.7}} />
                    <p style={{fontSize: '10px', color: '#999', margin: '10px 0'}}>HUY BANK - Smart Banking</p>
                </div>
            </div>

            {/* Các nút hành động bên dưới hóa đơn */}
            <div style={{position: 'absolute', bottom: '30px', display: 'flex', gap: '15px'}}>
                <button onClick={handleSaveBill} style={{padding: '12px 25px', borderRadius: '30px', border: 'none', background: 'white', color: '#333', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 5px 15px rgba(0,0,0,0.2)'}}>📸 Lưu ảnh</button>
                <button onClick={() => navigate('/dashboard')} style={{padding: '12px 25px', borderRadius: '30px', border: 'none', background: '#28a745', color: 'white', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 5px 15px rgba(40, 167, 69, 0.4)'}}>Về trang chủ 🏠</button>
            </div>
        </div>
    );
  }

  // --- RENDER FORM CHUYỂN TIỀN ---
  return (
    <div style={styles.wrapper}>
      <div style={styles.container}>
        
        {/* HEADER */}
        <div style={styles.header}>
            <button onClick={() => navigate('/dashboard')} style={{position: 'absolute', left: '20px', top: '25px', background: 'none', border: 'none', color: 'white', fontSize: '24px', cursor: 'pointer', opacity: 0.8}}>⬅</button>
            <h2 style={{margin: 0, fontSize: '20px', fontWeight: '600', letterSpacing: '1px'}}>CHUYỂN KHOẢN</h2>
        </div>

        {/* THẺ ATM ẢO HIỂN THỊ SỐ DƯ */}
        <div style={styles.atmCard}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'start'}}>
                <div style={styles.chip}></div>
                <span style={{fontSize: '16px', fontWeight: 'bold', opacity: 0.8}}>HUY BANK</span>
            </div>
            <div style={{fontSize: '14px', opacity: 0.9, marginBottom: '5px'}}>Số tài khoản nguồn</div>
            <div style={{fontSize: '20px', letterSpacing: '2px', fontWeight: 'bold', marginBottom: '15px', textShadow: '0 2px 4px rgba(0,0,0,0.2)'}}>
                {formData.sourceAccountNumber ? formData.sourceAccountNumber.replace(/(\d{4})/g, '$1 ').trim() : '.... .... ....'}
            </div>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end'}}>
                 <div>
                    <div style={{fontSize: '10px', opacity: 0.8}}>CHỦ THẺ</div>
                    <div style={{fontSize: '14px', fontWeight: 'bold'}}>{user?.accountName || user?.username || 'PHAM DUC HUY'}</div>
                 </div>
                 <div style={{textAlign: 'right'}}>
                     <div style={{fontSize: '10px', opacity: 0.8}}>SỐ DƯ KHẢ DỤNG</div>
                     <div style={{fontSize: '18px', fontWeight: 'bold'}}>{currentBalance.toLocaleString()} {currency}</div>
                 </div>
            </div>
        </div>

        <form onSubmit={handleSubmit} style={styles.formBody}>
            
            {/* BƯỚC 1: NHẬP SỐ TÀI KHOẢN */}
            <div style={styles.inputGroup}>
                <label style={styles.label}>Tài khoản thụ hưởng</label>
                <div style={{position: 'relative'}}>
                    <input 
                        placeholder="Nhập số tài khoản người nhận..." 
                        value={formData.destinationAccountNumber}
                        onChange={e => setFormData({...formData, destinationAccountNumber: e.target.value})} 
                        disabled={isAccountChecked}
                        style={styles.input}
                    />
                    {!isAccountChecked ? (
                        // TRƯỜNG HỢP 1: Nút Kiểm tra
                        <button 
                            type="button" 
                            onClick={handleCheckAccount} 
                            disabled={isLoading} // Chặn click khi đang tải
                            style={{
                                ...styles.btnCheck,
                                // Thêm hiệu ứng mờ đi khi đang tải
                                opacity: isLoading ? 0.7 : 1, 
                                cursor: isLoading ? 'not-allowed' : 'pointer'
                            }}
                        >
                            KIỂM TRA
                        </button>
                    ) : (
                        // TRƯỜNG HỢP 2: Nút Sửa (Reset)
                        <button 
                            type="button" 
                            onClick={handleResetAccount} 
                            disabled={isLoading} // Cũng nên chặn nút này luôn nếu app đang bận
                            style={{
                                ...styles.btnCheck, 
                                background: '#ffc107', 
                                color: '#333',
                                // Giữ hiệu ứng mờ đồng bộ
                                opacity: isLoading ? 0.7 : 1,
                                cursor: isLoading ? 'not-allowed' : 'pointer'
                            }}
                        >
                            SỬA ✏️
                        </button>
                    )}
                </div>
                
                {/* Hiển thị tên người nhận sau khi check */}
                {isAccountChecked && recipientName && (
                    <div style={styles.recipientBox}>
                        <div style={{width: '30px', height: '30px', background: '#d4edda', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>👤</div>
                        <div>
                            <div style={{fontSize: '10px', opacity: 0.7}}>TÊN NGƯỜI NHẬN</div>
                            <strong>{recipientName.toUpperCase()}</strong>
                        </div>
                    </div>
                )}
            </div>

            {/* BƯỚC 2: NHẬP TIỀN & NỘI DUNG */}
            {isAccountChecked && (
                <div style={{animation: 'fadeIn 0.5s'}}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Số tiền muốn chuyển</label>
                        <input 
                            type="number" 
                            placeholder="0 VND" 
                            value={formData.amount} 
                            onChange={e => setFormData({...formData, amount: e.target.value})} 
                            style={{...styles.input, color: '#0062cc', fontWeight: 'bold', fontSize: '20px'}}
                        />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Lời nhắn</label>
                        <input 
                            placeholder="Nhập nội dung chuyển tiền..." 
                            value={formData.description} 
                            onChange={e => setFormData({...formData, description: e.target.value})} 
                            style={styles.input}
                        />
                    </div>

                    {/* CAPTCHA SECTION */}
                    <div style={{background: '#f8f9fa', padding: '15px', borderRadius: '15px', border: '1px dashed #ccc', marginBottom: '20px'}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px'}}>
                             <label style={{...styles.label, margin: 0}}>Mã xác thực</label>
                             <div style={{fontFamily: 'monospace', fontSize: '18px', fontWeight: 'bold', letterSpacing: '3px', background: '#e2e6ea', padding: '5px 10px', borderRadius: '5px', userSelect: 'none'}}>{captchaCode}</div>
                             <span onClick={generateCaptcha} style={{cursor: 'pointer', fontSize: '18px'}}>🔄</span>
                        </div>
                        <input 
                            placeholder="Nhập mã bảo mật ở trên" 
                            value={inputCaptcha} 
                            onChange={e => setInputCaptcha(e.target.value)} 
                            style={{...styles.input, background: 'white'}}
                        />
                    </div>

                    <button type="submit" disabled={isLoading} style={styles.btnSubmit}>
                        {isLoading ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN CHUYỂN TIỀN 🚀'}
                    </button>
                </div>
            )}
        </form>
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

// Component con để render dòng hóa đơn
const RowBill = ({ label, value, isStatus }) => (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', borderBottom: '1px solid #eee', paddingBottom: '8px' }}>
        <span style={{ color: '#888', fontSize: '13px' }}>{label}</span>
        <span style={{ fontWeight: '600', color: isStatus ? '#28a745' : '#333', fontSize: '14px', textAlign: 'right', maxWidth: '60%' }}>
            {value}
        </span>
    </div>
);

// Thêm keyframe cho animation (cần chèn vào file CSS global hoặc dùng style tag)
const styleSheet = document.createElement("style");
styleSheet.innerText = `
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
`;
document.head.appendChild(styleSheet);

export default Transfer;