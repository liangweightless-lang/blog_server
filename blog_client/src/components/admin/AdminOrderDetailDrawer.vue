<template>
  <AppBottomSheet
    :visible="visible"
    :title="null"
    :width="isMobile ? 'calc(100% - 24px)' : '580px'"
    @update:visible="val => $emit('update:visible', val)"
  >
    <div v-if="order" class="admin-order-detail-container">
      <!-- 1. 顶部订单状态大卡片 -->
      <div class="drawer-status-card" :style="{ background: statusInfo.bg }">
        <div class="status-left">
          <div class="status-icon-wrapper" :style="{ color: statusInfo.color }">
            <icon-clock-circle v-if="order.status === 0" />
            <icon-check-circle v-else-if="order.status === 1" />
            <icon-send v-else-if="order.status === 3" />
            <icon-close-circle v-else-if="order.status === 2" />
            <icon-info-circle v-else />
          </div>
          <div class="status-texts">
            <div class="status-title-row">
              <h3 class="status-title" :style="{ color: statusInfo.color }">{{ statusInfo.text }}</h3>
              <a-tag :color="order.orderType === 'GROUP' ? 'orange' : 'arcoblue'" size="small">
                {{ order.orderType === 'GROUP' ? '拼团订单' : '普通单买' }}
              </a-tag>
            </div>
            <p class="status-desc">{{ statusInfo.desc }}</p>
          </div>
        </div>
        <div class="order-price-badge">
          <span class="p-label">实收金额</span>
          <span class="p-val">¥{{ order.amount }}</span>
        </div>
      </div>

      <!-- 2. 基础信息快捷条（单号快捷复制） -->
      <div class="drawer-sn-bar">
        <div class="sn-left">
          <span class="sn-label">订单编号</span>
          <span class="sn-val mono-font">#{{ order.id }}</span>
          <button class="sn-copy-btn" @click="copyText(order.id, '订单编号已复制')" title="复制单号">
            <icon-copy /> 复制
          </button>
        </div>
        <div class="sn-right">
          <span class="sn-time">{{ $formatTime(order.createTime) }}</span>
        </div>
      </div>

      <!-- 3. 订单生命周期时间线 (Timeline) -->
      <div class="drawer-section">
        <div class="section-header">
          <icon-schedule class="sec-icon" />
          <span class="sec-title">订单流转轨迹</span>
        </div>
        <div class="timeline-box">
          <div class="timeline-step done">
            <div class="step-dot"></div>
            <div class="step-content">
              <div class="step-title">买家提交订单</div>
              <div class="step-time">{{ $formatTime(order.createTime) }}</div>
            </div>
          </div>
          <div class="timeline-step" :class="{ done: order.status !== 0, active: order.status === 0 }">
            <div class="step-dot"></div>
            <div class="step-content">
              <div class="step-title">{{ order.status === 0 ? '等待付款 / 待人工核验' : '买家已付款完成' }}</div>
              <div class="step-time" v-if="order.payTime">{{ $formatTime(order.payTime) }}</div>
              <div class="step-time" v-else-if="order.status !== 0">已入账核销</div>
              <div class="step-desc" v-else>等待微信支付流水或店长手动确认收款</div>
            </div>
          </div>
          <div class="timeline-step" :class="{ done: order.status === 3, active: order.status === 1 }">
            <div class="step-dot"></div>
            <div class="step-content">
              <div class="step-title">
                {{ order.status === 3 ? '商品已出库配送/交付' : (order.status === 1 ? '待制作配货 / 待发货' : '履约交付') }}
              </div>
              <div class="step-desc" v-if="order.status === 1">已备货完毕，可标记已发货</div>
              <div class="step-desc" v-else-if="order.status === 3">已交付买家，订单履约完成</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 收货与联系人信息卡片（支持顺丰/跑腿一键整段打单复制） -->
      <div class="drawer-section">
        <div class="section-header-between">
          <div class="sec-left">
            <icon-location class="sec-icon" />
            <span class="sec-title">收货与履约信息</span>
          </div>
          <button class="batch-copy-btn" @click="copyFullShippingInfo" title="一键复制到快递录单软件">
            <icon-copy /> 复制整段打单信息
          </button>
        </div>

        <div class="shipping-info-card">
          <div class="shipping-row">
            <span class="ship-label">买家信息：</span>
            <span class="ship-val">
              UID: <strong>{{ order.userId }}</strong>
            </span>
          </div>

          <div class="shipping-row" v-if="order.contactPhone">
            <span class="ship-label">联系电话：</span>
            <span class="ship-val phone-val">
              <a :href="'tel:' + order.contactPhone" class="tel-link">
                <icon-phone /> {{ order.contactPhone }}
              </a>
              <button class="mini-icon-btn" @click="copyText(order.contactPhone, '手机号已复制')">
                <icon-copy />
              </button>
            </span>
          </div>

          <div class="shipping-row">
            <span class="ship-label">收货地址：</span>
            <span class="ship-val addr-val">
              {{ order.shippingAddress || '买家未填写收货地址（若为自提请核对自提点）' }}
            </span>
          </div>

          <!-- 烘焙定制特殊备注高亮提醒 -->
          <div v-if="order.remark" class="remark-alert-box">
            <div class="remark-header">
              <icon-message />
              <span>顾客特殊定制要求 / 备注：</span>
            </div>
            <div class="remark-content">{{ order.remark }}</div>
          </div>
        </div>
      </div>

      <!-- 5. 商品明细清单 -->
      <div class="drawer-section">
        <div class="section-header">
          <icon-storage class="sec-icon" />
          <span class="sec-title">商品明细清单</span>
        </div>

        <div class="prod-item-row">
          <img :src="$formatImageUrl(product?.image)" class="prod-thumb-img" />
          <div class="prod-meta">
            <h4 class="prod-title">{{ product?.name || '烘焙商品' }}</h4>
            <div class="prod-spec-pills">
              <span class="spec-pill" v-if="order.selectedSpec">规格: {{ order.selectedSpec }}</span>
              <span class="spec-pill count-pill">数量: ×{{ order.quantity || 1 }}</span>
            </div>
            <div class="prod-unit-price">
              单价: ¥{{ product?.price || order.amount }}
            </div>
          </div>
          <div class="prod-subtotal">
            <span class="subtotal-label">小计</span>
            <span class="subtotal-val">¥{{ (Number(product?.price || order.amount) * (order.quantity || 1)).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 6. 费用明细拆解 -->
      <div class="drawer-section">
        <div class="section-header">
          <icon-safe class="sec-icon" />
          <span class="sec-title">结算明细与收款核对</span>
        </div>

        <div class="fee-breakdown-card">
          <div class="fee-row">
            <span class="fee-label">商品总额</span>
            <span class="fee-val">¥{{ (Number(product?.price || order.amount) * (order.quantity || 1)).toFixed(2) }}</span>
          </div>
          <div class="fee-row">
            <span class="fee-label">配送费用</span>
            <span class="fee-val">{{ (order.deliveryFee && order.deliveryFee > 0) ? '+ ¥' + order.deliveryFee : '免配送费' }}</span>
          </div>
          <div class="fee-row" v-if="order.pointsUsed">
            <span class="fee-label">积分抵扣</span>
            <span class="fee-val deduct">- {{ order.pointsUsed }} 积分</span>
          </div>
          <div class="fee-divider"></div>
          <div class="fee-row total-row">
            <span class="total-label">实收结算总额</span>
            <span class="total-val">¥{{ order.amount }}</span>
          </div>
        </div>
      </div>

      <!-- 7. 底部操作栏 -->
      <div class="drawer-bottom-actions">
        <a-button size="large" shape="round" class="action-btn-close" @click="$emit('update:visible', false)">
          关闭
        </a-button>

        <a-button
          v-if="order.status === 0"
          type="primary"
          status="warning"
          size="large"
          shape="round"
          class="action-btn-main"
          @click="$emit('confirm-pay', order)"
        >
          <template #icon><icon-check-circle /></template>
          人工确认收款核销
        </a-button>

        <a-button
          v-if="order.status === 1"
          type="primary"
          size="large"
          shape="round"
          class="action-btn-main brand-action-btn"
          @click="$emit('ship', order)"
        >
          <template #icon><icon-send /></template>
          标记已配货发货
        </a-button>
      </div>
    </div>
  </AppBottomSheet>
</template>

<script>
import { Message } from '@arco-design/web-vue';

export default {
  name: 'AdminOrderDetailDrawer',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    order: {
      type: Object,
      default: null
    },
    product: {
      type: Object,
      default: null
    },
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'confirm-pay', 'ship'],
  computed: {
    statusInfo() {
      if (!this.order) return { text: '', desc: '', color: '#86909C', bg: '#F2F3F5' };
      switch (this.order.status) {
        case 0:
          return {
            text: '待付款',
            desc: '买家已提交订单，等待支付或店长对账后确认收款',
            color: '#FF7D00',
            bg: '#FFF7E8'
          };
        case 1:
          return {
            text: '待发货 / 待出炉',
            desc: '已付款成功，请核对商品与收货信息并安排配货出库',
            color: '#165DFF',
            bg: '#E8F3FF'
          };
        case 3:
          return {
            text: '已交付完成',
            desc: '商品已成功出库发货或买家已提货完成',
            color: '#00B42A',
            bg: '#E8FFEA'
          };
        case 2:
          return {
            text: '已取消 / 关闭',
            desc: '订单已取消，无需进行履约操作',
            color: '#86909C',
            bg: '#F2F3F5'
          };
        default:
          return {
            text: '处理中',
            desc: '订单状态更新中',
            color: '#86909C',
            bg: '#F2F3F5'
          };
      }
    }
  },
  methods: {
    async copyText(text, successMsg = '已成功复制到剪贴板') {
      if (!text) return;
      try {
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(String(text));
        } else {
          const input = document.createElement('textarea');
          input.value = String(text);
          input.style.position = 'fixed';
          input.style.opacity = '0';
          document.body.appendChild(input);
          input.focus();
          input.select();
          document.execCommand('copy');
          document.body.removeChild(input);
        }
        Message.success(successMsg);
      } catch (err) {
        Message.error('复制失败，请手动选中文本');
      }
    },
    copyFullShippingInfo() {
      if (!this.order) return;
      const uid = this.order.userId || '';
      const phone = this.order.contactPhone || '未留电话';
      const addr = this.order.shippingAddress || '未留地址';
      const remark = this.order.remark ? ` 【备注: ${this.order.remark}】` : '';
      const fullText = `收件人: UID_${uid}，电话: ${phone}，地址: ${addr}${remark}`;
      this.copyText(fullText, '📋 整段打单收货信息已复制，可直接粘贴到快递助手！');
    }
  }
};
</script>

