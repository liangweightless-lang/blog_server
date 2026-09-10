<template>
  <div class="campaign-order-list-component">
    <!-- 1. 状态微胶囊横滑筛选栏 -->
    <div class="filter-capsule-scroll" v-if="orders.length > 0">
      <div class="capsule-track">
        <button 
          v-for="filter in filterOptions" 
          :key="filter.key"
          class="filter-pill-btn" 
          :class="{ 'is-active': activeFilter === filter.key }"
          @click="activeFilter = filter.key"
        >
          <span class="pill-label">{{ filter.label }}</span>
          <span 
            class="pill-badge" 
            :class="{ 
              'badge-highlight': filter.key === 'pendingPickup' && getFilterCount(filter.key) > 0,
              'badge-danger': filter.key === 'pendingPay' && getFilterCount(filter.key) > 0,
              'badge-zero': getFilterCount(filter.key) === 0 
            }"
          >
            {{ getFilterCount(filter.key) }}
          </span>
        </button>
      </div>
    </div>

    <!-- 2. 空状态展示 -->
    <div v-if="filteredOrders.length === 0" class="empty-state-box">
      <div class="empty-icon-circle">
        <icon-fire style="font-size: 38px; color: #D3C1BA;" />
      </div>
      <p class="empty-primary-text">{{ getEmptyText() }}</p>
      <p class="empty-sub-text">快去社区快团挑选今日新鲜出炉的面包与甜点吧</p>
      <a-button 
        v-if="activeFilter !== 'ALL'" 
        size="small" 
        shape="round" 
        class="empty-reset-btn" 
        @click="activeFilter = 'ALL'"
      >
        查看全部快团记录
      </a-button>
    </div>

    <!-- 3. 跟团订单精美卡片列表 -->
    <div v-else class="campaign-card-list">
      <div 
        v-for="order in filteredOrders" 
        :key="order.id" 
        class="campaign-order-card"
        :class="getCardStatusClass(order.status)"
      >
        <!-- A. 卡片头部：跟团号、复制单号、状态微标 -->
        <div class="card-header-row">
          <div class="header-left-slot">
            <span class="campaign-badge-pill">社区快团</span>
            <div class="follow-num-wrap" @click="handleCopy(order.followNumber, '跟团号')">
              <span class="follow-label">跟团号</span>
              <span class="follow-no">#{{ order.followNumber }}</span>
              <icon-copy class="copy-tiny-icon" title="点击复制跟团号" />
            </div>
          </div>

          <div class="header-right-slot">
            <!-- 拼团达标状态小徽章 (若设置了成团人数) -->
            <span 
              v-if="order.campaign && order.campaign.targetNum > 0"
              class="group-tag-pill"
              :class="getGroupStatusClass(order.campaign.groupStatus)"
            >
              {{ order.campaign.groupStatusText || (order.campaign.groupStatus === 1 ? '拼团成功' : '拼团中') }}
            </span>
            <span v-else-if="!order.campaign" class="group-tag-pill group-archived">
              活动已下线
            </span>

            <!-- 订单交易与提货状态徽章 -->
            <span class="order-status-pill" :class="'status-' + order.status">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </div>

        <!-- B. 快团活动标题与直达快团详情入口 -->
        <div class="campaign-title-bar" @click="goToCampaignDetail(order)">
          <div class="title-content-box">
            <h3 class="campaign-main-title">{{ order.campaign?.title || '团购活动 (已下线)' }}</h3>
            <span class="campaign-intro-line" v-if="order.campaign?.intro">
              {{ order.campaign.intro }}
            </span>
          </div>
          <div class="title-enter-arrow" v-if="order.campaign">
            <span class="arrow-text">快团</span>
            <icon-right class="arrow-icon" />
          </div>
        </div>

        <!-- C. 成团进度条看板 (若有成团目标) -->
        <div 
          v-if="order.campaign && order.campaign.targetNum > 0" 
          class="groupbuy-progress-card"
          :class="{ 'is-success': order.campaign.groupStatus === 1 }"
        >
          <div class="progress-info-row">
            <div class="progress-left-desc">
              <icon-check-circle-fill v-if="order.campaign.groupStatus === 1" class="progress-icon-success" />
              <icon-fire v-else class="progress-icon-fire" />
              <span class="progress-target-text">成团目标 {{ order.campaign.targetNum }} 人</span>
              <span class="progress-current-text">已跟团 {{ order.campaign.currentNum || 0 }} 人</span>
            </div>
            <span class="progress-status-tip" :class="{ 'text-success': order.campaign.groupStatus === 1 }">
              {{ order.campaign.groupStatus === 1 ? '🎉 已达标·成团成功' : `还差 ${Math.max(0, order.campaign.targetNum - (order.campaign.currentNum || 0))} 人` }}
            </span>
          </div>

          <!-- 进度条本体 -->
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ width: calcProgressPercent(order.campaign) + '%' }"
              :class="{ 'fill-completed': order.campaign.groupStatus === 1 }"
            ></div>
          </div>
        </div>

        <!-- D. 履约自提点与预计出炉时间看板 -->
        <div class="fulfillment-box">
          <div class="fulfillment-row location-row">
            <div class="icon-indicator loc-indicator">
              <icon-location />
            </div>
            <div class="loc-text-col">
              <span class="loc-label">自提点</span>
              <span class="loc-name">{{ order.campaign?.deliveryLocation?.name || '指定自提点' }}</span>
              <span class="loc-detail-addr" v-if="order.campaign?.deliveryLocation?.address">
                {{ order.campaign.deliveryLocation.address }}
              </span>
            </div>
            <a 
              v-if="order.campaign?.deliveryLocation?.contactPhone" 
              :href="'tel:' + order.campaign.deliveryLocation.contactPhone"
              class="loc-phone-btn"
              @click.stop
              title="联系自提点/团长"
            >
              <icon-phone />
            </a>
          </div>

          <!-- 预计发货/自提时间 -->
          <div class="fulfillment-row time-row" v-if="order.campaign?.deliveryTime">
            <div class="icon-indicator time-indicator">
              <icon-clock-circle />
            </div>
            <div class="time-text-col">
              <span class="time-label">预计出炉/提货:</span>
              <span class="time-value">{{ $formatTime(order.campaign.deliveryTime) }}</span>
            </div>
          </div>

          <!-- 待提货专属提示与核销码快速入口 -->
          <div class="pickup-code-tip-banner" v-if="order.status === 1" @click="openVoucher(order)">
            <div class="tip-left">
              <icon-check-circle-fill class="tip-banner-icon" />
              <span>已付款·凭跟团号 <strong>#{{ order.followNumber }}</strong> 提货</span>
            </div>
            <button class="show-code-btn" @click.stop="openVoucher(order)">
              <icon-scan /> <span>出示提货码</span>
            </button>
          </div>
        </div>

        <!-- E. 订单内商品明细卡片列表 -->
        <div class="order-items-container">
          <div 
            v-for="item in order.items" 
            :key="item.id" 
            class="item-line-card"
          >
            <div class="item-thumb-box">
              <img 
                v-if="item.productImage" 
                :src="$formatImageUrl(item.productImage)" 
                class="item-thumb-img" 
                alt="商品图片"
              />
              <div v-else class="item-thumb-placeholder">
                <icon-image style="font-size: 20px; color: #C9CDD4;" />
              </div>
            </div>

            <div class="item-info-col">
              <div class="item-name-row">
                <span class="item-title">{{ item.productName || '精选烘焙' }}</span>
                <span class="item-calc-price">¥{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}</span>
              </div>
              <div class="item-meta-row">
                <span class="item-specs-badge" v-if="item.specs">
                  {{ item.specs }}
                </span>
                <span class="item-unit-and-qty">¥{{ item.price }} × {{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- F. 订单编号与下单时间信息 -->
        <div class="order-meta-footer-info">
          <div class="order-id-meta" @click="handleCopy(order.id, '订单编号')">
            <span>订单: {{ String(order.id).substring(0, 16) }}...</span>
            <icon-copy class="meta-copy-icon" />
          </div>
          <span class="order-create-time">{{ $formatTime(order.createTime) }}</span>
        </div>

        <!-- G. 卡片底部：实付款结算与全场景操作按钮 -->
        <div class="card-action-footer">
          <div class="settle-price-wrap">
            <span class="settle-qty-count">共 {{ calcTotalQuantity(order.items) }} 件</span>
            <span class="settle-label">实付:</span>
            <span class="settle-currency">¥</span>
            <span class="settle-amount">{{ order.totalAmount }}</span>
          </div>

          <!-- 独立场景操作按钮组 -->
          <div class="action-btn-group">
            <!-- 场景 1：待付款 (0) -->
            <template v-if="order.status === 0">
              <a-button 
                type="text" 
                status="danger" 
                size="small" 
                class="btn-action-cancel"
                @click.stop="handleDeleteOrder(order)"
              >
                取消跟团
              </a-button>
              <button 
                class="btn-action-pay"
                @click.stop="$emit('pay', order)"
              >
                <span>立即支付</span>
              </button>
            </template>

            <!-- 场景 2：待提货 / 备料中 (1) -->
            <template v-else-if="order.status === 1">
              <button 
                class="btn-action-outline"
                @click.stop="goToCampaignDetail(order)"
              >
                查看快团
              </button>
              <button 
                class="btn-action-voucher"
                @click.stop="openVoucher(order)"
              >
                <icon-scan class="voucher-icon" />
                <span>提货凭证</span>
              </button>
            </template>

            <!-- 场景 3：已提货完成 (2) -->
            <template v-else-if="order.status === 2">
              <button 
                class="btn-action-outline"
                @click.stop="handleReorder(order)"
              >
                再来一单
              </button>
              <button 
                class="btn-action-secondary"
                @click.stop="openVoucher(order)"
              >
                查看详情
              </button>
            </template>

            <!-- 场景 4：已取消 (3) -->
            <template v-else-if="order.status === 3">
              <a-button 
                type="text" 
                status="danger" 
                size="small" 
                class="btn-action-del"
                @click.stop="handleDeleteOrder(order)"
              >
                删除记录
              </a-button>
              <button 
                class="btn-action-outline"
                @click.stop="goToCampaignDetail(order)"
              >
                逛逛快团
              </button>
            </template>
          </div>
        </div>
      </div>

      <p class="list-end-divider">已展示全部 {{ filteredOrders.length }} 条跟团记录</p>
    </div>

    <!-- 4. 专属电子提货凭证微抽屉 (AppBottomSheet) -->
    <AppBottomSheet
      :visible="voucherDrawerVisible"
      title="提货核销凭证"
      subtitle="请向小柴包酱或自提点出示此凭证以领取商品"
      confirmText="已确认提货"
      @confirm="voucherDrawerVisible = false"
      @cancel="voucherDrawerVisible = false"
    >
      <div class="voucher-modal-content" v-if="currentVoucherOrder">
        <!-- 提货核心大号跟团号展示 -->
        <div class="voucher-highlight-card">
          <span class="voucher-card-badge">快团自提码</span>
          <div class="voucher-code-main">
            <span class="voucher-prefix">跟团号</span>
            <span class="voucher-large-number">#{{ currentVoucherOrder.followNumber }}</span>
          </div>
          <div class="voucher-barcode-simulation">
            <div class="barcode-lines"></div>
            <div class="barcode-number" @click="handleCopy(currentVoucherOrder.id, '订单单号')">
              <span>单号: {{ currentVoucherOrder.id }}</span>
              <icon-copy class="barcode-copy-icon" />
            </div>
          </div>
          <div class="voucher-status-stamp" :class="'stamp-' + currentVoucherOrder.status">
            {{ getStatusText(currentVoucherOrder.status) }}
          </div>
        </div>

        <!-- 履约与自提点指引 -->
        <div class="voucher-info-group">
          <div class="voucher-info-item">
            <span class="v-label"><icon-location /> 提货地点:</span>
            <span class="v-val highlight-loc">{{ currentVoucherOrder.campaign?.deliveryLocation?.name || '指定提货点' }}</span>
          </div>
          <div class="voucher-info-item" v-if="currentVoucherOrder.campaign?.deliveryLocation?.address">
            <span class="v-label">详细地址:</span>
            <span class="v-val">{{ currentVoucherOrder.campaign.deliveryLocation.address }}</span>
          </div>
          <div class="voucher-info-item" v-if="currentVoucherOrder.campaign?.deliveryTime">
            <span class="v-label"><icon-clock-circle /> 预计提货:</span>
            <span class="v-val highlight-time">{{ $formatTime(currentVoucherOrder.campaign.deliveryTime) }}</span>
          </div>
          <div class="voucher-info-item" v-if="currentVoucherOrder.contactPhone">
            <span class="v-label"><icon-phone /> 提货预留:</span>
            <span class="v-val">{{ currentVoucherOrder.contactPhone }} ({{ currentVoucherOrder.contactName || '顾客' }})</span>
          </div>
          <div class="voucher-info-item" v-if="currentVoucherOrder.remark">
            <span class="v-label">订单备注:</span>
            <span class="v-val remark-text">{{ currentVoucherOrder.remark }}</span>
          </div>
        </div>

        <!-- 核销商品清单 -->
        <div class="voucher-items-summary">
          <h4 class="v-items-title">跟团商品明细 (共 {{ calcTotalQuantity(currentVoucherOrder.items) }} 件)</h4>
          <div class="v-items-list">
            <div 
              v-for="item in currentVoucherOrder.items" 
              :key="item.id" 
              class="v-item-row"
            >
              <span class="v-item-name">{{ item.productName }}</span>
              <span class="v-item-specs" v-if="item.specs">({{ item.specs }})</span>
              <span class="v-item-qty">× {{ item.quantity }}</span>
              <span class="v-item-price">¥{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}</span>
            </div>
          </div>
          <div class="v-total-row">
            <span>实付款:</span>
            <span class="v-total-price">¥{{ currentVoucherOrder.totalAmount }}</span>
          </div>
        </div>
      </div>
    </AppBottomSheet>
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
  data() {
    return {
      activeFilter: 'ALL',
      filterOptions: [
        { key: 'ALL', label: '全部' },
        { key: 'pendingPay', label: '待付款' },
        { key: 'pendingPickup', label: '待提货' },
        { key: 'completed', label: '已完成' },
        { key: 'cancelled', label: '已取消' }
      ],
      voucherDrawerVisible: false,
      currentVoucherOrder: null
    }
  },
  computed: {
    filteredOrders() {
      if (this.activeFilter === 'ALL') {
        return this.orders;
      }
      if (this.activeFilter === 'pendingPay') {
        return this.orders.filter(o => o.status === 0);
      }
      if (this.activeFilter === 'pendingPickup') {
        return this.orders.filter(o => o.status === 1);
      }
      if (this.activeFilter === 'completed') {
        return this.orders.filter(o => o.status === 2);
      }
      if (this.activeFilter === 'cancelled') {
        return this.orders.filter(o => o.status === 3);
      }
      return this.orders;
    }
  },
  methods: {
    getFilterCount(key) {
      if (key === 'ALL') return this.orders.length;
      if (key === 'pendingPay') return this.orders.filter(o => o.status === 0).length;
      if (key === 'pendingPickup') return this.orders.filter(o => o.status === 1).length;
      if (key === 'completed') return this.orders.filter(o => o.status === 2).length;
      if (key === 'cancelled') return this.orders.filter(o => o.status === 3).length;
      return 0;
    },
    getEmptyText() {
      if (this.activeFilter === 'pendingPay') return '暂无待付款的跟团订单';
      if (this.activeFilter === 'pendingPickup') return '暂无待提货/备料中的快团';
      if (this.activeFilter === 'completed') return '暂无已提货完成的快团';
      if (this.activeFilter === 'cancelled') return '暂无已取消的快团记录';
      return '暂无社区快团记录';
    },
    calcTotalQuantity(items) {
      if (!items || !items.length) return 1;
      return items.reduce((acc, item) => acc + (item.quantity || 1), 0);
    },
    calcProgressPercent(campaign) {
      if (!campaign || !campaign.targetNum || campaign.targetNum <= 0) return 100;
      const cur = campaign.currentNum || 0;
      return Math.min(100, Math.round((cur / campaign.targetNum) * 100));
    },
    getStatusText(status) {
      const texts = {
        0: '待付款',
        1: '待提货/备料中',
        2: '已提货完成',
        3: '已取消'
      };
      return texts[status] || '处理中';
    },
    getCardStatusClass(status) {
      return `card-status-${status}`;
    },
    getGroupStatusClass(groupStatus) {
      if (groupStatus === 1) return 'group-success';
      if (groupStatus === 2) return 'group-failed';
      return 'group-active';
    },
    goToCampaignDetail(order) {
      if (!order?.campaign) {
        Message.warning('该快团活动已下线或不存在');
        return;
      }
      this.$router.push(`/campaign/${order.campaignId}`);
    },
    handleReorder(order) {
      if (!order?.campaign || order.campaign.status === 2) {
        Message.info('该快团已结团下线，已为您前往烘焙商城挑选美味');
        this.$router.push('/store');
        return;
      }
      this.$router.push(`/campaign/${order.campaignId}`);
    },
    openVoucher(order) {
      this.currentVoucherOrder = order;
      this.voucherDrawerVisible = true;
    },
    handleCopy(text, name = '单号') {
      if (!text) return;
      const str = String(text).trim();
      if (navigator?.clipboard?.writeText) {
        navigator.clipboard.writeText(str).then(() => {
          Message.success(`${name}已复制到剪贴板`);
        }).catch(() => {
          this.fallbackCopy(str, name);
        });
      } else {
        this.fallbackCopy(str, name);
      }
    },
    fallbackCopy(text, name) {
      const input = document.createElement('input');
      input.value = text;
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        Message.success(`${name}已复制到剪贴板`);
      } catch (err) {
        Message.warning(`复制失败，请手动选择复制`);
      }
      document.body.removeChild(input);
    },
    handleDeleteOrder(order) {
      Modal.confirm({
        title: order.status === 0 ? '取消跟团订单' : '删除跟团记录',
        content: order.status === 0 
          ? `确定要取消跟团号 #${order.followNumber} 的未支付订单吗？取消后不可恢复。` 
          : `确定要删除跟团号 #${order.followNumber} 的记录吗？`,
        okText: '确认',
        cancelText: '暂不取消',
        onOk: async () => {
          try {
            await deleteUnpaidCampaignOrder(order.id);
            Message.success('跟团订单已成功处理');
            this.$emit('refresh');
          } catch (e) {
            Message.error(e.response?.data?.message || '操作失败，请重试');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.campaign-order-list-component {
  width: 100%;
}

/* ================= 1. 状态分类微胶囊筛选栏 ================= */
.filter-capsule-scroll {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin-bottom: 14px;
  padding-bottom: 2px;
}
.filter-capsule-scroll::-webkit-scrollbar {
  display: none;
}

.capsule-track {
  display: flex;
  gap: 8px;
  align-items: center;
  min-width: max-content;
}

.filter-pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: #FFFFFF;
  color: #4E5969;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.filter-pill-btn:active {
  transform: scale(0.95);
}

.filter-pill-btn.is-active {
  background: #1D2129;
  color: #FFFFFF;
  border-color: #1D2129;
  box-shadow: 0 4px 12px rgba(29, 33, 41, 0.15);
}

.pill-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  background: #F2F3F5;
  color: #4E5969;
}
.filter-pill-btn.is-active .pill-badge {
  background: rgba(255, 255, 255, 0.2);
  color: #FFFFFF;
}

.pill-badge.badge-highlight {
  background: #FFF7E8;
  color: #D46B08;
}
.filter-pill-btn.is-active .pill-badge.badge-highlight {
  background: #F59E0B;
  color: #FFFFFF;
}

.pill-badge.badge-danger {
  background: #FFECE8;
  color: #F53F3F;
}
.filter-pill-btn.is-active .pill-badge.badge-danger {
  background: #F53F3F;
  color: #FFFFFF;
}

.pill-badge.badge-zero {
  opacity: 0.6;
}

/* ================= 2. 空状态样式 ================= */
.empty-state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;
}

.empty-icon-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #FAF7F5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.empty-primary-text {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  margin: 0 0 6px;
}

.empty-sub-text {
  font-size: 12px;
  color: #86909C;
  margin: 0 0 16px;
}

.empty-reset-btn {
  border-color: #E5E6EB;
  color: #4E5969;
  font-size: 12px;
}

/* ================= 3. 跟团卡片本体样式 ================= */
.campaign-card-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.campaign-order-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 16px 14px;
  box-shadow: 0 8px 24px rgba(17, 24, 39, 0.03), 0 1px 3px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

/* 待提货高光微弱金光边缘 */
.campaign-order-card.card-status-1 {
  border-color: rgba(245, 158, 11, 0.25);
}
.campaign-order-card.card-status-1::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #F59E0B 0%, #FF5A34 100%);
}

/* A. 头部样式 */
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #F7F8FA;
}

.header-left-slot {
  display: flex;
  align-items: center;
  gap: 8px;
}

.campaign-badge-pill {
  font-size: 10px;
  font-weight: 800;
  color: #FFFFFF;
  background: linear-gradient(135deg, #FF5A34 0%, #FF2A54 100%);
  padding: 2px 7px;
  border-radius: 6px;
  letter-spacing: 0.5px;
}

.follow-num-wrap {
  display: flex;
  align-items: baseline;
  gap: 3px;
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background 0.15s ease;
}
.follow-num-wrap:active {
  background: #F2F3F5;
}

.follow-label {
  font-size: 11px;
  color: #86909C;
}

.follow-no {
  font-size: 16px;
  font-weight: 900;
  color: #FF5A34;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
}

.copy-tiny-icon {
  font-size: 12px;
  color: #C9CDD4;
  margin-left: 2px;
}

.header-right-slot {
  display: flex;
  align-items: center;
  gap: 6px;
}

.group-tag-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 6px;
}
.group-tag-pill.group-success {
  background: #E8FFEA;
  color: #00B42A;
}
.group-tag-pill.group-active {
  background: #FFF7E8;
  color: #FF7D00;
}
.group-tag-pill.group-failed {
  background: #FEE2E2;
  color: #F53F3F;
}
.group-tag-pill.group-archived {
  background: #F2F3F5;
  color: #86909C;
}

.order-status-pill {
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 6px;
}
.order-status-pill.status-0 {
  background: #FFF7E8;
  color: #FF7D00;
}
.order-status-pill.status-1 {
  background: #E8FFEA;
  color: #00B42A;
}
.order-status-pill.status-2 {
  background: #F2F3F5;
  color: #4E5969;
}
.order-status-pill.status-3 {
  background: #F2F3F5;
  color: #86909C;
}

/* B. 快团标题直达栏 */
.campaign-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding: 8px 10px;
  background: #FAF8F7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.campaign-title-bar:active {
  background: #F2EFEA;
  transform: scale(0.99);
}

