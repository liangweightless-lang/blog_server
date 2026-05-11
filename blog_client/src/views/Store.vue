<template>
  <div class="store-container">
    <div class="store-header">
      <h1 class="store-title">品牌灵感橱窗</h1>
      <p class="store-subtitle">概念展示区：展示未来可能上架的生活方式好物与概念周边。</p>
    </div>

    <!-- 活跃拼团区 -->
    <div v-if="activeGroups.length > 0" class="group-buy-section">
      <h2 class="section-title"><i class="el-icon-timer"></i> 正在进行的拼团</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="group in activeGroups" :key="group.id">
          <div class="group-card">
            <div class="group-info">
              <span class="product-name">{{ group.productName }}</span>
              <span class="initiator">团长: {{ group.initiatorNickname }}</span>
              <div class="progress-bar">
                <el-progress :percentage="(group.currentNum / group.requiredNum) * 100" :format="() => `${group.currentNum}/${group.requiredNum}`"></el-progress>
              </div>
            </div>
            <el-button type="warning" size="mini" round @click="handleJoinGroup(group)">加入拼团</el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="12">
      <el-col :xs="12" :sm="12" :md="8" v-for="product in products" :key="product.id" class="product-col">
        <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }">
          <div class="product-image-wrapper">
            <img :src="product.image" class="product-image" :alt="product.name">
            <div class="product-badge" v-if="product.isDigital">品牌甄选</div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-bottom">
              <span class="product-price">¥{{ product.price }}</span>
              <div class="button-group">
                <el-button type="text" style="color: #E6A23C;" @click="handleRedeem(product)">1000积分兑换</el-button>
                <el-button type="primary" size="small" round @click="handleBuy(product)">立即购买</el-button>
                <el-button v-if="isMonday" type="warning" size="small" round @click="handleStartGroup(product)">发起拼团</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模拟购买弹窗 -->
    <el-dialog
      title="订单确认"
      :visible.sync="buyDialogVisible"
      width="400px"
      center>
      <div class="buy-dialog-content">
        <i class="el-icon-shopping-cart-2" style="font-size: 48px; color: #409EFF; margin-bottom: 15px;"></i>
        <h3>{{ currentProduct.name }}</h3>
        <p class="buy-price">需支付：<span style="color: #F56C6C; font-size: 20px; font-weight: bold;">¥{{ currentProduct.price }}</span></p>
        <p style="color: #909399; font-size: 14px; margin-top: 20px;">
          此功能为演示版本。在实际环境中，此处将接入微信支付/支付宝或 Stripe 的支付二维码。
        </p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="buyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmPurchase" :loading="purchasing">模拟支付</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'Store',
  data() {
    return {
      buyDialogVisible: false,
      purchasing: false,
      currentProduct: {},
      products: [],
      activeGroups: [],
      isMonday: new Date().getDay() === 1
    }
  },
  created() {
    this.fetchProducts();
    this.fetchActiveGroups();
  },
  methods: {
    async fetchActiveGroups() {
      try {
        const res = await axios.get('/api/groups/active');
        this.activeGroups = res.data;
      } catch (error) {
        console.error('获取拼团列表失败');
      }
    },
    async handleStartGroup(product) {
      if (!localStorage.getItem('token')) {
        return this.$message.warning('请先登录再发起拼团');
      }
      try {
        await axios.post('/api/groups/start', { productId: product.id }, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        this.$message.success('开团成功！快邀请好友来凑单吧 (满8人成团)');
        this.fetchActiveGroups();
      } catch (error) {
        this.$message.error(error.response?.data?.error || '开团失败');
      }
    },
    async handleJoinGroup(group) {
      if (!localStorage.getItem('token')) {
        return this.$message.warning('请先登录再参加拼团');
      }
      try {
        await axios.post(`/api/groups/${group.id}/join`, {}, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        this.$message.success('加入拼团成功！');
        this.fetchActiveGroups();
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '加入失败');
      }
    },
    async handleRedeem(product) {
      if (!localStorage.getItem('token')) {
        return this.$message.warning('请先登录再兑换');
      }
      try {
        await axios.post('/api/orders/redeem', { productId: product.id }, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        this.$message.success('兑换成功！商品已归入您的账户。');
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '兑换失败');
      }
    },
    async fetchProducts() {
      try {
        const res = await axios.get('/api/products');
        this.products = res.data;
      } catch (error) {
        this.$message.error('获取商品列表失败');
      }
    },
    handleBuy(product) {
      if (!localStorage.getItem('token')) {
        return this.$message.warning('请先登录后再进行购买');
      }
      this.currentProduct = product;
      this.buyDialogVisible = true;
    },
    async confirmPurchase() {
      const token = localStorage.getItem('token');
      if (!token) return;

      this.purchasing = true;
      try {
        // 1. 创建订单
        const orderRes = await axios.post('/api/orders/create', 
          { productId: this.currentProduct.id },
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        const orderId = orderRes.data.id;

        // 2. 模拟支付
        await axios.post(`/api/orders/${orderId}/pay`, {}, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        this.$message.success('支付成功！邀请人积分已同步更新。');
        this.buyDialogVisible = false;
        
        // 刷新用户信息（更新积分展示）
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '购买失败');
      } finally {
        this.purchasing = false;
      }
    }
  }
}
</script>

<style scoped>
.store-container {
  padding: 10px 0 40px;
}

.store-header {
  text-align: center;
  margin-bottom: 40px;
}

.store-title {
  font-size: 32px;
  font-weight: 800;
  color: #5C433B;
  margin-bottom: 12px;
}

.store-subtitle {
  font-size: 15px;
  color: #8C6A5D;
}

.product-col {
  margin-bottom: 24px;
}

.product-card {
  border-radius: 24px;
  overflow: hidden;
  transition: transform 0.4s ease, box-shadow 0.4s ease;
  border: none;
  background-color: #FFFFFF;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.08);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(255, 126, 103, 0.15);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 Aspect Ratio */
  overflow: hidden;
  background-color: #FFFDF8;
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
  background: #FF7E67;
  color: #FFFFFF;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.3);
}

.product-info {
  padding: 24px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 800;
  color: #5C433B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  font-size: 14px;
  color: #8C6A5D;
  line-height: 1.6;
  margin-bottom: 20px;
  height: 44px;
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
  padding: 20px 0;
}

@media (max-width: 768px) {
  .store-title {
    font-size: 28px;
  }
  .store-subtitle {
    font-size: 14px;
    padding: 0 15px;
  }
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
    gap: 4px;
  }
  .button-group .el-button {
    margin: 0 !important;
    padding: 4px 8px;
    font-size: 11px;
  }
}

/* 拼团样式 */
.group-buy-section {
  background: #FFFDF8;
  padding: 24px;
  border-radius: 24px;
  border: 2px dashed #FF7E67;
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  color: #FF7E67;
  margin-bottom: 20px;
  font-weight: 800;
}

.group-card {
  background: #FFFFFF;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 15px;
}

.group-info {
  flex-grow: 1;
  margin-right: 15px;
}

.group-info .product-name {
  display: block;
  font-weight: 800;
  font-size: 14px;
  margin-bottom: 4px;
}

.group-info .initiator {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-bottom: 8px;
}

.button-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}
.button-group .el-button {
  margin: 0 !important;
}
</style>
