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
            <el-button type="warning" size="mini" round @click="$router.push(`/product/group/${group.id}`)">查看详情</el-button>
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

    <!-- 订单确认业务组件 (已封装) -->
    <ProductBuyModal 
      :show.sync="buyDialogVisible" 
      :product="currentProduct"
      @success="fetchActiveGroups" />

    <!-- 拼团确认弹窗 -->
    <el-dialog
      :title="isJoiningGroup ? '加入拼团确认' : '发起拼团确认'"
      :visible.sync="groupDialogVisible"
      :width="isMobile ? '95%' : '450px'"
      center>
      <div class="group-dialog-content" v-if="currentProduct.id">
        <div class="product-mini-info">
          <img :src="currentProduct.image" class="mini-img">
          <div class="mini-text">
            <h4>{{ currentProduct.name }}</h4>
            <p class="price-row">
              <span class="label">拼团特惠:</span>
              <span class="group-price">¥{{ currentProduct.groupPrice || (currentProduct.price * 0.8).toFixed(2) }}</span>
              <span class="origin-price">¥{{ currentProduct.price }}</span>
            </p>
          </div>
        </div>
        
        <div class="order-form">
          <div class="form-item">
            <p class="form-label">配送地址 <span class="required">*</span></p>
            <el-input 
              type="textarea" 
              v-model="orderAddress" 
              placeholder="请输入详细收货地址" 
              :rows="3">
            </el-input>
            <p class="address-tip" v-if="!orderAddress">建议前往“个人资料”设置默认地址</p>
          </div>
          
          <div class="form-item">
            <p class="form-label">预计配送时间</p>
            <el-tag type="info" size="medium">成团后 3 个工作日内发货</el-tag>
          </div>

          <div class="rule-box">
            <p><i class="el-icon-info"></i> 拼团须知：</p>
            <ul>
              <li>需满 8 人方可成团</li>
              <li>24小时内未成团将自动退款</li>
              <li>成团后不支持取消订单</li>
            </ul>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="groupDialogVisible = false">取 消</el-button>
        <el-button type="warning" @click="confirmGroupAction" :loading="groupProcessing">
          {{ isJoiningGroup ? '立即加入' : '立即开团' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import ProductBuyModal from '@/components/product/ProductBuyModal.vue';

export default {
  name: 'Store',
  components: {
    ProductBuyModal
  },
  data() {
    return {
      buyDialogVisible: false,
      groupDialogVisible: false,
      groupProcessing: false,
      isJoiningGroup: false,
      currentProduct: {},
      products: [],
      currentGroupId: null,
      orderAddress: '',
      activeGroups: [],
      isMonday: new Date().getDay() === 1,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
  },
  created() {
    this.fetchProducts().then(() => {
      // 检查是否有直达购买请求
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
        return this.$message.warning('请先登录再发起拼团');
      }
      this.currentProduct = product;
      this.isJoiningGroup = false;
      await this.prepareOrder();
      this.groupDialogVisible = true;
    },
    async handleJoinGroup(group) {
      this.$router.push(`/product/group/${group.id}`);
    },
    async prepareOrder() {
      // Fetch user's default address
      try {
        const res = await axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        if (res.data.data.address) {
          this.orderAddress = res.data.data.address;
        }
      } catch (error) {
        console.error('获取默认地址失败');
      }
    },
    async confirmGroupAction() {
      if (!this.orderAddress) {
        return this.$message.warning('请填写配送地址');
      }
      
      this.groupProcessing = true;
      try {
        const token = localStorage.getItem('token');
        const headers = { 'Authorization': `Bearer ${token}` };
        
        if (this.isJoiningGroup) {
          await axios.post(`/api/groups/${this.currentGroupId}/join`, { address: this.orderAddress }, { headers });
          this.$message.success('加入拼团成功！');
          this.$router.push(`/product/group/${this.currentGroupId}`);
        } else {
          const res = await axios.post('/api/groups/start', { productId: this.currentProduct.id, address: this.orderAddress }, { headers });
          this.$message.success('开团成功！快邀请好友来凑单吧');
          this.$router.push(`/product/group/${res.data.data.id}`);
        }
        
        this.groupDialogVisible = false;
        this.fetchActiveGroups();
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '操作失败');
      } finally {
        this.groupProcessing = false;
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
        console.log('Fetched products:', res.data);
        this.products = res.data.data || [];
      } catch (error) {
        console.error('获取商品列表失败', error);
        this.$message.error('获取商品列表失败');
      }
    },
    handleBuy(product) {
      if (!localStorage.getItem('token')) {
        return this.$message.warning('请先登录后再进行购买');
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
  padding: 10px 0;
}
.points-deduction-box {
  margin: 20px 0;
  padding: 15px;
  background: #FFFDF8;
  border-radius: 12px;
  border: 1px dashed #FF7E67;
  text-align: left;
}
.points-slider {
  margin-top: 10px;
}
.deduction-tip {
  font-size: 12px;
  color: #8C6A5D;
  margin-top: 8px;
}
.final-price {
  margin-top: 20px;
  font-size: 16px;
  color: #5C433B;
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
/* 拼团对话框样式 */
.group-dialog-content {
  padding: 10px 0;
}
.product-mini-info {
  display: flex;
  background: #FFFDF8;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #FFE4D6;
}
.mini-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 12px;
}
.mini-text h4 {
  margin: 0 0 5px 0;
  font-size: 15px;
  color: #5C433B;
}
.price-row {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.price-row .label {
  font-size: 12px;
  color: #8C6A5D;
}
.group-price {
  color: #FF7E67;
  font-weight: 800;
  font-size: 18px;
}
.origin-price {
  text-decoration: line-through;
  color: #C0C4CC;
  font-size: 12px;
}
.form-item {
  margin-bottom: 20px;
}
.form-label {
  font-size: 14px;
  font-weight: 800;
  color: #5C433B;
  margin-bottom: 8px;
}
.required {
  color: #F56C6C;
}
.address-tip {
  font-size: 11px;
  color: #FF7E67;
  margin-top: 5px;
}
.rule-box {
  background: #F2F6FC;
  padding: 12px;
  border-radius: 8px;
  margin-top: 20px;
}
.rule-box p {
  margin: 0 0 8px 0;
  font-size: 12px;
  font-weight: 800;
  color: #409EFF;
}
.rule-box ul {
  margin: 0;
  padding-left: 18px;
  font-size: 11px;
  color: #909399;
}
.rule-box li {
  margin-bottom: 4px;
}
</style>
