<template>
  <div class="store-container">

    <!-- 社区快团区 -->
    <div v-if="campaigns.length > 0" class="group-buy-section">
      <h2 class="section-title" style="color: #FF5A34;"><icon-fire /> 社区快团</h2>
      <a-grid :cols="{ xs: 1, sm: 2, md: 3 }" :colGap="20" :rowGap="20">
        <a-grid-item v-for="campaign in campaigns" :key="campaign.id">
          <a-card class="campaign-card" hoverable :bordered="false" :body-style="{ padding: '20px' }" @click="$router.push(`/campaign/${campaign.id}`)" style="cursor: pointer;">
            <div class="campaign-header">
              <h3 class="campaign-title">{{ campaign.title }}</h3>
              <a-tag color="red" size="small" class="campaign-tag">进行中</a-tag>
            </div>
            <div class="campaign-details">
              <div class="detail-row">
                <icon-location class="detail-icon" /> 
                <span class="detail-text">提货点: <span class="detail-highlight">{{ campaign.deliveryLocation?.name || '未知' }}</span></span>
              </div>
              <div class="detail-row">
                <icon-clock-circle class="detail-icon" /> 
                <div class="detail-text" style="display: flex; align-items: center;">
                  距结束: <a-countdown :value="new Date(campaign.endTime).getTime()" format="D 天 H 时 m 分 s 秒" :value-style="{color: '#FF5A34', fontSize: '13px', fontWeight: 'bold', marginLeft: '4px'}" />
                </div>
              </div>
            </div>
            
            <div class="campaign-progress" v-if="campaign.targetNum > 0" style="margin-bottom: 12px;">
               <div style="display: flex; justify-content: space-between; font-size: 12px; color: #86909c; margin-bottom: 4px;">
                 <span>已跟团 {{ campaign.currentNum || 0 }} 人</span>
                 <span>目标 {{ campaign.targetNum }} 人</span>
               </div>
               <a-progress :percent="Math.min((campaign.currentNum || 0) / campaign.targetNum, 1)" size="small" color="#FF5A34" />
            </div>
            
            <div v-if="campaign.joinedAvatars && campaign.joinedAvatars.length > 0" class="joined-avatars">
              <a-avatar-group :size="24" :max-count="5">
                <a-avatar v-for="(avatar, idx) in campaign.joinedAvatars" :key="idx">
                  <img :src="avatar" />
                </a-avatar>
              </a-avatar-group>
              <span class="joined-text">等 {{ campaign.currentNum }} 人已跟团</span>
            </div>
            
            <div class="campaign-products-preview" v-if="campaign.products && campaign.products.length > 0">
              <div class="preview-imgs">
                <img v-for="cp in campaign.products.slice(0, 4)" :key="cp.id" :src="cp.product?.image" class="preview-img" />
                <div v-if="campaign.products.length > 4" class="preview-more">+{{ campaign.products.length - 4 }}</div>
              </div>
              <div class="preview-text">
                <span class="price-start">¥{{ getMinPrice(campaign) }}<span class="price-suffix">起</span></span>
                <span class="count-text">共 {{ campaign.products.length }} 款</span>
              </div>
            </div>
            
            <a-button type="primary" class="campaign-btn" shape="round" long @click.stop="$router.push(`/campaign/${campaign.id}`)">立即跟团</a-button>
          </a-card>
        </a-grid-item>
      </a-grid>
    </div>

    <!-- 普通单品区 -->
    <div class="product-grid" style="margin-top: 32px;">
      <h2 class="section-title" style="color: #1D2129; margin-bottom: 16px;"><icon-apps /> 发现好物 <span style="font-size: 13px; color: #86909C; font-weight: normal; margin-left: 8px;">单品自由选购</span></h2>
      <a-grid :cols="{ xs: 2, sm: 2, md: 3 }" :colGap="12" :rowGap="24">
      <a-grid-item v-for="product in products" :key="product.id" class="product-col">
        <a-card class="product-card" hoverable :bordered="false" :body-style="{ padding: '0px' }">
          <div class="product-image-wrapper" @click="$router.push(`/product/${product.id}`)">
            <img :src="product.image" class="product-image" :alt="product.name" />
            <div class="product-badge" v-if="product.isDigital">品牌甄选</div>
          </div>
          <div class="product-info">
            <div class="product-clickable" @click="$router.push(`/product/${product.id}`)">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
            </div>
            <div class="product-bottom">
              <span class="product-price">¥{{ product.price }}</span>
              <div class="button-group">
                <a-button type="text" style="color: #E6A23C; font-size: 12px; padding: 0 4px;" @click="handleRedeem(product)">1000积分兑换</a-button>
                <a-button type="primary" size="small" shape="round" class="buy-btn" @click="handleBuy(product)">立即购买</a-button>
                <a-button v-if="isMonday" type="primary" status="warning" size="small" shape="round" class="group-btn" @click="handleStartGroup(product)">发起拼团</a-button>
              </div>
            </div>
          </div>
        </a-card>
      </a-grid-item>
    </a-grid>
    </div>

    <!-- 订单确认业务组件 (已封装) -->
    <ProductBuyModal 
      v-model:show="buyDialogVisible" 
      :product="currentProduct"
      @success="fetchActiveGroups" />

    <!-- 拼团确认弹窗 -->
    <GroupActionModal 
      v-model:show="groupDialogVisible"
      :actionType="isJoiningGroup ? 'join' : 'start'"
      :product="currentProduct"
      :groupId="currentGroupId"
      @success="handleGroupSuccess" />
      
    <!-- 管理员专属发起团购按钮 (快团团模式) -->
    <div v-if="isAdmin" class="admin-fab-mini" @click="$router.push('/admin/campaign/create')">
      <icon-plus />
    </div>
  </div>
