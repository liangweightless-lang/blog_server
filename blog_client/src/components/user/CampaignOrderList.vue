<template>
  <div class="campaign-order-list-component">
    <!-- 1. 状态微胶囊横滑筛选栏 -->
    <div class="filter-capsule-scroll" v-if="orders.length > 0">
      <div class="capsule-track">
        <button 
          v-for="filter in filterOptions" 
          :key="filter.key"
          class="filter-pill-btn" 
          :class="{ 'is-active': activeFilter === filter.key }"
          @click="activeFilter = filter.key"
        >
          <span class="pill-label">{{ filter.label }}</span>
          <span 
            class="pill-badge" 
            :class="{ 
              'badge-highlight': filter.key === 'pendingPickup' && getFilterCount(filter.key) > 0,
              'badge-danger': filter.key === 'pendingPay' && getFilterCount(filter.key) > 0,
              'badge-zero': getFilterCount(filter.key) === 0 
            }"
          >
            {{ getFilterCount(filter.key) }}
          </span>
        </button>
      </div>
    </div>

    <!-- 2. 空状态展示 -->
    <a-empty v-if="filteredOrders.length === 0" :description="getEmptyText()" style="margin: 40px 0;">
      <template #image><icon-fire style="font-size: 48px; color: #D3C1BA; opacity: 0.5;" /></template>
      <template #extra v-if="activeFilter !== 'ALL'">
        <a-button size="small" shape="round" @click="activeFilter = 'ALL'">
          查看全部快团记录
        </a-button>
      </template>
    </a-empty>

    <!-- 3. 跟团订单精美卡片列表（全面对齐 OrderList 标准设计） -->
    <div v-else class="order-full-list">
      <div 
        v-for="order in filteredOrders" 
        :key="order.id" 
        class="order-card-item" 
        @click="$emit('detail', order)"
      >
        <!-- 1. 卡片头部：单号复制 + 下单时间 + 状态标签 -->
        <div class="order-card-header">
          <div class="order-id-wrap" @click.stop="handleCopy(order.id, '订单号')" title="点击复制订单号">
            <span class="order-id">单号: {{ String(order.id).substring(0, 14) }}...</span>
            <icon-copy class="copy-icon" />
          </div>
          <div class="header-right-tags">
            <span class="follow-no-tag" v-if="order.followNumber">跟团号 #{{ order.followNumber }}</span>
            <span class="order-date-text">{{ $formatTime(order.createTime) }}</span>
            <a-tag :color="getOrderStatusColor(order.status)" size="small" class="status-tag">
              {{ getOrderStatusText(order.status) }}
            </a-tag>
          </div>
        </div>

        <!-- 2. 卡片中间：商品图文信息与自提履约行 -->
        <div class="order-card-body">
          <div class="product-thumb-box">
            <img 
              v-if="getFirstProductImage(order)" 
              :src="$formatImageUrl(getFirstProductImage(order))" 
              class="product-img" 
              alt="商品图"
            />
            <div v-else class="empty-product-img">
              <icon-image style="font-size: 28px; color: #C9CDD4;" />
            </div>
          </div>
          
          <div class="order-main-info">
            <div class="title-price-row">
              <h4 class="order-pname">{{ getOrderDisplayTitle(order) }}</h4>
              <span class="unit-price" v-if="order.items && order.items.length === 1">¥{{ order.items[0].price }}</span>
            </div>
            
            <div class="meta-row">
              <span class="spec-capsule" v-if="order.items && order.items[0]?.specs">
                {{ order.items[0].specs }}
              </span>
              <span class="type-capsule type-group">
                快团
              </span>
              <span class="qty-text">共 {{ calcTotalQuantity(order.items) }} 件</span>
            </div>

            <!-- 自提点与预计出炉信息行 -->
            <div class="delivery-address-row" v-if="order.campaign?.deliveryLocation?.name">
              <icon-location class="addr-icon" />
              <span class="addr-text">{{ order.campaign.deliveryLocation.name }}</span>
              <span class="time-text-inline" v-if="order.campaign?.deliveryTime">· {{ $formatTime(order.campaign.deliveryTime) }}出炉</span>
            </div>
          </div>
        </div>

        <!-- 3. 卡片底部：实付金额结算与独立操作按钮区 -->
        <div class="order-card-footer">
          <div class="settle-summary-row">
            <div class="discount-tips">
              <span class="campaign-name-tag" v-if="order.campaign?.title">
                {{ order.campaign.title }}
              </span>
              <span v-else class="archived-tag">
                活动已下线
              </span>
            </div>
            <div class="actual-price-box">
              <span class="qty-sum">共 {{ calcTotalQuantity(order.items) }} 件</span>
              <span class="price-label">实付款:</span>
              <span class="price-currency">¥</span>
              <span class="price-number">{{ order.totalAmount }}</span>
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
                取消跟团
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

            <!-- 待提货状态 (1) -->
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

            <!-- 已提货完成状态 (2) -->
            <template v-else-if="order.status === 2">
              <a-button 
                type="outline" 
                size="small" 
                shape="round" 
                class="btn-reorder"
                @click.stop="handleReorder(order)"
              >
                再来一单
              </a-button>
              <a-button 
                type="outline" 
                size="small" 
                shape="round" 
                class="btn-view-detail"
                @click.stop="$emit('detail', order)"
              >
                查看详情
              </a-button>
            </template>

            <!-- 已取消状态 (3) -->
            <template v-else-if="order.status === 3">
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
      <p class="list-end-tip">已展示全部 {{ filteredOrders.length }} 条跟团记录</p>
    </div>
  </div>