<style scoped>
.admin-order-detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0 16px;
}

/* 1. 状态大卡片 */
.drawer-status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-radius: 14px;
  gap: 16px;
}

.status-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-icon-wrapper {
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-texts {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.status-desc {
  margin: 0;
  font-size: 12px;
  color: #4E5969;
  line-height: 1.4;
}

.order-price-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.p-label {
  font-size: 11px;
  color: #86909C;
}

.p-val {
  font-size: 20px;
  font-weight: 800;
  color: #1D2129;
}

/* 2. 单号快捷条 */
.drawer-sn-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F7F8FA;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
}

.sn-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sn-label {
  color: #86909C;
}

.sn-val {
  font-weight: 600;
  color: #1D2129;
}

.mono-font {
  font-family: monospace;
}

.sn-copy-btn {
  border: none;
  background: #E5E6EB;
  color: #165DFF;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.sn-copy-btn:hover {
  background: #C9CDD4;
}

.sn-right {
  color: #86909C;
  font-size: 12px;
}

/* 通用 Section 结构 */
.drawer-section {
  background: #FFFFFF;
  border: 1px solid #F2F3F5;
  border-radius: 12px;
  padding: 14px 16px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.section-header-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.sec-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sec-icon {
  font-size: 16px;
  color: #165DFF;
}

.sec-title {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.batch-copy-btn {
  border: 1px solid #165DFF;
  background: #F2F7FF;
  color: #165DFF;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.batch-copy-btn:hover {
  background: #165DFF;
  color: #FFFFFF;
}

/* 3. 时间线样式 */
.timeline-box {
  display: flex;
  flex-direction: column;
  position: relative;
  padding-left: 6px;
}

.timeline-step {
  display: flex;
  gap: 12px;
  position: relative;
  padding-bottom: 16px;
}

.timeline-step:last-child {
  padding-bottom: 0;
}

.timeline-step::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: #E5E6EB;
}

.timeline-step:last-child::before {
  display: none;
}

.step-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #C9CDD4;
  margin-top: 2px;
  flex-shrink: 0;
  z-index: 1;
}

.timeline-step.done .step-dot {
  background: #00B42A;
  box-shadow: 0 0 0 3px rgba(0, 180, 42, 0.15);
}

.timeline-step.active .step-dot {
  background: #165DFF;
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.2);
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-title {
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
}

.step-time {
  font-size: 12px;
  color: #86909C;
}

.step-desc {
  font-size: 12px;
  color: #4E5969;
}

/* 4. 收货履约卡片 */
.shipping-info-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
}

