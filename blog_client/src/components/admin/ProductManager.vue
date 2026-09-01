<template>
  <div class="product-manager">
    <!-- 顶部操作栏 -->
    <div class="header-action-bar">
      <div class="left-actions">
        <a-button type="primary" shape="round" @click="openCreateProductDialog">
          <template #icon><icon-plus /></template>
          上架新商品
        </a-button>
        <a-button type="outline" shape="round" @click="categoryDialogVisible = true">
          <template #icon><icon-tags /></template>
          分类管理
        </a-button>
      </div>
      <div class="right-filters">
        <a-select 
          v-model="selectedCategoryId" 
          placeholder="全部分类" 
          allow-clear 
          style="width: 140px;" 
          @change="fetchProducts"
        >
          <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
        </a-select>
        <a-input-search 
          v-model="searchKeyword" 
          placeholder="搜索商品名称..." 
          style="width: 180px;" 
          @search="fetchProducts" 
        />
      </div>
    </div>

    <!-- PC 端表格视图 -->
    <a-table 
      v-if="!isMobile" 
      :data="products" 
      :loading="loadingProducts" 
      stripe 
      style="margin-top: 16px;" 
      :pagination="{ pageSize: 10 }"
    >
      <template #columns>
        <a-table-column title="商品图片" :width="90">
          <template #cell="{ record }">
            <img v-if="record.image" :src="record.image" class="table-prod-img" />
            <span v-else class="text-muted">无图</span>
          </template>
        </a-table-column>
        <a-table-column title="商品名称" data-index="name" />
        <a-table-column title="分类" :width="110">
          <template #cell="{ record }">
            <a-tag size="small" color="arcoblue">{{ getCategoryName(record.categoryId) }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="价格" :width="110">
          <template #cell="{ record }">
            <span class="price-text">¥{{ record.price }}</span>
          </template>
        </a-table-column>
        <a-table-column title="库存" :width="100">
          <template #cell="{ record }">
            <span>{{ record.stock === -1 ? '不限' : record.stock }}</span>
          </template>
        </a-table-column>
        <a-table-column title="类型" :width="100">
          <template #cell="{ record }">
            <a-tag :color="record.isDigital ? 'green' : 'orangered'" size="small">
              {{ record.isDigital ? '数字' : '实物' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="140" fixed="right">
          <template #cell="{ record }">
            <a-button type="text" size="small" @click="openEditProductDialog(record)">编辑</a-button>
            <a-button type="text" status="danger" size="small" @click="handleDeleteProduct(record)">删除</a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端卡片视图 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingProducts" style="width: 100%; display: block;">
        <div v-for="prod in products" :key="prod.id" class="mobile-card-item">
          <div class="card-cover-row">
            <img v-if="prod.image" :src="prod.image" class="mobile-prod-img" />
            <div class="mobile-prod-info">
              <h4 class="mobile-prod-title">{{ prod.name }}</h4>
              <div class="mobile-prod-tags">
                <a-tag size="small" color="arcoblue">{{ getCategoryName(prod.categoryId) }}</a-tag>
                <a-tag :color="prod.isDigital ? 'green' : 'orangered'" size="small">
                  {{ prod.isDigital ? '数字商品' : '实物' }}
                </a-tag>
              </div>
              <div class="mobile-prod-price-row">
                <span class="mobile-price">¥{{ prod.price }}</span>
                <span class="mobile-stock">库存: {{ prod.stock === -1 ? '不限量' : prod.stock }}</span>
              </div>
            </div>
          </div>
          <div class="mobile-card-actions">
            <a-button type="outline" size="small" shape="round" @click="openEditProductDialog(prod)">
              <template #icon><icon-edit /></template> 编辑商品
            </a-button>
            <a-button type="primary" status="danger" size="small" shape="round" @click="handleDeleteProduct(prod)">
              <template #icon><icon-delete /></template> 下架删除
            </a-button>
          </div>
        </div>
        <a-empty v-if="products.length === 0 && !loadingProducts" description="暂无商品" />
      </a-spin>
    </div>

    <!-- 分类管理抽屉 -->
    <CategoryManagerDialog 
      v-model:show="categoryDialogVisible" 
      :is-mobile="isMobile"
      :categories="categories"
      @change="fetchCategories"
    />

    <!-- 添加/编辑商品现代标准 Bottom Sheet 抽屉 (彻底解决灵动岛遮挡与PC老气弹窗问题) -->
    <a-modal 
      :visible="productDialogVisible" 
      :width="isMobile ? '100%' : '580px'" 
      :footer="false"
      :header="false"
      :mask-closable="true"
      @cancel="productDialogVisible = false" 
      unmount-on-close
    >
      <div class="sheet-modern-container">
        <!-- 移动端顶部拉手横杠 -->
        <div class="sheet-handle-bar" v-if="isMobile"></div>
        
        <!-- 右上角磨砂圆圈关闭按钮 -->
        <button class="sheet-circle-close" @click="productDialogVisible = false" aria-label="关闭">
          <icon-close />
        </button>

        <div class="sheet-header">
          <h3 class="sheet-title">{{ isEditing ? '编辑商品详情' : '上架全新商品' }}</h3>
          <p class="sheet-subtitle">设置商品基础信息、图片素材与规格参数</p>
        </div>

        <!-- 极简微胶囊 Segment Tabs -->
        <div class="sheet-tabs-wrap">
          <button 
            class="sheet-tab-btn" 
            :class="{ active: currentTab === 'basic' }" 
            @click="currentTab = 'basic'"
          >
            基本信息
          </button>
          <button 
            class="sheet-tab-btn" 
            :class="{ active: currentTab === 'specs' }" 
            @click="currentTab = 'specs'"
          >
            规格配置
          </button>
        </div>

        <div class="sheet-body">
          <!-- 1. 基本信息面板 -->
          <div v-show="currentTab === 'basic'" class="custom-form-group">
            <div class="custom-form-item">
              <label class="form-label">商品名称</label>
              <a-input v-model="productForm.name" placeholder="输入商品名称 (如: 法式海盐卷)" size="large" class="luxury-form-input" />
            </div>

            <div class="custom-form-item">
              <label class="form-label">所属分类 <span class="label-tag">选填</span></label>
              <a-select v-model="productForm.categoryId" placeholder="选择分类" allow-clear size="large" class="luxury-select">
                <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
              </a-select>
            </div>

            <div class="custom-form-item">
              <label class="form-label">商品描述</label>
              <a-textarea v-model="productForm.description" placeholder="输入商品风味特色与发货说明..." :auto-size="{ minRows: 2, maxRows: 4 }" class="luxury-form-textarea" />
            </div>

            <div class="form-row-two">
              <div class="custom-form-item">
                <label class="form-label">售价 (元)</label>
                <a-input-number v-model="productForm.price" :precision="2" :step="1" placeholder="0.00" size="large" class="luxury-form-input" />
              </div>
              <div class="custom-form-item">
                <label class="form-label">库存 (-1为不限)</label>
                <a-input-number v-model="productForm.stock" :min="-1" placeholder="-1" size="large" class="luxury-form-input" />
              </div>
            </div>

            <!-- 商品主图上传 -->
            <div class="custom-form-item">
              <label class="form-label">商品封面大图</label>
              <a-upload
                :action="uploadAction"
                :show-file-list="false"
                @success="handleProductImageSuccess"
                @before-upload="beforeProductImageUpload"
              >
                <template #upload-button>
                  <div class="product-image-uploader-card">
                    <img v-if="productForm.image" :src="productForm.image" class="product-upload-preview" />
                    <div v-else class="product-upload-placeholder">
                      <icon-camera class="camera-icon" />
                      <span>点击上传封面图片</span>
                    </div>
                  </div>
                </template>
              </a-upload>
            </div>

            <div class="custom-form-item digital-switch-row">
              <div class="switch-text">
                <span class="switch-title">是否为数字/虚拟商品</span>
                <span class="switch-desc">数字商品购买后自动发货/免填收货地址</span>
              </div>
              <a-switch v-model="productForm.isDigital" />
            </div>
          </div>

          <!-- 2. 规格配置面板 -->
          <div v-show="currentTab === 'specs'" class="custom-form-group">
            <div class="specs-config-container">
              <div v-for="(spec, sIdx) in productForm.specsList" :key="sIdx" class="spec-group-card">
                <div class="spec-group-header">
                  <a-input v-model="spec.name" placeholder="规格名 (如: 甜度、尺寸)" size="small" class="spec-name-input" />
                  <a-button type="text" status="danger" size="small" @click="removeSpecGroup(sIdx)">
                    <template #icon><icon-delete /></template> 删除规格
                  </a-button>
                </div>
                <div class="spec-options-list">
                  <a-tag
                    v-for="(opt, oIdx) in spec.options"
                    :key="oIdx"
                    closable
                    @close="removeSpecOption(sIdx, oIdx)"
                    class="luxury-spec-tag"
                  >
                    {{ opt }}
                  </a-tag>
                  <a-input
                    class="input-new-tag"
                    v-if="spec.inputVisible"
                    v-model="spec.inputValue"
                    :ref="'saveTagInput' + sIdx"
                    size="small"
                    style="width: 90px;"
                    placeholder="输入选项"
                    @press-enter="handleInputConfirm(sIdx)"
                    @blur="handleInputConfirm(sIdx)"
                  />
                  <a-button v-else class="button-new-tag" size="small" shape="round" @click="showInput(sIdx)">
                    <template #icon><icon-plus /></template> 添加选项
                  </a-button>
                </div>
              </div>

              <button class="add-spec-group-btn" @click="addSpecGroup">
                <icon-plus /> <span>新增规格分组 (如: 尺寸/口味/分量)</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 底部吸底保存大胶囊按钮 -->
        <div class="sheet-footer-action">
          <button class="sheet-main-btn" @click="saveProduct">
            <span>{{ isEditing ? '保存商品修改' : '确认上架商品' }}</span>
          </button>
        </div>
      </div>
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
      currentTab: 'basic',
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
  computed: {
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/common/upload';
    }
  },
  created() {
    this.fetchCategories();
    this.fetchProducts();
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await getProductCategories();
        this.categories = res.data.data || [];
      } catch (e) {
        // ignore
      }
    },
    getCategoryName(id) {
      const cat = this.categories.find(c => c.id === id);
      return cat ? cat.name : '未分类';
    },
    async fetchProducts() {
      this.loadingProducts = true;
      try {
        const res = await getProducts();
        let list = res.data.data || [];
        if (this.selectedCategoryId) {
          list = list.filter(p => p.categoryId === this.selectedCategoryId);
        }
        if (this.searchKeyword) {
          list = list.filter(p => p.name && p.name.includes(this.searchKeyword));
        }
        this.products = list;
      } catch (e) {
        Message.error('获取商品列表失败');
      } finally {
        this.loadingProducts = false;
      }
    },
    openCreateProductDialog() {
      this.isEditing = false;
      this.currentTab = 'basic';
      this.productForm = {
        id: null,
        name: '',
        description: '',
        price: 0,
        image: '',
        isDigital: true,
        stock: -1,
        categoryId: null,
        specsList: []
      };
      this.productDialogVisible = true;
    },
    openEditProductDialog(prod) {
      this.isEditing = true;
      this.currentTab = 'basic';
      let specs = [];
      try {
        if (prod.specs) {
          specs = typeof prod.specs === 'string' ? JSON.parse(prod.specs) : prod.specs;
        }
      } catch (e) {
        specs = [];
      }
      this.productForm = {
        ...prod,
        specsList: specs.map(s => ({ ...s, inputVisible: false, inputValue: '' }))
      };
      this.productDialogVisible = true;
    },
    beforeProductImageUpload(file) {
      const isImg = file.type.startsWith('image/');
      if (!isImg) {
        Message.error('只能上传图片文件');
        return false;
      }
      return true;
    },
    handleProductImageSuccess(fileItem) {
      const res = fileItem.response;
      let url = (res && res.data) ? res.data : ((res && res.url) ? res.url : (typeof res === 'string' ? res : ''));
      this.productForm.image = url;
      Message.success('封面图片已上传');
    },
    addSpecGroup() {
      this.productForm.specsList.push({
        name: '',
        options: [],
        inputVisible: false,
        inputValue: ''
      });
    },
    removeSpecGroup(idx) {
      this.productForm.specsList.splice(idx, 1);
    },
    removeSpecOption(sIdx, oIdx) {
      this.productForm.specsList[sIdx].options.splice(oIdx, 1);
    },
    showInput(sIdx) {
      this.productForm.specsList[sIdx].inputVisible = true;
      this.$nextTick(() => {
        const inputRef = this.$refs['saveTagInput' + sIdx];
        if (inputRef && inputRef[0]) {
          inputRef[0].focus();
        }
      });
    },
    handleInputConfirm(sIdx) {
      const spec = this.productForm.specsList[sIdx];
      if (spec.inputValue && spec.inputValue.trim()) {
        if (!spec.options) spec.options = [];
        spec.options.push(spec.inputValue.trim());
      }
      spec.inputVisible = false;
      spec.inputValue = '';
    },
    async saveProduct() {
      if (!this.productForm.name || !this.productForm.name.trim()) {
        return Message.warning('请输入商品名称');
      }
      const payload = {
        ...this.productForm,
        specs: JSON.stringify(this.productForm.specsList.map(s => ({
          name: s.name,
          options: s.options
        })))
      };

      try {
        if (this.isEditing) {
          await updateProduct(payload.id, payload);
          Message.success('商品更新成功');
        } else {
          await saveProduct(payload);
          Message.success('商品上架成功');
        }
        this.productDialogVisible = false;
        this.fetchProducts();
      } catch (e) {
        Message.error(e.response?.data?.message || '操作失败');
      }
    },
    handleDeleteProduct(prod) {
      Modal.confirm({
        title: '提示',
        content: `确定要删除商品 "${prod.name}" 吗？`,
        onOk: async () => {
          try {
            await deleteProduct(prod.id);
            Message.success('商品已删除');
            this.fetchProducts();
          } catch (e) {
            Message.error('删除失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.product-manager {
  padding: 10px 0;
}

.header-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.left-actions, .right-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-prod-img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.price-text {
  font-weight: 700;
  color: #FF3B30;
}

.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.mobile-card-item {
  background: #F7F8FA;
  border-radius: 16px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-cover-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mobile-prod-img {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
}

.mobile-prod-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-prod-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-prod-tags {
  display: flex;
  gap: 6px;
}

.mobile-prod-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.mobile-price {
  font-size: 16px;
  font-weight: 800;
  color: #FF3B30;
}

.mobile-stock {
  font-size: 11px;
  color: #86909C;
}

.mobile-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  padding-top: 10px;
}

/* 标准抽屉样式 */
.sheet-modern-container {
  padding: 16px 18px 24px;
  position: relative;
}

.sheet-header {
  text-align: center;
  margin-bottom: 16px;
}

.sheet-title {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.sheet-subtitle {
  margin: 0;
  font-size: 12px;
  color: #86909C;
}

/* 微胶囊 Segment Tabs */
.sheet-tabs-wrap {
  display: flex;
  background: #F2F3F5;
  border-radius: 12px;
  padding: 3px;
  margin-bottom: 16px;
}

.sheet-tab-btn {
  flex: 1;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #86909C;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sheet-tab-btn.active {
  background: #FFFFFF;
  color: #1D2129;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.custom-form-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.custom-form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row-two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.form-label {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}

.label-tag {
  font-size: 10px;
  color: #86909C;
  font-weight: 500;
}

:deep(.luxury-form-input .arco-input-wrapper),
:deep(.luxury-form-input.arco-input-number) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}

:deep(.luxury-form-textarea) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}

:deep(.luxury-select .arco-select-view-single) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}

/* 图片上传卡片 */
.product-image-uploader-card {
  width: 100%;
  height: 110px;
  border-radius: 14px;
  background: #F7F8FA;
  border: 1px dashed #E5E6EB;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s ease;
}
.product-image-uploader-card:hover {
  border-color: #FF5E3A;
  background: #FFF9F8;
}

.product-upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #86909C;
  font-size: 12px;
}
.camera-icon {
  font-size: 24px;
  color: #FF5E3A;
}

.digital-switch-row {
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  background: #F7F8FA;
  padding: 12px 14px;
  border-radius: 14px;
}

.switch-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.switch-title {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}

.switch-desc {
  font-size: 11px;
  color: #86909C;
}

/* 规格配置 */
.spec-group-card {
  background: #F7F8FA;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 12px;
}

.spec-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

:deep(.spec-name-input .arco-input-wrapper) {
  border-radius: 8px !important;
  background: #FFFFFF !important;
}

.spec-options-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.luxury-spec-tag {
  background: #FFFFFF;
  border-radius: 6px;
  border: 1px solid #E5E6EB;
}

.add-spec-group-btn {
  width: 100%;
  height: 40px;
  border-radius: 12px;
  background: #F2F3F5;
  border: 1px dashed #C9CDD4;
  color: #4E5969;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.add-spec-group-btn:active {
  background: #E5E6EB;
}

.sheet-footer-action {
  margin-top: 24px;
}

.sheet-main-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(255, 42, 84, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.sheet-main-btn:active {
  transform: scale(0.96);
}
</style>
