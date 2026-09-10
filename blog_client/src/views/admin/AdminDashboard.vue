<template>
  <div class="admin-root-container">
    <!-- PC 端传统侧边栏后台布局 -->
    <a-layout class="admin-layout" v-if="!isMobile">
      <a-layout-sider 
        breakpoint="lg" 
        :width="220"
        collapsible
        :collapsed="collapsed"
        @collapse="onCollapse"
        class="admin-sider"
      >
        <div class="admin-logo">
          <h2 v-if="!collapsed">{{ isSuperAdmin ? 'WTLS 超级控制台' : '柴柴主理人工作台' }}</h2>
          <h2 v-else>W</h2>
        </div>
        <a-menu
          v-model:selected-keys="selectedKeys"
          :style="{ width: '100%' }"
          @menu-item-click="handleMenuClick"
        >
          <a-menu-item key="articles">
            <template #icon><icon-file /></template>
            文章日记
          </a-menu-item>
          <a-menu-item key="products">
            <template #icon><icon-storage /></template>
            商品库
          </a-menu-item>
          <a-menu-item key="orders">
            <template #icon><icon-calendar /></template>
            订单管理
          </a-menu-item>
          <a-menu-item key="groupbuys">
            <template #icon><icon-user-add /></template>
            拼团管理(单品)
          </a-menu-item>
          <a-menu-item key="campaigns">
            <template #icon><icon-tags /></template>
            社区快团
          </a-menu-item>

          <!-- 超级管理员专属权限菜单项 -->
          <a-menu-item v-if="isSuperAdmin" key="creators">
            <template #icon><icon-star /></template>
            主理人审核
          </a-menu-item>
          <a-menu-item v-if="isSuperAdmin" key="users">
            <template #icon><icon-user-group /></template>
            用户管理
          </a-menu-item>
          <a-menu-item v-if="isSuperAdmin" key="system">
            <template #icon><icon-settings /></template>
            系统配置
          </a-menu-item>
        </a-menu>
      </a-layout-sider>

      <a-layout class="admin-main-body">
        <a-layout-header class="admin-header">
          <div class="header-left">
            <a-button type="text" @click="collapsed = !collapsed">
              <template #icon>
                <icon-menu-fold v-if="!collapsed" />
                <icon-menu-unfold v-else />
              </template>
            </a-button>
            <span class="header-title">{{ currentTitle }}</span>
            <span class="header-identity-pill" :class="{ 'pill-super': isSuperAdmin }">
              {{ isSuperAdmin ? '超级管理员' : '认证主理人' }}
            </span>
          </div>
          <div class="header-right">
            <a-button type="text" @click="$router.push('/')">
              <template #icon><icon-home /></template> 返回前台
            </a-button>
          </div>
        </a-layout-header>
        
        <a-layout-content class="admin-content">
          <div class="content-wrapper">
            <ArticleManager v-if="activeTab === 'articles'" :isMobile="false" />
            <ProductManager v-if="activeTab === 'products'" :isMobile="false" />
            <OrderManager v-if="activeTab === 'orders'" :isMobile="false" :initialStatus="orderInitialStatus" />
            <GroupbuyManager v-if="activeTab === 'groupbuys'" :isMobile="false" />
            <CampaignManager v-if="activeTab === 'campaigns'" :isMobile="false" />
            <CreatorManager v-if="activeTab === 'creators' && isSuperAdmin" :isMobile="false" />
            <UserManager v-if="activeTab === 'users' && isSuperAdmin" :isMobile="false" />
            <SystemConfig v-if="activeTab === 'system' && isSuperAdmin" :isMobile="false" />
          </div>
        </a-layout-content>
      </a-layout>
    </a-layout>

    <!-- 移动端专属：2026 Apple / iOS 18 级现代极简工作台应用网格 -->
    <div class="mobile-workbench-container" v-else>
      <!-- 顶部工作台导航栏 -->
      <div class="workbench-nav-bar">
        <div class="nav-left-slot">
          <button v-if="mobileActiveModule" class="nav-back-workbench-btn" @click="mobileActiveModule = null">
            <icon-left class="back-arrow-icon" /> <span>工作台</span>
          </button>
          <button v-else class="nav-back-profile-btn" @click="$router.push('/profile')" title="返回我的">
            <icon-left class="back-arrow-icon" /> <span>我的</span>
          </button>
        </div>
        
        <div class="nav-center-title">
          <span v-if="mobileActiveModule">{{ currentTitle }}</span>
          <div v-else class="title-with-badge">
            <span class="main-head-text">{{ isSuperAdmin ? '超级控制台' : '主理人工作台' }}</span>
            <span class="role-badge-tag" :class="{ 'is-admin': isSuperAdmin }">
              {{ isSuperAdmin ? 'ADMIN' : 'CREATOR' }}
            </span>
          </div>
        </div>

        <div class="nav-right-slot">
          <button class="nav-action-btn" @click="$router.push('/')" title="前台首页">
            <icon-export />
          </button>
        </div>
      </div>

      <!-- 1. 工作台主面板：经营核心看板 + 分组金刚区高定微光图标网格 -->
      <div class="workbench-grid-view" v-if="!mobileActiveModule">
        <!-- 【今日经营微看板】 (深色轻奢黑金卡片，高频备料核销核心) -->
        <div class="today-ops-card">
          <div class="ops-card-header">
            <div class="ops-title-group">
              <span class="ops-dot-pulsing"></span>
              <span class="ops-title-text">今日经营核心</span>
              <button 
                class="ops-refresh-btn" 
                :class="{ 'is-refreshing': loadingMetrics }" 
                @click="fetchWorkbenchMetrics" 
                title="刷新今日数据"
              >
                <icon-refresh />
              </button>
            </div>
            
            <!-- 快速提货核销入口 -->
            <button class="quick-verify-btn" @click="openVerifyModal">
              <icon-scan class="verify-scan-icon" />
              <span>提货核销</span>
            </button>
          </div>

          <!-- 4 项经营指标看板网格 -->
          <div class="ops-metrics-grid">
            <!-- 指标 1：待自提 / 待出炉备料（点击直达待发货列表） -->
            <div class="metric-block is-primary-metric" @click="openModule('orders', 1)">
              <div class="metric-top-label">
                <span>待提货 / 待出炉</span>
                <icon-right class="metric-arrow" />
              </div>
              <div class="metric-value-row">
                <span class="metric-number-highlight">{{ metrics.pendingShip }}</span>
                <span class="metric-unit">单</span>
              </div>
              <div class="metric-footer-badge">
                <span class="badge-fire-dot"></span>
                <span>核心备料</span>
              </div>
            </div>

            <!-- 指标 2：今日营业额 -->
            <div class="metric-block">
              <div class="metric-top-label">
                <span>今日营业额</span>
              </div>
              <div class="metric-value-row">
                <span class="metric-currency-symbol">¥</span>
                <span class="metric-number-standard">{{ metrics.todayIncome.toFixed(2) }}</span>
              </div>
              <div class="metric-footer-note">
                今日已付入账
              </div>
            </div>

            <!-- 指标 3：今日订单笔数 -->
            <div class="metric-block" @click="openModule('orders', 'ALL')">
              <div class="metric-top-label">
                <span>今日成交单数</span>
                <icon-right class="metric-arrow" />
              </div>
              <div class="metric-value-row">
                <span class="metric-number-standard">{{ metrics.todayOrders }}</span>
                <span class="metric-unit">笔</span>
              </div>
              <div class="metric-footer-note">
                全渠道新订单
              </div>
            </div>

            <!-- 指标 4：进行中快团（点击直达快团管理） -->
            <div class="metric-block" @click="openModule('campaigns')">
              <div class="metric-top-label">
                <span>进行中快团</span>
                <icon-right class="metric-arrow" />
              </div>
              <div class="metric-value-row">
                <span class="metric-number-standard">{{ metrics.activeCampaigns }}</span>
                <span class="metric-unit">场</span>
              </div>
              <div class="metric-footer-note">
                正在开团截单中
              </div>
            </div>
          </div>
        </div>

        <!-- 分组 1：日常运营 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-pill-indicator bg-indicator-blue"></span>
            <span class="group-title-text">日常运营</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('articles')">
              <div class="app-icon-squircle icon-blue-frost">
                <div class="icon-inner-gloss"></div>
                <icon-file class="app-vector-symbol" />
              </div>
              <span class="app-name">文章日记</span>
            </div>

            <div class="app-item-card" @click="openModule('products')">
              <div class="app-icon-squircle icon-cyan-frost">
                <div class="icon-inner-gloss"></div>
                <icon-storage class="app-vector-symbol" />
              </div>
              <span class="app-name">商品管理</span>
            </div>

            <!-- 系统配置仅超管可见 -->
            <div v-if="isSuperAdmin" class="app-item-card" @click="openModule('system')">
              <div class="app-icon-squircle icon-purple-frost">
                <div class="icon-inner-gloss"></div>
                <icon-settings class="app-vector-symbol" />
              </div>
              <span class="app-name">系统配置</span>
            </div>
          </div>
        </div>

        <!-- 分组 2：交易与履约 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-pill-indicator bg-indicator-orange"></span>
            <span class="group-title-text">交易与履约</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('orders', 'ALL')">
              <div class="app-icon-squircle icon-indigo-frost">
                <div class="icon-inner-gloss"></div>
                <icon-calendar class="app-vector-symbol" />
              </div>
              <span class="app-name">订单管理</span>
            </div>

            <div class="app-item-card" @click="openModule('groupbuys')">
              <div class="app-icon-squircle icon-orange-frost">
                <div class="icon-inner-gloss"></div>
                <icon-user-add class="app-vector-symbol" />
              </div>
              <span class="app-name">单品拼团</span>
            </div>

            <div class="app-item-card" @click="openModule('campaigns')">
              <div class="app-icon-squircle icon-red-frost">
                <div class="icon-inner-gloss"></div>
                <icon-tags class="app-vector-symbol" />
              </div>
              <span class="app-name">社区快团</span>
            </div>
          </div>
        </div>

        <!-- 分组 3：用户与主理人（平台超管专属） -->
        <div v-if="isSuperAdmin" class="app-group-section">
          <div class="group-title-row">
            <span class="group-pill-indicator bg-indicator-gold"></span>
            <span class="group-title-text">平台管理</span>
            <span class="group-admin-pill">ADMIN</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('creators')">
              <div class="app-icon-squircle icon-gold-frost">
                <div class="icon-inner-gloss"></div>
                <icon-star class="app-vector-symbol" />
              </div>
              <span class="app-name">主理人审核</span>
            </div>

            <div class="app-item-card" @click="openModule('users')">
              <div class="app-icon-squircle icon-green-frost">
                <div class="icon-inner-gloss"></div>
                <icon-user-group class="app-vector-symbol" />
              </div>
              <span class="app-name">用户管理</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 子管理模块全屏自适应视图 (单手操作极佳) -->
      <div class="workbench-submodule-view" v-else>
        <ArticleManager v-if="mobileActiveModule === 'articles'" :isMobile="true" />
        <ProductManager v-if="mobileActiveModule === 'products'" :isMobile="true" />
        <OrderManager 
          v-if="mobileActiveModule === 'orders'" 
          :isMobile="true" 
          :initialStatus="orderInitialStatus" 
        />
        <GroupbuyManager v-if="mobileActiveModule === 'groupbuys'" :isMobile="true" />
        <CampaignManager v-if="mobileActiveModule === 'campaigns'" :isMobile="true" />
        <CreatorManager v-if="mobileActiveModule === 'creators' && isSuperAdmin" :isMobile="true" />
        <UserManager v-if="mobileActiveModule === 'users' && isSuperAdmin" :isMobile="true" />
        <SystemConfig v-if="mobileActiveModule === 'system' && isSuperAdmin" :isMobile="true" />
      </div>

      <!-- 3. 提货快捷核销抽屉 (高定 AppBottomSheet) -->
      <AppBottomSheet
        :visible="verifyModalVisible"
        title="提货核销"
        subtitle="核对订单号或提货码，快速一键确认商品交付"
        :confirmText="verifyConfirmText"
        :confirmDisabled="!canConfirmVerify"
        :confirmLoading="verifying"
        @confirm="handleConfirmVerify"
        @cancel="closeVerifyModal"
      >
        <div class="verify-sheet-content">
          <!-- 订单单号输入组 -->
          <div class="verify-input-wrapper">
            <a-input
              v-model="verifyOrderInput"
              placeholder="输入或粘贴订单号 (例如: 1024 或 #1024)"
              allow-clear
              size="large"
              class="verify-mono-input"
              @press-enter="searchOrderForVerify"
            >
              <template #prefix>
                <span class="verify-input-prefix">#</span>
              </template>
            </a-input>
            <button 
              class="verify-search-btn" 
              :disabled="!verifyOrderInput || searchingOrder" 
              @click="searchOrderForVerify"
            >
              <icon-loading v-if="searchingOrder" :spin="true" />
              <span v-else>核验</span>
            </button>
          </div>

          <!-- 核验结果卡片展示 -->
          <div v-if="verifiedOrder" class="verified-order-card" :class="'status-border-' + verifiedOrder.status">
            <div class="verified-card-header">
              <span class="verified-order-id">单号 #{{ verifiedOrder.id }}</span>
              <a-tag :color="getOrderTagColor(verifiedOrder.status)" size="small">
                {{ getOrderTagText(verifiedOrder.status) }}
              </a-tag>
            </div>

            <div class="verified-item-row">
              <img 
                v-if="getProdImage(verifiedOrder.productId)" 
                :src="$formatImageUrl(getProdImage(verifiedOrder.productId))" 
                class="verified-prod-thumb" 
              />
              <div v-else class="verified-thumb-placeholder">
                <icon-storage />
              </div>
              
              <div class="verified-item-info">
                <div class="verified-prod-name">{{ getProdName(verifiedOrder.productId) }}</div>
                <div class="verified-spec-text" v-if="verifiedOrder.selectedSpec">
                  规格: {{ verifiedOrder.selectedSpec }}
                </div>
                <div class="verified-qty-amount">
                  <span class="verified-qty-tag">× {{ verifiedOrder.quantity || 1 }} 件</span>
                  <span class="verified-price-tag">实付 ¥{{ verifiedOrder.amount }}</span>
                </div>
              </div>
            </div>

            <!-- 配送/自提信息与联系方式 -->
            <div class="verified-contact-section">
              <div class="verified-buyer-line">
                <span class="verified-meta-label"><icon-user /> 买家UID:</span>
                <span class="verified-meta-val">{{ verifiedOrder.userId }}</span>
                <a 
                  v-if="verifiedOrder.contactPhone" 
                  :href="'tel:' + verifiedOrder.contactPhone" 
                  class="verified-phone-link"
                >
                  <icon-phone /> {{ verifiedOrder.contactPhone }}
                </a>
              </div>
              <div class="verified-addr-line" v-if="verifiedOrder.shippingAddress">
                <icon-location /> <span>{{ verifiedOrder.shippingAddress }}</span>
              </div>
              <div class="verified-remark-box" v-if="verifiedOrder.remark">
                <icon-message /> 顾客备注: {{ verifiedOrder.remark }}
              </div>
            </div>

            <!-- 状态提示小横幅 -->
            <div class="verified-status-banner status-ready" v-if="verifiedOrder.status === 1">
              <icon-check-circle-fill class="banner-icon" /> 
              <span>订单已付款，可立即交付商品给买家</span>
            </div>
            <div class="verified-status-banner status-done" v-else-if="verifiedOrder.status === 3">
              <icon-info-circle-fill class="banner-icon" /> 
              <span>该订单已于之前交付完成，无需重复核销</span>
            </div>
            <div class="verified-status-banner status-unpaid" v-else-if="verifiedOrder.status === 0">
              <icon-exclamation-circle-fill class="banner-icon" /> 
              <span>订单尚未付款，请确认顾客已在线支付后再交付</span>
            </div>
            <div class="verified-status-banner status-cancelled" v-else-if="verifiedOrder.status === 2">
              <icon-close-circle-fill class="banner-icon" /> 
              <span>该订单已取消，无法核销提货</span>
            </div>
          </div>

          <!-- 空提示或操作指引 -->
          <div v-else-if="hasSearched && !verifiedOrder" class="verified-empty-tip">
            <icon-exclamation-circle class="empty-warn-icon" />
            <p>未检索到匹配的订单，请检查订单号是否正确</p>
          </div>
        </div>
      </AppBottomSheet>
    </div>
  </div>
