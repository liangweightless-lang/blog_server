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
          <h2 v-if="!collapsed">WTLS 控制台</h2>
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
          <a-menu-item key="users">
            <template #icon><icon-user-group /></template>
            用户管理
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
          <a-menu-item key="creators">
            <template #icon><icon-star /></template>
            主理人审核
          </a-menu-item>
          <a-menu-item key="system">
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
            <UserManager v-if="activeTab === 'users'" :isMobile="false" />
            <CreatorManager v-if="activeTab === 'creators'" :isMobile="false" />
            <OrderManager v-if="activeTab === 'orders'" :isMobile="false" />
            <GroupbuyManager v-if="activeTab === 'groupbuys'" :isMobile="false" />
            <CampaignManager v-if="activeTab === 'campaigns'" :isMobile="false" />
            <SystemConfig v-if="activeTab === 'system'" :isMobile="false" />
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
          <button v-else class="nav-home-btn" @click="$router.push('/')" title="返回前台">
            <icon-home />
          </button>
        </div>
        
        <div class="nav-center-title">
          {{ mobileActiveModule ? currentTitle : '工作台' }}
        </div>

        <div class="nav-right-slot">
          <button class="nav-action-btn" @click="$router.push('/')" title="前台首页">
            <icon-export />
          </button>
        </div>
      </div>

      <!-- 1. 工作台主面板：分组金刚区高定立体微光图标网格 -->
      <div class="workbench-grid-view" v-if="!mobileActiveModule">
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

            <div class="app-item-card" @click="openModule('system')">
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
            <div class="app-item-card" @click="openModule('orders')">
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

        <!-- 分组 3：用户与主理人 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-pill-indicator bg-indicator-gold"></span>
            <span class="group-title-text">用户与主理人</span>
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
        <UserManager v-if="mobileActiveModule === 'users'" :isMobile="true" />
        <CreatorManager v-if="mobileActiveModule === 'creators'" :isMobile="true" />
        <OrderManager v-if="mobileActiveModule === 'orders'" :isMobile="true" />
        <GroupbuyManager v-if="mobileActiveModule === 'groupbuys'" :isMobile="true" />
        <CampaignManager v-if="mobileActiveModule === 'campaigns'" :isMobile="true" />
        <SystemConfig v-if="mobileActiveModule === 'system'" :isMobile="true" />
      </div>
    </div>
  </div>
</template>

<script>
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
      activeTab: 'articles',
      selectedKeys: ['articles'],
      mobileActiveModule: null, // 移动端当前打开的具体模块，null表示在主工作台网格
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
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
      return titleMap[activeKey] || '管理后台';
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    this.handleResize();
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    onCollapse(val) {
      this.collapsed = val;
    },
    handleMenuClick(key) {
      this.activeTab = key;
    },
    openModule(key) {
      this.mobileActiveModule = key;
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
}
.admin-logo h2 {
  margin: 0;
  font-size: 18px;
  color: #1D2129;
  font-weight: bold;
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

/* ================== 移动端专属工作台应用网格设计 ================== */
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
  background: rgba(255, 255, 255, 0.92);
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
  font-size: 17px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.nav-home-btn, .nav-action-btn {
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
.nav-home-btn:active, .nav-action-btn:active {
  transform: scale(0.9);
}

.nav-back-workbench-btn {
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
.nav-back-workbench-btn:active {
  background: #E5E6EB;
  transform: scale(0.95);
}
.back-arrow-icon {
  font-size: 14px;
}

/* 工作台分组卡片 */
.workbench-grid-view {
  padding: 14px 14px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

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

/* ================= 2026 Apple / iOS 18 现代立体微光超椭圆图标 ================= */
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

/* 顶部高光微切角流光 */
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

/* 高定微磨砂渐变配色库 (Apple Palette) */
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
</style>
