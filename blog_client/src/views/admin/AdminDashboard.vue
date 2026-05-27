<template>
  <div class="admin-container">
    <a-tabs v-model:active-key="activeTab" :type="isMobile ? 'line' : 'capsule'" animation>
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
.admin-container {
  padding: 20px 0;
}
@media (max-width: 768px) {
  .admin-container {
    padding: 10px 15px 60px;
  }
}
</style>
