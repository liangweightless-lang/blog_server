<template>
  <div class="product-manager-layout" :class="{ 'is-mobile': isMobile }">
    <!-- 左侧分类侧边栏 -->
    <div class="category-sidebar">
      <div class="category-list">
        <div 
          class="category-item" 
          :class="{ active: selectedCategoryId === null }"
          @click="selectedCategoryId = null"
        >
          全部
        </div>
        <div 
          class="category-item" 
          v-for="cat in categories" 
          :key="cat.id"
          :class="{ active: selectedCategoryId === cat.id }"
          @click="selectedCategoryId = cat.id"
        >
          {{ cat.name }}
        </div>
      </div>
      <div class="sidebar-footer">
        <a-button type="text" size="small" @click="categoryDialogVisible = true" style="color: #4E5969; width: 100%;">
          <template #icon><icon-settings /></template>
          管理分类
        </a-button>
      </div>
    </div>

    <!-- 右侧主体内容 -->
    <div class="product-main-content">
      <!-- 顶部操作栏 -->
      <div class="main-header">
        <a-button type="outline" status="success" style="flex: 1;" @click="categoryDialogVisible = true">
          <template #icon><icon-settings /></template>管理分类
        </a-button>
        <a-button type="primary" status="success" style="flex: 1;" @click="showAddProductDialog">
          <template #icon><icon-plus /></template>添加商品
        </a-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar-wrapper">
        <a-input-search 
          v-model="searchKeyword" 
          placeholder="搜索商品名称" 
          class="search-input"
        />
      </div>

      <!-- 当前分类标题 -->
      <div class="category-title-bar">
        <span class="title">{{ currentCategoryName }}</span>
        <span class="count">共 {{ filteredProducts.length }} 件</span>
      </div>

      <!-- 商品列表 -->
      <div class="product-list-wrapper">
        <a-spin :loading="loadingProducts" style="width: 100%; min-height: 200px; display: block;">
          <div class="product-card-list">
            <div v-for="product in filteredProducts" :key="product.id" class="product-card">
              <img :src="product.image || 'https://via.placeholder.com/150'" class="product-cover" />
              <div class="product-info">
                <div class="product-name">
                  {{ product.name }}
                  <a-tag :color="product.isDigital ? 'green' : 'gray'" size="small" class="type-tag">
                    {{ product.isDigital ? '数字' : '实体' }}
                  </a-tag>
                </div>
                <div class="product-desc" v-if="product.description">{{ product.description }}</div>
                <div class="product-stock" :style="{ color: product.stock <= 10 ? '#F56C6C' : '#86909c' }">
                  库存: {{ product.stock === -1 ? '不限' : product.stock }}
                </div>
                <div class="product-bottom">
                  <span class="price-text">¥<span class="price-num">{{ product.price }}</span></span>
                  <div class="actions">
                    <span class="action-btn" @click="handleCopyProduct(product)">复制</span>
                    <span class="action-btn" @click="handleEditProduct(product)">编辑</span>
                    <span class="action-btn delete" @click="handleDeleteProduct(product)">删除</span>
                  </div>
                </div>
              </div>
            </div>
            <a-empty v-if="filteredProducts.length === 0 && !loadingProducts" description="暂无商品" />
          </div>
        </a-spin>
      </div>

      <!-- 底部全选/去开团栏 (仅展示 UI) -->
      <div class="bottom-action-bar">
        <a-checkbox>全选</a-checkbox>
        <span class="selected-count">共0件</span>
        <a-button type="primary" status="success" size="large" class="groupbuy-btn">去开团</a-button>
      </div>
    </div>

    <!-- 管理分类弹窗 -->
    <CategoryManagerDialog 
      v-model:show="categoryDialogVisible" 
      :is-mobile="isMobile"
      :categories="categories"
      @change="fetchCategories"
    />

    <!-- 添加/编辑商品弹窗 -->
    <a-modal :title="isEditing ? '编辑商品' : '上架新商品'" :visible="productDialogVisible" :width="isMobile ? '95%' : '600px'" @cancel="productDialogVisible = false" @ok="saveProduct" unmount-on-close>
      <a-form :model="productForm" layout="vertical">
        <a-tabs default-active-key="basic">
          <a-tab-pane title="基本信息" key="basic">
            <a-form-item label="所属分类">
              <a-select v-model="productForm.categoryId" placeholder="请选择分类 (可选)" allow-clear>
                <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item label="名称">
              <a-input v-model="productForm.name"></a-input>
            </a-form-item>
            <a-form-item label="描述">
              <a-textarea v-model="productForm.description" :auto-size="{minRows: 2}"></a-textarea>
            </a-form-item>
            <a-form-item label="价格">
              <a-input-number v-model="productForm.price" :precision="2" :step="0.1" style="width: 150px;"></a-input-number>
            </a-form-item>
            <a-form-item label="库存 (-1代表不限)">
              <a-input-number v-model="productForm.stock" :min="-1" style="width: 150px;"></a-input-number>
            </a-form-item>
            <a-form-item label="商品图片">
              <a-upload
                :action="uploadAction"
                :show-file-list="false"
                @success="handleProductImageSuccess"
                @before-upload="beforeProductImageUpload">
                <template #upload-button>
                  <div class="product-image-uploader">
                    <img v-if="productForm.image" :src="productForm.image" class="product-upload-preview">
                    <div v-else class="product-upload-placeholder">
                      <icon-plus />
                      <span>点击上传</span>
                    </div>
                  </div>
                </template>
              </a-upload>
            </a-form-item>
            <a-form-item label="数字商品">
              <a-switch v-model="productForm.isDigital">
                <template #checked>是</template>
                <template #unchecked>否</template>
              </a-switch>
            </a-form-item>
          </a-tab-pane>
          
          <a-tab-pane title="规格配置" key="specs">
            <div class="specs-config-container">
              <div v-for="(spec, sIdx) in productForm.specsList" :key="sIdx" class="spec-group-item">
                <div class="spec-group-header">
                  <a-input v-model="spec.name" placeholder="规格名 (如: 尺寸)" size="small" style="width: 120px;"></a-input>
                  <a-button type="text" status="danger" @click="removeSpecGroup(sIdx)">
                    <template #icon><icon-delete /></template>删除
                  </a-button>
                </div>
                <div class="spec-options-list">
                  <a-tag
                    v-for="(opt, oIdx) in spec.options"
                    :key="oIdx"
                    closable
                    @close="removeSpecOption(sIdx, oIdx)"
                    style="margin-right: 8px; margin-bottom: 8px;"
                  >
                    {{ opt }}
                  </a-tag>
                  <a-input
                    class="input-new-tag"
                    v-if="spec.inputVisible"
                    v-model="spec.inputValue"
                    :ref="'saveTagInput' + sIdx"
                    size="small"
                    style="width: 80px;"
                    @press-enter="handleInputConfirm(sIdx)"
                    @blur="handleInputConfirm(sIdx)"
                  >
                  </a-input>
                  <a-button v-else class="button-new-tag" size="small" @click="showInput(sIdx)">
                    <template #icon><icon-plus /></template>新增选项
                  </a-button>
                </div>
              </div>
              <a-button type="dashed" style="width: 100%; margin-top: 10px;" @click="addSpecGroup">
                <template #icon><icon-plus /></template>添加规格组 (如: 甜度、分量)
              </a-button>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { getProducts, updateProduct, saveProduct, deleteProduct } from '@/api/product';
