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
          <div class="order-price-info">
            <span class="price-val">¥{{ order.amount }}</span>
            <div class="unpaid-actions" v-if="order.status === 0">
              <a-button 
                type="text" 
                status="danger" 
                size="mini" 
                class="del-order-btn"
                @click.stop="handleDeleteOrder(order)"
              >
                删除
              </a-button>
              <a-button 
                type="primary" 
                size="small" 
                shape="round" 
                class="pay-now-btn"
                @click.stop="$emit('pay', order)"
              >
                去支付
              </a-button>
            </div>
          </div>
        </div>
      </a-list-item>
      <p class="list-end-tip">已展示全部 {{ orders.length }} 个订单</p>
    </a-list>
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
    handleDeleteOrder(order) {
      Modal.confirm({
        title: '删除订单确认',
        content: '确定要删除此未支付订单吗？删除后不可恢复。',
        okText: '确认删除',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidOrder(order.id);
            Message.success('未支付订单已成功删除');
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
.order-card-item {
  background: #F7F8FA;
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}
.order-card-item:active {
  transform: scale(0.99);
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.order-id {
  font-size: 12px;
  color: #86909C;
}

.order-card-body {
  display: flex;
  gap: 12px;
  align-items: center;
}

.full-order-img {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.order-main-info {
  flex: 1;
  min-width: 0;
}

.order-pname {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-spec {
  margin: 0 0 4px 0;
  font-size: 11px;
  color: #86909C;
}

.order-time {
  margin: 0;
  font-size: 11px;
  color: #C9CDD4;
}

.order-price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.price-val {
  font-size: 16px;
  font-weight: 800;
  color: #1D2129;
}

.unpaid-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.del-order-btn {
  font-size: 11px;
  padding: 0 4px;
}

.pay-now-btn {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%) !important;
  border: none;
  font-size: 12px;
  font-weight: 700;
}

.list-end-tip {
  text-align: center;
  font-size: 11px;
  color: #C9CDD4;
  margin-top: 14px;
}
</style>
