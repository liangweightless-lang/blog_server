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

      <!-- 支付方式选择区 -->
      <div class="payment-method-card">
        <div class="card-header" style="margin-bottom: 10px;">
          <span><icon-safe /> 支付方式</span>
        </div>
        <div class="pay-options">
          <div 
            class="pay-option-item" 
            :class="{ active: payChannel === 'WECHAT' }"
            @click="payChannel = 'WECHAT'"
          >
            <div class="pay-icon-name">
              <svg class="pay-svg" viewBox="0 0 24 24" fill="#07C160">
                <path d="M8.691 2.188C3.891 2.188 0 5.478 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .161.13.29.29.29.08 0 .15-.029.212-.068l1.96-1.141a.853.853 0 0 1 .639-.097c.92.251 1.897.39 2.913.39.309 0 .61-.019.91-.048-.718-2.038-.34-4.321 1.07-5.918 1.453-1.639 3.59-2.529 5.82-2.529.418 0 .833.03 1.238.087C16.892 4.398 13.064 2.188 8.691 2.188zm-2.42 4.145c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm4.84 0c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm8.567 4.144c-3.864 0-7.004 2.657-7.004 5.928 0 3.272 3.14 5.928 7.004 5.928.795 0 1.562-.116 2.278-.319a.69.69 0 0 1 .513.078l1.579.919c.05.029.106.048.173.048.13 0 .233-.106.233-.232a.38.38 0 0 0-.039-.175l-.32-1.19a.473.473 0 0 1 .174-.533c1.474-1.085 2.413-2.684 2.413-4.472 0-3.271-3.14-5.93-7.005-5.93zm-2.14 3.428c.552 0 1.007.456 1.007 1.008 0 .551-.455 1.007-1.008 1.007-.551 0-1.007-.456-1.007-1.007 0-.552.456-1.008 1.007-1.008zm4.28 0c.553 0 1.008.456 1.008 1.008 0 .551-.455 1.007-1.008 1.007-.552 0-1.007-.456-1.007-1.007 0-.552.455-1.008 1.007-1.008z"/>
              </svg>
              <span>微信支付</span>
            </div>
            <icon-check-circle-fill v-if="payChannel === 'WECHAT'" class="selected-check" style="color: #07C160;" />
            <div v-else class="unselected-circle"></div>
          </div>
          <div 
            class="pay-option-item" 
            :class="{ active: payChannel === 'ALIPAY' }"
            @click="payChannel = 'ALIPAY'"
          >
            <div class="pay-icon-name">
              <svg class="pay-svg" viewBox="0 0 24 24" fill="#1677FF">
                <path d="M21.42 13.91c-.69-.26-2.58-.94-4.59-1.57.84-1.63 1.5-3.47 1.94-5.46H22V5.11h-6.27V3.5h-2.16v1.61H7.8V6.88h8.54c-.38 1.54-.92 2.97-1.59 4.25-2.61-.93-5.28-1.55-7.46-1.55-3.8 0-6.19 1.87-6.19 4.67 0 2.65 2.19 4.41 5.48 4.41 3.51 0 6.64-1.92 8.94-4.88 2.06.74 3.86 1.48 4.7 1.83.67.28 1.02.77 1.02 1.41 0 1.25-1.42 2.37-3.9 2.93l.79 1.95c3.34-.84 5.3-2.52 5.3-4.83.01-1.39-.77-2.48-1.95-2.92zM7.29 16.59c-2.19 0-3.56-1.03-3.56-2.44 0-1.54 1.41-2.64 3.88-2.64 1.76 0 3.84.45 5.9 1.15-1.68 2.51-4.08 3.93-6.22 3.93z"/>
              </svg>
              <span>支付宝</span>
            </div>
            <icon-check-circle-fill v-if="payChannel === 'ALIPAY'" class="selected-check" style="color: #1677FF;" />
            <div v-else class="unselected-circle"></div>
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
    </div>

    <!-- 高德地图选点弹窗 -->
    <MapLocationDialog v-model:show="mapDialogVisible" @select="confirmMapLocation" />

    <!-- 微信扫码支付弹窗 -->
    <WechatPayQrModal
      v-model:show="wechatQrVisible"
      :order-id="currentOrderId"
      :amount="finalPrice"
      :code-url="wechatCodeUrl"
      @success="handlePaymentSuccess"
    />

    <!-- 支付宝支付状态确认弹窗 -->
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
import { createOrder, createAlipay, createWechatPay } from '@/api/order';
import { Message } from '@arco-design/web-vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'
import MapLocationDialog from '@/components/common/MapLocationDialog.vue';
import WechatPayQrModal from '@/components/pay/WechatPayQrModal.vue';

