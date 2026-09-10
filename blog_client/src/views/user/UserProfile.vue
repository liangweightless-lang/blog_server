<template>
  <div class="user-center-container">
    <PullToRefresh @refresh="handlePullRefresh">
      <!-- 主理人/个人轻奢头部卡片 -->
      <UserHeader 
        :user="user" 
        :creator-status="creatorStatus" 
        @edit="showEditDialog" 
        @apply-creator="applyDialogVisible = true" 
      />
      <UserStats :user="user" />
      
      <!-- 订单状态流向微看板 -->
      <div class="order-flow-card">
        <div class="flow-header">
          <span class="flow-title">我的交易</span>
          <span class="flow-all-link" @click="handleFlowClick('allOrders')">
            全部订单 <icon-right />
          </span>
        </div>
        <div class="flow-nav-grid">
          <div 
            class="flow-item" 
            :class="{ 'is-active': activeTab === 'orders' && orderFilterStatus === 'pendingPay' }"
            @click="handleFlowClick('pendingPay')"
          >
            <div class="flow-icon-wrap">
              <icon-clock-circle class="flow-icon" />
              <span class="flow-badge" v-if="user && pendingPayCount > 0">{{ pendingPayCount }}</span>
            </div>
            <span class="flow-label">待付款</span>
          </div>
          <div 
            class="flow-item" 
            :class="{ 'is-active': activeTab === 'orders' && orderFilterStatus === 'pendingPickup' }"
            @click="handleFlowClick('pendingPickup')"
          >
            <div class="flow-icon-wrap">
              <icon-storage class="flow-icon" />
              <span class="flow-badge" v-if="user && pendingPickupCount > 0">{{ pendingPickupCount }}</span>
            </div>
            <span class="flow-label">待提货/发货</span>
          </div>
          <div 
            class="flow-item" 
            :class="{ 'is-active': activeTab === 'campaignOrders' }"
            @click="handleFlowClick('campaigns')"
          >
            <div class="flow-icon-wrap">
              <icon-fire class="flow-icon" />
              <span class="flow-badge highlight" v-if="user && campaignOrders.length > 0">{{ campaignOrders.length }}</span>
            </div>
            <span class="flow-label">我的跟团</span>
          </div>
          <div 
            class="flow-item" 
            :class="{ 'is-active': activeTab === 'orders' && orderFilterStatus === 'all' }"
            @click="handleFlowClick('allOrders')"
          >
            <div class="flow-icon-wrap">
              <icon-ordered-list class="flow-icon" />
            </div>
            <span class="flow-label">历史订单</span>
          </div>
        </div>
      </div>

      <!-- 极简高定微胶囊 Tabs -->
      <div class="user-tabs-section">
        <!-- 未登录访客态占位引导 -->
        <div v-if="!user" class="guest-tab-placeholder">
          <div class="guest-tab-icon-wrap">
            <icon-gift class="guest-tab-icon" />
          </div>
          <h3 class="guest-tab-title">登录后查看我的订单与跟团</h3>
          <p class="guest-tab-desc">随时查看自营现烤出炉、拼团进度与提货码</p>
          <button class="guest-tab-btn" @click="handleOpenLogin">
            立即登录 / 注册
          </button>
        </div>

        <a-tabs v-else v-model:active-key="activeTab" @change="handleTabClick" type="line" justify>
          <a-tab-pane key="orders">
            <template #title>
              <icon-gift /> 我的订单
              <span class="tab-badge" v-if="filteredOrders.length > 0">{{ filteredOrders.length }}</span>
            </template>
            <div class="tab-content-wrapper">
              <!-- 订单状态微胶囊横滑筛选栏 -->
              <div class="order-filter-bar" v-if="orders && orders.length > 0">
                <button 
                  class="filter-pill" 
                  :class="{ active: orderFilterStatus === 'all' }"
                  @click="orderFilterStatus = 'all'"
                >全部 ({{ orders.length }})</button>
                <button 
                  class="filter-pill" 
                  :class="{ active: orderFilterStatus === 'pendingPay' }"
                  @click="orderFilterStatus = 'pendingPay'"
                >待付款 ({{ pendingPayCountNormal }})</button>
                <button 
                  class="filter-pill" 
                  :class="{ active: orderFilterStatus === 'pendingPickup' }"
                  @click="orderFilterStatus = 'pendingPickup'"
                >待提货/发货 ({{ pendingPickupCountNormal }})</button>
                <button 
                  class="filter-pill" 
                  :class="{ active: orderFilterStatus === 'completed' }"
                  @click="orderFilterStatus = 'completed'"
                >已完成 ({{ completedCountNormal }})</button>
              </div>
              <OrderList 
                :orders="filteredOrders" 
                :empty-text="orderFilterStatus === 'pendingPickup' ? '暂无待提货/发货订单' : (orderFilterStatus === 'pendingPay' ? '暂无待付款订单' : (orderFilterStatus === 'completed' ? '暂无已完成订单' : '暂无订单记录'))"
                @detail="showOrderDetail" 
                @pay="handleContinuePay" 
                @refresh="fetchOrders" 
              />
            </div>
          </a-tab-pane>
          
          <a-tab-pane key="campaignOrders">
            <template #title>
              <icon-fire /> 我的跟团
              <span class="tab-badge" v-if="campaignOrders.length > 0">{{ campaignOrders.length }}</span>
            </template>
            <div class="tab-content-wrapper">
              <CampaignOrderList :orders="campaignOrders" @pay="handleContinuePay" @refresh="fetchOrders" />
            </div>
          </a-tab-pane>

          <a-tab-pane key="favorites">
            <template #title><icon-heart /> 灵感收藏</template>
            <div class="tab-content-wrapper">
              <ArticleGrid :articles="favoriteArticles" :loading="loadingFavorites" />
              <a-empty v-if="!loadingFavorites && favoriteArticles.length === 0" description="还没有收藏任何灵感手记，去首页发现美好吧" style="margin: 40px 0;">
                <template #image><icon-heart style="font-size: 44px; color: #D3C1BA; opacity: 0.4;" /></template>
              </a-empty>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <UserToolList 
        :user="user" 
        @address="showAddressDialog" 
        @invite="showInviteDialog" 
        @groups="showGroupsDialog"
        @contact="contactDialogVisible = true"
        @apply-creator="applyDialogVisible = true"
        @logout="handleLogout" 
      />
    </PullToRefresh>

    <!-- 联系主理人/客服微信弹窗 (标准居中抽屉) -->
    <ContactConciergeModal v-model:show="contactDialogVisible" />

    <!-- 全自动对账收银台 (彻底解决浏览器拦截弹窗问题) -->

    <WechatPayQrModal 
      :show="wechatQrVisible"
      :order-id="payOrderId"
      :amount="payAmount"
      :code-url="wechatCodeUrl"
      @update:show="val => wechatQrVisible = val"
      @success="handlePaySuccess"
    />

    <!-- 主理人入驻申请弹窗 -->
    <CreatorApplyDialog v-model:show="applyDialogVisible" @success="handleApplySuccess" />

    <!-- 我的拼团弹窗 -->
    <MyGroupsDialog v-model:show="groupsDialogVisible" />

    <!-- 编辑资料弹窗 -->
    <ProfileEditDialog v-model:show="editDialogVisible" @updated="fetchUser" />

    <!-- 专属邀请码高定抽屉 (标准 AppBottomSheet) -->
    <AppBottomSheet
      v-model:visible="inviteDialogVisible"
      title="我的专属邀请码"
      subtitle="每邀请一位好友注册，双方均可获得 50 积分奖励"
      width="420px"
    >
      <div class="invite-dialog-content" v-if="user">
        <div class="invite-box">
          <span class="invite-label">专属邀请码</span>
          <h2 class="invite-code-text">{{ user.inviteCode }}</h2>
        </div>
      </div>
      <template #footer>
        <button class="sheet-main-btn" @click="copyInviteLink">
          <icon-copy /> <span>一键复制邀请链接</span>
        </button>
      </template>
    </AppBottomSheet>

    <!-- 订单详情弹窗 -->
    <OrderDetailDialog 
      v-model:show="orderDetailVisible" 
      :order="selectedOrder" 
      @pay="handleContinuePay" 
      @refresh="fetchOrders"
    />
  </div>