import { getProductCategories } from '@/api/productCategory';
import CategoryManagerDialog from './CategoryManagerDialog.vue';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'ProductManager',
  components: { CategoryManagerDialog },
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      products: [],
      categories: [],
      loadingProducts: false,
      productDialogVisible: false,
      categoryDialogVisible: false,
      isEditing: false,
      selectedCategoryId: null,
      searchKeyword: '',
      productForm: {
        id: null,
        name: '',
        description: '',
        price: 0,
        image: '',
        isDigital: true,
        stock: -1,
        categoryId: null,
        specsList: []
      }
    }
  },
  created() {
    this.fetchCategories();
    this.fetchProducts();
  },
  computed: {
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    },
    currentCategoryName() {
      if (this.selectedCategoryId === null) return '全部商品';
      const cat = this.categories.find(c => c.id === this.selectedCategoryId);
      return cat ? cat.name : '全部商品';
    },
    filteredProducts() {
      let result = this.products;
      if (this.selectedCategoryId !== null) {
        result = result.filter(p => p.categoryId === this.selectedCategoryId);
      }
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase();
        result = result.filter(p => p.name.toLowerCase().includes(kw));
      }
      return result;
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await getProductCategories();
        this.categories = res.data.data;
      } catch (error) {
        console.error(error);
      }
    },
    async fetchProducts() {
      this.loadingProducts = true;
      try {
        const res = await getProducts();
        this.products = res.data.data;
      } catch (error) {
        Message.error('加载商品失败');
      } finally {
        this.loadingProducts = false;
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
        stock: -1,
        categoryId: this.selectedCategoryId, // 默认选中当前分类
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
          await updateProduct(this.productForm.id, submitData);
        } else {
          await saveProduct(submitData);
        }
        Message.success('保存成功');
        this.productDialogVisible = false;
        this.fetchProducts();
      } catch (error) {
        Message.error('保存失败');
      }
    },
    handleCopyProduct(product) {
      this.isEditing = false;
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
      }
      this.productForm = { 
        ...product,
        id: null,
        name: product.name + ' (副本)',
        specsList: specsList
      };
      this.productDialogVisible = true;
    },
    handleDeleteProduct(product) {
      Modal.confirm({
        title: '提示',
        content: '确定要下架并删除该商品吗？',
        onOk: async () => {
          try {
            await deleteProduct(product.id);
            Message.success('已下架');
            this.fetchProducts();
          } catch (error) {
            Message.error('操作失败');
          }
        }
      });
    },
    handleProductImageSuccess(fileItem) {
      const res = fileItem.response;
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('图片上传失败，服务器返回了错误的格式。');
        return;
      }
      this.productForm.image = url;
      Message.success('图片上传成功');
    },
    beforeProductImageUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt8M = file.size / 1024 / 1024 < 8;
      if (!isImg) Message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt8M) Message.error('图片大小不能超过 8MB!');
      return isImg && isLt8M;
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
        const inputRef = this.$refs['saveTagInput' + index];
        if (inputRef && inputRef[0]) {
          inputRef[0].focus();
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
    }
  }
}
</script>

