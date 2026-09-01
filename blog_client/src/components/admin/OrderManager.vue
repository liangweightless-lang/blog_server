<template>
  <div class="order-manager">
    <!-- PC 端表格视图 -->
    <a-table 
      v-if="!isMobile" 
      :data="orders" 
      :loading="loadingOrders" 
      stripe 
      style="margin-top: 16px;" 
      :pagination="{ pageSize: 10 }"
    >
      <template #columns>
        <a-table-column title="商品信息" :width="220">
          <template #cell="{ record }">
            <div class="table-prod-cell">
              <img :src="getProdImage(record.productId)" class="table-thumb-img" />
              <div class="table-prod-info">
                <span class="table-prod-name">{{ getProdName(record.productId) }}</span>
                <span class="table-prod-spec" v-if="record.selectedSpec">规格: {{ record.selectedSpec }}</span>
                <a-tag :color="record.orderType === 'GROUP' ? 'orange' : 'blue'" size="small">
                  {{ record.orderType === 'GROUP' ? '拼团' : '单买' }}
                </a-tag>
              </div>
            </div>
          </template>
        </a-table-column>
        <a-table-column title="订单编号" :width="160">
          <template #cell="{ record }">
            <span class="mono-text">#{{ record.id }}</span>
          </template>
        </a-table-column>
        <a-table-column title="买家与地址">
          <template #cell="{ record }">
            <div class="table-addr-box">
              <div class="buyer-line"><icon-user /> 买家UID: {{ record.userId }}</div>
              <div class="addr-line"><icon-location /> {{ record.shippingAddress || '未填写' }}</div>
              <div class="time-line"><icon-clock-circle /> {{ $formatTime(record.createTime) }}</div>
            </div>
          </template>
        </a-table-column>
        <a-table-column title="实付金额" :width="120">
          <template #cell="{ record }">
            <div class="table-price">¥{{ record.amount }}</div>
            <div class="table-points" v-if="record.pointsUsed">抵扣: {{ record.pointsUsed }}分</div>
          </template>
        </a-table-column>
        <a-table-column title="状态/操作" :width="140" fixed="right">
          <template #cell="{ record }">
            <a-tag :color="getOrderStatusColor(record.status)" size="small" style="margin-bottom: 6px;">
              {{ getOrderStatusText(record.status) }}
            </a-tag>
            <div style="display: flex; gap: 6px;">
              <a-button v-if="record.status === 0" size="small" type="primary" status="warning" shape="round" @click="openConfirmDrawer(record)">
                确认收款
              </a-button>
              <a-button v-if="record.status === 1" size="small" type="primary" status="success" shape="round" @click="handleShip(record)">
                标记发货
              </a-button>
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端专属：高定现代大卡片视图 (信息清晰完整、核对一目了然) -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingOrders" style="width: 100%; display: block;">
        <div v-for="order in orders" :key="order.id" class="mobile-order-card">
          <!-- 头部：单号与状态 -->
          <div class="m-card-header">
            <div class="m-header-left">
              <span class="m-order-no">#{{ order.id }}</span>
              <a-tag :color="order.orderType === 'GROUP' ? 'orange' : 'arcoblue'" size="small" class="m-type-tag">
                {{ order.orderType === 'GROUP' ? '拼团单' : '普通单' }}
              </a-tag>
            </div>
            <a-tag :color="getOrderStatusColor(order.status)" size="small" class="m-status-tag">
              {{ getOrderStatusText(order.status) }}
            </a-tag>
          </div>

          <!-- 商品主要信息区 -->
          <div class="m-card-prod-row">
            <img :src="getProdImage(order.productId)" class="m-prod-thumb" />
            <div class="m-prod-details">
              <h4 class="m-prod-name">{{ getProdName(order.productId) }}</h4>
              <p class="m-prod-spec" v-if="order.selectedSpec">规格: {{ order.selectedSpec }}</p>
              <div class="m-buyer-tag">
                <icon-user /> 买家UID: <strong>{{ order.userId }}</strong>
              </div>
            </div>
            <div class="m-price-col">
              <span class="m-price-val">¥{{ order.amount }}</span>
              <span v-if="order.pointsUsed" class="m-deduct-tip">抵扣{{ order.pointsUsed }}分</span>
            </div>
          </div>

          <!-- 配送地址与时间 -->
          <div class="m-card-addr-box">
            <div class="m-addr-line">
              <icon-location class="m-addr-icon" />
              <span>{{ order.shippingAddress || '买家未填写收货地址' }}</span>
            </div>
            <div class="m-time-line">
              <icon-clock-circle /> 下单时间: {{ $formatTime(order.createTime) }}
            </div>
          </div>

          <!-- 底部操作区 -->
          <div class="m-card-footer">
            <span class="m-footer-status-desc">{{ getStatusHelpText(order.status) }}</span>
            <div class="m-action-btns">
              <button v-if="order.status === 0" class="m-confirm-pay-btn" @click="openConfirmDrawer(order)">
                <icon-check-circle /> 确认收款核销
              </button>
              <button v-if="order.status === 1" class="m-ship-btn" @click="handleShip(order)">
                <icon-send /> 标记已发货
              </button>
            </div>
          </div>
        </div>

        <a-empty v-if="orders.length === 0 && !loadingOrders" description="暂无订单记录" />
      </a-spin>
    </div>

    <!-- 专属人工收款核销确认标准抽屉 (彻底消除灵动岛遮挡，信息丰富严谨) -->
    <AppBottomSheet 
      v-model:visible="confirmDrawerVisible"
      title="确认核销收款"
      subtitle="请在微信/支付宝商户端核对真实到账后再确认"
    >
      <div v-if="currentOrder" class="confirm-verify-container">
        <!-- 核对金额大卡片 -->
        <div class="verify-amount-card">
          <span class="verify-label">应收核销金额</span>
          <div class="verify-price">
            <span class="currency">¥</span>
            <span class="num">{{ currentOrder.amount }}</span>
          </div>
          <div class="verify-points-row" v-if="currentOrder.pointsUsed">
            <span>积分抵扣: {{ currentOrder.pointsUsed }} 积分</span>
          </div>
        </div>

        <!-- 详细信息清单 -->
        <div class="verify-detail-list">
          <div class="verify-detail-item">
            <span class="v-item-label">核销商品</span>
            <div class="v-item-prod">
              <img :src="getProdImage(currentOrder.productId)" class="v-thumb" />
              <div class="v-prod-texts">
                <span class="v-pname">{{ getProdName(currentOrder.productId) }}</span>
                <span class="v-spec" v-if="currentOrder.selectedSpec">{{ currentOrder.selectedSpec }}</span>
              </div>
            </div>
          </div>

          <div class="verify-detail-item">
            <span class="v-item-label">买家信息</span>
            <span class="v-item-val highlight">买家用户 UID: {{ currentOrder.userId }}</span>
          </div>

          <div class="verify-detail-item">
            <span class="v-item-label">完整订单号</span>
            <span class="v-item-val mono">{{ currentOrder.id }}</span>
          </div>

          <div class="verify-detail-item">
            <span class="v-item-label">收货地址</span>
            <span class="v-item-val">{{ currentOrder.shippingAddress || '无' }}</span>
          </div>

          <div class="verify-detail-item">
            <span class="v-item-label">下单时间</span>
            <span class="v-item-val">{{ $formatTime(currentOrder.createTime) }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <button class="sheet-confirm-btn" :disabled="submitting" @click="submitConfirmPay">
          <icon-loading v-if="submitting" :spin="true" />
          <span>{{ submitting ? '正在核销入账...' : '确认收到款项，一键流转为已支付' }}</span>
        </button>
      </template>
    </AppBottomSheet>
  </div>
</template>

<script>
import { getOrdersAdmin, shipOrder, confirmOrderPay } from '@/api/order';
import { getProducts } from '@/api/product';
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
      products: [],
      prodMap: {},
      loadingOrders: false,
      confirmDrawerVisible: false,
      currentOrder: null,
      submitting: false
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loadingOrders = true;
      try {
        const [ordersRes, prodsRes] = await Promise.all([
          getOrdersAdmin(),
          getProducts().catch(() => ({ data: { data: [] } }))
        ]);
        this.products = prodsRes.data.data || [];
        const map = {};
        this.products.forEach(p => map[p.id] = p);
        this.prodMap = map;
        this.orders = ordersRes.data.data || [];
      } catch (error) {
        Message.error('加载订单数据失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    getProdName(productId) {
      const p = this.prodMap[productId];
      return p ? p.name : `商品 #${productId}`;
    },
    getProdImage(productId) {
      const p = this.prodMap[productId];
      return p?.image || '/img/avatar.png';
    },
    getOrderStatusColor(status) {
      const colors = ['orange', 'green', 'gray', 'arcoblue'];
      return colors[status] || 'gray';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    },
    getStatusHelpText(status) {
      if (status === 0) return '买家已提交订单，等待付款核销';
      if (status === 1) return '买家已完成支付，可进行配货发货';
      if (status === 3) return '商品已出库配送中';
      return '订单已归档或关闭';
    },
    openConfirmDrawer(order) {
      this.currentOrder = order;
      this.confirmDrawerVisible = true;
    },
    async submitConfirmPay() {
      if (!this.currentOrder) return;
      this.submitting = true;
      try {
        await confirmOrderPay(this.currentOrder.id);
        Message.success('🎉 订单已成功确认为已支付状态！');
        this.confirmDrawerVisible = false;
        this.fetchData();
      } catch (e) {
        Message.error(e.response?.data?.message || '确认核销失败');
      } finally {
        this.submitting = false;
      }
    },
    async handleShip(order) {
      try {
        await shipOrder(order.id);
        Message.success('发货成功');
        this.fetchData();
      } catch (error) {
        Message.error('操作失败');
      }
    }
  }
}
</script>

