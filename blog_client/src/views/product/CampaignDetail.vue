<template>
  <div class="campaign-detail">
    <!-- 自定义顶部导航 -->
    <div class="detail-header">
      <div class="header-back" @click="$router.back()">
        <icon-left style="font-size: 22px;" />
      </div>
      <span class="header-title">{{ campaign.title || '团购详情' }}</span>
      <div class="header-share" @click="shareCampaign">
        <icon-share-alt style="font-size: 18px;" />
      </div>
    </div>

    <div v-if="loading" style="text-align: center; padding: 80px 0;">
      <a-spin />
    </div>

    <template v-else>
      <!-- 活动头部信息区 -->
      <div class="campaign-hero">
        <div class="hero-badge">
          <icon-fire /> 火热进行中
        </div>
        <h1 class="hero-title">{{ campaign.title }}</h1>
        <div class="hero-countdown">
          <icon-clock-circle style="margin-right: 6px;" />
          <span>距结束: </span>
          <a-countdown :value="new Date(campaign.endTime).getTime()" format="D 天 H 时 m 分 s 秒" :value-style="{color: '#FFF', fontSize: '14px', fontWeight: 'bold', marginLeft: '4px'}" />
        </div>
      </div>

      <!-- 跟团进度 -->
      <div class="section-card" v-if="campaign.targetNum > 0 || (campaign.joinedAvatars && campaign.joinedAvatars.length > 0)">
        <div class="progress-header" v-if="campaign.targetNum > 0">
          <span>已跟团 <strong>{{ campaign.currentNum || 0 }}</strong> 人</span>
          <span>目标 <strong>{{ campaign.targetNum }}</strong> 人</span>
        </div>
        <a-progress v-if="campaign.targetNum > 0" :percent="Math.min((campaign.currentNum || 0) / campaign.targetNum, 1)" size="medium" color="#FF4B2B" style="margin-bottom: 8px;" />
        <div v-if="campaign.joinedAvatars && campaign.joinedAvatars.length > 0" class="joined-row">
          <a-avatar-group :size="28" :max-count="5">
            <a-avatar v-for="(avatar, idx) in campaign.joinedAvatars" :key="idx">
              <img :src="avatar" />
            </a-avatar>
          </a-avatar-group>
          <span class="joined-text">等 {{ campaign.currentNum }} 人已买</span>
        </div>
      </div>

      <!-- 自提信息卡片 -->
      <div class="section-card delivery-card">
        <div class="delivery-row">
          <div class="delivery-icon"><icon-location /></div>
          <div class="delivery-content">
            <div class="delivery-label">自提点</div>
            <div class="delivery-value">{{ campaign.deliveryLocation?.name }}</div>
            <div class="delivery-sub">{{ campaign.deliveryLocation?.address }}</div>
          </div>
        </div>
        <div class="delivery-divider"></div>
        <div class="delivery-row">
          <div class="delivery-icon"><icon-clock-circle /></div>
          <div class="delivery-content">
            <div class="delivery-label">提货时间</div>
            <div class="delivery-value">{{ formatTime(campaign.deliveryTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 活动说明 -->
      <div class="section-card intro-card" v-if="campaign.intro">
        <div class="intro-badge"><icon-bulb /> 团长说</div>
        <p class="intro-text">{{ campaign.intro }}</p>
      </div>

      <!-- 商品选购列表 -->
      <div class="section-card products-card">
        <h3 class="card-title"><icon-apps /> 选购商品</h3>
        <div v-for="item in campaign.products" :key="item.id" class="product-item">
          <img :src="item.product.image" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ item.product.name }}</div>
            <div class="product-price-row">
              <span class="product-price">¥{{ item.groupPrice }}</span>
              <span class="product-stock">{{ item.stockLimit === -1 ? '不限量' : `剩余${item.stockLimit}件` }}</span>
            </div>
            <div class="product-qty">
              <a-input-number v-model="cart[item.id]" :min="0" :max="item.stockLimit === -1 ? 99 : item.stockLimit" mode="button" size="small" style="width: 110px;" />
            </div>
          </div>
        </div>
      </div>

      <!-- 底部结账栏 -->
      <div class="bottom-bar">
        <div class="total-section">
          <span class="total-label">合计</span>
          <span class="total-symbol">¥</span>
          <span class="total-value">{{ totalPrice }}</span>
        </div>
        <a-button type="primary" class="checkout-btn" shape="round" size="large" @click="openCheckout" :disabled="isEnded || totalPrice <= 0">
          {{ isEnded ? '活动已结束' : '立即跟团' }}
        </a-button>
      </div>
    </template>

    <!-- 结账弹窗 -->
    <a-modal v-model:visible="checkoutVisible" :footer="false" :header="false" modal-class="checkout-modal" unmount-on-close>
      <div class="checkout-content">
        <h2 class="checkout-title">确认订单</h2>
        <a-form :model="orderForm" layout="vertical">
          <a-form-item label="取货人姓名" required>
            <a-input v-model="orderForm.contactName" placeholder="请输入姓名" size="large" />
          </a-form-item>
          <a-form-item label="联系电话" required>
            <a-input v-model="orderForm.contactPhone" placeholder="请输入手机号" size="large" />
          </a-form-item>
          <a-form-item label="备注">
            <a-input v-model="orderForm.remark" placeholder="有特殊要求请备注" />
          </a-form-item>
        </a-form>
        <div class="checkout-summary">
          <span>共 {{ totalItems }} 件商品</span>
          <span class="checkout-total">¥{{ totalPrice }}</span>
        </div>
        <a-button type="primary" class="submit-order-btn" shape="round" long size="large" :loading="submitting" @click="submitOrder">
          确认下单
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { getCampaignById, createCampaignOrder } from '@/api/campaign';
import { Message } from '@arco-design/web-vue';
import dayjs from 'dayjs';
import { mapState } from 'pinia';
import { useUserStore } from '@/stores/user';

export default {
  name: 'CampaignDetail',
  data() {
    return {
      campaign: {},
      loading: false,
      cart: {},
      checkoutVisible: false,
      submitting: false,
      orderForm: {
        contactName: '',
        contactPhone: '',
        remark: ''
      }
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    isEnded() {
      if (!this.campaign.endTime) return false;
      return new Date().getTime() >= new Date(this.campaign.endTime).getTime();
    },
    totalPrice() {
      let total = 0;
      if (!this.campaign.products) return 0;
      for (const p of this.campaign.products) {
        if (this.cart[p.id]) {
          total += p.groupPrice * this.cart[p.id];
        }
      }
      return total.toFixed(2);
    },
    totalItems() {
      let count = 0;
      for (const id in this.cart) {
        count += this.cart[id] || 0;
      }
      return count;
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    formatTime(t) {
      return t ? dayjs(t).format('MM月DD日 HH:mm') : '-';
    },
    async fetchData() {
      const id = this.$route.params.id;
      if (!id) return;
      this.loading = true;
      try {
        const res = await getCampaignById(id);
        this.campaign = res.data.data;
        if (this.campaign.products) {
          this.campaign.products.forEach(p => {
            this.cart[p.id] = 0;
          });
        }
      } catch (e) {
        Message.error('获取团购失败');
      } finally {
        this.loading = false;
      }
    },
    shareCampaign() {
      const shareUrl = window.location.href;
      const copyToClipboard = (text) => {
        if (navigator.clipboard && window.isSecureContext) {
          return navigator.clipboard.writeText(text);
        } else {
          const textArea = document.createElement("textarea");
          textArea.value = text;
          textArea.style.position = "fixed";
          textArea.style.left = "-9999px";
          document.body.appendChild(textArea);
          textArea.focus();
          textArea.select();
          try {
            const ok = document.execCommand('copy');
            document.body.removeChild(textArea);
            return ok ? Promise.resolve() : Promise.reject();
          } catch (err) {
            document.body.removeChild(textArea);
            return Promise.reject(err);
          }
        }
      };
      copyToClipboard(shareUrl).then(() => {
        Message.success('已复制快团链接，快去分享吧！');
      }).catch(() => {
        Message.error('复制失败，请手动复制链接分享');
      });
    },
    openCheckout() {
      if (!this.userInfo) {
        Message.warning('请先登录');
        return;
      }
      if (this.totalItems <= 0) {
        Message.warning('请先选择商品');
        return;
      }
      this.orderForm.contactName = this.userInfo.nickname || '';
      this.checkoutVisible = true;
    },
    async submitOrder() {
      if (!this.orderForm.contactName || !this.orderForm.contactPhone) {
        Message.warning('请填写取货人和电话');
        return false;
      }
      this.submitting = true;
      try {
        const items = [];
        this.campaign.products.forEach(p => {
          const qty = this.cart[p.id];
          if (qty > 0) {
            items.push({
              productId: p.productId,
              productName: p.product.name,
              productImage: p.product.image,
              price: p.groupPrice,
              quantity: qty
            });
          }
        });
        const payload = {
          totalAmount: this.totalPrice,
          contactName: this.orderForm.contactName,
          contactPhone: this.orderForm.contactPhone,
          remark: this.orderForm.remark,
          items: items
        };
        await createCampaignOrder(this.campaign.id, payload);
        Message.success('跟团成功！请按时前往自提点取货。');
        this.checkoutVisible = false;
        setTimeout(() => {
          this.$router.push('/profile');
        }, 1500);
      } catch (e) {
        Message.error('跟团失败');
        return false;
      } finally {
        this.submitting = false;
      }
    }
  }
}
</script>

<style scoped>
.campaign-detail {
  min-height: 100vh;
  background: transparent;
  padding-bottom: 100px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}
.header-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;
  color: #1D2129;
}
.header-back:active {
  transform: scale(0.9);
  background: rgba(0, 0, 0, 0.08);
}
.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
  max-width: 60%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.header-share {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 75, 43, 0.08);
  color: #FF4B2B;
  cursor: pointer;
  transition: all 0.2s;
}
.header-share:active {
  transform: scale(0.9);
}

.campaign-hero {
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  padding: 32px 24px 28px;
  color: white;
  position: relative;
  overflow: hidden;
}
.campaign-hero::after {
  content: '';
  position: absolute;
  bottom: -40px;
  right: -40px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.campaign-hero::before {
  content: '';
  position: absolute;
  top: -30px;
  left: -30px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}
.hero-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 16px 0;
  line-height: 1.4;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
  z-index: 1;
}
.hero-countdown {
  display: inline-flex;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  padding: 6px 14px;
  border-radius: 12px;
  position: relative;
  z-index: 1;
}

.section-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 20px;
  margin: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #FF4B2B;
  margin-bottom: 8px;
  font-weight: 600;
}
.progress-header strong {
  font-size: 18px;
  font-weight: 800;
}
.joined-row {
  display: flex;
  align-items: center;
  margin-top: 8px;
  background: rgba(255, 75, 43, 0.05);
  padding: 8px 12px;
  border-radius: 12px;
}
.joined-text {
  font-size: 13px;
  color: #86909C;
  margin-left: 8px;
}

