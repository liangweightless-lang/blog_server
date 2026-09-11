<template>
  <div class="order-list-component">
    <a-empty v-if="orders.length === 0" :description="emptyText || '暂无订单记录'" style="margin: 40px 0;">
      <template #image><icon-gift style="font-size: 48px; color: #D3C1BA; opacity: 0.5;" /></template>
    </a-empty>
    <div v-else class="order-full-list">
      <div 
        v-for="order in orders" 
        :key="order.id" 
        class="order-card-item" 
        @click="$emit('detail', order)"
      >
        <!-- 1. 卡片头部：单号（支持复制）与状态标签 -->
        <div class="order-card-header">
          <div class="order-id-wrap" @click.stop="handleCopyOrderId(order.id)" title="点击复制订单号">
            <span class="order-id">单号: {{ order.id.substring(0, 14) }}...</span>
            <icon-copy class="copy-icon" />
          </div>
          <div class="header-right-tags">
            <span class="order-date-text">{{ $formatTime(order.createTime) }}</span>
            <a-tag :color="getOrderStatusColor(order.status)" size="small" class="status-tag">
              {{ getOrderStatusText(order.status) }}
            </a-tag>
          </div>
        </div>

        <!-- 2. 卡片中间：商品信息区 -->
        <div class="order-card-body">
          <div class="product-thumb-box">
            <img 
              v-if="order.productImage" 
              :src="$formatImageUrl(order.productImage)" 
              class="product-img" 
              alt="商品图"
            />
            <div v-else class="empty-product-img">
              <icon-image style="font-size: 28px; color: #C9CDD4;" />
            </div>
          </div>
          
          <div class="order-main-info">
            <div class="title-price-row">
              <h4 class="order-pname">{{ order.productName || '商品详情' }}</h4>
              <span class="unit-price">¥{{ order.productOriginalPrice || order.amount }}</span>
            </div>
            
            <div class="meta-row">
              <span class="spec-capsule" v-if="order.selectedSpec">
                {{ order.selectedSpec }}
              </span>
              <span class="type-capsule" :class="order.orderType === 'GROUP' ? 'type-group' : 'type-single'">
                {{ order.orderType === 'GROUP' ? '拼团' : '单买' }}
              </span>
              <span class="qty-text">×{{ order.quantity || 1 }}</span>
            </div>

            <!-- 自提/配送地址展示 -->
            <div class="delivery-address-row" v-if="order.shippingAddress">
              <icon-location class="addr-icon" />
              <span class="addr-text">{{ order.shippingAddress }}</span>
            </div>
          </div>
        </div>

        <!-- 3. 卡片底部：实付金额结算与独立操作按钮区（彻底杜绝横向挤压） -->
        <div class="order-card-footer">
          <div class="settle-summary-row">
            <div class="discount-tips">
              <span v-if="order.pointsUsed" class="points-badge">
                积分已抵 ¥{{ (order.pointsUsed / 100).toFixed(2) }}
              </span>
              <span v-if="order.deliveryFee && Number(order.deliveryFee) > 0" class="fee-badge">
                含运费¥{{ order.deliveryFee }}
              </span>
            </div>
            <div class="actual-price-box">
              <span class="qty-sum">共 {{ order.quantity || 1 }} 件</span>
              <span class="price-label">实付款:</span>
              <span class="price-currency">¥</span>
              <span class="price-number">{{ order.amount }}</span>
            </div>
          </div>

          <!-- 独立操作按钮栏 -->
          <div class="order-actions-row">
            <!-- 待支付状态 (0) -->
            <template v-if="order.status === 0">
              <a-button 
                type="text" 
                status="danger" 
                size="small" 
                class="btn-cancel"
                @click.stop="handleDeleteOrder(order)"
              >
                取消订单
              </a-button>
              <a-button 
                type="primary" 
                size="small" 
                shape="round" 
                class="btn-pay-now"
                @click.stop="$emit('pay', order)"
              >
                立即支付
              </a-button>
            </template>

            <!-- 已支付状态 (1) -->
            <template v-else-if="order.status === 1">
              <a-button 
                type="outline" 
                size="small" 
                shape="round" 
                class="btn-view-detail"
                @click.stop="$emit('detail', order)"
              >
                查看提货详情
              </a-button>
            </template>

            <!-- 已发货/已完成状态 (3) -->
            <template v-else-if="order.status === 3">
              <a-button 
                type="outline" 
                size="small" 
                shape="round" 
                class="btn-view-detail"
                @click.stop="$emit('detail', order)"
              >
                查看物流/核销
              </a-button>
            </template>

            <!-- 已取消状态 (2) -->
            <template v-else-if="order.status === 2">
              <a-button 
                type="text" 
                status="danger" 
                size="small" 
                class="btn-del-record"
                @click.stop="handleDeleteOrder(order)"
              >
                <template #icon><icon-delete /></template>
                删除记录
              </a-button>
            </template>
          </div>
        </div>
      </div>
      <p class="list-end-tip">已展示全部 {{ orders.length }} 个订单</p>
    </div>
  </div>
