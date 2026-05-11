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
    </el-tabs>

    <!-- 商品编辑/添加弹窗 -->
    <el-dialog :title="isEditing ? '编辑商品' : '上架新商品'" :visible.sync="productDialogVisible" width="500px">
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
      articles: [],
      products: [],
      productDialogVisible: false,
      isEditing: false,
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
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchArticles() {
      this.loadingArticles = true;
      try {
        const res = await axios.get('/api/articles');
        this.articles = res.data;
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
        this.products = res.data;
      } catch (error) {
        this.$message.error('加载商品失败');
      } finally {
        this.loadingProducts = false;
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