</template>

<script>
import { getActiveGroups, getProducts } from '@/api/product';
import { getCampaigns } from '@/api/campaign';
import { redeemOrder } from '@/api/order';
import { Message } from '@arco-design/web-vue';
import ProductBuyModal from '@/components/product/ProductBuyModal.vue';
import GroupActionModal from '@/components/product/GroupActionModal.vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

export default {
  name: 'Store',
  components: {
    ProductBuyModal,
    GroupActionModal
  },
  data() {
    return {
      buyDialogVisible: false,
      groupDialogVisible: false,
      isJoiningGroup: false,
      currentProduct: {},
      products: [],
      currentGroupId: null,
      activeGroups: [],
      campaigns: [], // 社区快团数据
      isMonday: new Date().getDay() === 1,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    isAdmin() {
      return this.userInfo && this.userInfo.role === 'ADMIN';
    }
  },
  created() {
    this.fetchProducts().then(() => {
      const buyId = this.$route.query.buyProductId;
      if (buyId) {
        const product = this.products.find(p => p.id == buyId);
        if (product) {
          this.handleBuy(product);
        }
      }
    });
    this.fetchActiveGroups();
    this.fetchCampaigns();
  },
  methods: {
    ...mapActions(useUserStore, ['updatePoints']),
    getMinPrice(campaign) {
      if (!campaign.products || campaign.products.length === 0) return 0;
      return Math.min(...campaign.products.map(p => p.groupPrice)).toFixed(2);
    },
    async fetchActiveGroups() {
      try {
        const res = await getActiveGroups();
        this.activeGroups = res.data.data;
      } catch (error) {
        console.error('获取单品拼团列表失败');
      }
    },
    async fetchCampaigns() {
      try {
        const res = await getCampaigns();
        // 只显示状态为 1 (进行中) 的快团
        this.campaigns = (res.data.data || []).filter(c => c.status === 1);
      } catch (error) {
        console.error('获取社区快团列表失败');
      }
    },
    formatTime(time) {
      if (!time) return '未知';
      return dayjs(time).format('MM-DD HH:mm');
    },
    async handleStartGroup(product) {
      if (!this.userInfo) {
        return Message.warning('请先登录再发起拼团');
      }
      this.currentProduct = product;
      this.isJoiningGroup = false;
      this.prepareOrder();
      this.groupDialogVisible = true;
    },
    async handleJoinGroup(group) {
      this.$router.push(`/product/group/${group.id}`);
    },
    handleGroupSuccess(actionType) {
      if (actionType === 'join') {
        Message.success('加入拼团成功！');
        this.$router.push(`/product/group/${this.currentGroupId}`);
      } else {
        this.fetchActiveGroups();
      }
    },
    async handleRedeem(product) {
      if (!this.userInfo) {
        return Message.warning('请先登录再兑换');
      }
      try {
        await redeemOrder({ 
          productId: product.id,
          address: '积分直接兑换，暂无收货地址' // Added to bypass @NotBlank validation
        });
        Message.success('兑换成功！商品已归入您的账户。');
        this.updatePoints(1000);
      } catch (error) {
        Message.error(error.response?.data?.message || '兑换失败');
      }
    },
    async fetchProducts() {
      try {
        const res = await getProducts();
        this.products = res.data.data || [];
      } catch (error) {
        Message.error('获取商品列表失败');
      }
    },
    handleBuy(product) {
      if (!this.userInfo) {
        return Message.warning('请先登录后再进行购买');
      }
      this.currentProduct = product;
      this.buyDialogVisible = true;
    }
  }
}
</script>

<style scoped>
.store-container {
  padding: 10px 0 100px; /* Increased bottom padding to avoid bottom nav overlap */
}



.product-col {
  margin-bottom: 24px;
}

.product-card {
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.08);
  background-color: rgba(255, 255, 255, 0.8);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background-color: rgba(0,0,0,0.02);
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, rgba(255, 126, 103, 0.95) 0%, rgba(255, 90, 68, 0.95) 100%);
  color: #FFFFFF;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.3);
  backdrop-filter: blur(4px);
}

