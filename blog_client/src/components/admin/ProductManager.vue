<template>
  <div class="product-manager">
    <div class="tab-header">
      <a-button type="primary" status="success" size="small" @click="showAddProductDialog">
        <template #icon><icon-plus /></template>
        上架新商品
      </a-button>
    </div>

    <a-table v-if="!isMobile" :data="products" :loading="loadingProducts" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column data-index="id" title="ID" :width="80"></a-table-column>
        <a-table-column title="商品图" :width="100">
          <template #cell="{ record }">
            <img :src="record.image" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover;" />
          </template>
        </a-table-column>
        <a-table-column title="商品详情">
          <template #cell="{ record }">
            <div style="font-weight: bold; font-size: 15px; color: #333;">
              {{ record.name }}
              <a-tag :color="record.isDigital ? 'green' : 'gray'" size="small" style="margin-left: 8px;">
                {{ record.isDigital ? '数字' : '实体' }}
              </a-tag>
            </div>
            <div style="font-size: 12px; color: #999;">{{ record.description }}</div>
          </template>
        </a-table-column>
        <a-table-column title="库存/价格" :width="150">
          <template #cell="{ record }">
            <div :style="{ fontWeight: 'bold', color: record.stock <= 10 ? '#F56C6C' : '#67C23A', marginBottom: '4px' }">
              库存: {{ record.stock || 0 }}
            </div>
            <div style="font-weight: bold; color: #FF7E67;">¥{{ record.price }}</div>
            <div style="font-size: 11px; color: #999;">团: ¥{{ record.groupPrice }}</div>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="150" fixed="right">
          <template #cell="{ record }">
            <a-button size="small" type="text" @click="handleEditProduct(record)">编辑</a-button>
            <a-button size="small" type="text" status="danger" @click="handleDeleteProduct(record)">删除</a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端视图: 卡片列表 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingProducts" style="width: 100%; display: block;">
        <div v-for="product in products" :key="product.id" class="mobile-card-item">
          <img :src="product.image" class="card-cover" />
          <div class="card-info">
            <h4 class="card-title">
              {{ product.name }}
              <a-tag :color="product.isDigital ? 'green' : 'gray'" size="small" style="margin-left: 4px; transform: scale(0.8); transform-origin: left center;">
                {{ product.isDigital ? '数字' : '实体' }}
              </a-tag>
            </h4>
            <div class="card-desc">{{ product.description }}</div>
            <div class="card-meta">
              <span class="price-text">¥{{ product.price }}</span>
              <span :style="{ fontSize: '12px', color: product.stock <= 10 ? '#F56C6C' : '#67C23A' }">
                库存: {{ product.stock || 0 }}
              </span>
            </div>
          </div>
          <div class="card-actions" style="display: flex; flex-direction: column; gap: 8px;">
            <a-button type="primary" size="small" shape="circle" @click="handleEditProduct(product)">
              <template #icon><icon-edit /></template>
            </a-button>
            <a-button type="primary" status="danger" size="small" shape="circle" @click="handleDeleteProduct(product)">
              <template #icon><icon-delete /></template>
            </a-button>
          </div>
        </div>
        <a-empty v-if="products.length === 0 && !loadingProducts" description="暂无商品" />
      </a-spin>
    </div>

    <a-modal :title="isEditing ? '编辑商品' : '上架新商品'" :visible="productDialogVisible" :width="isMobile ? '95%' : '600px'" @cancel="productDialogVisible = false" @ok="saveProduct" unmount-on-close>
      <a-form :model="productForm" layout="vertical">
        <a-tabs default-active-key="basic">
          <a-tab-pane title="基本信息" key="basic">
            <a-form-item label="名称">
              <a-input v-model="productForm.name"></a-input>
            </a-form-item>
            <a-form-item label="描述">
              <a-textarea v-model="productForm.description" :auto-size="{minRows: 2}"></a-textarea>
            </a-form-item>
            <a-form-item label="价格">
              <a-input-number v-model="productForm.price" :precision="2" :step="0.1" style="width: 150px;"></a-input-number>
            </a-form-item>
            <a-form-item label="库存">
              <a-input-number v-model="productForm.stock" :min="0" style="width: 150px;"></a-input-number>
            </a-form-item>
            <a-form-item label="商品图片">
              <a-upload
                action="/api/files/upload"
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
import axios from 'axios';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'ProductManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      products: [],
      loadingProducts: false,
      productDialogVisible: false,
      isEditing: false,
      productForm: {
        id: null,
        name: '',
        description: '',
        price: 0,
        image: '',
        isDigital: true,
        stock: 0,
        specsList: []
      }
    }
  },
  created() {
    this.fetchProducts();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      // isMobile handled via props
    },
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchProducts() {
      this.loadingProducts = true;
      try {
        const res = await axios.get('/api/products');
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
        Message.success('保存成功');
        this.productDialogVisible = false;
        this.fetchProducts();
      } catch (error) {
        Message.error('保存失败');
      }
    },
    handleDeleteProduct(product) {
      Modal.confirm({
        title: '提示',
        content: '确定要下架并删除该商品吗？',
        onOk: async () => {
          try {
            await axios.delete(`/api/products/${product.id}`, { headers: this.getAuthHeader() });
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
      this.productForm.image = (res && res.url) ? res.url : res;
      Message.success('图片上传成功');
    },
    beforeProductImageUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImg) Message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt2M) Message.error('图片大小不能超过 2MB!');
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
.tab-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}
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

/* 移动端卡片列表样式 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}
.mobile-card-item {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 12px;
  gap: 12px;
  align-items: flex-start;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.card-cover {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-title {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
}
.card-desc {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price-text {
  font-weight: bold;
  color: #FF7E67;
  font-size: 14px;
}
</style>
