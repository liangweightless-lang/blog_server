<template>
  <a-spin :loading="loading" style="width: 100%; display: block;">
    <div class="product-detail-container">
      <!-- 顶部导航栏 -->
      <div class="detail-header">
        <div class="header-back" @click="$router.back()">
          <icon-left style="font-size: 22px;" />
        </div>
        <span class="header-title">商品详情</span>
        <div class="header-placeholder"></div>
      </div>

      <!-- 商品大图 -->
      <div class="product-hero">
        <a-image :src="product.image" class="hero-image" width="100%" fit="cover" :preview="true" />
        <div class="product-badge" v-if="product.isDigital">品牌甄选</div>
      </div>

      <!-- 价格区 -->
      <div class="price-section">
        <div class="price-row">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ product.price }}</span>
          <span class="price-group" v-if="product.groupPrice">
            拼团价 ¥{{ product.groupPrice }}
          </span>
        </div>
        <div class="stock-row">
          <a-tag v-if="product.stock <= 5 && product.stock > 0" color="red" size="small">仅剩 {{ product.stock }} 件</a-tag>
          <a-tag v-else-if="product.stock <= 0" color="gray" size="small">已售罄</a-tag>
          <span v-else class="stock-text">库存充足 · {{ product.stock }} 件</span>
        </div>
      </div>

      <!-- 商品名称 -->
      <div class="info-section">
        <h1 class="product-title">{{ product.name }}</h1>
      </div>

      <!-- 商品详细描述 -->
      <div class="desc-section">
        <div class="section-label">商品介绍</div>
        <div class="desc-content">{{ product.description }}</div>
      </div>

      <!-- 规格信息 -->
      <div class="spec-section" v-if="parsedSpecs.length > 0">
        <div class="section-label">可选规格</div>
        <div v-for="(spec, idx) in parsedSpecs" :key="idx" class="spec-group">
          <span class="spec-name">{{ spec.name }}：</span>
          <a-tag v-for="(opt, oIdx) in spec.options" :key="oIdx" size="medium" class="spec-tag">{{ opt }}</a-tag>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="detail-footer">
        <a-button
          type="text"
          class="footer-action-btn"
          @click="handleRedeem"
        >
          <template #icon><icon-trophy style="font-size: 20px; color: #E6A23C;" /></template>
          积分兑
        </a-button>
        <a-button
          type="primary"
          shape="round"
          size="large"
          class="buy-btn"
          @click="handleBuy"
        >
          ¥{{ product.price }} · 立即购买
        </a-button>
        <a-button
          v-if="isMonday"
          type="primary"
          status="warning"
          shape="round"
          size="large"
          class="group-btn"
          @click="handleStartGroup"
        >
          发起拼团
        </a-button>
      </div>

      <!-- 订单弹窗 -->
      <ProductBuyModal
        v-model:show="buyDialogVisible"
        :product="product"
      />

      <!-- 拼团弹窗 -->
      <GroupActionModal
        v-model:show="groupDialogVisible"
        actionType="start"
        :product="product"
        @success="handleGroupSuccess"
      />
    </div>
  </a-spin>
</template>

<script>
import { getProductDetail } from '@/api/product';
import { redeemOrder } from '@/api/order';
import { Message } from '@arco-design/web-vue';
import ProductBuyModal from '@/components/product/ProductBuyModal.vue';
import GroupActionModal from '@/components/product/GroupActionModal.vue';
import { mapActions } from 'pinia';
import { useUserStore } from '@/stores/user';

