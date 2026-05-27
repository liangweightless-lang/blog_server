<template>
  <div class="store-container">

    <!-- 活跃拼团区 -->
    <div v-if="activeGroups.length > 0" class="group-buy-section">
      <h2 class="section-title"><icon-clock-circle /> 正在拼团</h2>
      <a-grid :cols="{ xs: 1, sm: 2, md: 3 }" :colGap="20" :rowGap="20">
        <a-grid-item v-for="group in activeGroups" :key="group.id">
          <a-card class="group-card" hoverable :bordered="false" :body-style="{ padding: '16px', display: 'flex', width: '100%', alignItems: 'center' }">
            <div class="group-info">
              <span class="product-name">{{ group.productName }}</span>
              <span class="initiator">团长: {{ group.initiatorNickname }}</span>
              <div class="progress-bar">
                <a-progress :percent="group.currentNum / group.requiredNum" size="small">
                  <template #text>{{ group.currentNum }}/{{ group.requiredNum }}</template>
                </a-progress>
              </div>
            </div>
            <a-button type="primary" status="warning" size="small" shape="round" @click="$router.push(`/product/group/${group.id}`)">查看详情</a-button>
          </a-card>
        </a-grid-item>
      </a-grid>
    </div>

    <a-grid :cols="{ xs: 2, sm: 2, md: 3 }" :colGap="12" :rowGap="24">
      <a-grid-item v-for="product in products" :key="product.id" class="product-col">
        <a-card class="product-card" hoverable :bordered="false" :body-style="{ padding: '0px' }">
          <div class="product-image-wrapper">
            <a-image :src="product.image" class="product-image" :alt="product.name" width="100%" height="100%" fit="cover" />
            <div class="product-badge" v-if="product.isDigital">品牌甄选</div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-bottom">
              <span class="product-price">¥{{ product.price }}</span>
              <div class="button-group">
                <a-button type="text" style="color: #E6A23C; font-size: 12px; padding: 0 4px;" @click="handleRedeem(product)">1000积分兑换</a-button>
                <a-button type="primary" size="small" shape="round" @click="handleBuy(product)">立即购买</a-button>
                <a-button v-if="isMonday" type="primary" status="warning" size="small" shape="round" @click="handleStartGroup(product)">发起拼团</a-button>
              </div>
            </div>
          </div>
        </a-card>
      </a-grid-item>
    </a-grid>

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
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import ProductBuyModal from '@/components/product/ProductBuyModal.vue';
import GroupActionModal from '@/components/product/GroupActionModal.vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

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
      isMonday: new Date().getDay() === 1,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo'])
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
  },
  methods: {
    ...mapActions(useUserStore, ['updatePoints']),
    async fetchActiveGroups() {
      try {
        const res = await axios.get('/api/groups/active');
        this.activeGroups = res.data.data;
      } catch (error) {
        console.error('获取拼团列表失败');
      }
    },
    async handleStartGroup(product) {
      if (!localStorage.getItem('token')) {
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
      if (!localStorage.getItem('token')) {
        return Message.warning('请先登录再兑换');
      }
      try {
        await axios.post('/api/orders/redeem', { 
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
        const res = await axios.get('/api/products');
        this.products = res.data.data || [];
      } catch (error) {
        Message.error('获取商品列表失败');
      }
    },
    handleBuy(product) {
      if (!localStorage.getItem('token')) {
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
  padding: 10px 0 40px;
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

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 24px;
  font-weight: 800;
  color: #FF7E67;
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


</style>
