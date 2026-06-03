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
          <div class="order-price-info" style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px;">
            <span class="price-val">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </a-list-item>
      <p class="list-end-tip">已展示全部 {{ orders.length }} 个跟团记录</p>
    </a-list>
  </div>
</template>

<script>
export default {
  name: 'CampaignOrderList',
  props: {
    orders: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getStatusColor(status) {
      const colors = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'gray' };
      return colors[status] || 'gray';
    },
    getStatusText(status) {
      const texts = { 0: '待付款', 1: '待提货', 2: '已提货', 3: '已退款' };
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
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}
.order-card-body {
  display: flex;
  align-items: center;
  gap: 15px;
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
