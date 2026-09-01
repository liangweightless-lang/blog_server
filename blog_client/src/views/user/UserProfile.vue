<template>
  <div class="user-center-container">
    <!-- 主理人/个人轻奢头部卡片 -->
    <UserHeader 
      :user="user" 
      :creator-status="creatorStatus" 
      @edit="showEditDialog" 
      @apply-creator="applyDialogVisible = true" 
    />
    <UserStats :user="user" />
    
    <!-- 极简高定微胶囊 Tabs -->
    <div class="user-tabs-section">
      <a-tabs v-model:active-key="activeTab" @change="handleTabClick" type="line" justify>
        <a-tab-pane key="favorites">
          <template #title><icon-heart /> 我的收藏</template>
          <div class="tab-content-wrapper">
            <ArticleGrid :articles="favoriteArticles" :loading="loadingFavorites" />
            <a-empty v-if="!loadingFavorites && favoriteArticles.length === 0" description="还没有收藏任何灵感手记，去首页发现美好吧" style="margin: 40px 0;">
              <template #image><icon-heart style="font-size: 44px; color: #D3C1BA; opacity: 0.4;" /></template>
            </a-empty>
          </div>
        </a-tab-pane>
        
        <a-tab-pane key="orders">
          <template #title><icon-gift /> 我的订单</template>
          <div class="tab-content-wrapper">
            <OrderList :orders="orders" @detail="showOrderDetail" @pay="handleContinuePay" />
          </div>
        </a-tab-pane>
        
        <a-tab-pane key="campaignOrders">
          <template #title><icon-fire /> 我的跟团</template>
          <div class="tab-content-wrapper">
            <CampaignOrderList :orders="campaignOrders" @pay="handleContinuePay" />
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>

    <UserToolList 
      :user="user" 
      @address="showAddressDialog" 
      @invite="showInviteDialog" 
      @groups="showGroupsDialog"
      @apply-creator="applyDialogVisible = true"
      @logout="handleLogout" 
    />

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
    CampaignOrderList,
    CreatorApplyDialog,
    WechatPayQrModal
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
      creatorStatus: null,
      orderDetailVisible: false,
      selectedOrder: null,
      activeTab: 'favorites',
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
    this.loadUserAndForm();
    this.fetchMyFavorites();
    this.fetchMyOrders();
    this.fetchMyCampaignOrders();
    this.fetchCreatorStatus();
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
    pendingOrdersCount() {
      return this.orders.filter(o => o.status === 0).length;
    }
  },
  methods: {
    ...mapActions(useUserStore, ['clearUser', 'fetchUser']),
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
    handleLogout() {
      this.clearUser();
      this.$router.push('/');
      Message.success('已安全退出');
    },
    async loadUserAndForm() {
      if (!this.user) {
        await this.fetchUser();
      }
      if (!this.user) {
        this.$router.push('/');
        return;
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
  .user-tabs-section {
    padding: 6px 10px;
    border-radius: 16px;
    margin-top: 10px;
    margin-bottom: 10px;
  }
}
</style>
