<template>
  <a-modal
    :title="null"
    :header="false"
    :closable="false"
    :visible="visible"
    :footer="false"
    :width="380"
    :mask-closable="false"
    @cancel="handleClose"
    modal-class="universal-cashier-modal"
  >
    <div class="cashier-pay-box">
      <!-- 移动端顶部下拉拉手 (Handle Bar) -->
      <div class="sheet-handle-bar"></div>
      <button class="sheet-circle-close" @click="handleClose" aria-label="关闭">
        <icon-close />
      </button>

      <!-- 支付方式切换微胶囊 Segment Tabs (微信支付 / 支付宝走虎皮椒全自动) -->
      <div class="pay-method-tabs">
        <button 
          class="pay-method-tab" 
          :class="{ active: payChannel === 'wechat' }" 
          @click="switchChannel('wechat')"
        >
          <svg class="tab-icon" viewBox="0 0 24 24" fill="#07C160">
            <path d="M8.691 2.188C3.891 2.188 0 5.478 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .161.13.29.29.29.08 0 .15-.029.212-.068l1.96-1.141a.853.853 0 0 1 .639-.097c.92.251 1.897.39 2.913.39.309 0 .61-.019.91-.048-.718-2.038-.34-4.321 1.07-5.918 1.453-1.639 3.59-2.529 5.82-2.529.418 0 .833.03 1.238.087C16.892 4.398 13.064 2.188 8.691 2.188zm-2.42 4.145c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm4.84 0c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm8.567 4.144c-3.864 0-7.004 2.657-7.004 5.928 0 3.272 3.14 5.928 7.004 5.928.795 0 1.562-.116 2.278-.319a.69.69 0 0 1 .513.078l1.579.919c.05.029.106.048.173.048.13 0 .233-.106.233-.232a.38.38 0 0 0-.039-.175l-.32-1.19a.473.473 0 0 1 .174-.533c1.474-1.085 2.413-2.684 2.413-4.472 0-3.271-3.14-5.93-7.005-5.93zm-2.14 3.428c.552 0 1.007.456 1.007 1.008 0 .551-.455 1.007-1.008 1.007-.551 0-1.007-.456-1.007-1.007 0-.552.456-1.008 1.007-1.008zm4.28 0c.553 0 1.008.456 1.008 1.008 0 .551-.455 1.007-1.008 1.007-.552 0-1.007-.456-1.007-1.007 0-.552.455-1.008 1.007-1.008z"/>
          </svg>
          <span>微信支付</span>
        </button>
        <button 
          class="pay-method-tab" 
          :class="{ active: payChannel === 'alipay' }" 
          @click="switchChannel('alipay')"
        >
          <svg class="tab-icon" viewBox="0 0 24 24" fill="#1677FF">
            <path d="M21.42 16.29c-.77-.33-2.82-1.2-4.14-1.74-.83 1.54-1.87 3.01-3.11 4.35 3.32-.4 5.92-1.78 7.25-2.61zm-1.89-6.31h-4.99V8.65h6.14V7.08h-6.14V3.86h-1.8v3.22H7.49v1.57h5.25v1.33H5.97v1.57h9.87c-.52 1.48-1.28 2.89-2.26 4.17-1.57-.89-3.08-1.88-4.43-2.95l-1.12 1.25c1.47 1.17 3.12 2.25 4.86 3.22-1.87 1.76-4.04 3.05-6.42 3.82l.86 1.46c2.72-.92 5.21-2.43 7.34-4.43 2.19 1.01 4.54 1.94 6.79 2.5l.65-1.57c-1.86-.48-3.79-1.28-5.63-2.17 1.18-1.46 2.11-3.09 2.74-4.82h3.33v-1.57z"/>
          </svg>
          <span>支付宝支付</span>
        </button>
      </div>

      <!-- 金额展示区 -->
      <div class="pay-amount-box">
        <span class="currency">¥</span>
        <span class="amount-num">{{ amount }}</span>
      </div>

      <div class="auto-verify-badge">
        <icon-check-circle-fill class="badge-icon" />
        <span>{{ payChannel === 'alipay' ? '虎皮椒支付宝全自动对账 · 支付后无需等待' : '微信官方对账 · 支付后自动核销' }}</span>
      </div>

      <!-- 二维码 / 收款码展示区 (完全解除长按限制，支持微信长按识别) -->
      <div class="qrcode-touch-container">
        <div class="qrcode-wrapper">
          <div v-if="paidSuccess" class="paid-success-overlay">
            <icon-check-circle-fill class="success-icon" />
            <p class="success-text">支付成功！</p>
          </div>

          <template v-else>
            <!-- 1. 微信支付：优先展示后台设置的微信商家收款码 / 官方动态码 -->
            <template v-if="payChannel === 'wechat'">
              <img 
                v-if="wechatMerchantQrUrl" 
                :src="wechatMerchantQrUrl" 
                alt="微信商家收款码"
                class="real-touch-qrcode-img"
                draggable="true"
                @click="previewQr(wechatMerchantQrUrl)"
              />
              <img 
                v-else-if="qrDataUrl" 
                :src="qrDataUrl" 
                alt="微信支付码"
                class="real-touch-qrcode-img"
                draggable="true"
                @click="previewQr(qrDataUrl)"
              />
              <div v-else class="qrcode-loading">
                <a-spin dot />
                <p style="margin-top: 10px; font-size: 13px; color: #86909C;">正在加载微信付款码...</p>
              </div>
            </template>

            <!-- 2. 支付宝支付：展示虎皮椒动态码 / 支付宝收款码 -->
            <template v-else>
              <img 
                v-if="qrDataUrl" 
                :src="qrDataUrl" 
                alt="支付宝付款码"
                class="real-touch-qrcode-img"
                draggable="true"
                @click="previewQr(qrDataUrl)"
              />
              <img 
                v-else-if="alipayMerchantQrUrl" 
                :src="alipayMerchantQrUrl" 
                alt="支付宝收款码"
                class="real-touch-qrcode-img"
                draggable="true"
                @click="previewQr(alipayMerchantQrUrl)"
              />
              <div v-else class="qrcode-loading">
                <a-spin dot />
                <p style="margin-top: 10px; font-size: 13px; color: #86909C;">正在生成支付宝付款码...</p>
              </div>
            </template>
          </template>
        </div>
      </div>

      <!-- 快捷行动大胶囊按钮 (微信与支付宝专属双通道) -->
      <div class="quick-app-jump-wrap">
        <!-- 支付宝模式：一键唤起支付宝 App 极速扣款 -->
        <a 
          v-if="payChannel === 'alipay' && alipayJumpUrl"
          :href="alipayJumpUrl" 
          class="quick-jump-btn btn-alipay"
          target="_blank"
        >
          <svg class="jump-mini-icon" viewBox="0 0 24 24" fill="#FFFFFF">
            <path d="M21.42 16.29c-.77-.33-2.82-1.2-4.14-1.74-.83 1.54-1.87 3.01-3.11 4.35 3.32-.4 5.92-1.78 7.25-2.61zm-1.89-6.31h-4.99V8.65h6.14V7.08h-6.14V3.86h-1.8v3.22H7.49v1.57h5.25v1.33H5.97v1.57h9.87c-.52 1.48-1.28 2.89-2.26 4.17-1.57-.89-3.08-1.88-4.43-2.95l-1.12 1.25c1.47 1.17 3.12 2.25 4.86 3.22-1.87 1.76-4.04 3.05-6.42 3.82l.86 1.46c2.72-.92 5.21-2.43 7.34-4.43 2.19 1.01 4.54 1.94 6.79 2.5l.65-1.57c-1.86-.48-3.79-1.28-5.63-2.17 1.18-1.46 2.11-3.09 2.74-4.82h3.33v-1.57z"/>
          </svg>
          <span>一键唤起支付宝 App 支付</span>
        </a>

        <!-- 微信模式：一键保存付款码到相册并直接唤起微信 App -->
        <button 
          v-else-if="payChannel === 'wechat'"
          class="quick-jump-btn btn-wechat"
          @click="handleSaveAndOpenWechat"
        >
          <svg class="jump-mini-icon" viewBox="0 0 24 24" fill="#FFFFFF">
            <path d="M8.691 2.188C3.891 2.188 0 5.478 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .161.13.29.29.29.08 0 .15-.029.212-.068l1.96-1.141a.853.853 0 0 1 .639-.097c.92.251 1.897.39 2.913.39.309 0 .61-.019.91-.048-.718-2.038-.34-4.321 1.07-5.918 1.453-1.639 3.59-2.529 5.82-2.529.418 0 .833.03 1.238.087C16.892 4.398 13.064 2.188 8.691 2.188zm-2.42 4.145c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm4.84 0c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm8.567 4.144c-3.864 0-7.004 2.657-7.004 5.928 0 3.272 3.14 5.928 7.004 5.928.795 0 1.562-.116 2.278-.319a.69.69 0 0 1 .513.078l1.579.919c.05.029.106.048.173.048.13 0 .233-.106.233-.232a.38.38 0 0 0-.039-.175l-.32-1.19a.473.473 0 0 1 .174-.533c1.474-1.085 2.413-2.684 2.413-4.472 0-3.271-3.14-5.93-7.005-5.93zm-2.14 3.428c.552 0 1.007.456 1.007 1.008 0 .551-.455 1.007-1.008 1.007-.551 0-1.007-.456-1.007-1.007 0-.552.456-1.008 1.007-1.008zm4.28 0c.553 0 1.008.456 1.008 1.008 0 .551-.455 1.007-1.008 1.007-.552 0-1.007-.456-1.007-1.007 0-.552.455-1.008 1.007-1.008z"/>
          </svg>
          <span>保存二维码并打开微信</span>
        </button>
      </div>

      <div class="mobile-long-press-tip">
        <icon-scan /> {{ payChannel === 'wechat' ? '微信内长按可直接识别，外部可点击上方按钮唤起' : '手机端可点击上方按钮唤起支付宝，或扫码支付' }}
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
import { checkWechatPayStatus, checkXunhupayStatus, createXunhupay } from '@/api/order';
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
  emits: ['update:show', 'success'],
  data() {
    return {
      visible: false,
      payChannel: 'wechat', // 'wechat' | 'alipay'
      timer: null,
      checking: false,
      paidSuccess: false,
      qrDataUrl: '',
      alipayJumpUrl: '',
      wechatMerchantQrUrl: '',
      alipayMerchantQrUrl: ''
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
        this.initCurrentPay();
        this.startPolling();
      } else {
        this.stopPolling();
      }
    },
    codeUrl() {
      if (this.visible && this.payChannel === 'wechat') {
        this.initCurrentPay();
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
          const cfg = res.data.data;
          this.wechatMerchantQrUrl = cfg.wechatMerchantQrUrl || cfg.wechatQrUrl || '';
          this.alipayMerchantQrUrl = cfg.alipayMerchantQrUrl || cfg.alipayQrUrl || '';
        }
      } catch (e) {
        // 静默
      }
    },
    async switchChannel(channel) {
      if (this.payChannel === channel) return;
      this.payChannel = channel;
      this.qrDataUrl = '';
      this.alipayJumpUrl = '';
      await this.initCurrentPay();
    },
    async initCurrentPay() {
      if (!this.orderId) return;

      if (this.payChannel === 'alipay') {
        try {
          const res = await createXunhupay(this.orderId, 'alipay');
          const data = res.data?.data;
          if (data) {
            this.alipayJumpUrl = data.payUrl || data.url || '';
            const targetQr = data.qrUrl || data.url_qrcode || data.payUrl || data.url;
            if (targetQr) {
              this.qrDataUrl = await QRCode.toDataURL(targetQr, { width: 220, margin: 1 });
            }
          }
        } catch (e) {
          console.error('获取支付宝付款码失败', e);
        }
      } else {
        // 微信模式：如果传入了官方 codeUrl 动态码，则渲染；否则展示后台设置的微信商家收款码 wechatMerchantQrUrl
        if (this.codeUrl) {
          try {
            this.qrDataUrl = await QRCode.toDataURL(this.codeUrl, {
              width: 220,
              margin: 1,
              color: { dark: '#1D2129', light: '#FFFFFF' }
            });
          } catch (e) {
            console.error('生成微信二维码失败', e);
          }
        }
      }
    },
    previewQr(imgUrl) {
      if (!imgUrl) return;
      // 点击图片可全屏打开方便长按
      window.open(imgUrl, '_blank');
    },
    handleSaveAndOpenWechat() {
      const qrImg = this.wechatMerchantQrUrl || this.qrDataUrl;
      if (qrImg) {
        // 自动触发下载收款码图片
        const link = document.createElement('a');
        link.href = qrImg;
        link.download = `小柴包微信付款码_${this.amount}元.jpg`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        Message.info({
          content: '📸 付款码已保存至相册，正在为您打开微信...',
          duration: 2500
        });
      }

      // 1 秒后自动拉起微信 App
      setTimeout(() => {
        window.location.href = 'weixin://';
      }, 500);
    },
    startPolling() {
      this.stopPolling();
      if (!this.orderId) return;

      this.timer = setInterval(async () => {
        try {
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
          // 轮询静默
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
          Message.info('正在等待到账通知，若已扣款请稍等1-2秒即可自动更新');
        }
      } catch (e) {
        Message.error('查询异常，请稍后重试');
      } finally {
        this.checking = false;
      }
    },
    handleSuccess() {
      this.paidSuccess = true;
      this.stopPolling();
      Message.success('🎉 支付成功！');
      setTimeout(() => {
        this.visible = false;
        this.$emit('update:show', false);
        this.$emit('success');
      }, 1200);
    },
    handleClose() {
      this.stopPolling();
      this.visible = false;
      this.$emit('update:show', false);
    }
  }
}
</script>

