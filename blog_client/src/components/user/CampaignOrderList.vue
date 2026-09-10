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
          <div style="display: flex; gap: 6px; align-items: center;">
            <!-- 拼团达标状态徽章 -->
            <a-tag 
              v-if="order.campaign && order.campaign.targetNum > 0"
              :color="order.campaign.groupStatus === 1 ? 'green' : (order.campaign.groupStatus === 2 ? 'red' : 'orange')"
              size="small" 
              style="font-weight: 600;"
            >
              {{ order.campaign.groupStatusText }}
            </a-tag>
            <!-- 订单支付状态 -->
            <a-tag :color="getStatusColor(order.status)" size="small" style="font-weight: bold;">
              {{ getStatusText(order.status) }}
            </a-tag>
          </div>
        </div>
        <div class="order-card-body" style="align-items: flex-start;">
          <div class="order-main-info" style="width: 100%;">
            <p class="order-pname">{{ order.campaign?.title || '团购活动' }}</p>

            <!-- 成团进度实时看板 -->
            <div 
              v-if="order.campaign && order.campaign.targetNum > 0"
              style="margin: 6px 0; background: #FFF7F5; padding: 6px 10px; border-radius: 8px; font-size: 12px; color: #4E5969; display: flex; justify-content: space-between; align-items: center;"
            >
              <span>成团目标: <strong>{{ order.campaign.targetNum }}</strong> 人团</span>
              <span v-if="order.campaign.groupStatus === 1" style="color: #00B42A; font-weight: 600;">
                <icon-check-circle-fill /> 拼团成功
              </span>
              <span v-else-if="order.campaign.groupStatus === 2" style="color: #F53F3F; font-weight: 600;">
                <icon-close-circle-fill /> 拼团失败(未达标)
              </span>
              <span v-else style="color: #FF5A34; font-weight: 600;">
                已跟团 {{ order.campaign.currentNum || 0 }}/{{ order.campaign.targetNum }}人 (差{{ order.campaign.targetNum - (order.campaign.currentNum || 0) }}人)
              </span>
            </div>

            <p class="order-spec" style="background: transparent; color: #86909c; padding: 0;">提货点: {{ order.campaign?.deliveryLocation?.name || '校内指定提货点' }}</p>
            <p class="order-delivery-time" v-if="order.campaign?.deliveryTime" style="color: #FF5A34; font-size: 12px; margin: 4px 0; display: flex; align-items: center; gap: 4px; font-weight: 500;">
              <icon-clock-circle /> 预计发货/自提: {{ $formatTime(order.campaign.deliveryTime) }}
            </p>
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
        </div>

        <!-- 卡片底部：实付款结算与独立操作按钮区（彻底杜绝横向挤压） -->
        <div class="campaign-card-footer">
          <div class="settle-price-row">
            <span class="settle-label">实付款:</span>
            <span class="price-symbol">¥</span>
            <span class="price-val">{{ order.totalAmount }}</span>
          </div>

          <div class="campaign-actions-row" v-if="order.status === 0 || order.status === 3">
            <a-button 
              type="text" 
              status="danger" 
              size="small" 
              class="del-order-btn"
              @click.stop="handleDeleteOrder(order)"
            >
              {{ order.status === 0 ? '取消跟团' : '删除记录' }}
            </a-button>
            <a-button 
              v-if="order.status === 0"
              type="primary" 
              size="small" 
              shape="round" 
              class="pay-now-btn" 
              @click.stop="$emit('pay', order)"
            >
              立即支付
            </a-button>
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
        content: order.status === 0 
          ? '确定要取消此未支付跟团订单吗？删除后不可恢复。' 
          : '确定要删除此已取消跟团订单记录吗？',
        okText: '确认删除',
        cancelText: '取消',
        onOk: async () => {
          try {
            await deleteUnpaidCampaignOrder(order.id);
            Message.success('跟团订单已成功删除');
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
  background: #FFFFFF;
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
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
  flex-direction: column;
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

/* 底部结算与按钮 */
.campaign-card-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #F7F8FA;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settle-price-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.settle-label {
  font-size: 12px;
  color: #4E5969;
}

.price-symbol {
  font-size: 13px;
  font-weight: 800;
  color: #1D2129;
}

.price-val {
  font-size: 17px;
  font-weight: 800;
  color: #1D2129;
}

.campaign-actions-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.del-order-btn {
  font-size: 12px;
  color: #86909C;
}

.del-order-btn:hover {
  color: #F53F3F;
}

.pay-now-btn {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%) !important;
  border: none !important;
  font-size: 12px;
  font-weight: 700;
  padding: 0 16px;
  height: 30px;
  box-shadow: 0 4px 10px rgba(255, 94, 58, 0.25);
}

.list-end-tip {
  text-align: center;
  font-size: 11px;
  color: #C9CDD4;
  margin-top: 14px;
}
</style>
