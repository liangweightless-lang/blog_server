<template>
  <a-modal
    :visible="visible"
    :footer="false"
    :width="360"
    :mask-closable="false"
    @cancel="handleClose"
    modal-class="wechat-pay-modal"
  >
    <div class="wechat-pay-box">
      <!-- 移动端顶部下拉拉手 (Handle Bar) -->
      <div class="sheet-handle-bar"></div>

      <!-- 微信支付品牌头部 -->
      <div class="wechat-header">
        <div class="wechat-logo-wrap">
          <svg class="wechat-icon" viewBox="0 0 24 24" fill="#07C160">
            <path d="M8.691 2.188C3.891 2.188 0 5.478 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .161.13.29.29.29.08 0 .15-.029.212-.068l1.96-1.141a.853.853 0 0 1 .639-.097c.92.251 1.897.39 2.913.39.309 0 .61-.019.91-.048-.718-2.038-.34-4.321 1.07-5.918 1.453-1.639 3.59-2.529 5.82-2.529.418 0 .833.03 1.238.087C16.892 4.398 13.064 2.188 8.691 2.188zm-2.42 4.145c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm4.84 0c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm8.567 4.144c-3.864 0-7.004 2.657-7.004 5.928 0 3.272 3.14 5.928 7.004 5.928.795 0 1.562-.116 2.278-.319a.69.69 0 0 1 .513.078l1.579.919c.05.029.106.048.173.048.13 0 .233-.106.233-.232a.38.38 0 0 0-.039-.175l-.32-1.19a.473.473 0 0 1 .174-.533c1.474-1.085 2.413-2.684 2.413-4.472 0-3.271-3.14-5.93-7.005-5.93zm-2.14 3.428c.552 0 1.007.456 1.007 1.008 0 .551-.455 1.007-1.008 1.007-.551 0-1.007-.456-1.007-1.007 0-.552.456-1.008 1.007-1.008zm4.28 0c.553 0 1.008.456 1.008 1.008 0 .551-.455 1.007-1.008 1.007-.552 0-1.007-.456-1.007-1.007 0-.552.455-1.008 1.007-1.008z"/>
          </svg>
        </div>
        <h3 class="pay-title">微信安全支付</h3>
      </div>

      <!-- 金额区 -->
      <div class="pay-amount-box">
        <span class="currency">¥</span>
        <span class="amount-num">{{ amount }}</span>
      </div>

      <div class="auto-verify-badge">
        <icon-check-circle-fill class="badge-icon" />
        <span>系统已开启全自动对账 · 支付后无需任何操作</span>
      </div>

      <!-- 二维码/收款码展示区 -->
      <div class="qrcode-wrapper">
        <div v-if="paidSuccess" class="paid-success-overlay">
          <icon-check-circle-fill class="success-icon" />
          <p class="success-text">支付成功！</p>
        </div>
        <!-- 1. 优先展示动态生成的二维码 -->
        <img 
          v-else-if="qrDataUrl" 
          :src="qrDataUrl" 
          alt="微信支付二维码"
          class="custom-qrcode-img"
        />
        <!-- 2. 否则展示管理员配置的静态商家收款码图片 -->
        <img 
          v-else-if="merchantQrUrl" 
          :src="merchantQrUrl" 
          alt="微信收款码"
          class="merchant-qrcode-img"
        />
        <div v-else class="qrcode-loading">
          <a-spin dot />
          <p style="margin-top: 10px; font-size: 13px; color: #86909C;">正在加载付款码...</p>
        </div>
      </div>

      <div class="mobile-long-press-tip">
        <icon-scan /> 手机端可<strong>长按识别二维码</strong>或微信扫码支付
      </div>

      <!-- 底部行动按钮 -->
      <div class="modal-footer-actions">
        <button class="paid-done-btn" @click="handleManualCheck" :disabled="checking">
          <icon-loading v-if="checking" :spin="true" />
          <span>{{ checking ? '正在核验支付结果...' : '我已完成支付' }}</span>
        </button>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { checkWechatPayStatus, checkXunhupayStatus } from '@/api/order';
import { getHomeConfig } from '@/api/common';
import { Message } from '@arco-design/web-vue';
import QRCode from 'qrcode';

