<template>
  <div class="stats-bar-wrapper">
    <div class="stats-bar-card">
      <div class="stats-item" @click="handleItemClick('points')">
        <span class="stats-val">{{ user ? user.points : '--' }}</span>
        <span class="stats-label">我的积分</span>
      </div>
      <div class="stats-item" @click="handleItemClick('coupons')">
        <span class="stats-val">{{ user ? 0 : '--' }}</span>
        <span class="stats-label">优惠券</span>
      </div>
      <div class="stats-item" @click="handleItemClick('balance')">
        <span class="stats-val">{{ user ? '¥0.00' : '--' }}</span>
        <span class="stats-label">账户余额</span>
      </div>
    </div>
  </div>
</template>

<script>
import { Modal } from '@arco-design/web-vue';

export default {
  name: 'UserStats',
  props: {
    user: Object
  },
  methods: {
    handleItemClick(type) {
      if (!this.user) {
        window.dispatchEvent(new CustomEvent('open-login'));
        return;
      }
      if (type === 'points') {
        Modal.info({
          title: '我的积分与权益',
          content: `您当前拥有 ${this.user.points || 0} 积分。每 10 积分在结算自营现烤商品时可立减 ¥1.00；每天签到、邀请好友或完成拼团均可获取积分。`,
          okText: '知道了'
        });
      } else if (type === 'coupons') {
        Modal.info({
          title: '专属优惠券',
          content: '当前暂无可用优惠券。小柴包酱将不定期发放快团专属早鸟券，敬请关注首页与最新跟团通知！',
          okText: '知道了'
        });
      } else if (type === 'balance') {
        Modal.info({
          title: '账户余额',
          content: '当前账户现金余额为 ¥0.00。小柴包烘焙支持微信扫码与直接结账，无需预充值。',
          okText: '知道了'
        });
      }
    }
  }
}
</script>

<style scoped>
.stats-bar-wrapper {
  margin: 12px 0 0;
  z-index: 10;
  position: relative;
}
.stats-bar-card {
  background: #FFFFFF;
  border-radius: 18px;
  display: flex;
  padding: 16px 0;
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
  margin: 0 auto;
}
.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: transform 0.2s ease;
  position: relative;
}
.stats-item:active {
  transform: scale(0.95);
}
.stats-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background: #F2F3F5;
}
.stats-val {
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.5px;
}
.stats-label {
  font-size: 11px;
  color: #86909C;
  font-weight: 500;
}
</style>