</template>

<script>
import { mapState } from 'pinia';
import { useUserStore } from '@/stores/user';
import { getOrdersAdmin, shipOrder } from '@/api/order';
import { getCampaigns } from '@/api/campaign';
import { getProducts } from '@/api/product';
import { Message } from '@arco-design/web-vue';

import ArticleManager from '@/components/admin/ArticleManager.vue';
import ProductManager from '@/components/admin/ProductManager.vue';
import UserManager from '@/components/admin/UserManager.vue';
import OrderManager from '@/components/admin/OrderManager.vue';
import GroupbuyManager from '@/components/admin/GroupbuyManager.vue';
import CampaignManager from '@/components/admin/CampaignManager.vue';
import SystemConfig from '@/components/admin/SystemConfig.vue';
import CreatorManager from '@/components/admin/CreatorManager.vue';

export default {
  name: 'AdminDashboard',
  components: {
    ArticleManager,
    ProductManager,
    UserManager,
    OrderManager,
    GroupbuyManager,
    CampaignManager,
    SystemConfig,
    CreatorManager
  },
  data() {
    return {
      collapsed: false,
      activeTab: 'orders',
      selectedKeys: ['orders'],
      mobileActiveModule: null, // 移动端当前打开的具体模块，null表示在主工作台网格
      isMobile: window.innerWidth <= 768,

      // 工作台经营指标数据
      metrics: {
        pendingShip: 0,
        todayIncome: 0,
        todayOrders: 0,
        activeCampaigns: 0
      },
      loadingMetrics: false,
      orderInitialStatus: null,

      // 提货核销抽屉状态
      verifyModalVisible: false,
      verifyOrderInput: '',
      searchingOrder: false,
      verifiedOrder: null,
      verifying: false,
      hasSearched: false,
      cachedOrders: [],
      cachedProducts: []
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    isSuperAdmin() {
      return this.userInfo && this.userInfo.role === 'ADMIN';
    },
    canConfirmVerify() {
      return this.verifiedOrder && this.verifiedOrder.status === 1;
    },
    verifyConfirmText() {
      if (!this.verifiedOrder) return '请输入订单号核验';
      if (this.verifiedOrder.status === 1) return '确认核销并提货';
      if (this.verifiedOrder.status === 3) return '订单已提货完成';
      if (this.verifiedOrder.status === 0) return '订单未支付无法核销';
      return '不可核销';
    },
    currentTitle() {
      const activeKey = this.isMobile ? this.mobileActiveModule : this.activeTab;
      const titleMap = {
        articles: '文章日记管理',
        products: '商品库管理',
        users: '用户管理',
        orders: '订单管理',
        groupbuys: '单品拼团管理',
        campaigns: '社区快团管理',
        creators: '主理人审核',
        system: '系统配置'
      };
      return titleMap[activeKey] || (this.isSuperAdmin ? '超级管理后台' : '主理人工作台');
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('workbench-back', this.handleWorkbenchBack);
    this.handleResize();
    this.fetchWorkbenchMetrics();
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('workbench-back', this.handleWorkbenchBack);
  },
  methods: {
    handleWorkbenchBack() {
      if (this.mobileActiveModule) {
        this.mobileActiveModule = null;
      }
    },
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    onCollapse(val) {
      this.collapsed = val;
    },
    handleMenuClick(key) {
      this.activeTab = key;
    },
    openModule(key, initialStatus = null) {
      this.orderInitialStatus = initialStatus;
      this.mobileActiveModule = key;
    },

    // 加载工作台今日经营指标
    async fetchWorkbenchMetrics() {
      this.loadingMetrics = true;
      try {
        const [ordersRes, campaignsRes, productsRes] = await Promise.all([
          getOrdersAdmin().catch(() => ({ data: { data: [] } })),
          getCampaigns().catch(() => ({ data: { data: [] } })),
          getProducts().catch(() => ({ data: { data: [] } }))
        ]);

        const orders = ordersRes.data?.data || [];
        const campaigns = campaignsRes.data?.data || [];
        this.cachedOrders = orders;
        this.cachedProducts = productsRes.data?.data || [];

        // 计算今天零点
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const todayStartTime = todayStart.getTime();

        let pendingCount = 0;
        let income = 0;
        let todayCount = 0;

        orders.forEach(order => {
          // 待提货/待发货
          if (order.status === 1) {
            pendingCount++;
          }
          // 今日新订单
          const createTime = new Date(order.createTime).getTime();
          if (createTime >= todayStartTime) {
            todayCount++;
            // 今日营业额 (已付款或已交付完成)
            if (order.status === 1 || order.status === 3) {
              income += Number(order.amount) || 0;
            }
          }
        });

        // 进行中的快团
        const activeCount = campaigns.filter(c => c.status === 'ACTIVE' || c.status === 'PUBLISHED' || c.status === 1).length;

        this.metrics = {
          pendingShip: pendingCount,
          todayIncome: income,
          todayOrders: todayCount,
          activeCampaigns: activeCount
        };
      } catch (e) {
        console.error('获取工作台经营指标失败', e);
      } finally {
        this.loadingMetrics = false;
      }
    },

    // 提货核销弹窗控制
    openVerifyModal() {
      this.verifyModalVisible = true;
      this.verifyOrderInput = '';
      this.verifiedOrder = null;
      this.hasSearched = false;
    },
    closeVerifyModal() {
      this.verifyModalVisible = false;
      this.verifyOrderInput = '';
      this.verifiedOrder = null;
      this.hasSearched = false;
    },
    async searchOrderForVerify() {
      if (!this.verifyOrderInput) return;
      const cleanId = String(this.verifyOrderInput).trim().replace(/^#/, '');
      if (!cleanId) return;

      this.searchingOrder = true;
      this.hasSearched = true;
      try {
        // 优先从缓存中寻找
        let order = this.cachedOrders.find(o => String(o.id) === cleanId);
        if (!order) {
          // 实时从后端重拉管理端订单
          const res = await getOrdersAdmin();
          this.cachedOrders = res.data?.data || [];
          order = this.cachedOrders.find(o => String(o.id) === cleanId);
        }
        if (order) {
          this.verifiedOrder = order;
        } else {
          this.verifiedOrder = null;
          Message.warning(`未检索到单号为 #${cleanId} 的订单`);
        }
      } catch (err) {
        Message.error('查询订单信息失败，请稍后重试');
      } finally {
        this.searchingOrder = false;
      }
    },
    async handleConfirmVerify() {
      if (!this.verifiedOrder) return;
      if (this.verifiedOrder.status !== 1) {
        Message.warning('当前订单不是待提货状态');
        return;
      }

      this.verifying = true;
      try {
        await shipOrder(this.verifiedOrder.id);
        Message.success(`订单 #${this.verifiedOrder.id} 提货核销成功！已交付完成`);
        this.closeVerifyModal();
        this.fetchWorkbenchMetrics();
      } catch (err) {
        Message.error(err.response?.data?.message || '核销提货失败，请重试');
      } finally {
        this.verifying = false;
      }
    },
    getProdName(productId) {
      const prod = this.cachedProducts.find(p => p.id === productId);
      return prod ? prod.name : '精选面包烘焙';
    },
    getProdImage(productId) {
      const prod = this.cachedProducts.find(p => p.id === productId);
      return prod ? prod.imageUrl : '';
    },
    getOrderTagColor(status) {
      const map = { 0: 'orange', 1: 'green', 2: 'gray', 3: 'blue' };
      return map[status] || 'gray';
    },
    getOrderTagText(status) {
      const map = { 0: '待付款', 1: '待提货/出炉', 2: '已取消', 3: '已交付完成' };
      return map[status] || '未知状态';
    }
  }
}
</script>

<style scoped>
.admin-root-container {
  min-height: 100vh;
  background-color: #F7F8FA;
}

/* PC 端样式 */
.admin-layout {
  height: 100vh;
}
.admin-sider {
  background: #FFFFFF;
  box-shadow: 2px 0 8px 0 rgba(29, 33, 41, 0.05);
  z-index: 10;
}
.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #F2F3F5;
  padding: 0 16px;
}
.admin-logo h2 {
  margin: 0;
  font-size: 16px;
  color: #1D2129;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.admin-main-body {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.admin-header {
  height: 60px;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.02);
  z-index: 9;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
}
.header-identity-pill {
  font-size: 11px;
  font-weight: 600;
  color: #D97706;
  background: #FEF3C7;
  padding: 2px 8px;
  border-radius: 10px;
}
.header-identity-pill.pill-super {
  color: #DC2626;
  background: #FEE2E2;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.content-wrapper {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  min-height: calc(100vh - 100px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

/* ================== 移动端专属工作台设计 ================== */
.mobile-workbench-container {
  min-height: 100vh;
  background: #F4F6F9;
  padding-bottom: 80px;
}

/* 顶部工作台导航栏 */
.workbench-nav-bar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: calc(50px + var(--safe-top, 0px));
  padding-top: var(--safe-top, 0px);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 14px;
  padding-right: 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.nav-left-slot, .nav-right-slot {
  display: flex;
  align-items: center;
  min-width: 68px;
}
.nav-right-slot {
  justify-content: flex-end;
}

.nav-center-title {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 800;
  color: #1A1D20;
}

.title-with-badge {
  display: flex;
  align-items: center;
  gap: 6px;
}
.main-head-text {
  letter-spacing: -0.3px;
}
.role-badge-tag {
  font-size: 10px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 6px;
  color: #D97706;
  background: #FEF3C7;
  letter-spacing: 0.5px;
}
.role-badge-tag.is-admin {
  color: #DC2626;
  background: #FEE2E2;
}

.nav-back-workbench-btn,
.nav-back-profile-btn {
  background: #F2F3F5;
  border: none;
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 5px 10px;
  border-radius: 14px;
  transition: all 0.2s ease;
}
.nav-back-workbench-btn:active,
.nav-back-profile-btn:active {
  transform: scale(0.92);
  background: #E5E6EB;
}
.back-arrow-icon {
  font-size: 14px;
}
.nav-action-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  color: #4E5969;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: transform 0.15s ease;
}
.nav-action-btn:active {
  transform: scale(0.9);
}

/* 工作台主体卡片容器 */
.workbench-grid-view {
  padding: 14px 14px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ================= 2026 今日经营核心轻奢微看板 ================= */
.today-ops-card {
  position: relative;
  background: linear-gradient(135deg, #1A1D24 0%, #0F1216 100%);
  border-radius: 22px;
  padding: 18px 16px 16px;
  box-shadow: 0 12px 32px rgba(15, 18, 22, 0.18), 0 2px 6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #FFFFFF;
  overflow: hidden;
}

.today-ops-card::after {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 130px;
  height: 130px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.18) 0%, rgba(245, 158, 11, 0) 70%);
  pointer-events: none;
}

.ops-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  position: relative;
  z-index: 2;
}

