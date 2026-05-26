<template>
  <div class="product-manager">
    <div class="tab-header">
      <el-button type="success" size="small" icon="el-icon-plus" @click="showAddProductDialog">上架新商品</el-button>
    </div>

    <el-table :data="products" v-loading="loadingProducts" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="商品图" width="100">
        <template slot-scope="scope">
          <el-image :src="scope.row.image" style="width: 50px; height: 50px; border-radius: 8px;" fit="cover"></el-image>
        </template>
      </el-table-column>
      <el-table-column label="商品详情">
        <template slot-scope="scope">
          <div style="font-weight: bold; font-size: 15px; color: #333;">
            {{ scope.row.name }}
            <el-tag :type="scope.row.isDigital ? 'success' : 'info'" size="mini" style="margin-left: 8px;">
              {{ scope.row.isDigital ? '数字' : '实体' }}
            </el-tag>
          </div>
          <div style="font-size: 12px; color: #999;">{{ scope.row.description }}</div>
        </template>
      </el-table-column>
      <el-table-column label="库存/价格" width="150">
        <template slot-scope="scope">
          <div :style="{ fontWeight: 'bold', color: scope.row.stock <= 10 ? '#F56C6C' : '#67C23A', marginBottom: '4px' }">
            库存: {{ scope.row.stock || 0 }}
          </div>
          <div style="font-weight: bold; color: #FF7E67;">¥{{ scope.row.price }}</div>
          <div style="font-size: 11px; color: #999;">团: ¥{{ scope.row.groupPrice }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleEditProduct(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" style="color: #F56C6C;" @click="handleDeleteProduct(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
  name: 'ProductManager',
  data() {
    return {
      products: [],
      loadingProducts: false,
      productDialogVisible: false,
      isEditing: false,
      isMobile: window.innerWidth <= 768,
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
  border: 1px dashed #d9d9d9;
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
  border-color: #409EFF;
}
.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
}
.product-upload-placeholder i {
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
  border: 1px solid #ebeef5;
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
