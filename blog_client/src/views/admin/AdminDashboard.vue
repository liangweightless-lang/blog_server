<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1 class="admin-title">小柴包</h1>
      <p class="admin-subtitle">管理日常日记、商品、人员、订单与系统全局配置。</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 文章管理 -->
      <el-tab-pane label="日常/文章管理" name="articles">
        <div class="tab-header">
          <span style="margin-right: 15px; color: #8C6A5D; font-weight: bold;">[测试日志] 共 {{ articles.length }} 篇数据已到达</span>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="goToCreateArticle">发布新日常</el-button>
        </div>

        <!-- 弃用 el-table，改用手写响应式管理列表 -->
        <div class="custom-admin-list">
          <div class="list-header">
            <div class="col-id">ID</div>
            <div class="col-cover">封面</div>
            <div class="col-info">文章信息</div>
            <div class="col-stats">数据</div>
            <div class="col-actions">操作</div>
          </div>
          
          <div v-for="article in articles" :key="article.id" class="list-item">
            <div class="col-id">#{{ article.id }}</div>
            <div class="col-cover">
              <el-image :src="article.coverUrl" fit="cover" class="item-img">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
            </div>
            <div class="col-info">
              <div class="item-title">{{ article.title }}</div>
              <div class="item-time">{{ formatTime(article.createTime) }}</div>
            </div>
            <div class="col-stats">
              <span class="stat-badge"><i class="el-icon-sugar"></i> {{ article.likesCount || 0 }}</span>
            </div>
            <div class="col-actions">
              <el-button size="mini" type="danger" plain @click="handleDeleteArticle(article)">删除</el-button>
            </div>
          </div>

          <div v-if="articles.length === 0" class="empty-placeholder">
            <i class="el-icon-document"></i>
            <p>暂无数据</p>
          </div>
        </div>
      </el-tab-pane>

      <!-- 商品管理 -->
      <el-tab-pane label="灵感橱窗商品管理" name="products">
        <div class="tab-header">
          <el-button type="success" size="small" icon="el-icon-plus" @click="showAddProductDialog">上架新商品</el-button>
        </div>
        <!-- 商品管理自定义列表 -->
        <div class="custom-admin-list">
          <div class="list-header">
            <div class="col-id">ID</div>
            <div class="col-cover">商品图</div>
            <div class="col-info">商品详情</div>
            <div class="col-stats">库存/价格</div>
            <div class="col-actions">操作</div>
          </div>
          <div v-for="product in products" :key="product.id" class="list-item">
            <div class="col-id">#{{ product.id }}</div>
            <div class="col-cover">
              <el-image :src="product.image" fit="cover" class="item-img"></el-image>
            </div>
            <div class="col-info">
              <div class="item-title">
                {{ product.name }}
                <el-tag :type="product.isDigital ? 'success' : 'info'" size="mini" style="margin-left: 8px;">
                  {{ product.isDigital ? '数字' : '实体' }}
                </el-tag>
              </div>
              <div class="item-time">{{ product.description }}</div>
            </div>
            <div class="col-stats">
              <div :style="{ fontWeight: 'bold', color: product.stock <= 10 ? '#F56C6C' : '#67C23A', marginBottom: '4px' }">
                库存: {{ product.stock || 0 }}
              </div>
              <div style="font-weight: bold; color: #FF7E67;">¥{{ product.price }}</div>
              <div style="font-size: 11px; color: #999;">团: ¥{{ product.groupPrice }}</div>
            </div>
            <div class="col-actions">
              <el-button size="mini" type="text" @click="handleEditProduct(product)">编辑</el-button>
              <el-button size="mini" type="text" style="color: #F56C6C;" @click="handleDeleteProduct(product)">删除</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="注册人员管理" name="users">
        <!-- 用户管理自定义列表 -->
        <div class="custom-admin-list">
          <div class="list-header">
            <div class="col-cover" style="width: 60px;">头像</div>
            <div class="col-info">用户信息</div>
            <div class="col-stats" style="width: 140px;">角色/积分</div>
            <div class="col-actions" style="width: 180px;">加入时间</div>
          </div>
          <div v-for="user in users" :key="user.id" class="list-item">
            <div class="col-cover" style="width: 60px;">
              <el-avatar :size="32" :src="user.avatarUrl"></el-avatar>
            </div>
            <div class="col-info">
              <div class="item-title">{{ user.nickname }} ({{ user.username }})</div>
              <div class="item-time">微信: {{ user.wechatId || '未设置' }} | 地址: {{ user.address || '未设置' }}</div>
            </div>
            <div class="col-stats" style="width: 140px;">
              <el-tag size="mini" :type="user.role === 'ADMIN' ? 'danger' : 'info'">{{ user.role }}</el-tag>
              <div style="font-size: 12px; margin-top: 4px; color: #E6A23C; font-weight: bold;">{{ user.points }} 积分</div>
            </div>
            <div class="col-actions" style="width: 180px; font-size: 12px; color: #999;">
              {{ formatTime(user.createTime) }}
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 订单管理 -->
      <el-tab-pane label="订单/发货管理" name="orders">
        <!-- 订单管理自定义列表 -->
        <div class="custom-admin-list">
          <div class="list-header">
            <div class="col-id" style="width: 100px;">订单号</div>
            <div class="col-info">详情/地址</div>
            <div class="col-stats">实付</div>
            <div class="col-actions">状态/操作</div>
          </div>
          <div v-for="order in orders" :key="order.id" class="list-item">
            <div class="col-id" style="width: 100px;">
              <el-tooltip :content="order.id" placement="top" :open-delay="300">
                <span class="truncate-id">#{{ order.id }}</span>
              </el-tooltip>
            </div>
            <div class="col-info">
              <div class="item-title" style="font-size: 13px;">
                商品ID: {{ order.productId }}
                <el-tag :type="order.orderType === 'GROUP' ? 'warning' : 'primary'" size="mini" effect="plain" style="margin-left: 8px;">
                  {{ order.orderType === 'GROUP' ? '拼团' : '个买' }}
                </el-tag>
              </div>
              <div class="item-time">地址: {{ order.shippingAddress || '无' }} | 时间: {{ formatTime(order.createTime) }}</div>
            </div>
            <div class="col-stats">
              <div style="font-weight: bold; color: #F56C6C;">¥{{ order.amount }}</div>
              <div style="font-size: 11px; color: #999;" v-if="order.pointsUsed">抵扣: {{ order.pointsUsed }}</div>
            </div>
            <div class="col-actions" style="width: 150px;">
              <el-tag :type="getOrderStatusType(order.status)" size="mini" style="margin-bottom: 5px;">
                {{ getOrderStatusText(order.status) }}
              </el-tag>
              <div v-if="order.status === 1">
                <el-button size="mini" type="success" plain @click="handleShip(order)">标记发货</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 拼团管理 -->
      <el-tab-pane label="拼团进度管理" name="groupbuys">
        <!-- 拼团管理自定义列表 -->
        <div class="custom-admin-list">
          <div class="list-header">
            <div class="col-info">商品/发起人</div>
            <div class="col-stats" style="width: 150px;">进度</div>
            <div class="col-actions" style="width: 180px;">状态/操作</div>
          </div>
          <div v-for="group in groupbuys" :key="group.id" class="list-item">
            <div class="col-info">
              <div class="item-title">{{ group.productName }}</div>
              <div class="item-time">发起人: {{ group.initiatorNickname }} | 截止: {{ formatTime(group.expireTime) }}</div>
            </div>
            <div class="col-stats" style="width: 150px;">
              <div style="font-size: 13px; margin-bottom: 5px;">{{ group.currentNum }} / {{ group.requiredNum }} 人</div>
              <el-progress 
                :percentage="Math.min((group.currentNum / group.requiredNum) * 100, 100)" 
                :status="group.status === 1 ? 'success' : ''"
                :show-text="false" 
                stroke-width="8"
              ></el-progress>
            </div>
            <div class="col-actions" style="width: 180px;">
              <el-tag :type="getStatusType(group.status)" size="mini" style="margin-bottom: 5px;">
                {{ getStatusText(group.status) }}
              </el-tag>
              <div v-if="group.status === 0">
                <el-button size="mini" type="success" plain @click="handleForceSuccess(group)">成团</el-button>
                <el-button size="mini" type="danger" plain @click="handleForceFail(group)">解散</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 首页信息配置 -->
      <el-tab-pane label="首页信息配置" name="system">
        <el-form :model="homeConfigForm" label-width="120px" style="max-width: 600px; margin-top: 20px;" size="small">
          <el-form-item label="首页头像">
            <el-upload
              class="product-image-uploader"
              action="/api/files/upload"
              :show-file-list="false"
              :on-success="handleHomeAvatarSuccess"
              :before-upload="beforeProductImageUpload">
              <img v-if="homeConfigForm.avatarUrl" :src="homeConfigForm.avatarUrl" class="product-upload-preview">
              <div v-else class="product-upload-placeholder">
                <i class="el-icon-plus"></i>
                <span>上传头像</span>
              </div>
            </el-upload>
          </el-form-item>
          <el-form-item label="创作者名称">
            <el-input v-model="homeConfigForm.authorName" placeholder="例如: 小柴包"></el-input>
          </el-form-item>
          <el-form-item label="个性签名/简介">
            <el-input type="textarea" :rows="4" v-model="homeConfigForm.authorBio" placeholder="请输入创作者简介"></el-input>
          </el-form-item>
          <el-form-item label="兴趣标签 (用英文逗号分隔)">
            <el-input v-model="homeConfigForm.tagsString" placeholder="例如: 生活方式,独立品牌,创作手记"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingConfig" @click="saveHomeConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="isEditing ? '编辑商品' : '上架新商品'" :visible.sync="productDialogVisible" :width="isMobile ? '95%' : '600px'" top="5vh">
      <el-form :model="productForm" label-width="80px" size="small">
        <el-tabs value="basic">
          <el-tab-pane label="基本信息" name="basic">
            <el-form-item label="名称">
              <el-input v-model="productForm.name"></el-input>
            </el-form-item>
            <el-form-item label="描述">
              <el-input type="textarea" v-model="productForm.description"></el-input>
            </el-form-item>
            <el-form-item label="价格">
              <el-input-number v-model="productForm.price" :precision="2" :step="0.1" style="width: 150px;"></el-input-number>
            </el-form-item>
            <el-form-item label="库存">
              <el-input-number v-model="productForm.stock" :min="0" style="width: 150px;"></el-input-number>
            </el-form-item>
            <el-form-item label="商品图片">
              <el-upload
                class="product-image-uploader"
                action="/api/files/upload"
                :show-file-list="false"
                :on-success="handleProductImageSuccess"
                :before-upload="beforeProductImageUpload">
                <img v-if="productForm.image" :src="productForm.image" class="product-upload-preview">
                <div v-else class="product-upload-placeholder">
                  <i class="el-icon-plus"></i>
                  <span>点击上传</span>
                </div>
              </el-upload>
            </el-form-item>
            <el-form-item label="数字商品">
              <el-switch v-model="productForm.isDigital" active-text="是" inactive-text="否"></el-switch>
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="规格配置" name="specs">
            <div class="specs-config-container">
              <div v-for="(spec, sIdx) in productForm.specsList" :key="sIdx" class="spec-group-item">
                <div class="spec-group-header">
                  <el-input v-model="spec.name" placeholder="规格名 (如: 尺寸)" size="mini" style="width: 120px;"></el-input>
                  <el-button type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="removeSpecGroup(sIdx)">删除</el-button>
                </div>
                <div class="spec-options-list">
                  <el-tag
                    v-for="(opt, oIdx) in spec.options"
                    :key="oIdx"
                    closable
                    size="small"
                    @close="removeSpecOption(sIdx, oIdx)"
                    style="margin-right: 8px; margin-bottom: 8px;"
                  >
                    {{ opt }}
                  </el-tag>
                  <el-input
                    class="input-new-tag"
                    v-if="spec.inputVisible"
                    v-model="spec.inputValue"
                    ref="saveTagInput"
                    size="mini"
                    style="width: 80px;"
                    @keyup.enter.native="handleInputConfirm(sIdx)"
                    @blur="handleInputConfirm(sIdx)"
                  >
                  </el-input>
                  <el-button v-else class="button-new-tag" size="mini" @click="showInput(sIdx)">+ 新增选项</el-button>
                </div>
              </div>
              <el-button type="dashed" icon="el-icon-plus" style="width: 100%; border: 1px dashed #DCDFE6; margin-top: 10px;" @click="addSpecGroup">添加规格组 (如: 甜度、分量)</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
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
        isDigital: true,
        stock: 0,
        specsList: []
      },
      savingConfig: false,
      homeConfigForm: {
        avatarUrl: '',
        authorName: '',
        authorBio: '',
        tagsString: ''
      }
    }
  },
  created() {
    this.fetchArticles();
    this.fetchProducts();
    this.fetchUsers();
    this.fetchOrders();
    this.fetchGroups();
    this.fetchHomeConfig();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleProductImageSuccess(res) {
      this.productForm.image = res.url;
      this.$message.success('图片上传成功');
    },
    beforeProductImageUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImg) this.$message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt2M) this.$message.error('图片大小不能超过 2MB!');
      return isImg && isLt2M;
    },
    addSpecGroup() {
      this.productForm.specsList.push({
        name: '',
        options: [],
        inputVisible: false,
        inputValue: ''
      });
    },
    removeSpecGroup(index) {
      this.productForm.specsList.splice(index, 1);
    },
    showInput(index) {
      const spec = this.productForm.specsList[index];
      spec.inputVisible = true;
      this.$nextTick(() => {
        const inputs = this.$refs.saveTagInput;
        if (inputs && inputs[index]) {
          inputs[index].$refs.input.focus();
        }
      });
    },
    handleInputConfirm(index) {
      const spec = this.productForm.specsList[index];
      let inputValue = spec.inputValue;
      if (inputValue) {
        spec.options.push(inputValue);
      }
      spec.inputVisible = false;
      spec.inputValue = '';
    },
    removeSpecOption(sIdx, oIdx) {
      this.productForm.specsList[sIdx].options.splice(oIdx, 1);
    },
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
      this.productForm = { 
        id: null, 
        name: '', 
        description: '', 
        price: 0, 
        image: '', 
        isDigital: true,
        stock: 99,
        specsList: [] 
      };
      this.productDialogVisible = true;
    },
    handleEditProduct(product) {
      this.isEditing = true;
      let specsList = [];
      try {
        if (product.specs) {
          specsList = JSON.parse(product.specs).map(s => ({
            ...s,
            inputVisible: false,
            inputValue: ''
          }));
        }
      } catch (e) {
        console.error('Parse specs error', e);
      }
      this.productForm = { 
        ...product,
        specsList: specsList
      };
      this.productDialogVisible = true;
    },
    async saveProduct() {
      try {
        const submitData = { ...this.productForm };
        submitData.specs = JSON.stringify(this.productForm.specsList.map(s => ({
          name: s.name,
          options: s.options
        })));
        delete submitData.specsList;

        if (this.isEditing) {
          await axios.put(`/api/products/${this.productForm.id}`, submitData, { headers: this.getAuthHeader() });
        } else {
          await axios.post('/api/products', submitData, { headers: this.getAuthHeader() });
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
    },
    async fetchHomeConfig() {
      try {
        const res = await axios.get('/api/home/config');
        if (res.data && res.data.data) {
          const data = res.data.data;
          this.homeConfigForm = {
            avatarUrl: data.avatarUrl,
            authorName: data.authorName,
            authorBio: data.authorBio,
            tagsString: data.tags ? data.tags.join(',') : ''
          };
        }
      } catch (error) {
        this.$message.error('获取系统配置失败');
      }
    },
    handleHomeAvatarSuccess(res) {
      this.homeConfigForm.avatarUrl = res.url;
      this.$message.success('头像上传成功');
    },
    async saveHomeConfig() {
      this.savingConfig = true;
      try {
        const payload = {
          avatarUrl: this.homeConfigForm.avatarUrl,
          authorName: this.homeConfigForm.authorName,
          authorBio: this.homeConfigForm.authorBio,
          tags: this.homeConfigForm.tagsString.split(',').map(s => s.trim()).filter(Boolean)
        };
        await axios.post('/api/home/config', payload, { headers: this.getAuthHeader() });
        this.$message.success('系统配置保存成功！');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '保存失败');
      } finally {
        this.savingConfig = false;
      }
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

/* Custom Admin List Styles */
.custom-admin-list {
  margin-top: 20px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eee;
}

.list-header {
  display: flex;
  background: #f8f9fa;
  padding: 12px 15px;
  font-weight: bold;
  font-size: 14px;
  color: #5C433B;
  border-bottom: 1px solid #eee;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.list-item:hover {
  background: #fffaf9;
}

.col-id { width: 60px; font-family: monospace; color: #999; }
.col-cover { width: 80px; }
.col-info { flex: 1; padding: 0 20px; }
.col-stats { width: 100px; text-align: center; }
.col-actions { width: 120px; text-align: right; }

.truncate-id {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  cursor: help;
}

.item-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: #f0f0f0;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #ccc;
  font-size: 20px;
}

.item-title {
  font-weight: bold;
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.stat-badge {
  background: #fff0ed;
  color: #FF7E67;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.empty-placeholder {
  padding: 60px 0;
  text-align: center;
  color: #ccc;
}
.empty-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .list-header { display: none; }
  .list-item { flex-wrap: wrap; padding: 12px; }
  .col-id { width: 100%; margin-bottom: 8px; font-size: 12px; }
  .col-cover { width: 60px; }
  .col-info { width: calc(100% - 60px); padding-left: 12px; }
  .col-stats { width: 50%; text-align: left; padding-top: 10px; }
  .col-actions { width: 50%; padding-top: 10px; }
}
/* Specs Config Styles */
.specs-config-container {
  padding: 10px 0;
}
.spec-group-item {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 15px;
  border: 1px solid #eee;
}
.spec-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px dashed #ddd;
  padding-bottom: 8px;
}
.spec-options-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.button-new-tag {
  height: 24px;
  line-height: 22px;
  padding-top: 0;
  padding-bottom: 0;
}
.input-new-tag {
  vertical-align: bottom;
}
/* Product Image Uploader */
.product-image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  transition: border-color 0.3s;
  background: #fafafa;
}
.product-image-uploader:hover {
  border-color: #FF7E67;
}
.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #8c939d;
}
.product-upload-placeholder i {
  font-size: 28px;
  margin-bottom: 8px;
}
.product-upload-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>