.ops-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ops-dot-pulsing {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10B981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.25);
  animation: pulse-dot 2s infinite ease-in-out;
}
@keyframes pulse-dot {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5); }
  70% { transform: scale(1.1); box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.ops-title-text {
  font-size: 14px;
  font-weight: 800;
  color: #F3F4F6;
  letter-spacing: -0.2px;
}

.ops-refresh-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  padding: 2px;
  font-size: 14px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}
.ops-refresh-btn.is-refreshing {
  transform: rotate(360deg);
  color: #F59E0B;
}

/* 快速提货核销金色胶囊按钮 */
.quick-verify-btn {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #FFFFFF;
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.quick-verify-btn:active {
  transform: scale(0.94);
  box-shadow: 0 2px 6px rgba(217, 119, 6, 0.2);
}
.verify-scan-icon {
  font-size: 14px;
}

/* 4 项指标网格 */
.ops-metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  position: relative;
  z-index: 2;
}

.metric-block {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 12px 12px 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease;
}
.metric-block:active {
  transform: scale(0.96);
  background: rgba(255, 255, 255, 0.08);
}

.metric-block.is-primary-metric {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.14) 0%, rgba(217, 119, 6, 0.06) 100%);
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.metric-top-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.65);
}
.metric-arrow {
  font-size: 10px;
  opacity: 0.5;
}

