<template>
  <a-modal
    :title="null"
    :header="false"
    :closable="false"
    :visible="visible"
    :width="isMobile ? '100%' : '440px'"
    @cancel="visible = false"
    :footer="false"
    modal-class="product-buy-sheet-modal"
    unmount-on-close
  >
    <div class="buy-sheet-container" v-if="product">
      <!-- 1. 顶部商品基本信息栏 (大标题+价格+右侧商品图+右上角极简关闭按钮) -->
      <div class="sheet-header-product">
        <img :src="$formatImageUrl(product.image)" class="header-product-img" alt="商品图片" />
        <div class="header-product-info">
          <h2 class="product-title">{{ product.name }}</h2>
          <div class="product-price-row">
            <span class="price-symbol">¥</span>
            <span class="price-num">{{ product.price }}</span>
            <span v-if="product.stock === 0" class="stock-tag out">已售罄</span>
            <span v-else-if="product.stock > 0 && product.stock <= 5" class="stock-tag warning">仅剩 {{ product.stock }} 件</span>
            <span v-else-if="product.stock === -1" class="stock-tag normal">库存充足</span>
            <span v-else class="stock-tag normal">库存: {{ product.stock }}</span>
          </div>
        </div>
        <!-- 右上角轻量悬浮关闭按钮 -->
        <button class="sheet-close-btn" @click="visible = false" aria-label="关闭">
          <icon-close />
        </button>
      </div>

      <!-- 2. 可滚动内容区 -->
      <div class="sheet-scroll-body">
        <!-- 规格选择区 (胶囊单选风格) -->
        <div class="specs-group-list" v-if="parsedSpecs.length > 0">
          <div v-for="(spec, sIdx) in parsedSpecs" :key="sIdx" class="spec-section">
            <div class="spec-section-title">{{ spec.name }}</div>
            <div class="spec-capsule-row">
              <button
                v-for="(opt, oIdx) in spec.options"
                :key="oIdx"
                type="button"
                class="spec-capsule-btn"
                :class="{ active: selectedSpecs[spec.name] === opt }"
                @click="selectedSpecs[spec.name] = opt"
              >
                {{ opt }}
              </button>
            </div>
          </div>
        </div>

        <!-- 通透行：购买数量 -->
        <div class="cell-action-row">
          <div class="cell-label-left">
            <icon-gift class="cell-icon" />
            <span class="cell-title">购买数量</span>
          </div>
          <a-input-number 
            v-model="buyQuantity" 
            :min="1" 
            :max="product.stock === -1 ? 9999 : (product.stock > 0 ? product.stock : 1)" 
            size="small" 
            mode="button"
            class="qty-stepper"
          />
        </div>

        <!-- 通透行：积分抵扣 (若有积分) -->
        <div class="points-block-wrap" v-if="userPoints > 0">
          <div class="cell-action-row points-cell-row">
            <div class="cell-label-left">
              <icon-trophy class="cell-icon" />
              <span class="cell-title">积分抵扣</span>
              <span class="cell-badge-hint">可用 {{ userPoints }} 积分</span>
            </div>
            <a-switch v-model="usePoints" checked-color="#FF5A34" />
          </div>
          <!-- 积分滑块轻量面板 -->
          <div v-if="usePoints" class="points-slider-panel">
            <a-slider 
              v-model="pointsToUse" 
              :max="maxPointsPossible" 
              :step="100"
              show-input
            />
            <div class="points-hint-row">
              <span>已选积分: {{ pointsToUse }}</span>
              <span class="points-deduct-val">- ¥{{ (pointsToUse / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 配送与收货人卡片 (轻质卡片化收纳) -->
        <div class="section-card delivery-card">
          <div class="card-title-bar">
            <icon-location class="card-icon" />
            <span>配送与联系人</span>
          </div>
          <div class="delivery-inputs-wrap">
            <CampusLocationSelect v-model="shippingAddress" />
            <a-input 
              v-model="contactPhone" 
              placeholder="收件人手机号 (必填)" 
              allow-clear 
              class="sheet-input-item"
            >
              <template #prefix><icon-phone class="input-prefix-icon" /></template>
            </a-input>
            <a-input 
              v-model="buyerRemark" 
              placeholder="顾客备注 / 留言 (选填，如: 放门把手)" 
              allow-clear 
              class="sheet-input-item"
            >
              <template #prefix><icon-edit class="input-prefix-icon" /></template>
            </a-input>
          </div>
        </div>

        <!-- 支付方式卡片 -->
        <div class="section-card pay-card">
          <div class="card-title-bar">
            <icon-safe class="card-icon" />
            <span>支付方式</span>
          </div>
          <div class="pay-channel-grid">
            <div 
              class="pay-channel-capsule"
              :class="{ active: payChannel === 'WECHAT' }"
              @click="payChannel = 'WECHAT'"
            >
              <div class="pay-brand">
                <svg class="pay-icon-svg" viewBox="0 0 24 24" fill="#07C160">
                  <path d="M8.691 2.188C3.891 2.188 0 5.478 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .161.13.29.29.29.08 0 .15-.029.212-.068l1.96-1.141a.853.853 0 0 1 .639-.097c.92.251 1.897.39 2.913.39.309 0 .61-.019.91-.048-.718-2.038-.34-4.321 1.07-5.918 1.453-1.639 3.59-2.529 5.82-2.529.418 0 .833.03 1.238.087C16.892 4.398 13.064 2.188 8.691 2.188zm-2.42 4.145c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm4.84 0c.677 0 1.229.552 1.229 1.23 0 .676-.552 1.228-1.23 1.228-.676 0-1.228-.552-1.228-1.229 0-.677.552-1.229 1.229-1.229zm8.567 4.144c-3.864 0-7.004 2.657-7.004 5.928 0 3.272 3.14 5.928 7.004 5.928.795 0 1.562-.116 2.278-.319a.69.69 0 0 1 .513.078l1.579.919c.05.029.106.048.173.048.13 0 .233-.106.233-.232a.38.38 0 0 0-.039-.175l-.32-1.19a.473.473 0 0 1 .174-.533c1.474-1.085 2.413-2.684 2.413-4.472 0-3.271-3.14-5.93-7.005-5.93zm-2.14 3.428c.552 0 1.007.456 1.007 1.008 0 .551-.455 1.007-1.007 1.007-.551 0-1.007-.456-1.007-1.007 0-.552.456-1.008 1.007-1.008zm4.28 0c.553 0 1.008.456 1.008 1.008 0 .551-.455 1.007-1.008 1.007-.552 0-1.007-.456-1.007-1.007 0-.552.455-1.008 1.007-1.008z"/>
                </svg>
                <span>微信支付</span>
              </div>
              <icon-check-circle-fill v-if="payChannel === 'WECHAT'" style="color: #07C160; font-size: 16px;" />
              <div v-else class="check-empty-circle"></div>
            </div>

            <div 
              class="pay-channel-capsule"
              :class="{ active: payChannel === 'ALIPAY' }"
              @click="payChannel = 'ALIPAY'"
            >
              <div class="pay-brand">
                <svg class="pay-icon-svg" viewBox="0 0 24 24" fill="#1677FF">
                  <path d="M21.42 16.49c-.6-1.06-1.57-2.14-2.88-3.18 1.4-1.6 2.37-3.55 2.82-5.74H15.8V6.2h6.2V4.57h-6.2V2h-2.14v2.57H7.48V6.2h6.18v1.37H3.45v1.64h12.51c-.4 1.77-1.22 3.37-2.39 4.71-1.84-1.26-3.87-2.2-6.02-2.78l-.66 1.58c2.4.65 4.67 1.71 6.72 3.12-1.78 1.87-4.14 3.24-6.85 3.94l.73 1.59c3.15-.83 5.88-2.45 7.93-4.66 1.63 1.25 2.86 2.5 3.63 3.69l1.88-1.56z"/>
                </svg>
                <span>支付宝</span>
              </div>
              <icon-check-circle-fill v-if="payChannel === 'ALIPAY'" style="color: #1677FF; font-size: 16px;" />
              <div v-else class="check-empty-circle"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 已选规格摘要行 (若有规格) -->
      <div class="selected-spec-bar" v-if="specString">
        <span class="label">已选规格：</span>
        <span class="content">{{ specString }}</span>
      </div>

      <!-- 4. 底部吸底操作栏 (价格明细 + 活力橙红胶囊按钮 + 安全区) -->
      <div class="sheet-footer-action-bar">
        <div class="footer-price-group">
          <div class="main-price-row">
            <span class="price-yen">¥</span>
            <span class="price-val">{{ finalPrice }}</span>
            <span class="price-badge-free" v-if="!product.deliveryFee || Number(product.deliveryFee) === 0">包邮</span>
          </div>
          <div class="sub-fee-hint">
            <span v-if="product.deliveryFee && Number(product.deliveryFee) > 0">含配送费 ¥{{ Number(product.deliveryFee).toFixed(2) }}</span>
            <span v-else>免配送费</span>
            <span v-if="usePoints" class="points-tag">已抵 ¥{{ (pointsToUse / 100).toFixed(2) }}</span>
          </div>
        </div>

        <button 
          class="footer-submit-capsule-btn" 
          :disabled="isPayDisabled || loading"
          @click="handleConfirm"
        >
          <icon-loading v-if="loading" :spin="true" />
          <span v-else>{{ product.stock === 0 ? '暂时缺货' : '立即支付' }}</span>
        </button>
      </div>
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
import { createOrder, createAlipay, createWechatPay, createXunhupay } from '@/api/order';
import { Message, Modal } from '@arco-design/web-vue';
import { mapState, mapActions } from 'pinia';
import { useUserStore } from '@/stores/user';
import MapLocationDialog from '@/components/common/MapLocationDialog.vue';
import WechatPayQrModal from '@/components/pay/WechatPayQrModal.vue';
import CampusLocationSelect from '@/components/common/CampusLocationSelect.vue';

export default {
  name: 'ProductBuyModal',
  components: {
    MapLocationDialog,
    WechatPayQrModal,
    CampusLocationSelect
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
      buyQuantity: 1,
      selectedSpecs: {},
      shippingAddress: '',
      contactPhone: '',
      buyerRemark: '',
      payChannel: 'WECHAT',
      mapDialogVisible: false,
      paymentConfirmVisible: false,
      wechatQrVisible: false,
      wechatCodeUrl: '',
      currentOrderId: '',
      isMobile: window.innerWidth <= 768
    };
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    userPoints() {
      return this.userInfo ? this.userInfo.points : 0;
    },
    visible: {
      get() { return this.show; },
      set(val) { this.$emit('update:show', val); }
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
      if (this.product.stock === 0) return true;
      if (this.product.stock > 0 && this.buyQuantity > this.product.stock) return true;
      if (!this.shippingAddress || this.shippingAddress.trim() === '') return true;
      if (!this.contactPhone || this.contactPhone.trim() === '') return true;
      return this.parsedSpecs.some(spec => !this.selectedSpecs[spec.name]);
    },
    totalBaseAmount() {
      if (!this.product || !this.product.price) return 0;
      const count = this.buyQuantity > 0 ? this.buyQuantity : 1;
      const delivery = Number(this.product.deliveryFee) || 0;
      return (Number(this.product.price) * count) + delivery;
    },
    maxPointsPossible() {
      if (!this.product || !this.product.price) return 0;
      const pointsToCover = Math.floor((this.totalBaseAmount - 0.01) * 100);
      return Math.max(0, Math.min(this.userPoints, pointsToCover));
    },
    finalPrice() {
      if (!this.product || !this.product.price) return '0.00';
      const deduction = this.usePoints ? (this.pointsToUse / 100) : 0;
      return Math.max(0.01, this.totalBaseAmount - deduction).toFixed(2);
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.usePoints = false;
        this.pointsToUse = 0;
        this.buyQuantity = 1;
        this.buyerRemark = '';
        this.selectedSpecs = {};
        this.shippingAddress = this.userInfo?.address || '';
        this.contactPhone = this.userInfo?.phone || '';
        this.parsedSpecs.forEach(spec => {
          if (spec.options && spec.options.length > 0) {
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
      if (!this.contactPhone || !this.contactPhone.trim()) {
        return Message.warning('请填写收件人手机号');
      }

      this.loading = true;
      try {
        const orderRes = await createOrder({ 
          productId: this.product.id,
          pointsToUse: this.usePoints ? this.pointsToUse : 0,
          spec: this.specString,
          address: this.shippingAddress,
          quantity: this.buyQuantity,
          contactPhone: this.contactPhone.trim(),
          remark: this.buyerRemark ? this.buyerRemark.trim() : ''
        });

        const orderId = orderRes.data.data.id;
        this.currentOrderId = orderId;

        if (this.usePoints && this.pointsToUse > 0) {
          this.updatePoints(this.userPoints - this.pointsToUse);
        }

        if (this.payChannel === 'WECHAT') {
          // 微信支付
          try {
            const xunhuRes = await createXunhupay(orderId);
            const payData = xunhuRes.data.data;
            this.wechatCodeUrl = payData.qrUrl || payData.payUrl || '';
          } catch (e) {
            const payRes = await createWechatPay(orderId);
            const payData = payRes.data.data;
            this.wechatCodeUrl = payData.code_url || payData.h5_url || '';
          }
          this.wechatQrVisible = true;
        } else {
          // 支付宝支付
          try {
            const xunhuRes = await createXunhupay(orderId);
            const payData = xunhuRes.data.data;
            if (payData && payData.payUrl) {
              if (this.isMobile) {
                window.location.href = payData.payUrl;
              } else {
                const newWin = window.open(payData.payUrl, '_blank');
                if (!newWin) {
                  window.location.href = payData.payUrl;
                }
                this.paymentConfirmVisible = true;
              }
            }
          } catch (xunhuErr) {
            const payRes = await createAlipay(orderId);
            const formHtml = payRes.data.data;
            
            const newWindow = window.open('', '_blank');
            if (newWindow) {
              newWindow.document.write(formHtml);
              newWindow.document.close();
              this.paymentConfirmVisible = true;
            } else {
              Message.warning('支付页面被浏览器拦截，请允许弹出窗口');
            }
          }
          this.visible = false;
        }
      } catch (error) {
        Message.error(error.response?.data?.message || '支付发起失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handlePaymentSuccess() {
      Modal.confirm({
        title: '确认已完成支付？',
        content: '请确保您已成功完成付款。确认后系统将为您跳转到订单中心查看发货与自提进度。',
        okText: '确认已付款',
        cancelText: '尚未付款',
        okButtonProps: {
          style: { backgroundColor: '#FF7E67', borderColor: '#FF7E67' }
        },
        onOk: () => {
          this.paymentConfirmVisible = false;
          this.wechatQrVisible = false;
          this.visible = false;
          Message.success('支付成功，正在前往个人中心');
          this.$router.push('/profile');
        }
      });
    },
    handlePaymentFail() {
      this.paymentConfirmVisible = false;
      this.wechatQrVisible = false;
      Message.info('您可以稍后在我的订单中继续支付');
    }
  }
};
</script>

<style scoped>
/* 1. 整体抽屉容器 */
.buy-sheet-container {
  width: 100%;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  max-height: 86dvh;
  position: relative;
  overflow: hidden;
  user-select: none;
}

/* 2. 顶部商品基本信息栏 (吸顶固定) */
.sheet-header-product {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #F2F3F5;
  background: #FFFFFF;
  position: relative;
  flex-shrink: 0;
}

.header-product-img {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-product-info {
  flex: 1;
  min-width: 0;
  padding-right: 28px; /* 留出关闭按钮空间 */
}

.product-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-symbol {
  font-size: 14px;
  font-weight: 700;
  color: #FF5A34;
}

.price-num {
  font-size: 22px;
  font-weight: 800;
  color: #FF5A34;
  line-height: 1;
}

.stock-tag {
  font-size: 11px;
  margin-left: 8px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.stock-tag.normal {
  color: #86909C;
  background: #F2F3F5;
}

.stock-tag.warning {
  color: #F53F3F;
  background: #FFECE8;
  font-weight: 600;
}

.stock-tag.out {
  color: #C9CDD4;
  background: #F7F8FA;
}

/* 右上角轻巧关闭按钮 */
.sheet-close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #86909C;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sheet-close-btn:hover {
  background: #E5E6EB;
  color: #1D2129;
}

.sheet-close-btn:active {
  transform: scale(0.92);
}

/* 3. 中间可滚动区域 (呼吸感极佳) */
.sheet-scroll-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  -webkit-overflow-scrolling: touch;
}

.sheet-scroll-body::-webkit-scrollbar {
  width: 4px;
}

.sheet-scroll-body::-webkit-scrollbar-thumb {
  background: #E5E6EB;
  border-radius: 4px;
}

/* 规格选择 */
.specs-group-list {
  margin-bottom: 8px;
}

.spec-section {
  margin-bottom: 10px;
}

.spec-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 8px;
}

.spec-capsule-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-capsule-btn {
  padding: 6px 14px;
  border-radius: 8px;
  background: #F4F5F7;
  border: 1.5px solid transparent;
  color: #4E5969;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  transition: all 0.16s ease;
}

.spec-capsule-btn:active {
  transform: scale(0.96);
}

.spec-capsule-btn.active {
  background: #FFF5F2;
  border-color: #FF5A34;
  color: #FF5A34;
  font-weight: 700;
}

/* 通透行布局 (数量 & 积分) */
.cell-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F5F6F8;
}

.cell-label-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.cell-icon {
  font-size: 15px;
  color: #86909C;
}

.cell-title {
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
}

.cell-badge-hint {
  font-size: 11px;
  color: #86909C;
  background: #F2F3F5;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 400;
}

.qty-stepper {
  width: 106px;
}

/* 积分滑块面板 */
.points-block-wrap {
  border-bottom: 1px solid #F5F6F8;
}

.points-cell-row {
  border-bottom: none;
}

.points-slider-panel {
  background: #FFF9F7;
  border-radius: 10px;
  padding: 10px 14px 8px;
  margin-bottom: 10px;
}

.points-hint-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}

.points-deduct-val {
  color: #FF5A34;
  font-weight: 700;
}

/* 4. 轻质卡片收纳 (配送与联系人、支付方式) */
.section-card {
  background: #F9FAFB;
  border: 1px solid #F0F2F5;
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}

.card-title-bar {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 10px;
}

.card-icon {
  font-size: 14px;
  color: #86909C;
}

.delivery-inputs-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sheet-input-item {
  border-radius: 8px;
  background: #FFFFFF;
}

.input-prefix-icon {
  color: #86909C;
  font-size: 14px;
}

/* 支付渠道胶囊 */
.pay-channel-grid {
  display: flex;
  gap: 10px;
}

.pay-channel-capsule {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 10px;
  border: 1.5px solid #E5E6EB;
  background: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pay-channel-capsule.active {
  border-color: #FF5A34;
  background: #FFF9F7;
}

.pay-brand {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
}

.pay-icon-svg {
  width: 18px;
  height: 18px;
}

.check-empty-circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1.5px solid #C9CDD4;
}

/* 5. 已选规格摘要行 */
.selected-spec-bar {
  padding: 6px 16px;
  background: #FAFAFA;
  border-top: 1px solid #F2F3F5;
  font-size: 12px;
  line-height: 1.4;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.selected-spec-bar .label {
  color: #4E5969;
  font-weight: 600;
}

.selected-spec-bar .content {
  color: #1D2129;
  font-weight: 500;
}

/* 6. 底部吸底操作条 */
.sheet-footer-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom, 0px));
  background: #FFFFFF;
  border-top: 1px solid #F2F3F5;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.footer-price-group {
  display: flex;
  flex-direction: column;
}

.main-price-row {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-yen {
  font-size: 14px;
  font-weight: 700;
  color: #FF5A34;
}

.price-val {
  font-size: 24px;
  font-weight: 800;
  color: #FF5A34;
}

.price-badge-free {
  margin-left: 6px;
  font-size: 11px;
  background: #E8FFEA;
  color: #00B42A;
  padding: 1px 5px;
  border-radius: 4px;
  font-weight: 600;
}

.sub-fee-hint {
  font-size: 11px;
  color: #86909C;
  display: flex;
  gap: 6px;
  align-items: center;
}

.points-tag {
  color: #FF5A34;
  font-weight: 600;
}

/* 品牌活力大胶囊按钮 */
.footer-submit-capsule-btn {
  height: 42px;
  padding: 0 28px;
  border-radius: 21px;
  background: linear-gradient(135deg, #FF6B4A 0%, #FF4D2E 100%);
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  box-shadow: 0 4px 14px rgba(255, 77, 46, 0.3);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.footer-submit-capsule-btn:not(:disabled):active {
  transform: scale(0.96);
  box-shadow: 0 2px 6px rgba(255, 77, 46, 0.2);
}

.footer-submit-capsule-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* PC 端弹窗居中与大圆角 */
@media (min-width: 769px) {
  .buy-sheet-container {
    border-radius: 20px;
    max-height: 85vh;
  }
}
</style>

