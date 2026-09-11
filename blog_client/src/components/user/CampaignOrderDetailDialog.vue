<template>
  <a-modal 
    :title="null"
    :header="false"
    :closable="false"
    :visible="visible" 
    :width="isMobile ? 'calc(100% - 32px)' : '520px'" 
    @cancel="handleCancel" 
    :footer="false"
  >
    <div v-if="order" class="order-detail-container">
      <button class="sheet-circle-close" @click="handleCancel" aria-label="关闭">
        <icon-close />
      </button>

      <!-- 1. 状态区 -->
      <div class="status-header">
        <div class="status-icon-box" :style="{ background: getStatusBg(order.status) }">
          <component :is="getStatusIcon(order.status)" class="status-icon" :style="{ color: getStatusColor(order.status) }" />
        </div>
        <div class="status-text">
          <div class="status-title-row">
            <h3>{{ getStatusText(order.status) }}</h3>
            <span class="follow-badge" v-if="order.followNumber">跟团号 #{{ order.followNumber }}</span>
          </div>
          <p>{{ getStatusSubText(order.status) }}</p>
        </div>
      </div>

      <!-- 2. 自提履约与联系信息区 -->
      <div class="detail-card">
        <div class="card-title">
          <icon-location /> 提货履约与联系信息
        </div>
        <div class="address-text" style="font-weight: 600; color: #1D2129;">
          {{ order.campaign?.deliveryLocation?.name || '指定自提点' }}
        </div>
        <div class="address-subtext" v-if="order.campaign?.deliveryLocation?.address">
          {{ order.campaign.deliveryLocation.address }}
        </div>
        <div class="contact-links-row">
          <a 
            v-if="order.campaign?.deliveryLocation?.contactPhone" 
            :href="'tel:' + order.campaign.deliveryLocation.contactPhone"
            class="contact-link-pill"
          >
            <icon-phone /> 提货点电话: {{ order.campaign.deliveryLocation.contactPhone }}
          </a>
          <span v-if="order.contactPhone" class="contact-user-info">
            顾客预留: {{ order.contactPhone }} ({{ order.contactName || '顾客' }})
          </span>
        </div>
        <div v-if="order.campaign?.deliveryTime" class="delivery-time-tip">
          <icon-clock-circle /> 预计出炉/提货: {{ $formatTime(order.campaign.deliveryTime) }}
        </div>
        <div v-if="order.remark" class="remark-box">
          <icon-message /> 备注: {{ order.remark }}
        </div>
      </div>

      <!-- 3. 商品清单区 -->
      <div class="detail-card">
        <div class="card-title-between">
          <div class="card-title" style="margin-bottom: 0;">
            <icon-gift /> 跟团商品清单
          </div>
          <span class="campaign-tag-pill" v-if="order.campaign?.title">
            {{ order.campaign.title }}
          </span>
        </div>

        <div class="product-items-list">
          <div 
            v-for="item in (order.items || [])" 
            :key="item.id" 
            class="product-item"
          >
            <a-image 
              :src="$formatImageUrl(item.productImage)" 
              class="product-img" 
              width="60" 
              height="60" 
              fit="cover" 
            />
            <div class="product-info">
              <div class="pname">{{ item.productName || '烘焙商品' }}</div>
              <div class="pspec" v-if="item.specs">规格: {{ item.specs }}</div>
              <div class="pprice-qty">
                <span class="item-unit-price">¥{{ item.price }}</span>
                <span class="item-qty-tag">×{{ item.quantity || 1 }}</span>
              </div>
            </div>
            <div class="item-subtotal">
              ¥{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 订单基本信息区 -->
      <div class="detail-card">
        <div class="card-title"><icon-list /> 订单信息</div>
        <div class="info-row">
          <span class="info-label">订单编号</span>
          <span class="info-value copyable" @click="handleCopy(order.id, '订单编号')">
            {{ order.id }} <icon-copy class="copy-tiny" />
          </span>
        </div>
        <div class="info-row">
          <span class="info-label">下单时间</span>
          <span class="info-value">{{ $formatTime(order.createTime) }}</span>
        </div>
        <div class="info-row" v-if="order.payTime">
          <span class="info-label">支付时间</span>
          <span class="info-value">{{ $formatTime(order.payTime) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单类型</span>
          <span class="info-value">社区快团跟团</span>
        </div>
        <div class="info-row">
          <span class="info-label">商品总件数</span>
          <span class="info-value">{{ calcTotalQuantity(order.items) }} 件</span>
        </div>
        <div class="info-row">
          <span class="info-label">实付款</span>
          <span class="info-value price">¥{{ order.totalAmount }}</span>
        </div>
      </div>

      <!-- 5. 底部操作区 -->
      <div class="action-footer" v-if="order.status === 0 || order.status === 3">
        <a-button status="danger" shape="round" @click="handleDeleteCurrentOrder">
          {{ order.status === 0 ? '取消跟团' : '删除记录' }}
        </a-button>
        <a-button 
          v-if="order.status === 0" 
          type="primary" 
          style="background: linear-gradient(135deg, #FF7E67 0%, #FF5A34 100%); border: none;" 
          shape="round" 
          @click="handlePay"
        >
          立即支付
        </a-button>
      </div>
      <div class="action-footer" v-else-if="order.status === 2">
        <a-button shape="round" type="outline" @click="handleReorder">
          再来一单
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { deleteUnpaidCampaignOrder } from '@/api/campaign';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'CampaignOrderDetailDialog',
  props: {
    show: Boolean,
    order: Object
  },
  emits: ['update:show', 'pay', 'refresh'],
  data() {
    return {
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleCancel() {
      this.visible = false;
    },
    handlePay() {
      this.$emit('pay', this.order);
      this.visible = false;
    },
    handleReorder() {
      this.visible = false;
      if (!this.order?.campaign || this.order.campaign.status === 2) {
        Message.info('该快团已结团下线，已为您前往烘焙商城挑选美味');
        this.$router.push('/store');
        return;
      }
      this.$router.push(`/campaign/${this.order.campaignId}`);
    },
    calcTotalQuantity(items) {
      if (!items || !Array.isArray(items)) return 1;
      return items.reduce((sum, it) => sum + (it.quantity || 1), 0);
    },
    handleCopy(text, name = '信息') {
      if (!text) return;
      if (navigator?.clipboard?.writeText) {
        navigator.clipboard.writeText(String(text).trim()).then(() => {
          Message.success(`${name}已复制到剪贴板`);
        });
      }
    },
    handleDeleteCurrentOrder() {
      if (!this.order) return;
      Modal.confirm({
        title: '删除订单确认',
        content: this.order.status === 0 
          ? '确定要取消此未支付跟团订单吗？' 
          : '确定要删除此跟团记录吗？删除后不可恢复。',
        okText: '确认删除',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidCampaignOrder(this.order.id);
            Message.success('订单已成功删除');
            this.visible = false;
            this.$emit('refresh');
          } catch (e) {
            Message.error(e.response?.data?.message || '删除订单失败');
          }
        }
      });
    },
    getStatusText(status) {
      const texts = ['等待付款', '待提货 / 备料中', '已提货完成', '交易已关闭'];
      return texts[status] || '处理中';
    },
    getStatusSubText(status) {
      const texts = [
        '请尽快完成支付，超时订单将被自动取消',
        '商家正在现烤备料中，送达自提点后将电话联系您',
        '商品已顺利提货，感谢您参与本次快团',
        '跟团订单已取消或关闭'
      ];
      return texts[status] || '';
    },
    getStatusIcon(status) {
      const icons = ['icon-safe', 'icon-check-circle-fill', 'icon-check-circle', 'icon-close-circle-fill'];
      return icons[status] || 'icon-info-circle-fill';
    },
    getStatusColor(status) {
      const colors = ['#FF7E67', '#00B42A', '#165DFF', '#86909C'];
      return colors[status] || '#86909C';
    },
    getStatusBg(status) {
      const bgs = ['#FFF5F4', '#E8FFEA', '#E8F3FF', '#F2F3F5'];
      return bgs[status] || '#F2F3F5';
    }
  }
}
</script>

<style scoped>
.order-detail-container {
  padding: 10px 14px 20px;
  max-height: 75vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
}

.sheet-circle-close {
  position: absolute;
  right: 14px;
  top: 14px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}
.sheet-circle-close:active {
  background: #E5E6EB;
  transform: scale(0.92);
}

.status-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 24px;
}
.status-icon-box {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.status-icon {
  font-size: 28px;
}
.status-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-text h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1D2129;
}
.status-text p {
  margin: 0;
  font-size: 13px;
  color: #86909C;
}
.follow-badge {
  font-size: 11px;
  font-weight: 700;
  color: #FF5A34;
  background: #FFF2ED;
  padding: 2px 8px;
  border-radius: 12px;
}