.metric-value-row {
  margin: 6px 0 4px;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.metric-number-highlight {
  font-size: 26px;
  font-weight: 900;
  color: #FBBF24;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
  letter-spacing: -0.5px;
}
.metric-number-standard {
  font-size: 22px;
  font-weight: 800;
  color: #FFFFFF;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif;
}
.metric-currency-symbol {
  font-size: 14px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.7);
}
.metric-unit {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.metric-footer-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  font-weight: 700;
  color: #FCD34D;
  background: rgba(245, 158, 11, 0.2);
  padding: 2px 6px;
  border-radius: 6px;
  align-self: flex-start;
}
.badge-fire-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #F59E0B;
}

.metric-footer-note {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
}

/* 工作台功能分组卡片 */
.app-group-section {
  background: #FFFFFF;
  border-radius: 22px;
  padding: 18px 16px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03), 0 1px 2px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.group-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.group-pill-indicator {
  width: 4px;
  height: 14px;
  border-radius: 2px;
}
.bg-indicator-blue { background: #388BFD; }
.bg-indicator-orange { background: #FF922B; }
.bg-indicator-gold { background: #FCC419; }

.group-title-text {
  font-size: 14px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.2px;
}
.group-admin-pill {
  font-size: 9px;
  font-weight: 800;
  color: #EF4444;
  background: #FEE2E2;
  padding: 1px 5px;
  border-radius: 4px;
}

.app-grid-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px 8px;
}

.app-item-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}
.app-item-card:active {
  transform: scale(0.88) translateY(2px);
}

/* 现代立体微光超椭圆图标 */
.app-icon-squircle {
  position: relative;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.45);
  transition: all 0.2s ease;
}