<style scoped>
.order-manager {
  padding: 10px 0;
}

/* PC 端样式 */
.table-prod-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.table-thumb-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.table-prod-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.table-prod-name {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}
.table-prod-spec {
  font-size: 11px;
  color: #86909C;
}
.mono-text {
  font-family: monospace;
  font-size: 12px;
  color: #4E5969;
}
.table-addr-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #4E5969;
}
.table-price {
  font-size: 15px;
  font-weight: 800;
  color: #FF3B30;
}
.table-points {
  font-size: 11px;
  color: #86909C;
}

/* 移动端高定订单卡片 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mobile-order-card {
  background: #FFFFFF;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.m-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #F2F3F5;
}
.m-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}
.m-order-no {
  font-size: 12px;
  font-weight: 700;
  color: #86909C;
  font-family: monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-card-prod-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.m-prod-thumb {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
  background: #F2F3F5;
}
.m-prod-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.m-prod-name {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.m-prod-spec {
  margin: 0;
  font-size: 11px;
  color: #86909C;
}
.m-buyer-tag {
  font-size: 11px;
  color: #4E5969;
  display: flex;
  align-items: center;
  gap: 3px;
}

.m-price-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.m-price-val {
  font-size: 17px;
  font-weight: 900;
  color: #FF3B30;
}
.m-deduct-tip {
  font-size: 10px;
  color: #86909C;
}

.m-card-addr-box {
  background: #F7F8FA;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.m-addr-line {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #1D2129;
  line-height: 1.4;
}
.m-addr-icon {
  color: #FF5E3A;
  margin-top: 2px;
  flex-shrink: 0;
}
.m-time-line {
  font-size: 11px;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 4px;
}

.m-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 6px;
}
.m-footer-status-desc {
  font-size: 11px;
  color: #86909C;
}

.m-confirm-pay-btn {
  background: linear-gradient(135deg, #FF922B 0%, #F76707 100%);
  color: #FFFFFF;
  border: none;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(247, 103, 7, 0.3);
  display: flex;
  align-items: center;
  gap: 4px;
}
.m-confirm-pay-btn:active {
  transform: scale(0.95);
}

.m-ship-btn {
  background: linear-gradient(135deg, #20C997 0%, #0CA678 100%);
  color: #FFFFFF;
  border: none;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(32, 201, 151, 0.3);
  display: flex;
  align-items: center;
  gap: 4px;
}
.m-ship-btn:active {
  transform: scale(0.95);
}

/* 人工核销抽屉 */
.confirm-verify-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.verify-amount-card {
  background: #FFF9F8;
  border: 1px solid #FFE4DE;
  border-radius: 16px;
  padding: 16px;
  text-align: center;
}
.verify-label {
  font-size: 12px;
  color: #86909C;
  font-weight: 600;
}
.verify-price {
  color: #FF3B30;
  margin: 6px 0 2px;
  display: flex;
  align-items: baseline;
  justify-content: center;
}
.verify-price .currency {
  font-size: 20px;
  font-weight: 800;
  margin-right: 2px;
}
.verify-price .num {
  font-size: 34px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.verify-points-row {
  font-size: 11px;
  color: #86909C;
}

.verify-detail-list {
  background: #F7F8FA;
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.verify-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 13px;
}
.v-item-label {
  color: #86909C;
  font-weight: 600;
  flex-shrink: 0;
  width: 75px;
}
.v-item-val {
  color: #1D2129;
  font-weight: 600;
  text-align: right;
  word-break: break-all;
}
.v-item-val.highlight {
  color: #FF5E3A;
  font-weight: 700;
}
.v-item-val.mono {
  font-family: monospace;
  font-size: 11px;
}

.v-item-prod {
  display: flex;
  align-items: center;
  gap: 8px;
}
.v-thumb {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
}
.v-prod-texts {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.v-pname {
  font-weight: 700;
  color: #1D2129;
}
.v-spec {
  font-size: 11px;
  color: #86909C;
}

.sheet-confirm-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FF922B 0%, #F76707 100%);
  color: #FFFFFF;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(247, 103, 7, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.sheet-confirm-btn:active:not(:disabled) {
  transform: scale(0.96);
}
.sheet-confirm-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