</template>

<script>
import { deleteUnpaidCampaignOrder } from '@/api/campaign';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'CampaignOrderList',
  props: {
    orders: {
      type: Array,
      default: () => []
    }
  },
  emits: ['pay', 'refresh', 'detail'],
  data() {
    return {
      activeFilter: 'ALL',
      filterOptions: [
        { key: 'ALL', label: '全部' },
        { key: 'pendingPay', label: '待付款' },
        { key: 'pendingPickup', label: '待提货' },
        { key: 'completed', label: '已完成' },
        { key: 'cancelled', label: '已取消' }
      ]
    }
  },
  computed: {
    filteredOrders() {
      if (this.activeFilter === 'ALL') {
        return this.orders;
      }
      if (this.activeFilter === 'pendingPay') {
        return this.orders.filter(o => o.status === 0);
      }
      if (this.activeFilter === 'pendingPickup') {
        return this.orders.filter(o => o.status === 1);
      }
      if (this.activeFilter === 'completed') {
        return this.orders.filter(o => o.status === 2);
      }
      if (this.activeFilter === 'cancelled') {
        return this.orders.filter(o => o.status === 3);
      }
      return this.orders;
    }
  },
  methods: {
    getFilterCount(key) {
      if (key === 'ALL') return this.orders.length;
      if (key === 'pendingPay') return this.orders.filter(o => o.status === 0).length;
      if (key === 'pendingPickup') return this.orders.filter(o => o.status === 1).length;
      if (key === 'completed') return this.orders.filter(o => o.status === 2).length;
      if (key === 'cancelled') return this.orders.filter(o => o.status === 3).length;
      return 0;
    },
    getEmptyText() {
      const map = {
        ALL: '暂无任何跟团记录',
        pendingPay: '暂无待付款跟团记录',
        pendingPickup: '暂无待提货跟团记录',
        completed: '暂无已完成跟团记录',
        cancelled: '暂无已取消记录'
      };
      return map[this.activeFilter] || '暂无数据';
    },
    getOrderStatusColor(status) {
      const colors = ['orange', 'green', 'blue', 'gray'];
      return colors[status] || 'gray';
    },
    getOrderStatusText(status) {
      const texts = ['待付款', '待提货', '已完成', '已取消'];
      return texts[status] || '处理中';
    },
    getFirstProductImage(order) {
      if (order.items && order.items.length > 0 && order.items[0].productImage) {
        return order.items[0].productImage;
      }
      return '';
    },
    getOrderDisplayTitle(order) {
      if (order.items && order.items.length > 0) {
        const first = order.items[0].productName;
        return order.items.length > 1 ? `${first} 等 ${this.calcTotalQuantity(order.items)} 件商品` : first;
      }
      return order.campaign?.title || '团购商品';
    },
    calcTotalQuantity(items) {
      if (!items || !Array.isArray(items)) return 1;
      return items.reduce((sum, it) => sum + (it.quantity || 1), 0);
    },
    handleReorder(order) {
      if (!order?.campaign || order.campaign.status === 2) {
        Message.info('该快团已结团下线，已为您前往烘焙商城挑选美味');
        this.$router.push('/store');
        return;
      }
      this.$router.push(`/campaign/${order.campaignId}`);
    },
    handleCopy(text, name = '单号') {
      if (!text) return;
      const str = String(text).trim();
      if (navigator?.clipboard?.writeText) {
        navigator.clipboard.writeText(str).then(() => {
          Message.success(`${name}已复制到剪贴板`);
        }).catch(() => {
          this.fallbackCopy(str, name);
        });
      } else {
        this.fallbackCopy(str, name);
      }
    },
    fallbackCopy(text, name) {
      const input = document.createElement('input');
      input.value = text;
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        Message.success(`${name}已复制到剪贴板`);
      } catch (err) {
        Message.error('复制失败，请手动选择复制');
      }
      document.body.removeChild(input);
    },
    handleDeleteOrder(order) {
      Modal.confirm({
        title: '跟团订单处理确认',
        content: order.status === 0 
          ? '确定要取消此未支付跟团订单吗？' 
          : '确定要删除此跟团订单记录吗？删除后不可恢复。',
        okText: '确认',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidCampaignOrder(order.id);
            Message.success('操作成功');
            this.$emit('refresh');
          } catch (e) {
            Message.error(e.response?.data?.message || '操作失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.campaign-order-list-component {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 1. 顶部微胶囊横滑筛选栏 */
.filter-capsule-scroll {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  margin-bottom: 4px;
}
.filter-capsule-scroll::-webkit-scrollbar {
  display: none;
}

.capsule-track {
  display: inline-flex;
  gap: 8px;
  padding: 4px 2px;
}

.filter-pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  background: #F2F3F5;
  border: 1px solid transparent;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #4E5969;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.filter-pill-btn.is-active {
  background: #FFFFFF;
  color: #FF5A34;
  border-color: rgba(255, 90, 52, 0.3);
  box-shadow: 0 2px 8px rgba(255, 90, 52, 0.12);
}

.pill-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 0 5px;
  border-radius: 10px;
  background: #E5E6EB;
  color: #86909C;
  min-width: 14px;
  text-align: center;
}

.filter-pill-btn.is-active .pill-badge {
  background: #FFF2ED;
  color: #FF5A34;
}

.pill-badge.badge-highlight {
  background: #FFF7E8;
  color: #FF7D00;
}

.pill-badge.badge-danger {
  background: #FEE2E2;
  color: #F53F3F;
}

/* 2. 订单卡片列表结构（与 OrderList.vue 完全统一） */
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

/* A. 卡片头部 */
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
}

.follow-no-tag {
  font-size: 11px;
  font-weight: 700;
  color: #FF5A34;
  background: #FFF2ED;
  padding: 1px 6px;
  border-radius: 4px;
}

.order-date-text {
  font-size: 11px;
  color: #C9CDD4;
}

.status-tag {
  font-weight: 600;
  border-radius: 6px;
}

/* B. 卡片中间：商品图文 */
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

.time-text-inline {
  color: #FF7D00;
  white-space: nowrap;
}

/* C. 卡片底部：实付与独立操作栏 */
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
  min-width: 0;
}

