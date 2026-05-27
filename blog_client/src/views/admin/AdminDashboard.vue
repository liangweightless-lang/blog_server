<template>
  <div class="admin-layout">
    <div class="admin-container">
      <div class="admin-header">
        <h2>控制台 Dashboard</h2>
        <p class="subtitle">System Administration Panel</p>
      </div>
      <a-tabs v-model:active-key="activeTab" :type="isMobile ? 'line' : 'capsule'" animation class="admin-tabs">
        <a-tab-pane title="日常" key="articles">
          <ArticleManager v-if="activeTab === 'articles'" :isMobile="isMobile" />
        </a-tab-pane>

        <a-tab-pane title="商品" key="products">
          <ProductManager v-if="activeTab === 'products'" :isMobile="isMobile" />
        </a-tab-pane>

        <a-tab-pane title="用户" key="users">
          <UserManager v-if="activeTab === 'users'" :isMobile="isMobile" />
        </a-tab-pane>

        <a-tab-pane title="订单" key="orders">
          <OrderManager v-if="activeTab === 'orders'" :isMobile="isMobile" />
        </a-tab-pane>

        <a-tab-pane title="拼团" key="groupbuys">
          <GroupbuyManager v-if="activeTab === 'groupbuys'" :isMobile="isMobile" />
        </a-tab-pane>

        <a-tab-pane title="配置" key="system">
          <SystemConfig v-if="activeTab === 'system'" :isMobile="isMobile" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script>
import ArticleManager from '@/components/admin/ArticleManager.vue';
import ProductManager from '@/components/admin/ProductManager.vue';
import UserManager from '@/components/admin/UserManager.vue';
import OrderManager from '@/components/admin/OrderManager.vue';
import GroupbuyManager from '@/components/admin/GroupbuyManager.vue';
import SystemConfig from '@/components/admin/SystemConfig.vue';

export default {
  name: 'AdminDashboard',
  components: {
    ArticleManager,
    ProductManager,
    UserManager,
    OrderManager,
    GroupbuyManager,
    SystemConfig
  },
  data() {
    return {
      activeTab: 'articles',
      isMobile: window.innerWidth <= 768
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
    }
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: #F7F8FA;
  padding: 24px 0;
}
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  padding: 30px;
  border: 1px solid #E5E6EB;
}
.admin-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #F2F3F5;
  padding-bottom: 16px;
}
.admin-header h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  color: #1D2129;
  font-weight: 700;
}
.subtitle {
  margin: 0;
  font-size: 13px;
  color: #86909C;
  letter-spacing: 0.5px;
}
:deep(.admin-tabs .arco-tabs-nav-capsule) {
  background-color: #F2F3F5;
  border-radius: 6px;
  padding: 4px;
}
:deep(.admin-tabs .arco-tabs-tab) {
  border-radius: 4px;
  color: #4E5969;
}
:deep(.admin-tabs .arco-tabs-tab-active) {
  background-color: #FFFFFF;
  color: #1D2129;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
@media (max-width: 768px) {
  .admin-layout {
    background-color: #FFFFFF;
    padding: 0;
  }
  .admin-container {
    border-radius: 0;
    box-shadow: none;
    border: none;
    padding: 16px 16px 60px 16px;
  }
  .admin-header h2 {
    font-size: 20px;
  }
}
</style>