.delivery-card {
  background: linear-gradient(180deg, rgba(255,255,255,0.8) 0%, rgba(255,247,245,0.7) 100%);
}
.delivery-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.delivery-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%);
  color: #FF4B2B;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.delivery-content {
  flex: 1;
}
.delivery-label {
  font-size: 12px;
  color: #86909C;
  margin-bottom: 4px;
  font-weight: 600;
}
.delivery-value {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
}
.delivery-sub {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}
.delivery-divider {
  height: 1px;
  background: rgba(0,0,0,0.05);
  margin: 14px 0 14px 50px;
}

.intro-card {
  background: linear-gradient(135deg, rgba(255,247,232,0.8) 0%, rgba(255,240,214,0.6) 100%);
  border: 1px solid rgba(255, 200, 100, 0.3);
}
.intro-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #FA8C16;
  margin-bottom: 10px;
}
.intro-text {
  font-size: 14px;
  color: #4E5969;
  line-height: 1.8;
  margin: 0;
}

.products-card {
  padding-bottom: 12px;
}
.card-title {
  margin: 0 0 16px 0;
  font-size: 17px;
  font-weight: 800;
  color: #1D2129;
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title svg {
  color: #FF4B2B;
}
.product-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  margin-bottom: 12px;
  border: 1px solid rgba(0,0,0,0.03);
  transition: all 0.2s ease;
}
.product-item:last-child {
  margin-bottom: 0;
}
.product-item:active {
  background: rgba(255, 255, 255, 0.9);
  transform: scale(0.99);
}
.product-img {
  width: 88px;
  height: 88px;
  border-radius: 14px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.product-name {
  font-weight: 700;
  font-size: 15px;
  color: #1D2129;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 6px;
}
.product-price {
  color: #FF4B2B;
  font-size: 18px;
  font-weight: 800;
}
.product-stock {
  font-size: 12px;
  color: #86909C;
  background: rgba(0,0,0,0.03);
  padding: 2px 8px;
  border-radius: 6px;
}
.product-qty {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.04);
  max-width: 1000px;
  margin: 0 auto;
}
.total-section {
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.total-label {
  font-size: 14px;
  color: #86909C;
  margin-right: 8px;
  font-weight: 600;
}
.total-symbol {
  color: #FF4B2B;
  font-size: 16px;
  font-weight: 700;
}
.total-value {
  color: #FF4B2B;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
}
.checkout-btn {
  height: 48px;
  padding: 0 36px;
  font-size: 16px;
  font-weight: 800;
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%)) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(255, 75, 43, 0.35);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.checkout-btn:not([disabled]):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 75, 43, 0.45);
}
.checkout-btn:active {
  transform: scale(0.97);
}
.checkout-btn[disabled] {
  background: #C9CDD4 !important;
  box-shadow: none;
}

:deep(.checkout-modal) {
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.checkout-content {
  padding: 10px;
}
.checkout-title {
  font-size: 20px;
  font-weight: 800;
  color: #1D2129;
  text-align: center;
  margin: 0 0 24px 0;
}
.checkout-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px dashed rgba(0,0,0,0.08);
  margin-bottom: 20px;
  font-size: 14px;
  color: #86909C;
}
.checkout-total {
  color: #FF4B2B;
  font-size: 24px;
  font-weight: 800;
}
.submit-order-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 800;
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%)) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(255, 75, 43, 0.3);
}

@media (max-width: 768px) {
  .section-card {
    margin: 12px;
    padding: 16px;
    border-radius: 16px;
  }
  .campaign-hero {
    padding: 28px 20px 24px;
  }
  .hero-title {
    font-size: 20px;
  }
  .product-img {
    width: 76px;
    height: 76px;
  }
  .product-item {
    padding: 12px;
  }
  :deep(.checkout-modal) {
    position: absolute !important;
    bottom: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    border-radius: 24px 24px 0 0;
    padding-bottom: env(safe-area-inset-bottom);
    animation: slideUpModal 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  }
}

@keyframes slideUpModal {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