</template>

<script>
import { deleteUnpaidOrder } from '@/api/order';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'OrderList',
  props: {
    orders: {
      type: Array,
      default: () => []
    },
    emptyText: {
      type: String,
      default: '暂无订单记录'
    }
  },
  emits: ['detail', 'pay', 'refresh'],
  methods: {
    getOrderStatusColor(status) {
      const types = ['orange', 'green', 'gray', 'blue'];
      return types[status] || 'gray';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    },
    handleCopyOrderId(id) {
      if (!id) return;
      if (navigator?.clipboard?.writeText) {
        navigator.clipboard.writeText(id).then(() => {
          Message.success('订单号已复制到剪贴板');
        }).catch(() => {
          this.fallbackCopy(id);
        });
      } else {
        this.fallbackCopy(id);
      }
    },
    fallbackCopy(text) {
      const input = document.createElement('input');
      input.value = text;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      Message.success('订单号已复制');
    },
    handleDeleteOrder(order) {
      Modal.confirm({
        title: '删除订单确认',
        content: order.status === 0 
          ? '确定要取消此未支付订单吗？抵扣积分将原路退回。' 
          : '确定要删除此订单记录吗？删除后不可恢复。',
        okText: '确认删除',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidOrder(order.id);
            Message.success('订单已成功删除');
            this.$emit('refresh');
          } catch (e) {
            Message.error(e.response?.data?.message || '删除订单失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.order-full-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card-item {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  cursor: pointer;
  box-sizing: border-box;
}

.order-card-item:active {
  transform: scale(0.992);
}

/* 1. 卡片头部 */
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #F2F3F5;
}

.order-id-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #86909C;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}

.order-id-wrap:hover .copy-icon {
  color: #FF5A34;
}

.copy-icon {
  font-size: 13px;
  color: #C9CDD4;
  cursor: pointer;
}

.header-right-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.order-date-text {
  font-size: 11px;
  color: #C9CDD4;
  white-space: nowrap;
}

.status-tag {
  font-weight: 600;
  border-radius: 6px;
}

/* 2. 卡片中部：商品图文 */
.order-card-body {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-thumb-box {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #F7F8FA;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.empty-product-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.order-main-info {
  flex: 1;
  min-width: 0;
}

.title-price-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.order-pname {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unit-price {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  flex-shrink: 0;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 6px 0 4px 0;
}

.spec-capsule {
  font-size: 11px;
  color: #4E5969;
  background: #F2F3F5;
  padding: 2px 8px;
  border-radius: 4px;
}

.type-capsule {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}

.type-group {
  color: #FF5A34;
  background: #FFF2ED;
}

.type-single {
  color: #165DFF;
  background: #E8F3FF;
}

.qty-text {
  font-size: 12px;
  color: #86909C;
  margin-left: auto;
}

.delivery-address-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #86909C;
  margin-top: 4px;
  background: #FAFAFA;
  padding: 4px 8px;
  border-radius: 6px;
}

.addr-icon {
  font-size: 12px;
  color: #FF5A34;
  flex-shrink: 0;
}

.addr-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 3. 卡片底部：实付与独立操作栏 */
.order-card-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #F7F8FA;
}

.settle-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.discount-tips {
  display: flex;
  align-items: center;
  gap: 6px;
}

.points-badge {
  font-size: 10px;
  color: #FF7D00;
  background: #FFF7E8;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.fee-badge {
  font-size: 10px;
  color: #86909C;
  background: #F2F3F5;
  padding: 2px 6px;
  border-radius: 4px;
}

.actual-price-box {
  margin-left: auto;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.qty-sum {
  font-size: 11px;
  color: #86909C;
}

.price-label {
  font-size: 12px;
  color: #4E5969;
}

.price-currency {
  font-size: 13px;
  font-weight: 800;
  color: #1D2129;
}

.price-number {
  font-size: 17px;
  font-weight: 800;
  color: #1D2129;
}

.order-actions-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.btn-cancel {
  font-size: 12px;
  color: #86909C;
}

.btn-del-record {
  font-size: 12px;
  color: #86909C;
}

.btn-del-record:hover {
  color: #F53F3F;
}

.btn-pay-now {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%) !important;
  border: none !important;
  font-size: 12px;
  font-weight: 700;
  padding: 0 16px;
  height: 30px;
  box-shadow: 0 4px 10px rgba(255, 94, 58, 0.25);
}

.btn-view-detail {
  font-size: 12px;
  border-color: #E5E6EB;
  color: #4E5969;
  height: 30px;
  padding: 0 14px;
}

.btn-view-detail:hover {
  color: #FF5A34;
  border-color: #FF5A34;
}

.list-end-tip {
  text-align: center;
  font-size: 11px;
  color: #C9CDD4;
  margin-top: 14px;
}
</style>