.campaign-name-tag {
  font-size: 11px;
  color: #FF5A34;
  background: #FFF2ED;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.archived-tag {
  font-size: 10px;
  color: #86909C;
  background: #F2F3F5;
  padding: 2px 6px;
  border-radius: 4px;
}

.actual-price-box {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-shrink: 0;
}

.qty-sum {
  font-size: 11px;
  color: #86909C;
  margin-right: 4px;
}

.price-label {
  font-size: 11px;
  color: #4E5969;
}

.price-currency {
  font-size: 12px;
  font-weight: 700;
  color: #FF5A34;
}

.price-number {
  font-size: 17px;
  font-weight: 900;
  color: #FF5A34;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
}

.order-actions-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.btn-cancel, .btn-del-record {
  font-size: 12px;
}

.btn-pay-now {
  background: linear-gradient(135deg, #FF7E67 0%, #FF5A34 100%) !important;
  border: none !important;
  font-weight: 700;
  padding: 0 16px;
  box-shadow: 0 3px 10px rgba(255, 90, 52, 0.25);
}

.btn-reorder {
  border-color: #FF7E67 !important;
  color: #FF5A34 !important;
  font-weight: 600;
}

.btn-view-detail {
  border-color: #E5E6EB !important;
  color: #1D2129 !important;
  font-weight: 500;
}

.btn-view-detail:hover {
  background: #F7F8FA !important;
}

.list-end-tip {
  text-align: center;
  font-size: 11px;
  color: #C9CDD4;
  margin: 16px 0 8px;
}
</style>