.title-content-box {
  flex: 1;
  min-width: 0;
}

.campaign-main-title {
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.2px;
}

.campaign-intro-line {
  font-size: 11px;
  color: #86909C;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.title-enter-arrow {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #86909C;
  font-size: 12px;
  font-weight: 600;
  margin-left: 8px;
}
.arrow-icon {
  font-size: 11px;
}

/* C. 成团进度条微看板 */
.groupbuy-progress-card {
  margin-top: 10px;
  background: #FFF9F7;
  border: 1px solid rgba(255, 90, 52, 0.12);
  border-radius: 12px;
  padding: 10px 12px;
}
.groupbuy-progress-card.is-success {
  background: #F6FFED;
  border-color: rgba(82, 196, 26, 0.15);
}

.progress-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 11px;
}

.progress-left-desc {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #4E5969;
}
.progress-icon-fire {
  color: #FF5A34;
  font-size: 13px;
}
.progress-icon-success {
  color: #00B42A;
  font-size: 13px;
}
.progress-target-text {
  font-weight: 700;
  color: #1D2129;
}
.progress-current-text {
  color: #86909C;
}

.progress-status-tip {
  font-weight: 700;
  color: #FF5A34;
}
.progress-status-tip.text-success {
  color: #00B42A;
}

.progress-track {
  width: 100%;
  height: 6px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF7D00 0%, #FF5A34 100%);
  border-radius: 3px;
  transition: width 0.4s ease;
}
.progress-fill.fill-completed {
  background: linear-gradient(90deg, #52C41A 0%, #00B42A 100%);
}

/* D. 履约自提点与预计出炉时间看板 */
.fulfillment-box {
  margin-top: 10px;
  background: #FAF8F7;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fulfillment-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
}

.icon-indicator {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
  margin-top: 1px;
}
.loc-indicator {
  background: rgba(22, 93, 255, 0.1);
  color: #165DFF;
}
.time-indicator {
  background: rgba(245, 158, 11, 0.12);
  color: #D97706;
}

.loc-text-col, .time-text-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.loc-label {
  font-size: 10px;
  font-weight: 600;
  color: #86909C;
}
.loc-name {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}
.loc-detail-addr {
  font-size: 11px;
  color: #86909C;
  margin-top: 1px;
}

.loc-phone-btn {
  color: #165DFF;
  background: #FFFFFF;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  font-size: 13px;
}

.time-row {
  align-items: center;
}
.time-text-col {
  flex-direction: row;
  align-items: center;
  gap: 6px;
}
.time-label {
  font-size: 12px;
  color: #86909C;
}
.time-value {
  font-size: 12px;
  font-weight: 700;
  color: #D97706;
}

/* 待提货醒目横幅 */
.pickup-code-tip-banner {
  background: #FFFFFF;
  border: 1px solid rgba(0, 180, 42, 0.2);
  border-radius: 10px;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.tip-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #1D2129;
}
.tip-left strong {
  color: #00B42A;
  font-size: 13px;
}
.tip-banner-icon {
  color: #00B42A;
  font-size: 14px;
}

.show-code-btn {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border: none;
  color: #FFFFFF;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(5, 150, 105, 0.25);
}

/* E. 商品列表 */
.order-items-container {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-line-card {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #FAF8F7;
  padding: 8px 10px;
  border-radius: 12px;
}

.item-thumb-box {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  background: #F2F3F5;
  flex-shrink: 0;
}
.item-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.item-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-info-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.item-name-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}

.item-title {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-calc-price {
  font-size: 13px;
  font-weight: 800;
  color: #1D2129;
}

.item-meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-specs-badge {
  font-size: 10px;
  font-weight: 600;
  color: #86909C;
  background: #FFFFFF;
  padding: 1px 6px;
  border-radius: 4px;
}

.item-unit-and-qty {
  font-size: 11px;
  color: #86909C;
}

/* F. 单号与时间信息 */
.order-meta-footer-info {
  margin-top: 10px;
  padding-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #C9CDD4;
}

.order-id-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #86909C;
}
.meta-copy-icon {
  font-size: 11px;
}
.order-create-time {
  color: #C9CDD4;
}

/* G. 底部结算与按钮 */
.card-action-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #F7F8FA;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settle-price-wrap {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.settle-qty-count {
  font-size: 11px;
  color: #86909C;
  margin-right: 4px;
}

