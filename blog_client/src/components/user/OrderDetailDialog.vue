<template>
  <a-modal 
    title="订单详情" 
    :visible="visible" 
    :width="isMobile ? '90%' : '500px'" 
    @cancel="handleCancel" 
    :footer="false"
  >
    <div v-if="order" class="order-detail-container">
      <!-- 状态区 -->
      <div class="status-header">
        <div class="status-icon-box" :style="{ background: getStatusBg(order.status) }">
          <component :is="getStatusIcon(order.status)" class="status-icon" :style="{ color: getStatusColor(order.status) }" />
        </div>
        <div class="status-text">
          <h3>{{ getStatusText(order.status) }}</h3>
          <p>{{ getStatusSubText(order.status) }}</p>
        </div>
      </div>

      <!-- 收货地址区 -->
      <div class="detail-card">
        <div class="card-title"><icon-location /> 配送信息</div>
        <div class="address-text">{{ order.shippingAddress || '暂无地址信息' }}</div>
      </div>

      <!-- 商品区 -->
      <div class="detail-card">
        <div class="card-title"><icon-gift /> 商品信息</div>
        <div class="product-item">
          <a-image :src="order.productImage" class="product-img" width="60" height="60" fit="cover" />
          <div class="product-info">
            <div class="pname">{{ order.productName || '商品ID: ' + order.productId }}</div>
            <div class="pspec" v-if="order.selectedSpec">规格: {{ order.selectedSpec }}</div>
          </div>
        </div>
      </div>

      <!-- 订单信息区 -->
      <div class="detail-card">
        <div class="card-title"><icon-list /> 订单信息</div>
        <div class="info-row">
          <span class="info-label">订单编号</span>
          <span class="info-value">{{ order.id }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">创建时间</span>
          <span class="info-value">{{ formatTime(order.createTime) }}</span>
        </div>
        <div class="info-row" v-if="order.payTime">
          <span class="info-label">支付时间</span>
          <span class="info-value">{{ formatTime(order.payTime) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单类型</span>
          <span class="info-value">{{ order.orderType === 'GROUP' ? '拼团订单' : '普通订单' }}</span>
        </div>
        <div class="info-row" v-if="order.pointsUsed">
          <span class="info-label">积分抵扣</span>
          <span class="info-value deduct">- ¥{{ (order.pointsUsed / 100).toFixed(2) }} (使用 {{ order.pointsUsed }} 积分)</span>
        </div>
        <div class="info-row">
          <span class="info-label">实付款</span>
          <span class="info-value price">¥{{ order.amount }}</span>
        </div>
      </div>

      <!-- 底部操作区 -->
      <div class="action-footer" v-if="order.status === 0">
        <a-button @click="handleCancel" shape="round">稍后支付</a-button>
        <a-button type="primary" style="background-color: #FF7E67;" shape="round" @click="handlePay">立即支付</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script>
export default {
  name: 'OrderDetailDialog',
  props: {
    show: Boolean,
    order: Object
  },
  data() {
    return {
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleCancel() {
      this.visible = false;
    },
    handlePay() {
      this.$emit('pay', this.order);
      this.visible = false;
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      const d = new Date(timeStr);
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    getStatusText(status) {
      const texts = ['等待付款', '买家已付款', '交易关闭', '卖家已发货'];
      return texts[status] || '未知状态';
    },
    getStatusSubText(status) {
      const texts = [
        '请尽快完成支付，超时订单将被取消',
        '我们将尽快为您处理订单',
        '订单已取消或关闭',
        '商品已在路上，请耐心等待'
      ];
      return texts[status] || '';
    },
    getStatusIcon(status) {
      const icons = ['icon-safe', 'icon-check-circle-fill', 'icon-close-circle-fill', 'icon-send'];
      return icons[status] || 'icon-info-circle-fill';
    },
    getStatusColor(status) {
      const colors = ['#FF7E67', '#00B42A', '#86909C', '#165DFF'];
      return colors[status] || '#86909C';
    },
    getStatusBg(status) {
      const bgs = ['#FFF5F4', '#E8FFEA', '#F2F3F5', '#E8F3FF'];
      return bgs[status] || '#F2F3F5';
    }
  }
}
</script>

<style scoped>
.order-detail-container {
  padding: 0 10px;
}
.status-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 24px;
}
.status-icon-box {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.status-icon {
  font-size: 28px;
}
.status-text h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #1D2129;
}
.status-text p {
  margin: 0;
  font-size: 13px;
  color: #86909C;
}

.detail-card {
  background: #F7F8FA;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}
.card-title {
  font-size: 14px;
  font-weight: bold;
  color: #1D2129;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.address-text {
  font-size: 14px;
  color: #4E5969;
  line-height: 1.5;
}

.product-item {
  display: flex;
  gap: 12px;
}
.product-img {
  border-radius: 6px;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.pname {
  font-size: 14px;
  color: #1D2129;
  font-weight: 500;
  margin-bottom: 4px;
}
.pspec {
  font-size: 12px;
  color: #86909C;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
}
.info-row:last-child {
  margin-bottom: 0;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #E5E6EB;
}
.info-label {
  color: #86909C;
}
.info-value {
  color: #1D2129;
  text-align: right;
  word-break: break-all;
  max-width: 70%;
}
.info-value.deduct {
  color: #00B42A;
}
.info-value.price {
  color: #FF7E67;
  font-size: 18px;
  font-weight: bold;
}

.action-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>
