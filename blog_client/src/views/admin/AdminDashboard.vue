<template>
  <a-layout class="admin-layout">
    <!-- PC 端侧边栏 -->
    <a-layout-sider 
      v-if="!isMobile"
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

    <!-- 移动端滑出式抽屉菜单 (解决移动端侧边栏挤压屏幕的问题) -->
    <a-drawer 
      v-else
      :visible="mobileDrawerVisible" 
      placement="left" 
      :width="260"
      :header="false"
      :footer="false"
      @cancel="mobileDrawerVisible = false"
    >
      <div class="mobile-drawer-inner">
        <div class="drawer-header">
          <div class="drawer-brand">
            <span class="brand-sparkle">✦</span>
            <h3>WTLS 管理后台</h3>
          </div>
          <button class="drawer-close-btn" @click="mobileDrawerVisible = false">
            <icon-close />
          </button>
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

        <div class="drawer-bottom-action">
          <a-button long type="outline" @click="$router.push('/')">
            <template #icon><icon-home /></template>返回前台首页
          </a-button>
        </div>
      </div>
    </a-drawer>
    
    <a-layout class="admin-main-body">
      <a-layout-header class="admin-header">
        <div class="header-left">
          <!-- 移动端汉堡菜单按钮 -->
          <a-button v-if="isMobile" type="text" size="large" @click="mobileDrawerVisible = true" class="mobile-menu-trigger">
            <template #icon><icon-menu style="font-size: 20px;" /></template>
          </a-button>
          <!-- PC 端折叠按钮 -->
          <a-button v-else type="text" @click="collapsed = !collapsed">
            <template #icon>
              <icon-menu-fold v-if="!collapsed" />
              <icon-menu-unfold v-else />
            </template>
          </a-button>
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <a-button type="text" @click="$router.push('/')">
            <template #icon><icon-home /></template>
            <span v-if="!isMobile">返回前台</span>
          </a-button>
        </div>
      </a-layout-header>
      
      <a-layout-content class="admin-content">
        <div class="content-wrapper">
          <ArticleManager v-if="activeTab === 'articles'" :isMobile="isMobile" />
          <ProductManager v-if="activeTab === 'products'" :isMobile="isMobile" />
          <UserManager v-if="activeTab === 'users'" :isMobile="isMobile" />
          <CreatorManager v-if="activeTab === 'creators'" :isMobile="isMobile" />
          <OrderManager v-if="activeTab === 'orders'" :isMobile="isMobile" />
          <GroupbuyManager v-if="activeTab === 'groupbuys'" :isMobile="isMobile" />
          <CampaignManager v-if="activeTab === 'campaigns'" :isMobile="isMobile" />
          <SystemConfig v-if="activeTab === 'system'" :isMobile="isMobile" />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
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
      mobileDrawerVisible: false,
      activeTab: 'articles',
      selectedKeys: ['articles'],
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    currentTitle() {
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
      return titleMap[this.activeTab] || '管理后台';
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
      if (this.isMobile) {
        this.mobileDrawerVisible = false; // 移动端点击后自动关闭抽屉
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #F7F8FA;
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
  -webkit-overflow-scrolling: touch;
}
.content-wrapper {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  min-height: calc(100vh - 100px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

/* 移动端抽屉样式 */
.mobile-drawer-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px 12px;
}
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  margin-bottom: 8px;
  border-bottom: 1px solid #F2F3F5;
}
.drawer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}
.brand-sparkle {
  color: #FF5E3A;
  font-size: 16px;
}
.drawer-brand h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: #1D2129;
}
.drawer-close-btn {
  background: #F2F3F5;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4E5969;
  cursor: pointer;
}
.drawer-bottom-action {
  margin-top: auto;
  padding-top: 16px;
}

@media (max-width: 768px) {
  .admin-header {
    padding: 0 12px;
  }
  .admin-content {
    padding: 10px;
    padding-bottom: 60px;
  }
  .content-wrapper {
    padding: 12px;
    border-radius: 10px;
    min-height: auto;
  }
}
</style>
