<template>
  <a-modal
    :visible="visible"
    :footer="false"
    :header="false"
    :width="isMobile ? '100%' : '520px'"
    :mask-closable="true"
    @cancel="visible = false"
    unmount-on-close
  >
    <div class="sheet-modern-container">
      <div class="sheet-handle-bar" v-if="isMobile"></div>
      
      <button class="sheet-circle-close" @click="visible = false" aria-label="关闭">
        <icon-close />
      </button>

      <div class="sheet-header">
        <h3 class="sheet-title">商品分类管理</h3>
        <p class="sheet-subtitle">新增、重命名或删除商品分类</p>
      </div>

      <div class="sheet-body">
        <!-- 新增分类输入栏 -->
        <div class="add-category-bar">
          <a-input 
            v-model="newCategoryName" 
            placeholder="输入新分类名称 (如: 法式甜点)" 
            allow-clear 
            size="large"
            class="luxury-form-input"
            @keyup.enter="handleAdd"
          />
          <button class="add-cat-btn" @click="handleAdd">添加</button>
        </div>
        
        <a-spin :loading="loading" style="width: 100%; min-height: 120px; display: block; margin-top: 16px;">
          <div class="category-list">
            <div v-for="item in localCategories" :key="item.id" class="category-item-card">
              <div class="cat-left">
                <span class="cat-bullet">✦</span>
                <a-input v-if="editingId === item.id" v-model="editName" size="small" class="cat-edit-input" />
                <span v-else class="cat-name-text">{{ item.name }}</span>
              </div>
              <div class="category-actions">
                <template v-if="editingId === item.id">
                  <a-button type="text" size="small" @click="saveEdit(item)">保存</a-button>
                  <a-button type="text" size="small" @click="cancelEdit">取消</a-button>
                </template>
                <template v-else>
                  <a-button type="text" size="small" @click="startEdit(item)">编辑</a-button>
                  <a-button type="text" size="small" status="danger" @click="handleDelete(item)">删除</a-button>
                </template>
              </div>
            </div>
          </div>
        </a-spin>
      </div>

      <div class="sheet-footer-action">
        <button class="sheet-main-btn" @click="visible = false">
          <span>完成</span>
        </button>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { getProductCategories, createProductCategory, updateProductCategory, deleteProductCategory } from '@/api/productCategory';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'CategoryManagerDialog',
  props: {
    show: Boolean,
    isMobile: Boolean,
    categories: Array
  },
  data() {
    return {
      localCategories: [],
      newCategoryName: '',
      loading: false,
      editingId: null,
      editName: ''
    }
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  watch: {
    categories: {
      immediate: true,
      handler(val) {
        this.localCategories = JSON.parse(JSON.stringify(val || []));
      }
    },
    show(val) {
      if (val) {
        this.fetchData();
      }
    }
  },
  methods: {
    async fetchData() {
      this.loading = true;
      try {
        const res = await getProductCategories();
        this.localCategories = res.data.data || [];
      } catch (error) {
        Message.error('获取分类列表失败');
      } finally {
        this.loading = false;
      }
    },
    async handleAdd() {
      if (!this.newCategoryName.trim()) {
        return Message.warning('请输入分类名称');
      }
      try {
        await createProductCategory({ name: this.newCategoryName.trim() });
        Message.success('添加分类成功');
        this.newCategoryName = '';
        this.fetchData();
        this.$emit('change');
      } catch (error) {
        Message.error('添加失败');
      }
    },
    startEdit(item) {
      this.editingId = item.id;
      this.editName = item.name;
    },
    cancelEdit() {
      this.editingId = null;
      this.editName = '';
    },
    async saveEdit(item) {
      if (!this.editName.trim()) {
        return Message.warning('分类名称不能为空');
      }
      try {
        await updateProductCategory(item.id, { name: this.editName.trim() });
        Message.success('修改成功');
        this.editingId = null;
        this.fetchData();
        this.$emit('change');
      } catch (error) {
        Message.error('修改失败');
      }
    },
    handleDelete(item) {
      Modal.confirm({
        title: '提示',
        content: `确定要删除分类 "${item.name}" 吗？`,
        onOk: async () => {
          try {
            await deleteProductCategory(item.id);
            Message.success('删除成功');
            this.fetchData();
            this.$emit('change');
          } catch (error) {
            Message.error(error.response?.data?.message || '删除失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
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

.add-category-bar {
  display: flex;
  gap: 8px;
}

:deep(.luxury-form-input .arco-input-wrapper) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}

.add-cat-btn {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 13px;
  font-weight: 700;
  padding: 0 18px;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 42, 84, 0.25);
  flex-shrink: 0;
}
.add-cat-btn:active {
  transform: scale(0.95);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F7F8FA;
  padding: 10px 14px;
  border-radius: 12px;
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.cat-bullet {
  color: #FF5E3A;
  font-size: 10px;
}

.cat-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.sheet-footer-action {
  margin-top: 20px;
}

.sheet-main-btn {
  width: 100%;
  height: 46px;
  border-radius: 23px;
  background: #1A1D20;
  color: #FFFFFF;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
.sheet-main-btn:active {
  transform: scale(0.97);
}
</style>
