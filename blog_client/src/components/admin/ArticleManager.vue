<template>
  <div class="article-manager">
    <div class="tab-header">
      <span style="margin-right: 15px; color: #4E5969; font-weight: bold;">[测试日志] 共 {{ articles.length }} 篇数据已到达</span>
      <a-button type="primary" size="small" @click="goToCreateArticle"><template #icon><icon-plus /></template>发布新日常</a-button>
    </div>

    <a-table v-if="!isMobile" :data="articles" :loading="loadingArticles" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column data-index="id" title="ID" :width="80"></a-table-column>
        <a-table-column title="封面" :width="100">
          <template #cell="{ record }">
            <img :src="record.coverUrl" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover;" />
          </template>
        </a-table-column>
        <a-table-column title="文章信息">
          <template #cell="{ record }">
            <div style="font-weight: 600; font-size: 14px; color: #1D2129;">{{ record.title }}</div>
            <div style="font-size: 12px; color: #86909C; margin-top: 4px;">{{ $formatTime(record.createTime) }}</div>
          </template>
        </a-table-column>
        <a-table-column title="数据" :width="100">
          <template #cell="{ record }">
            <span class="stat-badge"><icon-heart /> {{ record.likesCount || 0 }}</span>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="150" fixed="right">
          <template #cell="{ record }">
            <a-button size="small" type="text" @click="handleEditArticle(record)">编辑</a-button>
            <a-button size="small" type="text" status="danger" @click="handleDeleteArticle(record)">删除</a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端视图: 卡片列表 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingArticles" style="width: 100%; display: block;">
        <div v-for="article in articles" :key="article.id" class="mobile-card-item">
          <img :src="article.coverUrl" class="card-cover" />
          <div class="card-info">
            <h4 class="card-title">{{ article.title }}</h4>
            <div class="card-meta">
              <span class="stat-badge"><icon-heart /> {{ article.likesCount || 0 }}</span>
              <span class="card-time">{{ $formatTime(article.createTime) }}</span>
            </div>
          </div>
          <div class="card-actions" style="display: flex; gap: 8px;">
            <a-button type="primary" size="small" shape="circle" @click="handleEditArticle(article)">
              <template #icon><icon-edit /></template>
            </a-button>
            <a-button type="primary" status="danger" size="small" shape="circle" @click="handleDeleteArticle(article)">
              <template #icon><icon-delete /></template>
            </a-button>
          </div>
        </div>
        <a-empty v-if="articles.length === 0 && !loadingArticles" description="暂无文章" />
      </a-spin>
    </div>

    <!-- 编辑日常的弹窗 -->
    <a-modal title="编辑日常文章" :visible="editModalVisible" :width="isMobile ? '95%' : '600px'" @cancel="editModalVisible = false" @ok="saveEditedArticle" unmount-on-close>
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="日常标题">
          <a-input v-model="editForm.title" placeholder="输入日常标题"></a-input>
        </a-form-item>
        <a-form-item label="日常正文">
          <a-textarea v-model="editForm.content" :auto-size="{minRows: 6}" placeholder="添加日常正文内容"></a-textarea>
        </a-form-item>
        <a-form-item label="推荐好物 (关联商品)">
          <a-select v-model="editForm.productId" allow-clear placeholder="选择日常中提到的商品" style="width: 100%">
            <a-option
              v-for="item in products"
              :key="item.id"
              :value="item.id"
              :label="item.name + ' (¥' + item.price + ')'">
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="图片与视频素材">
          <a-upload
            :action="uploadAction"
            list-type="picture-card"
            v-model:file-list="editFileList"
            @success="handleUploadSuccess"
            @error="handleUploadError"
            multiple
            image-preview
            accept="image/*,video/*"
          >
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { getArticles, updateArticle, deleteArticle } from '@/api/article';
import { getProducts } from '@/api/product';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'ArticleManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loadingArticles: false,
      articles: [],
      editModalVisible: false,
      products: [],
      editFileList: [],
      editForm: {
        id: null,
        title: '',
        content: '',
        coverUrl: '',
        mediaUrls: '[]',
        productId: null
      }
    }
  },
  computed: {
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  created() {
    this.fetchArticles();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchArticles() {
      this.loadingArticles = true;
      try {
        const res = await getArticles();
        this.articles = res.data.data || [];
      } catch (error) {
        Message.error('加载文章失败');
      } finally {
        this.loadingArticles = false;
      }
    },
    goToCreateArticle() {
      this.$router.push('/create');
    },
    handleDeleteArticle(article) {
      Modal.confirm({
        title: '提示',
        content: '确定要删除这篇日常吗？',
        onOk: async () => {
          try {
            await deleteArticle(article.id);
            Message.success('已删除');
            this.fetchArticles();
          } catch (error) {
            Message.error('删除失败');
          }
        }
      });
    },
    async fetchProducts() {
      try {
        const res = await getProducts();
        this.products = res.data.data || [];
      } catch (e) {
        console.error('加载商品列表失败', e);
      }
    },
    handleUploadSuccess(fileItem) {
      const res = fileItem.response;
      if (typeof res === 'string' && (res.trim().startsWith('<!DOCTYPE') || res.trim().startsWith('<html'))) {
        Message.error('文件上传失败，服务器返回了错误的格式。');
        fileItem.status = 'error';
        fileItem.uploadedUrl = '';
        return;
      }
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('文件上传失败，服务器返回了错误的格式。');
        fileItem.status = 'error';
        fileItem.uploadedUrl = '';
        return;
      }
      fileItem.uploadedUrl = url;
    },
    handleUploadError(fileItem) {
      Message.error('文件上传失败，请重试');
    },
    async handleEditArticle(article) {
      this.editForm = {
        id: article.id,
        title: article.title,
        content: article.content,
        coverUrl: article.coverUrl || '',
        mediaUrls: article.mediaUrls || '[]',
        productId: article.productId || null
      };
      
      let initialFileList = [];
      if (article.mediaUrls) {
        try {
          let urls = JSON.parse(article.mediaUrls);
          if (Array.isArray(urls)) {
            initialFileList = urls.map((url, idx) => ({
              uid: idx.toString(),
              name: `image_${idx}`,
              url: url,
              status: 'done',
              uploadedUrl: url
            }));
          }
        } catch (e) {
          // ignore
        }
      }
      this.editFileList = initialFileList;
      
      await this.fetchProducts();
      this.editModalVisible = true;
    },
    async saveEditedArticle() {
      if (!this.editForm.title.trim()) {
        Message.warning('标题不能为空哦~');
        return;
      }
      if (!this.editForm.content.trim()) {
        Message.warning('日常正文不能为空哦~');
        return;
      }
      
      const urls = this.editFileList.map(f => {
        if (f.uploadedUrl) return f.uploadedUrl;
        const r = f.response;
        let url = r ? (typeof r === 'string' ? r : r.url) : f.url;
        if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
          return null;
        }
        return url;
      }).filter(Boolean);
      
      this.editForm.mediaUrls = JSON.stringify(urls);
      this.editForm.coverUrl = urls.length > 0 ? urls[0] : '';
      
      try {
        await updateArticle(this.editForm.id, this.editForm);
        Message.success('文章修改成功！');
        this.editModalVisible = false;
        this.fetchArticles();
      } catch (e) {
        Message.error('文章修改失败，请重试');
      }
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
.stat-badge {
  background: #F2F3F5;
  color: #4E5969;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
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
  border-radius: 8px;
  padding: 12px;
  gap: 12px;
  align-items: center;
  border: 1px solid #E5E6EB;
}
.card-cover {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #F2F3F5;
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #1D2129;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.card-time {
  font-size: 12px;
  color: #86909C;
}
.card-actions {
  flex-shrink: 0;
}
</style>