<style scoped>
.product-manager-layout {
  display: flex;
  height: calc(100vh - 100px); /* Adjust based on admin layout */
  background: #f7f8fa;
  margin: -20px; /* Counter admin padding if needed, or adjust */
  border-radius: 8px;
  overflow: hidden;
}

.product-manager-layout.is-mobile {
  height: calc(100vh - 60px);
  margin: -10px;
}

/* 左侧侧边栏 */
.category-sidebar {
  width: 90px;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e6eb;
  flex-shrink: 0;
}
.category-list {
  flex: 1;
  overflow-y: auto;
}
.category-item {
  padding: 15px 10px;
  text-align: center;
  font-size: 13px;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.category-item.active {
  background: #ffffff;
  color: #1d2129;
  font-weight: 600;
}
.category-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 15px;
  bottom: 15px;
  width: 3px;
  background: #4ade80; /* Success green */
  border-radius: 0 4px 4px 0;
}
.sidebar-footer {
  padding: 10px 5px;
  background: #fff;
  border-top: 1px solid #e5e6eb;
}

/* 右侧主体内容 */
.product-main-content {
  flex: 1;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}
.main-header {
  padding: 12px 15px;
  display: flex;
  gap: 10px;
}
.search-bar-wrapper {
  padding: 0 15px 12px;
}
.search-input {
  border-radius: 16px;
  background-color: #f7f8fa;
  border: none;
}
.category-title-bar {
  padding: 0 15px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f2f3f5;
}
.category-title-bar .title {
  font-size: 13px;
  font-weight: 600;
  color: #1d2129;
}
.category-title-bar .count {
  font-size: 12px;
  color: #86909c;
}

.product-list-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 10px 15px 60px; /* bottom padding for action bar */
}

.product-card-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.product-card {
  display: flex;
  gap: 12px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f2f3f5;
}
.product-card:last-child {
  border-bottom: none;
}
.product-cover {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #f2f3f5;
}
.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.type-tag {
  transform: scale(0.85);
  transform-origin: left center;
}
.product-desc {
  font-size: 12px;
  color: #86909c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.product-stock {
  font-size: 12px;
  margin-bottom: 8px;
}
.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}
.price-text {
  color: #ff5a34;
  font-size: 12px;
  font-weight: 600;
}
.price-num {
  font-size: 16px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}
.action-btn {
  font-size: 13px;
  color: #4e5969;
  cursor: pointer;
  white-space: nowrap;
  padding: 4px;
  background: #f2f3f5;
  border-radius: 4px;
}
.action-btn:hover {
  color: #165dff;
}
.action-btn.delete {
  color: #f53f3f;
}

/* 底部全选栏 */
.bottom-action-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #ffffff;
  border-top: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  padding: 0 15px;
  gap: 15px;
  z-index: 10;
}
.selected-count {
  font-size: 13px;
  color: #1d2129;
  flex: 1;
  text-align: right;
  margin-right: 10px;
}
.groupbuy-btn {
  width: 100px;
  border-radius: 4px;
}

/* 弹窗中的图片上传器样式 */
.product-image-uploader {
  border: 1px dashed #E5E6EB;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.product-image-uploader:hover {
  border-color: #165DFF;
}
.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
}
.product-upload-placeholder svg {
  font-size: 24px;
  margin-bottom: 8px;
}
.product-upload-preview {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
.specs-config-container {
  padding: 10px 0;
}
.spec-group-item {
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fafafa;
}
.spec-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.spec-options-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.input-new-tag, .button-new-tag {
  height: 24px;
  line-height: 22px;
  padding-top: 0;
  padding-bottom: 0;
}
</style>