.settle-label {
  font-size: 12px;
  font-weight: 600;
  color: #4E5969;
}

.settle-currency {
  font-size: 12px;
  font-weight: 800;
  color: #1D2129;
}

.settle-amount {
  font-size: 18px;
  font-weight: 900;
  color: #1D2129;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
}

.action-btn-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-action-cancel, .btn-action-del {
  font-size: 12px;
}

.btn-action-pay {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  border: none;
  color: #FFFFFF;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(255, 94, 58, 0.3);
  cursor: pointer;
  transition: transform 0.15s ease;
}
.btn-action-pay:active {
  transform: scale(0.93);
}

.btn-action-voucher {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  border: none;
  color: #FFFFFF;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.25);
  cursor: pointer;
  transition: transform 0.15s ease;
}
.btn-action-voucher:active {
  transform: scale(0.93);
}
.voucher-icon {
  font-size: 13px;
}

.btn-action-outline {
  background: #FFFFFF;
  border: 1px solid #E5E6EB;
  color: #1D2129;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.btn-action-outline:active {
  background: #F2F3F5;
  transform: scale(0.93);
}

.btn-action-secondary {
  background: #F2F3F5;
  border: none;
  color: #1D2129;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
}

.list-end-divider {
  text-align: center;
  font-size: 11px;
  color: #C9CDD4;
  margin: 20px 0 10px;
}

