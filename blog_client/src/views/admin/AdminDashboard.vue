<template>
  <a-layout class="admin-layout">
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
        <a-menu-item key="system">
          <template #icon><icon-settings /></template>
          系统配置
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <a-layout>
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
            <template #icon><icon-home /></template>返回前台
          </a-button>
        </div>
      </a-layout-header>
      
      <a-layout-content class="admin-content">
        <div class="content-wrapper">
          <ArticleManager v-if="activeTab === 'articles'" :isMobile="isMobile" />
          <ProductManager v-if="activeTab === 'products'" :isMobile="isMobile" />
          <UserManager v-if="activeTab === 'users'" :isMobile="isMobile" />
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

export default {
  name: 'AdminDashboard',
  components: {
    ArticleManager,
    ProductManager,
    UserManager,
    OrderManager,
    GroupbuyManager,
    CampaignManager,
    SystemConfig
  },
  data() {
    return {
      activeTab: 'products',
      selectedKeys: ['products'],
      collapsed: false,
      isMobile: window.innerWidth <= 768,
      menuTitles: {
        articles: '文章日记',
        products: '商品库',
        users: '用户管理',
        orders: '订单管理',
        groupbuys: '拼团管理(单品)',
        campaigns: '社区快团(多品)',
        system: '系统配置'
      }
    }
  },
  computed: {
    currentTitle() {
      return this.menuTitles[this.activeTab];
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
      if (this.isMobile) {
        this.collapsed = true;
      }
    },
    onCollapse(val) {
      this.collapsed = val;
    },
    handleMenuClick(key) {
      this.activeTab = key;
      if (this.isMobile) {
        this.collapsed = true; // 移动端点击后收起菜单
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
.admin-header {
  height: 60px;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.02);
  z-index: 9;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
}
.admin-content {
  padding: 20px;
  overflow-y: auto;
}
.content-wrapper {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  min-height: calc(100vh - 100px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

@media (max-width: 768px) {
  .admin-content {
    padding: 10px;
  }
  .content-wrapper {
    padding: 15px;
  }
}
</style>
