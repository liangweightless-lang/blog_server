<template>
  <el-dialog
    title="订单确认"
    :visible.sync="visible"
    :width="isMobile ? '90%' : '420px'"
    center
    append-to-body
    custom-class="buy-modal">
    <div class="buy-dialog-content" v-if="product">
      <!-- 商品概要区 -->
      <div class="product-preview">
        <img :src="product.image" class="product-thumb-large" />
        <div class="product-info-text">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-price-line">
            <span class="price-symbol">¥</span>
            <span class="price-val">{{ product.price }}</span>
            <el-tag v-if="product.stock <= 5 && product.stock > 0" type="danger" size="mini" effect="plain" style="margin-left: 10px;">仅剩 {{ product.stock }} 件</el-tag>
            <el-tag v-else-if="product.stock <= 0" type="info" size="mini" style="margin-left: 10px;">已售罄</el-tag>
            <span v-else class="stock-text">库存: {{ product.stock }}</span>
          </p>
        </div>
      </div>

      <!-- 规格选择区 -->
      <div class="specs-selection-card" v-if="parsedSpecs.length > 0">
        <div v-for="(spec, sIdx) in parsedSpecs" :key="sIdx" class="spec-group">
          <p class="spec-group-title">{{ spec.name }}</p>
          <div class="spec-options">
            <div 
              v-for="(opt, oIdx) in spec.options" 
              :key="oIdx"
              class="spec-option-btn"
              :class="{ active: selectedSpecs[spec.name] === opt }"
              @click="selectSpec(spec.name, opt)"
            >
              {{ opt }}
            </div>
          </div>
        </div>
      </div>

      <!-- 积分抵扣区 -->
      <div class="points-deduction-card" v-if="userPoints > 0">
        <div class="card-header">
          <i class="el-icon-coin"></i> 积分抵扣
          <el-switch v-model="usePoints" active-color="#FF7E67"></el-switch>
        </div>
        <div v-if="usePoints" class="slider-wrapper">
          <el-slider 
            v-model="pointsToUse" 
            :max="maxPointsPossible" 
            :step="100"
            show-input
            input-size="mini">
          </el-slider>
          <div class="deduction-info">
            <span>可用积分: {{ userPoints }}</span>
            <span class="deduct-amount">- ¥{{ (pointsToUse / 100).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 结算区 -->
      <div class="summary-card">
        <div class="summary-item" v-if="specString">
          <span>已选规格</span>
          <span style="color: #8C6A5D;">{{ specString }}</span>
        </div>
        <div class="summary-item">
          <span>商品金额</span>
          <span>¥{{ product.price }}</span>
        </div>
        <div class="summary-item" v-if="usePoints">
          <span>积分抵扣</span>
          <span style="color: #FF7E67;">- ¥{{ (pointsToUse / 100).toFixed(2) }}</span>
        </div>
        <div class="total-row">
          <span>实付金额</span>
          <span class="total-price">¥{{ finalPrice }}</span>
        </div>
      </div>

      <p class="demo-tip">此功能为演示版本。在生产环境中，点击后将调起微信/支付宝支付。</p>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false" round>再想想</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm" 
        :loading="loading" 
        :disabled="isPayDisabled"
        round 
        class="pay-btn"
      >
        {{ product.stock <= 0 ? '库存不足' : '立即支付' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProductBuyModal',
  props: {
    show: Boolean,
    product: Object
  },
  data() {
    return {
      loading: false,
      usePoints: false,
      pointsToUse: 0,
      userPoints: 0,
      selectedSpecs: {},
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    visible: {
      get() { return this.show },
      set(val) { this.$emit('update:show', val) }
    },
    parsedSpecs() {
      if (!this.product || !this.product.specs) return [];
      try {
        return JSON.parse(this.product.specs);
      } catch (e) {
        return [];
      }
    },
    specString() {
      const values = Object.values(this.selectedSpecs).filter(v => v);
      return values.join(', ');
    },
    isPayDisabled() {
      if (!this.product) return true;
      if (this.product.stock <= 0) return true;
      // Check if all specs are selected
      return this.parsedSpecs.some(spec => !this.selectedSpecs[spec.name]);
    },
    maxPointsPossible() {
      if (!this.product || !this.product.price) return 0;
      const pointsToCover = Math.floor((this.product.price - 0.01) * 100);
      return Math.min(this.userPoints, pointsToCover);
    },
    finalPrice() {
      if (!this.product || !this.product.price) return '0.00';
      const deduction = this.usePoints ? (this.pointsToUse / 100) : 0;
      return (this.product.price - deduction).toFixed(2);
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.fetchUserPoints();
        this.usePoints = false;
        this.pointsToUse = 0;
        this.selectedSpecs = {};
        // Auto select if only one option
        this.parsedSpecs.forEach(spec => {
          if (spec.options && spec.options.length === 1) {
            this.$set(this.selectedSpecs, spec.name, spec.options[0]);
          }
        });
      }
    }
  },
  methods: {
    selectSpec(name, opt) {
      this.$set(this.selectedSpecs, name, opt);
    },
    async fetchUserPoints() {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;
        const res = await axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.userPoints = res.data.data.points || 0;
      } catch (e) {
        console.error('获取积分失败');
      }
    },
    async handleConfirm() {
      const token = localStorage.getItem('token');
      if (!token) return this.$message.warning('请先登录');

      this.loading = true;
      try {
        // 1. 创建订单
        const orderRes = await axios.post('/api/orders/create', { 
          productId: this.product.id,
          pointsToUse: this.usePoints ? this.pointsToUse : 0,
          spec: this.specString
        }, { headers: { 'Authorization': `Bearer ${token}` } });

        const orderId = orderRes.data.data.id;

        // 2. 模拟支付逻辑
        await axios.post(`/api/orders/${orderId}/pay`, {}, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        this.$message({
          message: '支付成功！欢迎开启焙刻生活。',
          type: 'success',
          duration: 3000
        });
        
        this.visible = false;
        // 通知外部刷新数据
        this.$emit('success');
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '支付失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.buy-dialog-content {
  padding: 10px 0;
}
.product-preview {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #FFFDF8;
  padding: 15px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid #FFE4D6;
}
.product-thumb-large {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.product-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #5C433B;
  font-weight: 800;
}
.price-symbol {
  color: #FF7E67;
  font-size: 14px;
  font-weight: 600;
}
.price-val {
  color: #FF7E67;
  font-size: 24px;
  font-weight: 800;
}
.stock-text {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.specs-selection-card {
  background: #FFFDF8;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid #FFE4D6;
}
.spec-group {
  margin-bottom: 12px;
}
.spec-group:last-child {
  margin-bottom: 0;
}
.spec-group-title {
  font-size: 13px;
  color: #8C6A5D;
  font-weight: bold;
  margin-bottom: 8px;
}
.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.spec-option-btn {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #DCDFE6;
  border-radius: 20px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
}
.spec-option-btn:hover {
  border-color: #FF7E67;
  color: #FF7E67;
}
.spec-option-btn.active {
  background: #FF7E67;
  border-color: #FF7E67;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2px 6px rgba(255, 126, 103, 0.3);
}

.points-deduction-card {
  background: #FFFDF8;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px dashed #FF7E67;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  color: #8C6A5D;
  font-size: 14px;
}
.slider-wrapper {
  margin-top: 15px;
}
.deduction-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #D3C1BA;
  margin-top: 5px;
}
.deduct-amount {
  color: #FF7E67;
  font-weight: bold;
}

.summary-card {
  padding: 15px 5px;
  border-top: 1px solid #FDF0E6;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #8C6A5D;
  margin-bottom: 10px;
}
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #FDF0E6;
  font-weight: 800;
  color: #5C433B;
}
.total-price {
  color: #FF7E67;
  font-size: 26px;
}

.demo-tip {
  font-size: 11px;
  color: #D3C1BA;
  text-align: center;
  margin-top: 20px;
}

.pay-btn {
  background: linear-gradient(135deg, #FF7E67 0%, #FF5A44 100%);
  border: none;
  padding: 12px 40px;
  font-weight: 800;
}
</style>