export default {
  name: 'WechatPayQrModal',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    orderId: {
      type: String,
      default: ''
    },
    amount: {
      type: [String, Number],
      default: '0.00'
    },
    codeUrl: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      timer: null,
      checking: false,
      paidSuccess: false,
      qrDataUrl: '',
      merchantQrUrl: ''
    }
  },
  created() {
    this.fetchMerchantQr();
  },
  watch: {
    show(val) {
      this.visible = val;
      if (val) {
        this.paidSuccess = false;
        this.fetchMerchantQr();
        this.generateQrCode();
        this.startPolling();
      } else {
        this.stopPolling();
      }
    },
    codeUrl() {
      if (this.visible) {
        this.generateQrCode();
      }
    }
  },
  beforeUnmount() {
    this.stopPolling();
  },
  methods: {
    async fetchMerchantQr() {
      try {
        const res = await getHomeConfig();
        if (res.data && res.data.data) {
          this.merchantQrUrl = res.data.data.wechatMerchantQrUrl || res.data.data.wechatQrUrl || '';
        }
      } catch (e) {
        // 静默
      }
    },
    async generateQrCode() {
      if (!this.codeUrl) {
        this.qrDataUrl = '';
        return;
      }
      try {
        this.qrDataUrl = await QRCode.toDataURL(this.codeUrl, {
          width: 200,
          margin: 1,
          color: {
            dark: '#1D2129',
            light: '#FFFFFF'
          }
        });
      } catch (e) {
        console.error('生成二维码失败', e);
      }
    },
    startPolling() {
      this.stopPolling();
      if (!this.orderId) return;

      this.timer = setInterval(async () => {
        try {
          // 双重轮询：微信官方与虎皮椒接口
          const [wechatRes, xunhuRes] = await Promise.all([
            checkWechatPayStatus(this.orderId).catch(() => ({ data: { data: {} } })),
            checkXunhupayStatus(this.orderId).catch(() => ({ data: { data: {} } }))
          ]);
          
          const isPaid = (wechatRes.data && wechatRes.data.data && wechatRes.data.data.paid) ||
                         (xunhuRes.data && xunhuRes.data.data && xunhuRes.data.data.paid);

          if (isPaid) {
            this.handleSuccess();
          }
        } catch (e) {
          // 轮询异常静默
        }
      }, 1500);
    },
    stopPolling() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    async handleManualCheck() {
      if (!this.orderId) return;
      this.checking = true;
      try {
        const [wechatRes, xunhuRes] = await Promise.all([
          checkWechatPayStatus(this.orderId).catch(() => ({ data: { data: {} } })),
          checkXunhupayStatus(this.orderId).catch(() => ({ data: { data: {} } }))
        ]);
        
        const isPaid = (wechatRes.data && wechatRes.data.data && wechatRes.data.data.paid) ||
                       (xunhuRes.data && xunhuRes.data.data && xunhuRes.data.data.paid);

        if (isPaid) {
          this.handleSuccess();
        } else {
          Message.info('正在等待微信到账通知，到账后将自动完成...');
        }
      } catch (e) {
        Message.error('核验支付状态失败');
      } finally {
        this.checking = false;
      }
    },
    handleSuccess() {
      this.stopPolling();
      this.paidSuccess = true;
      Message.success('支付成功！正在为您跳转订单中心');
      setTimeout(() => {
        this.handleClose();
        this.$emit('success');
      }, 1000);
    },
    handleClose() {
      this.stopPolling();
      this.visible = false;
      this.$emit('update:show', false);
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
.wechat-pay-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 0 10px;
  text-align: center;
  position: relative;
}

/* 仿 iOS 底部抽屉顶部拉手条 */
.sheet-handle-bar {
  display: none;
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: #E5E6EB;
  margin: 0 auto 14px auto;
}

@media (max-width: 768px) {
  .sheet-handle-bar {
    display: block;
  }
}

.wechat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.wechat-logo-wrap {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wechat-icon {
  width: 26px;
  height: 26px;
}

.pay-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #1D2129;
}

.pay-amount-box {
  display: flex;
  align-items: baseline;
  justify-content: center;
  color: #07C160;
  margin-bottom: 8px;
}

.currency {
  font-size: 18px;
  font-weight: 700;
  margin-right: 2px;
}

.amount-num {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.auto-verify-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: #E8FFEA;
  color: #00B42A;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid rgba(0, 180, 42, 0.2);
}

.auto-verify-badge .badge-icon {
  font-size: 13px;
}

.qrcode-wrapper {
  position: relative;
  width: 210px;
  height: 210px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #E5E6EB;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(7, 193, 96, 0.1);
  margin-bottom: 14px;
  overflow: hidden;
}

.merchant-qrcode-img,
.custom-qrcode-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.paid-success-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  animation: fadeInScale 0.3s ease;
}

.success-icon {
  font-size: 56px;
  color: #07C160;
  margin-bottom: 10px;
}

.success-text {
  font-size: 17px;
  font-weight: 800;
  color: #07C160;
  margin: 0;
}

.mobile-long-press-tip {
  display: none;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .mobile-long-press-tip {
    display: flex;
  }
}

/* 底部按钮 */
.modal-footer-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.paid-done-btn {
  width: 100%;
  height: 46px;
  border-radius: 23px;
  border: none;
  background: linear-gradient(135deg, #07C160 0%, #059649 100%);
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(7, 193, 96, 0.28);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.paid-done-btn:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}
.paid-done-btn:active {
  transform: scale(0.97);
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.85);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
