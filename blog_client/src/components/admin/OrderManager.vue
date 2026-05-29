<template>
  <div class="order-manager">
    <a-table v-if="!isMobile" :data="orders" :loading="loadingOrders" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column title="订单号" :width="120">
          <template #cell="{ record }">
            <a-tooltip :content="record.id" placement="top">
              <span style="display: inline-block; width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 11px; cursor: help;">#{{ record.id }}</span>
            </a-tooltip>
          </template>
        </a-table-column>
        <a-table-column title="详情/地址">
          <template #cell="{ record }">
            <div style="font-weight: bold; font-size: 13px; color: #333; margin-bottom: 4px;">
              商品ID: {{ record.productId }}
              <a-tag :color="record.orderType === 'GROUP' ? 'orange' : 'blue'" size="small" style="margin-left: 8px;">
                {{ record.orderType === 'GROUP' ? '拼团' : '个买' }}
              </a-tag>
            </div>
            <div style="font-size: 12px; color: #999;">地址: {{ record.shippingAddress || '无' }} | 时间: {{ $formatTime(record.createTime) }}</div>
          </template>
        </a-table-column>
        <a-table-column title="实付" :width="120">
          <template #cell="{ record }">
            <div style="font-weight: bold; color: #F56C6C;">¥{{ record.amount }}</div>
            <div style="font-size: 11px; color: #999;" v-if="record.pointsUsed">抵扣: {{ record.pointsUsed }}</div>
          </template>
        </a-table-column>
        <a-table-column title="状态/操作" :width="150" fixed="right">
          <template #cell="{ record }">
            <a-tag :color="getOrderStatusColor(record.status)" size="small" style="margin-bottom: 5px; display: block; width: fit-content;">
              {{ getOrderStatusText(record.status) }}
            </a-tag>
            <div v-if="record.status === 1">
              <a-button size="small" type="primary" status="success" @click="handleShip(record)">标记发货</a-button>
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端视图: 卡片列表 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingOrders" style="width: 100%; display: block;">
        <div v-for="order in orders" :key="order.id" class="mobile-card-item">
          <div class="card-header">
            <span class="order-id">订单号: {{ order.id }}</span>
            <a-tag :color="getOrderStatusColor(order.status)" size="small">
              {{ getOrderStatusText(order.status) }}
            </a-tag>
          </div>
          <div class="card-body">
            <div class="card-row">
              <span class="label">商品ID:</span>
              <span class="value">{{ order.productId }}
                <a-tag :color="order.orderType === 'GROUP' ? 'orange' : 'blue'" size="small" style="margin-left: 4px; transform: scale(0.85); transform-origin: left center;">
                  {{ order.orderType === 'GROUP' ? '拼团' : '个买' }}
                </a-tag>
              </span>
            </div>
            <div class="card-row">
              <span class="label">实付:</span>
              <span class="value price">¥{{ order.amount }}
                <span v-if="order.pointsUsed" class="points-used">(抵扣: {{ order.pointsUsed }})</span>
              </span>
            </div>
            <div class="card-row">
              <span class="label">地址:</span>
              <span class="value">{{ order.shippingAddress || '未填写' }}</span>
            </div>
          </div>
          <div class="card-footer">
            <span class="card-time">{{ $formatTime(order.createTime) }}</span>
            <a-button v-if="order.status === 1" size="small" type="primary" status="success" shape="round" @click="handleShip(order)">标记发货</a-button>
          </div>
        </div>
        <a-empty v-if="orders.length === 0 && !loadingOrders" description="暂无订单" />
      </a-spin>
    </div>
  </div>
</template>

<script>
import { getOrdersAdmin, shipOrder } from '@/api/order';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'OrderManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      orders: [],
      loadingOrders: false
    }
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchOrders() {
      this.loadingOrders = true;
      try {
        const res = await getOrdersAdmin();
        this.orders = res.data.data;
      } catch (error) {
        Message.error('加载订单列表失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    getOrderStatusColor(status) {
      const colors = ['gray', 'green', 'red', 'blue'];
      return colors[status] || 'gray';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    },
    async handleShip(order) {
      try {
        await shipOrder(order.id);
        Message.success('发货成功');
        this.fetchOrders();
      } catch (error) {
        Message.error('操作失败');
      }
    }
  }
}
</script>

<style scoped>
/* 移动端卡片列表样式 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}
.mobile-card-item {
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #F0F0F0;
}
.order-id {
  font-size: 11px;
  color: #999;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.card-row {
  display: flex;
  font-size: 13px;
}
.card-row .label {
  color: #8C6A5D;
  width: 60px;
  flex-shrink: 0;
}
.card-row .value {
  color: #333;
  font-weight: 600;
  display: flex;
  align-items: center;
}
.card-row .price {
  color: #F56C6C;
  font-size: 15px;
}
.points-used {
  font-size: 11px;
  color: #999;
  margin-left: 6px;
  font-weight: normal;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
.card-time {
  font-size: 11px;
  color: #C0C4CC;
}
</style>