</template>

<script>
import { Message } from '@arco-design/web-vue';
import { getMyFavorites } from '@/api/article';
import { getMyOrders, createXunhupay, createWechatPay } from '@/api/order';
import { getMyCampaignOrders } from '@/api/campaign';
import { getProducts } from '@/api/product';
import { getMyCreatorStatus } from '@/api/creator';
import UserHeader from '@/components/user/UserHeader.vue';
import UserStats from '@/components/user/UserStats.vue';
import UserToolList from '@/components/user/UserToolList.vue';
import ArticleGrid from '@/components/home/ArticleGrid.vue';
import ProfileEditDialog from '@/components/user/ProfileEditDialog.vue';
import MyGroupsDialog from '@/components/user/MyGroupsDialog.vue';
import OrderDetailDialog from '@/components/user/OrderDetailDialog.vue';
import OrderList from '@/components/user/OrderList.vue';
import CampaignOrderList from '@/components/user/CampaignOrderList.vue';
import CreatorApplyDialog from '@/components/user/CreatorApplyDialog.vue';
import WechatPayQrModal from '@/components/pay/WechatPayQrModal.vue';
import ContactConciergeModal from '@/components/common/ContactConciergeModal.vue';
import PullToRefresh from '@/components/common/PullToRefresh.vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'UserProfile',
  components: {
    PullToRefresh,
    UserHeader,
    UserStats,
    UserToolList,
    ArticleGrid,
    ProfileEditDialog,
    MyGroupsDialog,
    OrderDetailDialog,
    OrderList,
    CampaignOrderList,
    CreatorApplyDialog,
    WechatPayQrModal,
    ContactConciergeModal
  },
  data() {
    return {
      orders: [],
      campaignOrders: [],
      loadingOrders: false,
      editDialogVisible: false,
      inviteDialogVisible: false,
      groupsDialogVisible: false,
      applyDialogVisible: false,
      contactDialogVisible: false,
      creatorStatus: null,
      orderDetailVisible: false,
      selectedOrder: null,
      activeTab: 'orders',
      orderFilterStatus: 'all', // 'all', 'pendingPay', 'pendingPickup', 'completed'
      favoriteArticles: [],
      loadingFavorites: false,
      isMobile: window.innerWidth <= 768,
      wechatQrVisible: false,
      payOrderId: '',
      payAmount: '0.00',
      wechatCodeUrl: ''
    }
  },
  created() {
    if (this.$route.query.tab) {
      this.activeTab = this.$route.query.tab;
    }
    const token = localStorage.getItem('token');
    if (token) {
      this.loadUserAndForm();
      this.fetchMyOrders();
      this.fetchMyCampaignOrders();
      this.fetchCreatorStatus();
      if (this.activeTab === 'favorites') {
        this.fetchMyFavorites();
      }
    }
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('tab-refresh', this.handleTabRefresh);
    window.addEventListener('auth-success', this.handleAuthSuccess);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('tab-refresh', this.handleTabRefresh);
    window.removeEventListener('auth-success', this.handleAuthSuccess);
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo;
    },
    pendingOrdersCount() {
      return (this.orders || []).filter(o => o.status === 0).length;
    },
    pendingPayCount() {
      const normal = (this.orders || []).filter(o => o.status === 0).length;
      const campaign = (this.campaignOrders || []).filter(o => o.orderStatus === 0).length;
      return normal + campaign;
    },
    pendingPickupCount() {
      const normal = (this.orders || []).filter(o => o.status === 1).length;
      const campaign = (this.campaignOrders || []).filter(o => o.orderStatus === 1).length;
      return normal + campaign;
    },
    filteredOrders() {
      if (!this.orders || this.orders.length === 0) return [];
      if (this.orderFilterStatus === 'pendingPay') {
        return this.orders.filter(o => o.status === 0);
      }
      if (this.orderFilterStatus === 'pendingPickup') {
        return this.orders.filter(o => o.status === 1);
      }
      if (this.orderFilterStatus === 'completed') {
        return this.orders.filter(o => o.status === 2);
      }
      return this.orders;
    },
    pendingPayCountNormal() {
      return (this.orders || []).filter(o => o.status === 0).length;
    },
    pendingPickupCountNormal() {
      return (this.orders || []).filter(o => o.status === 1).length;
    },
    completedCountNormal() {
      return (this.orders || []).filter(o => o.status === 2).length;
    }
  },
  methods: {
    ...mapActions(useUserStore, ['clearUser', 'fetchUser']),
    handleOpenLogin() {
      window.dispatchEvent(new CustomEvent('open-login'));
    },
    handleAuthSuccess() {
      this.loadUserAndForm();
      this.fetchCreatorStatus();
      this.fetchMyOrders();
      this.fetchMyCampaignOrders();
      if (this.activeTab === 'favorites') {
        this.fetchMyFavorites();
      }
    },
    handleFlowClick(type) {
      if (!this.user) {
        this.handleOpenLogin();
        return;
      }
      if (type === 'campaigns') {
        this.activeTab = 'campaignOrders';
        this.fetchMyCampaignOrders();
      } else {
        this.activeTab = 'orders';
        if (type === 'pendingPay') {
          this.orderFilterStatus = 'pendingPay';
        } else if (type === 'pendingPickup') {
          this.orderFilterStatus = 'pendingPickup';
        } else {
          this.orderFilterStatus = 'all';
        }
        this.fetchMyOrders();
      }
      this.$nextTick(() => {
        const tabsEl = document.querySelector('.user-tabs-section');
        if (tabsEl) {
          tabsEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    },
    async handlePullRefresh() {
      const token = localStorage.getItem('token');
      if (!token) {
        Message.info('当前为访客模式，登录后查看个人订单与资产');
        return;
      }
      await Promise.allSettled([
        this.fetchUser(),
        this.fetchCreatorStatus(),
        this.fetchMyFavorites(),
        this.fetchMyOrders(),
        this.fetchMyCampaignOrders()
      ]);
      Message.success('个人中心数据已刷新');
    },
    handleTabRefresh(e) {
      const token = localStorage.getItem('token');
      if (!token) return;
      if (e.detail?.path === '/profile' || this.$route.path === '/profile') {
        this.fetchUser();
        this.fetchMyFavorites();
        this.fetchMyOrders();
        this.fetchMyCampaignOrders();
      }
    },
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    async fetchCreatorStatus() {
      const token = localStorage.getItem('token');
      if (!token) return;
      try {
        const res = await getMyCreatorStatus();
        this.creatorStatus = res.data.data;
      } catch (e) {
        // ignore
      }
    },
    handleApplySuccess() {
      this.fetchCreatorStatus();
      this.fetchUser();
    },
    handleTabClick(key) {
      if (key === 'favorites') {
        this.fetchMyFavorites();
      } else if (key === 'orders') {
        this.fetchMyOrders();
      } else if (key === 'campaignOrders') {
        this.fetchMyCampaignOrders();
      }
    },
    async fetchMyFavorites() {
      if (!this.user) return;
      this.loadingFavorites = true;
      try {
        const res = await getMyFavorites();
        this.favoriteArticles = res.data.data || [];
      } catch (error) {
        console.error('获取收藏失败');
      } finally {
        this.loadingFavorites = false;
      }
    },
    showEditDialog() {
      this.editDialogVisible = true;
    },
    showAddressDialog() {
      this.editDialogVisible = true;
    },
    showInviteDialog() {
      this.inviteDialogVisible = true;
    },
    showGroupsDialog() {
      this.groupsDialogVisible = true;
    },
    fetchOrders() {
      this.fetchMyOrders();
      this.fetchMyCampaignOrders();
    },
    handleLogout() {
      this.clearUser();
      this.$router.push('/');
      Message.success('已安全退出');
    },
    async loadUserAndForm() {
      if (!this.user) {
        await this.fetchUser();
      }
    },
    async fetchMyOrders() {
      if (!this.user) return;
      this.loadingOrders = true;
      try {
        const [orderRes, prodRes] = await Promise.all([
          getMyOrders(),
          getProducts().catch(() => ({ data: { data: [] } }))
        ]);
        
        const products = prodRes.data.data || [];
        const productMap = {};
        products.forEach(p => productMap[p.id] = p);
        
        const rawOrders = orderRes.data.data || [];
        this.orders = rawOrders.map(order => ({
          ...order,
          productName: productMap[order.productId]?.name || '',
          productImage: productMap[order.productId]?.image || ''
        }));
      } catch (error) {
        console.error('加载订单失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    async fetchMyCampaignOrders() {
      if (!this.user) return;
      try {
        const res = await getMyCampaignOrders();
        this.campaignOrders = res.data.data || [];
      } catch (error) {
        console.error('加载跟团记录失败', error);
      }
    },
    showOrderDetail(order) {
      this.selectedOrder = order;
      this.orderDetailVisible = true;
    },
    async handleContinuePay(order) {
      if (!this.user) return Message.warning('请先登录');
      this.payOrderId = String(order.id);
      this.payAmount = String(order.amount || order.totalAmount || '0.00');

      try {
        const xunhuRes = await createXunhupay(order.id, 'wechat');
        const payData = xunhuRes.data.data;
        this.wechatCodeUrl = payData.qrUrl || payData.payUrl || '';
      } catch (e) {
        try {
          const payRes = await createWechatPay(order.id);
          const payData = payRes.data.data;
          this.wechatCodeUrl = payData.code_url || payData.h5_url || '';
        } catch (err) {
          // ignore
        }
      }
      // 弹出全自动收银台，绝无任何浏览器跳转拦截！
      this.wechatQrVisible = true;
    },
    handlePaySuccess() {
      this.fetchMyOrders();
      this.fetchMyCampaignOrders();
      Message.success('支付完成，订单状态已自动更新！');
    },
    copyInviteLink() {
      const baseUrl = window.location.origin;
      const link = `${baseUrl}/?invite=${this.user.inviteCode}`;
      
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(link).then(() => {
          Message.success('邀请链接已复制到剪贴板');
        }).catch(() => {
          this.fallbackCopy(link);
        });
      } else {
        this.fallbackCopy(link);
      }
      this.inviteDialogVisible = false;
    },
    fallbackCopy(text) {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-9999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        document.execCommand('copy');
        Message.success('邀请链接已复制');
      } catch (err) {
        Message.warning('复制失败，请长按手动复制');
      }
      document.body.removeChild(textArea);
    }
  }
}
</script>

<style scoped>
.user-center-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 10px 16px 100px;
}

.user-tabs-section {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 8px 14px;
  margin-top: 14px;
  margin-bottom: 14px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.tab-content-wrapper {
  padding-top: 12px;
  min-height: 200px;
}

/* 订单状态流向微看板 */
.order-flow-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 18px;
  margin-top: 14px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.flow-title {
  font-size: 14px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.2px;
}

.flow-all-link {
  font-size: 12px;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  transition: color 0.2s;
}

.flow-all-link:hover {
  color: #FF5E3A;
}

.flow-nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 4px;
  border-radius: 12px;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.flow-item:active {
  transform: scale(0.94);
  background: #F7F8FA;
}

.flow-icon-wrap {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #F7F8FA;
  display: flex;
  align-items: center;
  justify-content: center;
}

.flow-icon {
  font-size: 20px;
  color: #272E3B;
}

.flow-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #FF5E3A;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #FFFFFF;
}

.flow-badge.highlight {
  background: #FF2A6D;
}

.flow-label {
  font-size: 12px;
  font-weight: 600;
  color: #4E5969;
}

/* 看板选中高亮态 */
.flow-item.is-active .flow-icon-wrap {
  background: linear-gradient(135deg, #FFF2EE 0%, #FFE5DF 100%);
  box-shadow: 0 4px 12px rgba(255, 94, 58, 0.16);
}
.flow-item.is-active .flow-icon {
  color: #FF5E3A;
  transform: scale(1.08);
}
.flow-item.is-active .flow-label {
  color: #FF5E3A;
  font-weight: 800;
}

/* 订单筛选微胶囊横滑栏 */
.order-filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 0 14px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}
.order-filter-bar::-webkit-scrollbar {
  display: none;
}
.filter-pill {
  flex-shrink: 0;
  border: 1px solid #F0F2F5;
  background: #F7F8FA;
  color: #4E5969;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.filter-pill.active {
  background: linear-gradient(135deg, #FF693B 0%, #FF3D57 100%);
  color: #FFFFFF;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(255, 61, 87, 0.25);
  transform: scale(1.02);
}
.filter-pill:active {
  transform: scale(0.96);
}

.tab-badge {
  display: inline-block;
  background: rgba(255, 94, 58, 0.1);
  color: #FF5E3A;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: 4px;
}

/* 访客态微胶囊占位卡片 */
.guest-tab-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.guest-tab-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFF0ED 0%, #FFE4DD 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.guest-tab-icon {
  font-size: 28px;
  color: #FF5E3A;
}

.guest-tab-title {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 800;
  color: #1D2129;
}

.guest-tab-desc {
  margin: 0 0 18px 0;
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;
}

.guest-tab-btn {
  background: linear-gradient(135deg, #FF693B 0%, #FF3D57 100%);
  color: #FFFFFF;
  border: none;
  padding: 9px 22px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(255, 61, 87, 0.28);
  transition: all 0.2s ease;
}

.guest-tab-btn:active {
  transform: scale(0.96);
}

.invite-dialog-content {
  text-align: center;
  padding: 10px 0;
}
.invite-box {
  background: #F2F3F5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}
.invite-label {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #86909C;
}
.invite-code-text {
  margin: 0;
  font-size: 26px;
  color: #FF5E3A;
  letter-spacing: 2px;
  font-family: monospace;
}
.invite-tip {
  margin: 12px 0 0 0;
  font-size: 12px;
  color: #86909C;
}

@media (max-width: 768px) {
  .user-center-container {
    padding: 6px 12px 100px;
  }
  .order-flow-card {
    padding: 14px 12px;
    border-radius: 16px;
    margin-top: 10px;
  }
  .flow-icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 12px;
  }
  .flow-icon {
    font-size: 18px;
  }
  .user-tabs-section {
    padding: 6px 10px;
    border-radius: 16px;
    margin-top: 10px;
    margin-bottom: 10px;
  }
}
</style>
