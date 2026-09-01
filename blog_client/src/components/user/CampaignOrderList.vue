<template>
  <div class="campaign-order-list">
    <a-empty v-if="orders.length === 0" description="暂无快团记录，去逛逛吧" style="margin: 40px 0;">
      <template #image><icon-archive style="font-size: 48px; color: #D3C1BA; opacity: 0.5;" /></template>
    </a-empty>
    <a-list v-else class="order-full-list" :bordered="false" :split="false">
      <a-list-item 
        v-for="order in orders" 
        :key="order.id" 
        class="order-card-item" 
      >
        <div class="order-card-header">
          <span class="order-id">跟团号: <strong style="color: #FF5A34; font-size: 14px;">#{{ order.followNumber }}</strong></span>
          <a-tag :color="getStatusColor(order.status)" size="small" style="font-weight: bold;">
            {{ getStatusText(order.status) }}
          </a-tag>
        </div>
        <div class="order-card-body" style="align-items: flex-start;">
          <div class="order-main-info" style="width: 100%;">
            <p class="order-pname">{{ order.campaign?.title || '团购活动' }}</p>
            <p class="order-spec" style="background: transparent; color: #86909c; padding: 0;">提货点: {{ order.campaign?.deliveryLocation?.name || '未知' }}</p>
            <div class="m-order-items" style="margin-top: 10px;">
              <div v-for="item in order.items" :key="item.id" style="display: flex; gap: 10px; margin-bottom: 8px; align-items: center;">
                <img :src="item.productImage" style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px; background: #f2f3f5; box-shadow: 0 2px 8px rgba(0,0,0,0.05);" v-if="item.productImage" />
                <div v-else style="width: 48px; height: 48px; border-radius: 8px; background: #f2f3f5; display: flex; align-items: center; justify-content: center; color: #bbb;">
                  <icon-image />
                </div>
                <div style="flex: 1; display: flex; flex-direction: column;">
                  <span style="font-size: 13px; font-weight: 600; color: #1D2129; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">{{ item.productName || '商品' }}</span>
                  <span style="font-size: 12px; color: #86909C; margin-top: 4px;">x {{ item.quantity }}</span>
                </div>
              </div>
            </div>
            <p class="order-time" style="margin-top: 8px;">{{ $formatTime(order.createTime) }}</p>
          </div>
          <div class="order-price-info">
            <span class="price-val">¥{{ order.totalAmount }}</span>
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
      <p class="list-end-tip">已展示全部 {{ orders.length }} 个跟团记录</p>
    </a-list>
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
  emits: ['pay', 'refresh'],
  methods: {
    getStatusColor(status) {
      const colors = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'gray' };
      return colors[status] || 'gray';
    },
    getStatusText(status) {
      const texts = { 0: '待付款', 1: '已支付', 2: '已提货', 3: '已取消' };
      return texts[status] || '未知';
    },
    handleDeleteOrder(order) {
      Modal.confirm({
        title: '删除跟团订单确认',
        content: '确定要删除此未支付跟团订单吗？删除后不可恢复。',
        okText: '确认删除',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidCampaignOrder(order.id);
            Message.success('未支付跟团订单已成功删除');
            this.$emit('refresh');
          } catch (e) {
            Message.error(e.response?.data?.message || '删除跟团订单失败');
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
  font-size: 13px;
  color: #1D2129;
}

.order-card-body {
  display: flex;
  justify-content: space-between;
}

.order-pname {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
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
  flex-shrink: 0;
  margin-left: 12px;
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
