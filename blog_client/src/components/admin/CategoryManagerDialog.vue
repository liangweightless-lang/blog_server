<template>
  <a-modal
    v-model:visible="visible"
    title="管理分类"
    :footer="false"
    :width="isMobile ? '95%' : '500px'"
    unmount-on-close
  >
    <div class="category-manager">
      <div class="add-category">
        <a-input v-model="newCategoryName" placeholder="输入分类名称" allow-clear />
        <a-button type="primary" @click="handleAdd">添加</a-button>
      </div>
      
      <a-spin :loading="loading" style="width: 100%;">
        <div class="category-list">
          <div v-for="(item, index) in localCategories" :key="item.id" class="category-item">
            <div class="drag-handle"><icon-drag-dot-vertical /></div>
            <div class="category-name">
              <a-input v-if="editingId === item.id" v-model="editName" size="small" />
              <span v-else>{{ item.name }}</span>
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
  </a-modal>
</template>

<script>
import { getProductCategories, createProductCategory, updateProductCategory, deleteProductCategory, sortProductCategories } from '@/api/productCategory';
import { Message, Modal } from '@arco-design/web-vue';
// 简单使用内置方法模拟拖拽，这里为了简化先不做复杂的原生拖拽，如果有 SortableJS 会更好
// 此处先实现基础 CRUD

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
      get() { return this.show; },
      set(val) { this.$emit('update:show', val); }
    }
  },
  watch: {
    visible(val) {
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
        this.localCategories = res.data.data;
      } catch (e) {
        Message.error('加载分类失败');
      } finally {
        this.loading = false;
      }
    },
    async handleAdd() {
      if (!this.newCategoryName.trim()) {
        Message.warning('请输入分类名称');
        return;
      }
      try {
        await createProductCategory({ name: this.newCategoryName });
        Message.success('添加成功');
        this.newCategoryName = '';
        this.fetchData();
        this.$emit('change');
      } catch (e) {
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
      if (!this.editName.trim()) return;
      try {
        await updateProductCategory(item.id, { name: this.editName });
        Message.success('修改成功');
        this.editingId = null;
        this.fetchData();
        this.$emit('change');
      } catch (e) {
        Message.error('修改失败');
      }
    },
    handleDelete(item) {
      Modal.confirm({
        title: '删除提示',
        content: `确定删除分类 "${item.name}" 吗？该分类下的商品将被归为未分类。`,
        onOk: async () => {
          try {
            await deleteProductCategory(item.id);
            Message.success('删除成功');
            this.fetchData();
            this.$emit('change');
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
.category-manager {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.add-category {
  display: flex;
  gap: 8px;
}
.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}
.category-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 6px;
}
.drag-handle {
  cursor: grab;
  color: #c9cdd4;
  margin-right: 8px;
}
.category-name {
  flex: 1;
  font-weight: 500;
  color: #1d2129;
}
.category-actions {
  display: flex;
  gap: 4px;
}
</style>
