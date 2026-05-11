<template>
  <div class="store-container">
    <div class="store-header">
      <h1 class="store-title">品牌灵感橱窗</h1>
      <p class="store-subtitle">概念展示区：展示未来可能上架的生活方式好物与概念周边。</p>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="12" :md="8" v-for="product in products" :key="product.id" class="product-col">
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
              <el-button type="primary" size="small" round @click="handleBuy(product)">立即购买</el-button>
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
      products: []
    }
  },
  created() {
    this.fetchProducts();
  },
  methods: {
    async fetchProducts() {
      try {
        const res = await axios.get('/api/products');
        this.products = res.data;
      } catch (error) {
        this.$message.error('获取商品列表失败');
      }
    },
    handleBuy(product) {
      this.currentProduct = product;
      this.buyDialogVisible = true;
    },
    confirmPurchase() {
      this.purchasing = true;
      setTimeout(() => {
        this.purchasing = false;
        this.buyDialogVisible = false;
        this.$message.success('支付成功！感谢您的支持。');
      }, 1500);
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
    font-size: 18px;
  }
}
</style>
