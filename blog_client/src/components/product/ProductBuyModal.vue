<template>
  <a-modal
    title="订单确认"
    :visible="visible"
    :width="isMobile ? '90%' : '420px'"
    @cancel="visible = false"
    :footer="false"
    modal-class="buy-modal">
    <div class="buy-dialog-content" v-if="product">
      <!-- 商品概要区 -->
      <div class="product-preview">
        <a-image :src="product.image" class="product-thumb-large" width="90" height="90" fit="cover" />
        <div class="product-info-text">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-price-line">
            <span class="price-symbol">¥</span>
            <span class="price-val">{{ product.price }}</span>
            <a-tag v-if="product.stock <= 5 && product.stock > 0" color="red" size="small" style="margin-left: 10px;">仅剩 {{ product.stock }} 件</a-tag>
            <a-tag v-else-if="product.stock <= 0" color="gray" size="small" style="margin-left: 10px;">已售罄</a-tag>
            <span v-else class="stock-text">库存: {{ product.stock }}</span>
          </p>
        </div>
      </div>

      <!-- 规格选择区 -->
      <div class="specs-selection-card" v-if="parsedSpecs.length > 0">
        <div v-for="(spec, sIdx) in parsedSpecs" :key="sIdx" class="spec-group">
          <p class="spec-group-title">{{ spec.name }}</p>
          <div class="spec-options">
            <a-radio-group v-model="selectedSpecs[spec.name]" type="button">
              <a-radio 
                v-for="(opt, oIdx) in spec.options" 
                :key="oIdx"
                :value="opt"
              >
                {{ opt }}
              </a-radio>
            </a-radio-group>
          </div>
        </div>
      </div>

      <!-- 积分抵扣区 -->
      <div class="points-deduction-card" v-if="userPoints > 0">
        <div class="card-header">
          <span><icon-trophy /> 积分抵扣</span>
          <a-switch v-model="usePoints" checked-color="#FF7E67"></a-switch>
        </div>
        <div v-if="usePoints" class="slider-wrapper">
          <a-slider 
            v-model="pointsToUse" 
            :max="maxPointsPossible" 
            :step="100"
            show-input>
          </a-slider>
          <div class="deduction-info">
            <span>可用积分: {{ userPoints }}</span>
            <span class="deduct-amount">- ¥{{ (pointsToUse / 100).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 配送信息区 -->
      <div class="address-card">
        <div class="card-header" style="margin-bottom: 12px;">
          <span><icon-location /> 配送信息</span>
        </div>
        <div style="display: flex; gap: 10px; align-items: center; width: 100%;">
          <a-input 
            v-model="shippingAddress" 
            placeholder="请输入详细收货地址 (必填)" 
            allow-clear 
            style="flex: 1;"
          />
          <a-button type="primary" shape="circle" @click="openMapDialog" title="地图定位" style="background-color: #FF7E67; border-color: #FF7E67;">
            <icon-location />
          </a-button>
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

    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
      <a-button @click="visible = false" shape="round">再想想</a-button>
      <a-button 
        type="primary" 
        @click="handleConfirm" 
        :loading="loading" 
        :disabled="isPayDisabled"
        shape="round" 
        class="pay-btn"
      >
        {{ product.stock <= 0 ? '库存不足' : '立即支付' }}
      </a-button>
    <!-- 高德地图选点弹窗 -->
    <MapLocationDialog v-model:show="mapDialogVisible" @select="confirmMapLocation" />

    <!-- 支付状态确认弹窗 -->
    <a-modal 
      v-model:visible="paymentConfirmVisible" 
      title="支付确认"
      :footer="false"
      :mask-closable="false"
      :closable="false"
    >
      <div style="text-align: center; padding: 20px 0;">
        <icon-check-circle style="font-size: 48px; color: #00B42A; margin-bottom: 20px;" />
        <h3 style="margin-bottom: 30px;">请在新打开的页面中完成支付</h3>
        <p style="color: #86909C; margin-bottom: 30px; font-size: 13px;">支付完成前请不要关闭此窗口。完成支付后，请根据您的情况点击下面按钮。</p>
        <div style="display: flex; justify-content: center; gap: 15px;">
          <a-button @click="handlePaymentFail">遇到问题，重新支付</a-button>
          <a-button type="primary" @click="handlePaymentSuccess" style="background-color: #FF7E67;">我已完成支付</a-button>
        </div>
      </div>
    </a-modal>
  </a-modal>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'
import MapLocationDialog from '@/components/common/MapLocationDialog.vue';

export default {
  name: 'ProductBuyModal',
  components: {
    MapLocationDialog
  },
  props: {
    show: Boolean,
    product: Object
  },
  data() {
    return {
      loading: false,
      usePoints: false,
      pointsToUse: 0,
      selectedSpecs: {},
      shippingAddress: '',
      mapDialogVisible: false,
      paymentConfirmVisible: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    userPoints() {
      return this.userInfo ? this.userInfo.points : 0;
    },
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
      if (!this.shippingAddress || this.shippingAddress.trim() === '') return true;
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
        this.usePoints = false;
        this.pointsToUse = 0;
        this.selectedSpecs = {};
        this.shippingAddress = this.userInfo?.address || '';
        this.parsedSpecs.forEach(spec => {
          if (spec.options && spec.options.length === 1) {
            this.selectedSpecs[spec.name] = spec.options[0];
          }
        });
      }
    }
  },
  methods: {
    ...mapActions(useUserStore, ['updatePoints']),
    openMapDialog() {
      this.mapDialogVisible = true;
    },
    confirmMapLocation(address) {
      this.shippingAddress = address;
      this.mapDialogVisible = false;
    },
    async handleConfirm() {
      const token = localStorage.getItem('token');
      if (!token) return Message.warning('请先登录');

      this.loading = true;
      try {
        const orderRes = await axios.post('/api/orders/create', { 
          productId: this.product.id,
          pointsToUse: this.usePoints ? this.pointsToUse : 0,
          spec: this.specString,
          address: this.shippingAddress
        }, { headers: { 'Authorization': `Bearer ${token}` } });

        const orderId = orderRes.data.data.id;

        const payRes = await axios.post(`/api/pay/alipay/create?orderId=${orderId}`, {}, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        const formHtml = payRes.data.data;
        const div = document.createElement('div');
        div.innerHTML = formHtml;
        document.body.appendChild(div);
        
        if (document.forms && document.forms.length > 0) {
           const form = document.forms[document.forms.length - 1];
           form.target = "_blank"; // 强制在新标签页打开，避免覆盖当前网站
           form.submit();
        }
        
        if (this.usePoints && this.pointsToUse > 0) {
           this.updatePoints(this.pointsToUse);
        }
        
        // 弹出支付确认框
        this.paymentConfirmVisible = true;
      } catch (error) {
        Message.error(error.response?.data?.message || '支付失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handlePaymentSuccess() {
      this.paymentConfirmVisible = false;
      this.visible = false;
      Message.success('订单已提交，正在等待系统确认...');
      // 真实项目中这里通常会轮询后端查询订单状态，或者直接跳转到我的订单页
      this.$router.push('/user/profile');
    },
    handlePaymentFail() {
      this.paymentConfirmVisible = false;
      Message.info('您可以稍后在我的订单中继续支付');
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
  margin-top: 8px;
}

.points-deduction-card, .address-card {
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
  padding: 0 40px;
  font-weight: 800;
}
</style>