<style scoped>
.cashier-pay-box {
  padding: 16px 18px 24px;
  position: relative;
  text-align: center;
}

.sheet-handle-bar {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: #E5E6EB;
  margin: 0 auto 12px;
}

.sheet-circle-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
}
.sheet-circle-close:active {
  background: #E5E6EB;
  transform: scale(0.92);
}

/* 支付渠道微胶囊 Tabs */
.pay-method-tabs {
  display: flex;
  background: #F2F3F5;
  border-radius: 14px;
  padding: 3px;
  margin: 4px 0 16px;
}

.pay-method-tab {
  flex: 1;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #86909C;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.pay-method-tab.active {
  background: #FFFFFF;
  color: #1D2129;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tab-icon {
  width: 18px;
  height: 18px;
}

.pay-amount-box {
  display: flex;
  align-items: baseline;
  justify-content: center;
  color: #1D2129;
  margin-bottom: 8px;
}
.currency {
  font-size: 20px;
  font-weight: 800;
  margin-right: 2px;
}
.amount-num {
  font-size: 36px;
  font-weight: 900;
  letter-spacing: -0.5px;
}

.auto-verify-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(7, 193, 96, 0.08);
  color: #07C160;
  font-size: 11px;
  font-weight: 700;
  padding: 5px 12px;
  border-radius: 20px;
  margin-bottom: 16px;
}
.badge-icon {
  font-size: 13px;
}