/* ================= 4. 电子提货核销凭证抽屉专属样式 ================= */
.voucher-modal-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 4px;
}

.voucher-highlight-card {
  position: relative;
  background: linear-gradient(135deg, #1A1D24 0%, #11141A 100%);
  border-radius: 18px;
  padding: 20px 18px 16px;
  color: #FFFFFF;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.voucher-card-badge {
  font-size: 11px;
  font-weight: 700;
  color: #FBBF24;
  background: rgba(245, 158, 11, 0.2);
  padding: 2px 10px;
  border-radius: 12px;
  margin-bottom: 10px;
}

.voucher-code-main {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.voucher-prefix {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}
.voucher-large-number {
  font-size: 40px;
  font-weight: 900;
  color: #FBBF24;
  letter-spacing: 1px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
  line-height: 1;
}

.voucher-barcode-simulation {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.barcode-lines {
  width: 80%;
  height: 38px;
  background: repeating-linear-gradient(
    90deg,
    #FFFFFF 0,
    #FFFFFF 2px,
    transparent 2px,
    transparent 5px,
    #FFFFFF 5px,
    #FFFFFF 9px,
    transparent 9px,
    transparent 11px
  );
  opacity: 0.85;
  border-radius: 2px;
}

.barcode-number {
  margin-top: 6px;
  font-size: 11px;
  font-family: monospace;
  color: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.barcode-copy-icon {
  font-size: 11px;
}

.voucher-status-stamp {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid currentColor;
}
.voucher-status-stamp.stamp-1 {
  color: #10B981;
  background: rgba(16, 185, 129, 0.15);
}
.voucher-status-stamp.stamp-2 {
  color: #9CA3AF;
  background: rgba(156, 163, 175, 0.15);
}
.voucher-status-stamp.stamp-0 {
  color: #F59E0B;
  background: rgba(245, 158, 11, 0.15);
}

.voucher-info-group {
  background: #FAF8F7;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.voucher-info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
}
.v-label {
  color: #86909C;
  font-size: 12px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}
.v-val {
  color: #1D2129;
  font-weight: 600;
  flex: 1;
}
.v-val.highlight-loc {
  color: #165DFF;
  font-weight: 800;
}
.v-val.highlight-time {
  color: #D97706;
  font-weight: 700;
}
.v-val.remark-text {
  color: #D46B08;
  font-size: 12px;
}

.voucher-items-summary {
  border: 1px solid #F2F3F5;
  border-radius: 14px;
  padding: 14px;
}
.v-items-title {
  margin: 0 0 10px 0;
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}
.v-items-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.v-item-row {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #4E5969;
}
.v-item-name {
  flex: 1;
  color: #1D2129;
  font-weight: 600;
}
.v-item-specs {
  color: #86909C;
  margin-right: 6px;
}
.v-item-qty {
  color: #86909C;
  margin-right: 12px;
}
.v-item-price {
  font-weight: 700;
  color: #1D2129;
}

.v-total-row {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #E5E6EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}
.v-total-price {
  font-size: 17px;
  font-weight: 900;
  color: #FF5A34;
}
</style>