.detail-card {
  background: #F7F8FA;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}
.card-title {
  font-size: 14px;
  font-weight: bold;
  color: #1D2129;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.card-title-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.campaign-tag-pill {
  font-size: 11px;
  color: #FF5A34;
  background: rgba(255, 90, 52, 0.08);
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 600;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.address-text {
  font-size: 14px;
  color: #1D2129;
  line-height: 1.5;
}
.address-subtext {
  font-size: 12px;
  color: #86909C;
  margin-top: 3px;
}
.contact-links-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.contact-link-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #165DFF;
  background: #E8F3FF;
  padding: 3px 8px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
}
.contact-user-info {
  font-size: 12px;
  color: #4E5969;
}
.delivery-time-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #D97706;
  background: #FFF7E8;
  padding: 4px 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}
.remark-box {
  margin-top: 8px;
  font-size: 12px;
  color: #86909C;
  background: #FFFFFF;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #E5E6EB;
}

.product-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.product-item {
  display: flex;
  gap: 12px;
  align-items: center;
  background: #FFFFFF;
  padding: 10px;
  border-radius: 8px;
}
.product-img {
  border-radius: 6px;
  flex-shrink: 0;
}
.product-info {
  flex: 1;
  min-width: 0;
}
.pname {
  font-size: 13px;
  color: #1D2129;
  font-weight: 600;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pspec {
  font-size: 11px;
  color: #86909C;
}
.pprice-qty {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}
.item-unit-price {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}
.item-qty-tag {
  font-size: 11px;
  color: #86909C;
}
.item-subtotal {
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
  flex-shrink: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
}
.info-row:last-child {
  margin-bottom: 0;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #E5E6EB;
}
.info-label {
  color: #86909C;
}
.info-value {
  color: #1D2129;
  text-align: right;
  word-break: break-all;
  max-width: 70%;
}
.info-value.copyable {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #4E5969;
}
.info-value.copyable:hover {
  color: #165DFF;
}
.copy-tiny {
  font-size: 12px;
  color: #C9CDD4;
}
.info-value.price {
  color: #FF5A34;
  font-size: 18px;
  font-weight: bold;
}

.action-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 20px 18px 16px;
    max-height: calc(82vh - 70px);
  }
  .sheet-circle-close {
    top: auto !important;
    right: auto !important;
    bottom: -58px !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
    width: 40px !important;
    height: 40px !important;
    border-radius: 50% !important;
    background: rgba(30, 30, 30, 0.45) !important;
    backdrop-filter: blur(8px) !important;
    -webkit-backdrop-filter: blur(8px) !important;
    border: 1.5px solid rgba(255, 255, 255, 0.85) !important;
    color: #FFFFFF !important;
    font-size: 18px !important;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25) !important;
    z-index: 9999 !important;
  }
  .sheet-circle-close:active {
    transform: translateX(-50%) scale(0.9) !important;
    background: rgba(0, 0, 0, 0.75) !important;
  }
}
</style>
