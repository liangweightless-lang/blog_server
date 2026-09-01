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

    <!-- 移动端专属：App 级工作台应用网格架构 (完全对标企业微信/钉钉/工作台架构) -->
    <div class="mobile-workbench-container" v-else>
      <!-- 移动端顶部导航栏 -->
      <div class="workbench-nav-bar">
        <div class="nav-left-slot">
          <button v-if="mobileActiveModule" class="nav-back-workbench-btn" @click="mobileActiveModule = null">
            <icon-left /> <span>工作台</span>
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

      <!-- 1. 工作台主面板：分组金刚区大图标网格 -->
      <div class="workbench-grid-view" v-if="!mobileActiveModule">
        <!-- 分组 1：日常运营 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-title-text">日常运营</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('articles')">
              <div class="app-icon-box bg-blue">
                <icon-file class="app-icon" />
              </div>
              <span class="app-name">文章日记</span>
            </div>

            <div class="app-item-card" @click="openModule('products')">
              <div class="app-icon-box bg-cyan">
                <icon-storage class="app-icon" />
              </div>
              <span class="app-name">商品管理</span>
            </div>

            <div class="app-item-card" @click="openModule('system')">
              <div class="app-icon-box bg-purple">
                <icon-settings class="app-icon" />
              </div>
              <span class="app-name">系统配置</span>
            </div>
          </div>
        </div>

        <!-- 分组 2：交易与履约 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-title-text">交易与履约</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('orders')">
              <div class="app-icon-box bg-indigo">
                <icon-calendar class="app-icon" />
              </div>
              <span class="app-name">订单管理</span>
            </div>

            <div class="app-item-card" @click="openModule('groupbuys')">
              <div class="app-icon-box bg-orange">
                <icon-user-add class="app-icon" />
              </div>
              <span class="app-name">单品拼团</span>
            </div>

            <div class="app-item-card" @click="openModule('campaigns')">
              <div class="app-icon-box bg-red">
                <icon-tags class="app-icon" />
              </div>
              <span class="app-name">社区快团</span>
            </div>
          </div>
        </div>

        <!-- 分组 3：用户与审核 -->
        <div class="app-group-section">
          <div class="group-title-row">
            <span class="group-title-text">用户与主理人</span>
          </div>
          <div class="app-grid-row">
            <div class="app-item-card" @click="openModule('creators')">
              <div class="app-icon-box bg-gold">
                <icon-star class="app-icon" />
              </div>
              <span class="app-name">主理人审核</span>
            </div>

            <div class="app-item-card" @click="openModule('users')">
              <div class="app-icon-box bg-green">
                <icon-user-group class="app-icon" />
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
  background: #F7F8FA;
  padding-bottom: 80px;
}

/* 顶部工作台导航栏 */
.workbench-nav-bar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: calc(48px + var(--safe-top, 0px));
  padding-top: var(--safe-top, 0px);
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 14px;
  padding-right: 14px;
  border-bottom: 1px solid #F2F3F5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.nav-left-slot, .nav-right-slot {
  display: flex;
  align-items: center;
  min-width: 60px;
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
}

.nav-back-workbench-btn {
  background: transparent;
  border: none;
  font-size: 14px;
  font-weight: 700;
  color: #FF5E3A;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 0;
}

/* 工作台分组卡片 */
.workbench-grid-view {
  padding: 14px 14px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-group-section {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 14px;
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.group-title-row {
  margin-bottom: 14px;
}

.group-title-text {
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
  letter-spacing: -0.2px;
}

.app-grid-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.app-item-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.15s ease;
  user-select: none;
}
.app-item-card:active {
  transform: scale(0.92);
}

.app-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
}

.app-icon {
  font-size: 24px;
  color: #FFFFFF;
}

/* 图标高定渐变色 */
.bg-blue { background: linear-gradient(135deg, #388BFD 0%, #1D61E2 100%); box-shadow: 0 4px 14px rgba(56, 139, 253, 0.3); }
.bg-cyan { background: linear-gradient(135deg, #20C997 0%, #0CA678 100%); box-shadow: 0 4px 14px rgba(32, 201, 151, 0.3); }
.bg-purple { background: linear-gradient(135deg, #845EF7 0%, #6741D9 100%); box-shadow: 0 4px 14px rgba(132, 94, 247, 0.3); }
.bg-indigo { background: linear-gradient(135deg, #4C6EF5 0%, #364FC7 100%); box-shadow: 0 4px 14px rgba(76, 110, 245, 0.3); }
.bg-orange { background: linear-gradient(135deg, #FF922B 0%, #F76707 100%); box-shadow: 0 4px 14px rgba(255, 146, 43, 0.3); }
.bg-red { background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%); box-shadow: 0 4px 14px rgba(255, 94, 58, 0.3); }
.bg-gold { background: linear-gradient(135deg, #FCC419 0%, #E67700 100%); box-shadow: 0 4px 14px rgba(252, 196, 25, 0.35); }
.bg-green { background: linear-gradient(135deg, #51CF66 0%, #2B8A3E 100%); box-shadow: 0 4px 14px rgba(81, 207, 102, 0.3); }

.app-name {
  font-size: 12px;
  font-weight: 600;
  color: #1D2129;
  text-align: center;
  white-space: nowrap;
}

/* 子模块全屏视图 */
.workbench-submodule-view {
  padding: 12px 14px;
}
</style>
