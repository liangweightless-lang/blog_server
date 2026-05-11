<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1 class="admin-title">超级管理员后台</h1>
      <p class="admin-subtitle">管理日常发布、文章列表以及商品橱窗。</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 文章管理 -->
      <el-tab-pane label="日常/文章管理" name="articles">
        <div class="tab-header">
          <el-button type="primary" size="small" icon="el-icon-plus" @click="goToCreateArticle">发布新日常</el-button>
        </div>
        <el-table :data="articles" style="width: 100%" v-loading="loadingArticles">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column label="封面" width="120">
            <template slot-scope="scope">
              <el-image :src="scope.row.coverUrl" fit="cover" style="width: 50px; height: 50px; border-radius: 8px;"></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="likesCount" label="点赞数" width="100"></el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template slot-scope="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="danger" plain @click="handleDeleteArticle(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 商品管理 -->
      <el-tab-pane label="灵感橱窗商品管理" name="products">
        <div class="tab-header">
          <el-button type="success" size="small" icon="el-icon-plus" @click="showAddProductDialog">上架新商品</el-button>
        </div>
        <el-table :data="products" style="width: 100%" v-loading="loadingProducts">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column label="商品图" width="120">
            <template slot-scope="scope">
              <el-image :src="scope.row.image" fit="cover" style="width: 50px; height: 50px; border-radius: 8px;"></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称"></el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template slot-scope="scope">¥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isDigital ? 'success' : 'info'" size="small">
                {{ scope.row.isDigital ? '数字商品' : '实体商品' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" plain @click="handleEditProduct(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" plain @click="handleDeleteProduct(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="注册人员管理" name="users">
        <el-table :data="users" style="width: 100%" v-loading="loadingUsers">
          <el-table-column label="头像" width="70">
            <template slot-scope="scope">
              <el-avatar :size="32" :src="scope.row.avatarUrl"></el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
          <el-table-column prop="username" label="手机号/账号" width="120"></el-table-column>
          <el-table-column prop="points" label="积分" width="80" sortable>
            <template slot-scope="scope">
              <el-tag type="warning" size="mini">{{ scope.row.points }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="wechatId" label="微信号" width="120"></el-table-column>
          <el-table-column prop="address" label="配送地址" min-width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="role" label="角色" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'" size="mini">{{ scope.row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" width="160">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 订单管理 -->
      <el-tab-pane label="订单/发货管理" name="orders">
        <el-table :data="orders" style="width: 100%" v-loading="loadingOrders">
          <el-table-column prop="id" label="订单号" width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="productId" label="商品ID" width="80"></el-table-column>
          <el-table-column prop="amount" label="实付金额" width="100">
            <template slot-scope="scope">¥{{ scope.row.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getOrderStatusType(scope.row.status)" size="mini">
                {{ getOrderStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderType" label="类型" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.orderType === 'GROUP' ? 'warning' : 'primary'" size="mini" effect="plain">
                {{ scope.row.orderType === 'GROUP' ? '拼团' : '个买' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="shippingAddress" label="配送地址" min-width="200" show-overflow-tooltip></el-table-column>
          <el-table-column label="下单时间" width="160">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === 1" size="mini" type="success" plain @click="handleShip(scope.row)">标记发货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 拼团管理 -->
      <el-tab-pane label="拼团进度管理" name="groupbuys">
        <el-table :data="groupbuys" style="width: 100%" v-loading="loadingGroups">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="productName" label="拼团商品"></el-table-column>
          <el-table-column prop="initiatorNickname" label="发起人" width="120"></el-table-column>
          <el-table-column label="进度" width="150">
            <template slot-scope="scope">
              {{ scope.row.currentNum }} / {{ scope.row.requiredNum }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="mini">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="到期时间" width="180">
            <template slot-scope="scope">{{ formatTime(scope.row.expireTime) }}</template>
          </el-table-column>
          <el-table-column label="管理操作" width="220" fixed="right">
            <template slot-scope="scope" v-if="scope.row.status === 0">
              <el-button size="mini" type="success" plain @click="handleForceSuccess(scope.row)">强制成团</el-button>
              <el-button size="mini" type="danger" plain @click="handleForceFail(scope.row)">强制退款</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 商品编辑/添加弹窗 -->
    <el-dialog :title="isEditing ? '编辑商品' : '上架新商品'" :visible.sync="productDialogVisible" :width="isMobile ? '90%' : '500px'">
      <el-form :model="productForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="productForm.name"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="productForm.description"></el-input>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productForm.price" :precision="2" :step="0.1"></el-input-number>
        </el-form-item>
        <el-form-item label="图片链接">
          <el-input v-model="productForm.image" placeholder="/img/product_xxx.png"></el-input>
        </el-form-item>
        <el-form-item label="数字商品">
          <el-switch v-model="productForm.isDigital" active-text="是" inactive-text="否"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="productDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveProduct">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'AdminDashboard',
  data() {
    return {
      activeTab: 'articles',
      loadingArticles: false,
      loadingProducts: false,
      loadingUsers: false,
      loadingOrders: false,
      articles: [],
      products: [],
      users: [],
      orders: [],
      groupbuys: [],
      productDialogVisible: false,
      isEditing: false,
      isMobile: window.innerWidth <= 768,
      loadingGroups: false,
      productForm: {
        id: null,
        name: '',
        description: '',
        price: 0,
        image: '',
        isDigital: true
      }
    }
  },
  created() {
    this.fetchArticles();
    this.fetchProducts();
    this.fetchUsers();
    this.fetchOrders();
    this.fetchGroups();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchArticles() {
      this.loadingArticles = true;
      try {
        const res = await axios.get('/api/articles');
        console.log('Admin fetch articles:', res.data);
        this.articles = res.data.data || [];
      } catch (error) {
        this.$message.error('加载文章失败');
      } finally {
        this.loadingArticles = false;
      }
    },
    async fetchProducts() {
      this.loadingProducts = true;
      try {
        const res = await axios.get('/api/products');
        this.products = res.data.data;
      } catch (error) {
        this.$message.error('加载商品失败');
      } finally {
        this.loadingProducts = false;
      }
    },
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        const res = await axios.get('/api/users', { headers: this.getAuthHeader() });
        this.users = res.data.data;
      } catch (error) {
        this.$message.error('加载用户列表失败');
      } finally {
        this.loadingUsers = false;
      }
    },
    async fetchOrders() {
      this.loadingOrders = true;
      try {
        const res = await axios.get('/api/orders', { headers: this.getAuthHeader() });
        this.orders = res.data.data;
      } catch (error) {
        this.$message.error('加载订单列表失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    async fetchGroups() {
      this.loadingGroups = true;
      try {
        const res = await axios.get('/api/groups', { headers: this.getAuthHeader() });
        this.groupbuys = res.data.data;
      } catch (error) {
        this.$message.error('加载拼团列表失败');
      } finally {
        this.loadingGroups = false;
      }
    },
    async handleForceSuccess(group) {
      try {
        await this.$confirm('确定要手动强制该团【成团】吗？所有成员订单将变为待发货。', '危险操作', { type: 'warning' });
        await axios.post(`/api/groups/${group.id}/force-success`, {}, { headers: this.getAuthHeader() });
        this.$message.success('操作成功，已强制成团');
        this.fetchGroups();
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.response?.data?.message || '操作失败');
      }
    },
    async handleForceFail(group) {
      try {
        await this.$confirm('确定要【强制失败】该团吗？系统将自动取消订单并退回成员积分！', '危险操作', { type: 'error' });
        await axios.post(`/api/groups/${group.id}/force-fail`, {}, { headers: this.getAuthHeader() });
        this.$message.success('已强制失败并退款');
        this.fetchGroups();
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.response?.data?.message || '操作失败');
      }
    },
    getStatusType(status) {
      const types = ['warning', 'success', 'danger'];
      return types[status] || 'info';
    },
    getStatusText(status) {
      const texts = ['拼团中', '拼团成功', '拼团失败'];
      return texts[status] || '未知';
    },
    getOrderStatusType(status) {
      const types = ['info', 'success', 'danger', 'primary'];
      return types[status] || 'info';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
    },
    async handleShip(order) {
      try {
        await axios.post(`/api/orders/${order.id}/ship`, {}, { headers: this.getAuthHeader() });
        this.$message.success('发货成功');
        this.fetchOrders();
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    goToCreateArticle() {
      this.$router.push('/create');
    },
    async handleDeleteArticle(article) {
      try {
        await this.$confirm('确定要删除这篇日常吗？', '提示', { type: 'warning' });
        await axios.delete(`/api/articles/${article.id}`, { headers: this.getAuthHeader() });
        this.$message.success('已删除');
        this.fetchArticles();
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },
    showAddProductDialog() {
      this.isEditing = false;
      this.productForm = { id: null, name: '', description: '', price: 0, image: '', isDigital: true };
      this.productDialogVisible = true;
    },
    handleEditProduct(product) {
      this.isEditing = true;
      this.productForm = { ...product };
      this.productDialogVisible = true;
    },
    async saveProduct() {
      try {
        if (this.isEditing) {
          await axios.put(`/api/products/${this.productForm.id}`, this.productForm, { headers: this.getAuthHeader() });
        } else {
          await axios.post('/api/products', this.productForm, { headers: this.getAuthHeader() });
        }
        this.$message.success('保存成功');
        this.productDialogVisible = false;
        this.fetchProducts();
      } catch (error) {
        this.$message.error('保存失败');
      }
    },
    async handleDeleteProduct(product) {
      try {
        await this.$confirm('确定要下架并删除该商品吗？', '提示', { type: 'warning' });
        await axios.delete(`/api/products/${product.id}`, { headers: this.getAuthHeader() });
        this.$message.success('已下架');
        this.fetchProducts();
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败');
      }
    },
    formatTime(timeStr) {
      return new Date(timeStr).toLocaleString();
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
  }
  .admin-subtitle {
    font-size: 13px;
  }
  ::v-deep .el-table {
    font-size: 12px;
  }
  ::v-deep .el-tabs__item {
    padding: 0 10px !important;
    font-size: 13px;
  }
  .admin-container {
    padding: 10px 0 60px;
  }
}

.tab-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
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
