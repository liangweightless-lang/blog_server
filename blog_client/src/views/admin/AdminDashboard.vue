<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1 class="admin-title">小柴包</h1>
      <p class="admin-subtitle">管理日常日记、商品、人员、订单与系统全局配置。</p>
    </div>

    <el-tabs v-model="activeTab" :type="isMobile ? '' : 'border-card'">
      <el-tab-pane label="日常/文章管理" name="articles">
        <ArticleManager v-if="activeTab === 'articles'" />
      </el-tab-pane>

      <el-tab-pane label="灵感橱窗商品管理" name="products">
        <ProductManager v-if="activeTab === 'products'" />
      </el-tab-pane>

      <el-tab-pane label="注册人员管理" name="users">
        <UserManager v-if="activeTab === 'users'" />
      </el-tab-pane>

      <el-tab-pane label="订单/发货管理" name="orders">
        <OrderManager v-if="activeTab === 'orders'" />
      </el-tab-pane>

      <el-tab-pane label="拼团进度管理" name="groupbuys">
        <GroupbuyManager v-if="activeTab === 'groupbuys'" />
      </el-tab-pane>

      <el-tab-pane label="首页信息配置" name="system">
        <SystemConfig v-if="activeTab === 'system'" />
      </el-tab-pane>
    </el-tabs>
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
  beforeDestroy() {
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
.admin-header {
  margin-bottom: 30px;
}
.admin-title {
  font-size: 28px;
  font-weight: 800;
  color: #5C433B;
  margin-bottom: 8px;
}
.admin-subtitle {
  color: #8C6A5D;
}
@media (max-width: 768px) {
  .admin-title {
    font-size: 22px;
    padding: 0 15px;
  }
  .admin-subtitle {
    font-size: 13px;
    padding: 0 15px;
  }
  .admin-container {
    padding: 10px 0 60px;
  }
  .el-tabs {
    border-radius: 0;
    box-shadow: none;
  }
  ::v-deep .el-tabs__content {
    padding: 15px 5px !important;
  }
  ::v-deep .el-tabs__nav-scroll {
    overflow-x: auto !important;
    overflow-y: hidden !important;
  }
  ::v-deep .el-tabs__nav {
    display: flex;
    white-space: nowrap;
    float: none;
  }
  ::v-deep .el-tabs__item {
    padding: 0 15px !important;
    font-size: 14px;
    flex-shrink: 0;
  }
  ::v-deep .el-table {
    font-size: 12px;
  }
  ::v-deep .el-table .cell {
    padding-left: 5px !important;
    padding-right: 5px !important;
  }
}
.el-tabs {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}
::v-deep .el-tabs--border-card {
  border: none;
}
::v-deep .el-tabs__header {
  background-color: #FDF0E6 !important;
}
::v-deep .el-tabs__item.is-active {
  color: #FF7E67 !important;
  background-color: #FFF !important;
}
</style>