export default {
  name: 'ProductBuyModal',
  components: {
    MapLocationDialog,
    WechatPayQrModal
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
      payChannel: 'WECHAT', // 默认微信支付
      mapDialogVisible: false,
      paymentConfirmVisible: false,
      wechatQrVisible: false,
      wechatCodeUrl: '',
      currentOrderId: '',
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
        const orderRes = await createOrder({ 
          productId: this.product.id,
          pointsToUse: this.usePoints ? this.pointsToUse : 0,
          spec: this.specString,
          address: this.shippingAddress
        });

        const orderId = orderRes.data.data.id;
        this.currentOrderId = orderId;

        if (this.payChannel === 'WECHAT') {
          // 微信支付
          const payRes = await createWechatPay(orderId);
          const payData = payRes.data.data;

          if (this.usePoints && this.pointsToUse > 0) {
            this.updatePoints(this.pointsToUse);
          }

          if (payData.payType === 'H5' && payData.h5_url) {
            // 移动端 H5 支付，直接跳转或唤起微信
            window.location.href = payData.h5_url;
          } else {
            // PC 端微信扫码支付弹窗
            this.wechatCodeUrl = payData.code_url || '';
            this.wechatQrVisible = true;
          }
        } else {
          // 支付宝支付
          const payRes = await createAlipay(orderId);
          const formHtml = payRes.data.data;
          
          const newWindow = window.open('', '_blank');
          if (newWindow) {
            newWindow.document.write(formHtml);
            newWindow.document.close();
          } else {
            Message.warning('支付页面被浏览器拦截，请在地址栏右侧允许弹出窗口');
            this.loading = false;
            return;
          }
          
          if (this.usePoints && this.pointsToUse > 0) {
            this.updatePoints(this.pointsToUse);
          }
          
          this.paymentConfirmVisible = true;
        }
      } catch (error) {
        Message.error(error.response?.data?.message || '支付发起失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handlePaymentSuccess() {
      this.paymentConfirmVisible = false;
      this.wechatQrVisible = false;
      this.visible = false;
      Message.success('支付成功，正在前往个人中心');
      this.$router.push('/profile');
    },
    handlePaymentFail() {
      this.paymentConfirmVisible = false;
      this.wechatQrVisible = false;
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

.points-deduction-card, .address-card, .payment-method-card {
  background: #FFFDF8;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid #FFE4D6;
}
.points-deduction-card {
  border: 1px dashed #FF7E67;
}

.pay-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}

.pay-option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #FFFFFF;
  border: 1.5px solid #F0F1F2;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pay-option-item:hover {
  border-color: #E5E6EB;
}

.pay-option-item.active {
  background: #FAFAFA;
  border-color: #FF7E67;
  box-shadow: 0 2px 8px rgba(255, 126, 103, 0.12);
}

.pay-icon-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.pay-svg {
  width: 22px;
  height: 22px;
}

.selected-check {
  font-size: 18px;
}

.unselected-circle {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1.5px solid #C9CDD4;
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

<style>
@media (max-width: 768px) {
  /* 将 Arco 默认的中间弹窗重置为小红书同款【底部半屏抽屉弹窗】 */
  .arco-modal-container {
    align-items: flex-end !important; /* 强制对齐到屏幕最底部 */
  }
  
  .buy-modal.arco-modal {
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    border-radius: 24px 24px 0 0 !important; /* 顶部左、右大圆角 */
    padding-bottom: calc(12px + env(safe-area-inset-bottom)) !important; /* 留出手机底部安全区 */
    box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.08) !important;
    animation: slide-up 0.3s cubic-bezier(0.25, 1, 0.5, 1);
  }
  
  .buy-modal .arco-modal-header {
    border-bottom: none !important;
    padding: 24px 20px 12px 20px !important;
    text-align: center;
    position: relative;
  }
  
  .buy-modal .arco-modal-title {
    font-weight: 800 !important;
    font-size: 17px !important;
    color: #1D2129 !important;
    width: 100%;
    justify-content: center; /* 标题居中 */
  }
  
  .buy-modal .arco-modal-body {
    padding: 0 20px 20px 20px !important;
    max-height: 70vh !important; /* 限制抽屉最大高度为屏幕的 70% */
    overflow-y: auto !important; /* 启用内部滚动 */
    -webkit-overflow-scrolling: touch;
  }

  /* 绘制小红书标志性的顶部【灰色拖拽条/指示条】 */
  .buy-modal .arco-modal-header::before {
    content: '';
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: 40px;
    height: 5px;
    background: #E5E6EB;
    border-radius: 3px;
  }
}
</style>