/* 核心：解除图片长按限制，确保微信与系统能弹出长按识别/保存图片菜单 */
.qrcode-touch-container {
  display: flex;
  justify-content: center;
  -webkit-touch-callout: default !important;
  user-select: auto !important;
  -webkit-user-select: auto !important;
}

.qrcode-wrapper {
  position: relative;
  width: 210px;
  height: 210px;
  margin: 0 auto;
  padding: 10px;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-touch-callout: default !important;
  user-select: auto !important;
  -webkit-user-select: auto !important;
  pointer-events: auto !important;
}

.real-touch-qrcode-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
  -webkit-touch-callout: default !important;
  user-select: auto !important;
  -webkit-user-select: auto !important;
  pointer-events: auto !important;
  cursor: pointer;
}

.paid-success-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #07C160;
}
.success-icon {
  font-size: 56px;
}
.success-text {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
}

.quick-app-jump-wrap {
  margin-top: 14px;
}

.quick-jump-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 42px;
  border-radius: 21px;
  color: #FFFFFF;
  border: none;
  text-decoration: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-jump-btn.btn-alipay {
  background: linear-gradient(135deg, #1677FF 0%, #0958D9 100%);
  box-shadow: 0 4px 14px rgba(22, 119, 255, 0.35);
}
.quick-jump-btn.btn-wechat {
  background: linear-gradient(135deg, #07C160 0%, #059649 100%);
  box-shadow: 0 4px 14px rgba(7, 193, 96, 0.35);
}
.quick-jump-btn:active {
  transform: scale(0.96);
}

.jump-mini-icon {
  width: 16px;
  height: 16px;
}

.mobile-long-press-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #86909C;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.mobile-long-press-tip strong {
  color: #1D2129;
}

.modal-footer-actions {
  margin-top: 20px;
}

.paid-done-btn {
  width: 100%;
  height: 46px;
  border-radius: 23px;
  background: linear-gradient(135deg, #1A1D20 0%, #2D3139 100%);
  color: #FFFFFF;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}
.paid-done-btn:active:not(:disabled) {
  transform: scale(0.97);
}
.paid-done-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