.icon-inner-gloss {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 45%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.app-vector-symbol {
  font-size: 26px;
  color: #FFFFFF;
  position: relative;
  z-index: 2;
  filter: drop-shadow(0 2px 5px rgba(0, 0, 0, 0.18));
}

.icon-blue-frost {
  background: linear-gradient(145deg, #4FA2FF 0%, #1765F6 100%);
  box-shadow: 0 8px 20px -4px rgba(23, 101, 246, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-cyan-frost {
  background: linear-gradient(145deg, #38D9A9 0%, #08976C 100%);
  box-shadow: 0 8px 20px -4px rgba(8, 151, 108, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-purple-frost {
  background: linear-gradient(145deg, #9775FA 0%, #6741D9 100%);
  box-shadow: 0 8px 20px -4px rgba(103, 65, 217, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-indigo-frost {
  background: linear-gradient(145deg, #5C7CFA 0%, #364FC7 100%);
  box-shadow: 0 8px 20px -4px rgba(54, 79, 199, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-orange-frost {
  background: linear-gradient(145deg, #FFA94D 0%, #F76707 100%);
  box-shadow: 0 8px 20px -4px rgba(247, 103, 7, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-red-frost {
  background: linear-gradient(145deg, #FF6B6B 0%, #E03131 100%);
  box-shadow: 0 8px 20px -4px rgba(224, 49, 49, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-gold-frost {
  background: linear-gradient(145deg, #FFD43B 0%, #F08C00 100%);
  box-shadow: 0 8px 20px -4px rgba(240, 140, 0, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.icon-green-frost {
  background: linear-gradient(145deg, #69DB7C 0%, #2B8A3E 100%);
  box-shadow: 0 8px 20px -4px rgba(43, 138, 62, 0.45), inset 0 1.5px 1px 0 rgba(255, 255, 255, 0.5);
}

.app-name {
  font-size: 12px;
  font-weight: 700;
  color: #1A1D20;
  text-align: center;
  white-space: nowrap;
  letter-spacing: -0.2px;
}

/* 子模块全屏视图 */
.workbench-submodule-view {
  padding: 10px 12px;
}

/* ================= 提货核销抽屉专属样式 ================= */
.verify-sheet-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 4px;
}

.verify-input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.verify-mono-input {
  flex: 1;
  background: #F8F9FA;
  border-radius: 12px;
  font-family: monospace;
  font-size: 16px;
  font-weight: 700;
}
.verify-input-prefix {
  color: #86909C;
  font-size: 16px;
  font-weight: bold;
}

.verify-search-btn {
  background: #1D2129;
  color: #FFFFFF;
  border: none;
  border-radius: 12px;
  padding: 0 18px;
  height: 40px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.verify-search-btn:disabled {
  background: #C9CDD4;
  cursor: not-allowed;
}
.verify-search-btn:not(:disabled):active {
  transform: scale(0.95);
}

/* 订单卡片 */
.verified-order-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #E5E6EB;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}
.status-border-1 {
  border-color: #52C41A;
  background: #F6FFED;
}
.status-border-0 {
  border-color: #FAAD14;
}

.verified-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.08);
}
.verified-order-id {
  font-family: monospace;
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
}

.verified-item-row {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
.verified-prod-thumb {
  width: 58px;
  height: 58px;
  border-radius: 10px;
  object-fit: cover;
}
.verified-thumb-placeholder {
  width: 58px;
  height: 58px;
  border-radius: 10px;
  background: #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #86909C;
}

.verified-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.verified-prod-name {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.3;
}
.verified-spec-text {
  font-size: 12px;
  color: #86909C;
}
.verified-qty-amount {
  display: flex;
  align-items: center;
  gap: 8px;
}
.verified-qty-tag {
  font-size: 12px;
  font-weight: 700;
  color: #E03131;
}
.verified-price-tag {
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
}

.verified-contact-section {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #4E5969;
}
.verified-buyer-line {
  display: flex;
  align-items: center;
  gap: 10px;
}
.verified-phone-link {
  color: #165DFF;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.verified-addr-line {
  color: #1D2129;
  font-weight: 500;
}
.verified-remark-box {
  background: #FFF7E8;
  color: #D46B08;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
}

.verified-status-banner {
  margin-top: 12px;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
}
.banner-icon {
  font-size: 14px;
}
.status-ready {
  background: #E8FFEA;
  color: #00B42A;
}
.status-done {
  background: #E8F3FF;
  color: #165DFF;
}
.status-unpaid {
  background: #FFF7E8;
  color: #FF7D00;
}
.status-cancelled {
  background: #F2F3F5;
  color: #86909C;
}

.verified-empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 0;
  color: #86909C;
  font-size: 13px;
}
.empty-warn-icon {
  font-size: 28px;
  color: #FF7D00;
}
</style>
