<template>
  <div class="user-center-container">
    <UserHeader :user="user" @edit="showEditDialog" />
    <UserStats :user="user" />
    
    <div class="user-tabs-section">
      <a-tabs v-model:active-key="activeTab" @change="handleTabClick" type="line" justify>
        <a-tab-pane key="favorites">
          <template #title><icon-star /> 我的收藏</template>
          <div class="tab-content-wrapper">
            <ArticleGrid :articles="favoriteArticles" :loading="loadingFavorites" />
            <a-empty v-if="!loadingFavorites && favoriteArticles.length === 0" description="还没有收藏任何灵感，快去首页逛逛吧" style="margin: 40px 0;">
              <template #image><icon-heart style="font-size: 48px; color: #D3C1BA; opacity: 0.5;" /></template>
            </a-empty>
          </div>
        </a-tab-pane>
        
        <a-tab-pane key="orders">
          <template #title><icon-storage /> 我的订单</template>
          <div class="tab-content-wrapper">
            <OrderList :orders="orders" @detail="showOrderDetail" @pay="handleContinuePay" />
          </div>
        </a-tab-pane>
        
        <a-tab-pane key="campaignOrders">
          <template #title><icon-fire /> 我的跟团</template>
          <div class="tab-content-wrapper">
            <CampaignOrderList :orders="campaignOrders" />
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>

    <UserToolList 
      :user="user" 
      @address="showAddressDialog" 
      @invite="showInviteDialog" 
      @groups="showGroupsDialog"
      @logout="handleLogout" 
    />

    <!-- 我的拼团弹窗 -->
    <MyGroupsDialog v-model:show="groupsDialogVisible" />

    <!-- 编辑资料弹窗 -->
    <ProfileEditDialog v-model:show="editDialogVisible" @updated="fetchUser" />

    <!-- 邀请码弹窗 -->
    <a-modal title="我的邀请码" :visible="inviteDialogVisible" :width="isMobile ? '85%' : '400px'" @cancel="inviteDialogVisible = false" :footer="false">
      <div class="invite-dialog-content" v-if="user">
        <div class="invite-box">
          <p class="invite-label">专属邀请码</p>
          <h2 class="invite-code-text">{{ user.inviteCode }}</h2>
        </div>
        <a-button type="primary" shape="round" style="width: 100%" @click="copyInviteLink">复制邀请链接</a-button>
        <p class="invite-tip">每邀请一位好友注册，双方均可获得50积分奖励</p>
      </div>
    </a-modal>

    <!-- 支付状态确认弹窗 -->
    <a-modal 
      v-model:visible="paymentConfirmVisible" 
      title="支付确认"
      :footer="false"
      :mask-closable="false"
      :closable="false"
    >
      <div style="text-align: center; padding: 20px 0;">
        <icon-check-circle style="font-size: 48px; color: #00B42A; margin-bottom: 20px;" />
        <h3 style="margin-bottom: 30px;">请在新打开的页面中完成支付</h3>
        <p style="color: #86909C; margin-bottom: 30px; font-size: 13px;">支付完成前请不要关闭此窗口。完成支付后，请根据您的情况点击下面按钮。</p>
        <div style="display: flex; justify-content: center; gap: 15px;">
          <a-button @click="handlePaymentFail">遇到问题，重新支付</a-button>
          <a-button type="primary" @click="handlePaymentSuccess" style="background-color: #FF7E67;">我已完成支付</a-button>
        </div>
      </div>
    </a-modal>

    <!-- 订单详情弹窗 -->
    <OrderDetailDialog 
      v-model:show="orderDetailVisible" 
      :order="selectedOrder" 
      @pay="handleContinuePay" 
    />
  </div>
</template>