export default {
  name: 'ProductDetail',
  components: {
    ProductBuyModal,
    GroupActionModal
  },
  data() {
    return {
      loading: true,
      product: {},
      buyDialogVisible: false,
      groupDialogVisible: false,
      isMonday: new Date().getDay() === 1
    }
  },
  computed: {
    parsedSpecs() {
      if (!this.product || !this.product.specs) return [];
      try {
        return JSON.parse(this.product.specs);
      } catch (e) {
        return [];
      }
    }
  },
  created() {
    this.fetchProduct();
  },
  methods: {
    ...mapActions(useUserStore, ['updatePoints']),
    async fetchProduct() {
      const id = this.$route.params.id;
      try {
        const res = await getProductDetail(id);
        this.product = (res.data && res.data.data) ? res.data.data : {};
      } catch (error) {
        Message.error('获取商品信息失败');
      } finally {
        this.loading = false;
      }
    },
    handleBuy() {
      if (!localStorage.getItem('token')) {
        return Message.warning('请先登录后再进行购买');
      }
      this.buyDialogVisible = true;
    },
    handleStartGroup() {
      if (!localStorage.getItem('token')) {
        return Message.warning('请先登录再发起拼团');
      }
      this.groupDialogVisible = true;
    },
    handleGroupSuccess() {
      Message.success('拼团发起成功！');
    },
    async handleRedeem() {
      if (!localStorage.getItem('token')) {
        return Message.warning('请先登录再兑换');
      }
      try {
        await redeemOrder({
          productId: this.product.id,
          address: '积分直接兑换，暂无收货地址'
        });
        Message.success('兑换成功！');
        this.updatePoints(1000);
      } catch (error) {
        Message.error(error.response?.data?.message || '兑换失败');
      }
    }
  }
}
</script>

<style scoped>
.product-detail-container {
  background: transparent;
  min-height: 100vh;
  padding-bottom: 90px;
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
}
.header-back:active {
  transform: scale(0.9);
  background: rgba(0, 0, 0, 0.08);
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
}

.header-placeholder {
  width: 36px;
}

.product-hero {
  position: relative;
  width: 100%;
  background: #f5f5f5;
}

.hero-image {
  width: 100%;
  display: block;
}

.product-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, rgba(255, 126, 103, 0.95) 0%, rgba(255, 90, 68, 0.95) 100%);
  color: #fff;
  padding: 5px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.3);
}

.price-section {
  background: linear-gradient(135deg, #FFF5F3 0%, #FFF0EC 100%);
  padding: 20px 24px;
  margin: 0;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  color: #FF5A44;
  font-size: 16px;
  font-weight: 700;
}

.price-value {
  color: #FF5A44;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.price-group {
  margin-left: 12px;
  font-size: 13px;
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.1);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 600;
}

.stock-row {
  margin-top: 10px;
}

.stock-text {
  font-size: 13px;
  color: #86909C;
}

.info-section {
  padding: 20px 24px 12px;
  background: #fff;
}

.product-title {
  font-size: 20px;
  font-weight: 800;
  color: #1D2129;
  margin: 0;
  line-height: 1.5;
  letter-spacing: 0.3px;
}

.desc-section {
  padding: 0 24px 24px;
  background: #fff;
}

.section-label {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-label::before {
  content: '';
  width: 3px;
  height: 14px;
  background: #FF7E67;
  border-radius: 2px;
}

.desc-content {
  font-size: 15px;
  color: #4E5969;
  line-height: 1.9;
  white-space: pre-wrap;
  word-wrap: break-word;
  letter-spacing: 0.2px;
}

.spec-section {
  padding: 20px 24px;
  background: #fff;
  margin-top: 10px;
}

.spec-group {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-name {
  font-size: 13px;
  color: #86909C;
  font-weight: 600;
}

.spec-tag {
  background: #FFF5F3 !important;
  color: #FF7E67 !important;
  border: 1px solid #FFE4D6 !important;
  border-radius: 8px !important;
  font-weight: 600;
}

.detail-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
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

.footer-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 11px;
  color: #86909C;
  font-weight: 600;
  gap: 2px;
}

.buy-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 800;
  background: linear-gradient(135deg, #FF7E67 0%, #FF5A44 100%) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(255, 90, 68, 0.3);
}

.group-btn {
  height: 48px;
  font-size: 14px;
  font-weight: 700;
}
</style>
