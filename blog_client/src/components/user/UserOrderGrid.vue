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
            <span class="mini-order-price">¥{{ order.amount }}</span>
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
  border-radius: 12px;
  max-width: 600px;
}
:deep(.arco-card-body) {
  padding: 15px;
  background: white;
  border-radius: 12px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.section-title {
  font-weight: bold;
  font-size: 15px;
  color: #303133;
}
.view-all {
  font-size: 12px;
  color: #909399;
  padding: 0;
}
.order-status-grid {
  display: flex;
  padding: 5px 0;
}
.status-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.status-item .icon {
  font-size: 24px;
  color: #5C433B;
}
.status-item span {
  font-size: 12px;
  color: #606266;
}
.recent-orders {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #F2F6FC;
}
.mini-order-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
  background: #FDFDFD;
  border: 1px solid #F2F6FC;
  border-radius: 8px;
  padding: 10px;
}
.mini-order-card:last-child {
  margin-bottom: 0;
}
.mini-product-img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
  background-color: #f5f7fa;
}
.mini-order-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 44px;
}
.mini-order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mini-pname {
  font-size: 13px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}
.mini-status {
  font-size: 12px;
  color: #FF7E67;
}
.mini-order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
.mini-id {
  font-size: 11px;
  color: #909399;
}
.mini-order-price {
  font-size: 13px;
  font-weight: bold;
  color: #F56C6C;
}
</style>