<script>
import { Message } from '@arco-design/web-vue';
import { getMyFavorites } from '@/api/article';
import { getMyOrders, createAlipay } from '@/api/order';
import { getMyCampaignOrders } from '@/api/campaign';
import { getProducts } from '@/api/product';
import UserHeader from '@/components/user/UserHeader.vue';
import UserStats from '@/components/user/UserStats.vue';
import UserToolList from '@/components/user/UserToolList.vue';
import ArticleGrid from '@/components/home/ArticleGrid.vue';
import ProfileEditDialog from '@/components/user/ProfileEditDialog.vue';
import MyGroupsDialog from '@/components/user/MyGroupsDialog.vue';
import OrderDetailDialog from '@/components/user/OrderDetailDialog.vue';
import OrderList from '@/components/user/OrderList.vue';
import CampaignOrderList from '@/components/user/CampaignOrderList.vue';
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'UserProfile',
  components: {
    UserHeader,
    UserStats,
    UserToolList,
    ArticleGrid,
    ProfileEditDialog,
    MyGroupsDialog,
    OrderDetailDialog,
    OrderList,
    CampaignOrderList
  },
  data() {
    return {
      orders: [],
      campaignOrders: [],
      loadingOrders: false,
      editDialogVisible: false,
      inviteDialogVisible: false,
      groupsDialogVisible: false,
      paymentConfirmVisible: false,
      orderDetailVisible: false,
      selectedOrder: null,
      activeTab: 'favorites',
      favoriteArticles: [],
      loadingFavorites: false,
      isMobile: window.innerWidth <= 768
    }
  },
  created() {
    this.loadUserAndForm();
    this.fetchMyFavorites();
    this.fetchMyOrders();
    this.fetchMyCampaignOrders();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo;
    },
    unpaidCount() {
      return this.orders.filter(o => o.status === 0).length;
    }
  },
  methods: {
    ...mapActions(useUserStore, ['clearUser', 'fetchUser']),
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
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
    handleLogout() {
      this.clearUser();
      this.$router.push('/');
      Message.success('已安全退出');
    },
    async loadUserAndForm() {
      // If we don't have userInfo yet, fetch it.
      if (!this.user) {
        await this.fetchUser();
      }
      if (!this.user) {
        this.$router.push('/');
        return;
      }
      this.profileForm = {
        nickname: this.user.nickname,
        avatarUrl: this.user.avatarUrl,
        wechatId: this.user.wechatId || '',
        age: this.user.age || 18,
        gender: this.user.gender || 'OTHER',
        address: this.user.address || ''
      };
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
      try {
        const payRes = await createAlipay(order.id);
        const formHtml = payRes.data.data;
        
        // 业界标准做法：打开新窗口并将支付宝返回的 HTML 表单直接写入新窗口
        // 这样不仅避免了污染当前 SPA 也就是 Vue 的 DOM (document.body)，
        // 而且可以原生地执行支付宝 HTML 中自带的自动提交 script 标签。
        const newWindow = window.open('', '_blank');
        if (newWindow) {
          newWindow.document.write(formHtml);
          newWindow.document.close();
          this.paymentConfirmVisible = true;
        } else {
          Message.warning('支付页面被浏览器拦截，请在地址栏右侧允许弹出窗口');
        }
      } catch (error) {
        Message.error(error.response?.data?.message || '获取支付链接失败');
      }
    },
    handlePaymentSuccess() {
      this.paymentConfirmVisible = false;
      this.fetchMyOrders();
      Message.success('订单已刷新，请查看最新状态');
    },
    handlePaymentFail() {
      this.paymentConfirmVisible = false;
      Message.info('您可以稍后再次尝试支付');
    },
    copyInviteLink() {
      const baseUrl = window.location.origin;
      const link = `${baseUrl}/?invite=${this.user.inviteCode}`;
      
      const input = document.createElement('input');
      input.value = link;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      
      Message.success('邀请链接已复制到剪贴板，快去发给好友吧！');
    }
  }
}
</script>

<style scoped>
.user-center-container {
  min-height: 100vh;
  padding-bottom: 80px;
}

.user-tabs-section {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255,255,255,0.8);
  border-bottom: 1px solid rgba(0,0,0,0.03);
  margin: 20px 0;
  padding: 10px 15px;
}

:deep(.arco-tabs-nav::before) {
  background-color: transparent !important;
}

:deep(.arco-tabs-tab-active) {
  color: var(--brand-primary, #FF4B2B) !important;
  font-weight: 800 !important;
}

:deep(.arco-tabs-nav-ink) {
  background-color: var(--brand-primary, #FF4B2B) !important;
  height: 3px !important;
  border-radius: 2px;
}

.tab-content-wrapper {
  padding: 15px 0;
  min-height: 200px;
}

/* Invite Dialog Styles */
.invite-dialog-content {
  text-align: center;
}
.invite-box {
  background: #FDF0E6;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.invite-label {
  font-size: 12px;
  color: #8C6A5D;
  margin-bottom: 8px;
}
.invite-code-text {
  font-size: 32px;
  color: #FF7E67;
  letter-spacing: 2px;
  margin: 0;
}
.invite-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 15px;
}
</style>
