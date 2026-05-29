<template>
  <div class="campaign-detail">
    <a-page-header :title="campaign.title || '团购详情'" @back="$router.back()" style="background: white;" />
    
    <div v-if="loading" style="text-align: center; padding: 50px;">
      <a-spin />
    </div>
    
    <template v-else>
      <div class="campaign-header">
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
          <h2 style="flex: 1;">{{ campaign.title }}</h2>
          <a-button type="text" shape="circle" @click="shareCampaign" style="color: #FF5A34;"><icon-share-alt size="18" /></a-button>
        </div>
        <div class="campaign-meta">
          <a-tag color="red" style="margin-right: 8px; border-radius: 4px; font-weight: bold;">火热进行中</a-tag>
          <div style="display: flex; align-items: center; color: #F53F3F; font-weight: bold;">
             <icon-clock-circle style="margin-right: 4px;"/> 距结束: 
             <a-countdown :value="new Date(campaign.endTime).getTime()" format="D 天 H 时 m 分 s 秒" :value-style="{color: '#F53F3F', fontSize: '14px', fontWeight: 'bold', marginLeft: '4px'}" />
          </div>
        </div>
        
        <div class="campaign-progress-box" v-if="campaign.targetNum > 0 || (campaign.joinedAvatars && campaign.joinedAvatars.length > 0)">
           <div class="p-text" v-if="campaign.targetNum > 0">
             <span>已跟团 <strong>{{ campaign.currentNum || 0 }}</strong> 人</span>
             <span>目标 <strong>{{ campaign.targetNum }}</strong> 人</span>
           </div>
           <a-progress v-if="campaign.targetNum > 0" :percent="Math.min((campaign.currentNum || 0) / campaign.targetNum, 1)" size="medium" color="#F53F3F" style="margin-bottom: 8px;" />
           
           <div v-if="campaign.joinedAvatars && campaign.joinedAvatars.length > 0" class="joined-avatars-box">
             <a-avatar-group :size="28" :max-count="5">
                <a-avatar v-for="(avatar, idx) in campaign.joinedAvatars" :key="idx">
                  <img :src="avatar" />
                </a-avatar>
              </a-avatar-group>
              <span class="joined-text">等 {{ campaign.currentNum }} 人已买</span>
           </div>
        </div>
        
        <div class="delivery-info">
          <div class="info-item">
            <div class="icon-wrapper"><icon-location /></div>
            <div class="info-content">
              <div class="info-label">自提点</div>
              <div class="info-value">{{ campaign.deliveryLocation?.name }}</div>
              <div class="info-sub">{{ campaign.deliveryLocation?.address }}</div>
            </div>
          </div>
          <div class="info-divider"></div>
          <div class="info-item">
            <div class="icon-wrapper"><icon-clock-circle /></div>
            <div class="info-content">
              <div class="info-label">提货时间</div>
              <div class="info-value">{{ formatTime(campaign.deliveryTime) }}</div>
            </div>
          </div>
        </div>
        
        <div class="intro-box" v-if="campaign.intro">
          <p class="intro-text">{{ campaign.intro }}</p>
        </div>
      </div>
      
      <div class="products-list">
        <h3 class="section-title">选购商品</h3>
        
        <a-card v-for="(item, index) in campaign.products" :key="item.id" class="p-card" :bordered="false">
          <div class="p-row">
            <img :src="item.product.image" class="p-img" />
            <div class="p-info">
              <div class="p-name">{{ item.product.name }}</div>
              <div class="p-price">团购价: <span class="price-val">¥{{ item.groupPrice }}</span></div>
              <div class="p-stock">剩余库存: {{ item.stockLimit === -1 ? '不限' : item.stockLimit }}</div>
            </div>
            <div class="p-action">
              <a-input-number v-model="cart[item.id]" :min="0" :max="item.stockLimit === -1 ? 99 : item.stockLimit" mode="button" size="small" style="width: 100px;" />
            </div>
          </div>
        </a-card>
      </div>
      
      <div class="bottom-bar">
        <div class="total-price">
          合计: <span class="val">¥{{ totalPrice }}</span>
        </div>
        <a-button type="primary" class="bottom-action-btn" shape="round" size="large" @click="openCheckout" :disabled="isEnded || totalPrice <= 0">
          {{ isEnded ? '活动已结束' : '立即跟团' }}
        </a-button>
      </div>
    </template>
    
    <!-- 结账弹窗 -->
    <a-modal title="确认订单" v-model:visible="checkoutVisible" @ok="submitOrder" :ok-loading="submitting">
      <a-form :model="orderForm" layout="vertical">
        <a-form-item label="取货人姓名" required>
          <a-input v-model="orderForm.contactName" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="联系电话" required>
          <a-input v-model="orderForm.contactPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model="orderForm.remark" placeholder="有特殊要求请备注" />
        </a-form-item>
      </a-form>
      <div class="checkout-summary">
        <div>共 {{ totalItems }} 件商品</div>
        <div style="color: #F53F3F; font-weight: bold; font-size: 18px;">¥ {{ totalPrice }}</div>
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
      cart: {}, // { campaignProductId: quantity }
      
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
        // init cart
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
      if (navigator.clipboard) {
        navigator.clipboard.writeText(shareUrl).then(() => {
          Message.success('已复制快团链接，快去分享吧！');
        }).catch(err => {
          Message.error('复制失败，请点击右上角手动分享');
        });
      } else {
        const input = document.createElement('input');
        input.value = shareUrl;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        Message.success('已复制快团链接，快去分享吧！');
      }
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
      // Auto-fill from user profile if available
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
        // Optional: redirect to order list
        setTimeout(() => {
          this.$router.push('/profile'); // temporary, can route to my orders later
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
  background-color: #F4F6F9;
  padding-bottom: 80px;
}
.campaign-header {
  background: white;
  padding: 20px;
  margin-bottom: 12px;
}
.campaign-header h2 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: bold;
}
.campaign-meta {
  margin-bottom: 20px;
  font-size: 13px;
  color: #86909C;
  display: flex;
  align-items: center;
}
.campaign-progress-box {
  background: #FFF2F0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  border: 1px solid #FFCECB;
}
.campaign-progress-box .p-text {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #F53F3F;
  margin-bottom: 8px;
}
.campaign-progress-box .p-text strong {
  font-size: 16px;
}
.joined-avatars-box {
  display: flex;
  align-items: center;
  margin-top: 4px;
}
.joined-text {
  font-size: 13px;
  color: #86909C;
  margin-left: 8px;
}
.delivery-info {
  background: linear-gradient(to right, #F7F8FA, #FFFFFF);
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.icon-wrapper {
  background: #E8F3FF;
  color: #165DFF;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}
.info-content {
  flex-grow: 1;
}
.info-label {
  font-size: 12px;
  color: #86909C;
  margin-bottom: 4px;
}
.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}
.info-sub {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}
.info-divider {
  height: 1px;
  background: #E5E6EB;
  margin: 4px 0;
}
.intro-box {
  background: #FFF7E8;
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 4px solid #FF7D00;
}
.intro-text {
  font-size: 14px;
  color: #4E5969;
  line-height: 1.6;
  margin: 0;
}

.products-list {
  background: white;
  padding: 16px;
}
.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: bold;
}
.p-card {
  margin-bottom: 12px;
  background: #F9F9F9;
  border-radius: 8px;
}
:deep(.p-card > .arco-card-body) {
  padding: 12px;
}
.p-row {
  display: flex;
  gap: 12px;
}
.p-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
}
.p-info {
  flex-grow: 1;
}
.p-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 6px;
}
.p-price {
  font-size: 12px;
  color: #86909C;
}
.price-val {
  color: #F53F3F;
  font-size: 15px;
  font-weight: bold;
}
.p-stock {
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}
.p-action {
  display: flex;
  align-items: center;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 12px 16px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  box-sizing: border-box;
  padding-bottom: calc(12px + var(--safe-bottom));
}
.total-price {
  font-size: 14px;
  color: #1D2129;
}
.total-price .val {
  color: #F53F3F;
  font-size: 20px;
  font-weight: bold;
}
.checkout-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #E5E6EB;
}
</style>
