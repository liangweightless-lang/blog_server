<template>
  <a-card class="section-card order-section" :bordered="false" :body-style="{ padding: '15px' }">
    <div class="section-header">
      <span class="section-title">我的订单</span>
      <a-button type="text" class="view-all" @click="$emit('view-all')">全部订单 <icon-right /></a-button>
    </div>
    <div class="order-status-grid">
      <div class="status-item" @click="$emit('view-all')">
        <a-badge :count="unpaidCount" class="badge-item">
          <icon-safe class="icon" />
        </a-badge>
        <span>待付款</span>
      </div>
      <div class="status-item" @click="$emit('view-all')">
        <icon-gift class="icon" />
        <span>待发货</span>
      </div>
      <div class="status-item" @click="$emit('view-all')">
        <icon-send class="icon" />
        <span>待收货</span>
      </div>
      <div class="status-item" @click="$emit('view-all')">
        <icon-message class="icon" />
        <span>评价</span>
      </div>
    </div>
    
    <!-- 快捷查看最近订单 -->
    <div class="recent-orders" v-if="orders.length > 0">
      <div v-for="order in orders.slice(0, 2)" :key="order.id" class="mini-order-card">
        <img :src="order.productImage" class="mini-product-img" />
        <div class="mini-order-content">
          <div class="mini-order-info">
            <span class="mini-pname">{{ order.productName || '商品ID: ' + order.productId }}</span>
            <span class="mini-status" :style="{ color: getStatusColor(order.status) }">
              {{ getStatusText(order.status) }}
            </span>
          </div>
          <div class="mini-order-bottom">
            <span class="mini-id">#{{ order.id.substring(0, 8) }}</span>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="mini-order-price">¥{{ order.amount }}</span>
              <a-button v-if="order.status === 0" type="primary" size="mini" shape="round" style="background-color: #FF7E67; height: 20px; padding: 0 8px; font-size: 12px;" @click.stop="$emit('pay', order)">去支付</a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script>
export default {
  name: 'UserOrderGrid',
  props: {
    orders: Array
  },
  computed: {
    unpaidCount() {
      return this.orders.filter(o => o.status === 0).length;
    }
  },
  methods: {
    getStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    },
    getStatusColor(status) {
      const colors = ['#909399', '#67C23A', '#F56C6C', '#409EFF'];
      return colors[status] || '#909399';
    }
  }
}
</script>

<style scoped>
.section-card {
  background: transparent !important;
  margin: 15px auto;
  border-radius: 16px;
  max-width: 600px;
}
:deep(.arco-card-body) {
  padding: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.04), 0 2px 8px rgba(0,0,0,0.02);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  font-weight: 800;
  font-size: 16px;
  color: #1D2129;
}
.view-all {
  font-size: 13px;
  color: #86909C;
  padding: 0;
  transition: color 0.2s;
}
.view-all:hover {
  color: #FF7E67;
  background: transparent;
}
.order-status-grid {
  display: flex;
  padding: 5px 0 15px 0;
}
.status-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.status-item .icon {
  font-size: 26px;
  color: #4E5969;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.status-item:hover .icon {
  transform: scale(1.15) translateY(-2px);
  color: #FF7E67;
}
.status-item span {
  font-size: 12px;
  color: #4E5969;
  font-weight: 500;
  transition: color 0.3s;
}
.status-item:hover span {
  color: #FF7E67;
}
.recent-orders {
  margin-top: 5px;
  padding-top: 20px;
  border-top: 1px dashed rgba(0,0,0,0.06);
}
.mini-order-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  background: white;
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.mini-order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.08);
  border-color: rgba(255, 126, 103, 0.2);
}
.mini-order-card:last-child {
  margin-bottom: 0;
}
.mini-product-img {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
  background-color: #f5f7fa;
}
.mini-order-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 50px;
}
.mini-order-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.mini-pname {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  max-width: 180px;
  line-height: 1.4;
}
.mini-status {
  font-size: 12px;
  color: #FF7E67;
  font-weight: 500;
}
.mini-order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.mini-id {
  font-size: 11px;
  color: #86909C;
  background: #F2F3F5;
  padding: 2px 6px;
  border-radius: 4px;
}
.mini-order-price {
  font-size: 15px;
  font-weight: 800;
  color: #FF7E67;
}
</style>