.shipping-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.ship-label {
  color: #86909C;
  width: 70px;
  flex-shrink: 0;
}

.ship-val {
  color: #1D2129;
  flex: 1;
}

.phone-val {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tel-link {
  color: #165DFF;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.mini-icon-btn {
  border: none;
  background: #F2F3F5;
  color: #4E5969;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
  cursor: pointer;
}

.mini-icon-btn:hover {
  background: #E5E6EB;
  color: #165DFF;
}

.remark-alert-box {
  margin-top: 6px;
  background: #FFF7E8;
  border: 1px solid #FFE4BA;
  border-radius: 8px;
  padding: 10px 12px;
  color: #D46B08;
}

.remark-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
}

.remark-content {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
}

/* 5. 商品清单 */
.prod-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.prod-thumb-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #F2F3F5;
}

.prod-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prod-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.prod-spec-pills {
  display: flex;
  align-items: center;
  gap: 6px;
}

.spec-pill {
  font-size: 11px;
  background: #F2F3F5;
  color: #4E5969;
  padding: 2px 6px;
  border-radius: 4px;
}

.count-pill {
  background: #FFF0F0;
  color: #F53F3F;
  font-weight: 600;
}

.prod-unit-price {
  font-size: 12px;
  color: #86909C;
}

.prod-subtotal {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.subtotal-label {
  font-size: 11px;
  color: #86909C;
}

.subtotal-val {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
}

/* 6. 费用明细 */
.fee-breakdown-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #4E5969;
}

.fee-val.deduct {
  color: #F53F3F;
}

.fee-divider {
  height: 1px;
  background: #F2F3F5;
  margin: 4px 0;
}

.fee-row.total-row {
  margin-top: 2px;
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.total-val {
  font-size: 20px;
  font-weight: 800;
  color: #F53F3F;
}

/* 7. 底部操作栏 */
.drawer-bottom-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #F2F3F5;
}

.action-btn-close {
  min-width: 90px;
}

.action-btn-main {
  min-width: 140px;
}

.brand-action-btn {
  background: linear-gradient(135deg, #165DFF 0%, #0E42D2 100%);
  border: none;
}
</style>
