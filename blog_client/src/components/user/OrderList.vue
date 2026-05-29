<template>
  <div class="order-list-component">
    <a-empty v-if="orders.length === 0" description="暂无订单记录" style="margin: 40px 0;">
      <template #image><icon-gift style="font-size: 48px; color: #D3C1BA; opacity: 0.5;" /></template>
    </a-empty>
    <a-list v-else class="order-full-list" :bordered="false" :split="false">
      <a-list-item 
        v-for="order in orders" 
        :key="order.id" 
        class="order-card-item" 
        @click="$emit('detail', order)" 
        style="cursor: pointer;"
      >
        <div class="order-card-header">
          <span class="order-id">订单号: {{ order.id.substring(0, 12) }}...</span>
          <a-tag :color="getOrderStatusColor(order.status)" size="small">
            {{ getOrderStatusText(order.status) }}
          </a-tag>
        </div>
        <div class="order-card-body">
          <a-image :src="order.productImage" class="full-order-img" width="60" height="60" fit="cover" />
          <div class="order-main-info">
            <p class="order-pname">{{ order.productName || '商品ID: ' + order.productId }}</p>
            <p class="order-spec" v-if="order.selectedSpec">规格: {{ order.selectedSpec }}</p>
            <p class="order-time">{{ $formatTime(order.createTime) }}</p>
          </div>
          <div class="order-price-info" style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px;">
            <span class="price-val">¥{{ order.amount }}</span>
            <a-button v-if="order.status === 0" type="primary" size="small" shape="round" style="background-color: #FF7E67;" @click.stop="$emit('pay', order)">去支付</a-button>
          </div>
        </div>
      </a-list-item>
      <p class="list-end-tip">已展示全部 {{ orders.length }} 个订单</p>
    </a-list>
  </div>
</template>

<script>
export default {
  name: 'OrderList',
  props: {
    orders: {
      type: Array,
      default: () => []
    }
  },
  emits: ['detail', 'pay'],
  methods: {
    getOrderStatusColor(status) {
      const types = ['gray', 'green', 'red', 'blue'];
      return types[status] || 'gray';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    }
  }
}
</script>

<style scoped>
.order-full-list {
  padding: 0 10px;
}
.order-card-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 15px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 1);
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  transition: all 0.3s ease;
}
.order-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0,0,0,0.06);
  border-color: rgba(255, 126, 103, 0.15);
}
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}
.order-id {
  font-size: 11px;
  color: #999;
}
.order-card-body {
  display: flex;
  align-items: center;
  gap: 15px;
}
.full-order-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
  background-color: #f5f7fa;
  object-fit: cover;
}
.order-main-info {
  flex: 1;
}
.order-pname {
  font-weight: bold;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}
.order-spec {
  font-size: 11px;
  color: #FF7E67;
  background: #FFF0ED;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 4px;
}
.order-time {
  font-size: 11px;
  color: #bbb;
}
.price-val {
  font-weight: bold;
  font-size: 16px;
  color: #F56C6C;
}
.list-end-tip {
  text-align: center;
  font-size: 12px;
  color: #ccc;
  margin-top: 15px;
}
</style>
