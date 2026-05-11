<template>
  <div class="section-card order-section">
    <div class="section-header">
      <span class="section-title">我的订单</span>
      <el-button type="text" class="view-all">全部订单 <i class="el-icon-arrow-right"></i></el-button>
    </div>
    <div class="order-status-grid">
      <div class="status-item">
        <el-badge :value="unpaidCount" :hidden="unpaidCount === 0" class="badge-item">
          <i class="el-icon-wallet icon"></i>
        </el-badge>
        <span>待付款</span>
      </div>
      <div class="status-item">
        <i class="el-icon-box icon"></i>
        <span>待发货</span>
      </div>
      <div class="status-item">
        <i class="el-icon-truck icon"></i>
        <span>待收货</span>
      </div>
      <div class="status-item">
        <i class="el-icon-chat-line-round icon"></i>
        <span>评价</span>
      </div>
    </div>
    
    <!-- 快捷查看最近订单 -->
    <div class="recent-orders" v-if="orders.length > 0">
      <div v-for="order in orders.slice(0, 2)" :key="order.id" class="mini-order-card">
        <div class="mini-order-info">
          <span class="mini-id">#{{ order.id.substring(0, 8) }}</span>
          <span class="mini-status" :style="{ color: getStatusColor(order.status) }">
            {{ getStatusText(order.status) }}
          </span>
        </div>
        <div class="mini-order-price">¥{{ order.amount }}</div>
      </div>
    </div>
  </div>
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
  background: white;
  margin: 15px;
  border-radius: 12px;
  padding: 15px;
  max-width: 600px;
  margin: 15px auto;
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
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 8px;
  color: #909399;
}
.mini-status {
  color: #FF7E67;
  margin-left: 8px;
}
</style>