.product-info {
  padding: 20px 24px 24px 24px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 17px;
  font-weight: 800;
  color: #1D2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.3px;
}

.product-desc {
  font-size: 13px;
  color: #86909C;
  line-height: 1.6;
  margin-bottom: 20px;
  height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-clickable {
  cursor: pointer;
  transition: opacity 0.2s;
}
.product-clickable:active {
  opacity: 0.6;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 24px;
  font-weight: 800;
  color: #FF4B2B;
}

.buy-btn {
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  border: none;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 75, 43, 0.2);
}

.group-btn {
  background: linear-gradient(135deg, #F5A623 0%, #F57C00 100%);
  border: none;
  font-weight: bold;
}

.buy-dialog-content {
  text-align: center;
  padding: 10px 0;
}

@media (max-width: 768px) {

  .product-info {
    padding: 15px;
  }
  .product-price {
    font-size: 16px;
  }
  .product-name {
    font-size: 15px;
  }
  .button-group {
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }
}

/* 拼团样式 */
.group-buy-section {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 24px;
  border-radius: 24px;
  margin-bottom: 30px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 32px rgba(0,0,0,0.03);
}

.section-title {
  font-size: 18px;
  color: #1D2129;
  margin-bottom: 20px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-card {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02) !important;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}
.group-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06) !important;
  background: rgba(255, 255, 255, 0.9) !important;
}

:deep(.group-card > .arco-card-body) {
  background: transparent;
}

.group-info {
  flex-grow: 1;
  margin-right: 15px;
}

.group-info .product-name {
  display: block;
  font-weight: 800;
  font-size: 15px;
  color: #1D2129;
  margin-bottom: 6px;
}

.group-info .initiator {
  font-size: 12px;
  color: #86909C;
  display: block;
  margin-bottom: 10px;
}

.button-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

/* 悬浮按钮样式 (仿照 Home.vue) */
.admin-fab-mini {
  position: fixed;
  right: 24px;
  bottom: 100px;
  background: rgba(40, 40, 40, 0.85); /* Premium dark glass */
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #FFD700; /* Gold plus */
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-size: 28px;
}

.admin-fab-mini:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.3);
  background: rgba(20, 20, 20, 0.9);
}

.admin-fab-mini:active {
  transform: scale(0.9);
}

@media (max-width: 768px) {
  .admin-fab-mini {
    right: 20px;
    bottom: 100px;
    width: 50px;
    height: 50px;
    font-size: 24px;
  }
}

/* 社区快团优化样式 */
.campaign-card {
  background: linear-gradient(180deg, #FFFFFF 0%, #FFF7F5 100%) !important;
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(255, 90, 52, 0.05) !important;
  border: 1px solid rgba(255, 90, 52, 0.1);
  transition: all 0.3s ease;
}
.campaign-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(255, 90, 52, 0.1) !important;
}
.campaign-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.campaign-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #1D2129;
  line-height: 1.4;
}
.campaign-tag {
  border-radius: 12px;
  font-weight: bold;
}
.campaign-details {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}
.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.detail-row:last-child {
  margin-bottom: 0;
}
.detail-icon {
  color: #FF5A34;
  margin-right: 8px;
  font-size: 14px;
}
.detail-text {
  font-size: 13px;
  color: #86909C;
}
.detail-highlight {
  color: #1D2129;
  font-weight: 600;
  margin-left: 4px;
}
.campaign-btn {
  background: linear-gradient(135deg, #FF7E67 0%, #FF5A34 100%);
  border: none;
  font-weight: bold;
  font-size: 15px;
  height: 40px;
  box-shadow: 0 4px 12px rgba(255, 90, 52, 0.3);
}
.campaign-btn:hover {
  background: linear-gradient(135deg, #FF8E79 0%, #FF6B47 100%);
}

.campaign-products-preview {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.preview-imgs {
  display: flex;
  gap: 8px;
}
.preview-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #E5E6EB;
}
.preview-more {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: #F7F8FA;
  color: #86909C;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #C9CDD4;
}
.preview-text {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
}
.price-start {
  color: #F53F3F;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.2;
}
.price-suffix {
  font-size: 12px;
  font-weight: normal;
  margin-left: 2px;
}
.count-text {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}

.joined-avatars {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  background: rgba(255, 90, 52, 0.05);
  padding: 6px 10px;
  border-radius: 8px;
}
.joined-text {
  font-size: 12px;
  color: #86909C;
  margin-left: 8px;
}
</style>
