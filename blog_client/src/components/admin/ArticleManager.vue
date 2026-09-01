<template>
  <div class="article-manager">
    <div class="action-bar">
      <a-button type="primary" shape="round" @click="goToCreateArticle">
        <template #icon><icon-plus /></template>
        发布新文章
      </a-button>
    </div>

    <!-- PC 端表格视图 -->
    <a-table v-if="!isMobile" :data="articles" :loading="loadingArticles" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column title="封面" :width="100">
          <template #cell="{ record }">
            <img v-if="record.coverUrl" :src="record.coverUrl" class="table-cover-img" />
            <span v-else style="color: #999; font-size: 12px;">无封面</span>
          </template>
        </a-table-column>
        <a-table-column title="标题" data-index="title" />
        <a-table-column title="发布时间">
          <template #cell="{ record }">
            {{ $formatTime(record.createTime) }}
          </template>
        </a-table-column>
        <a-table-column title="点赞数" data-index="likesCount" :width="100" />
        <a-table-column title="操作" :width="150" fixed="right">
          <template #cell="{ record }">
            <a-button type="text" size="small" @click="handleEditArticle(record)">编辑</a-button>
            <a-button type="text" status="danger" size="small" @click="handleDeleteArticle(record)">删除</a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端卡片视图 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingArticles" style="width: 100%; display: block;">
        <div v-for="article in articles" :key="article.id" class="mobile-card-item">
          <div class="card-cover-row">
            <img v-if="article.coverUrl" :src="article.coverUrl" class="mobile-article-cover" />
            <div class="mobile-article-info">
              <h4 class="mobile-article-title">{{ article.title }}</h4>
              <span class="mobile-article-time">{{ $formatTime(article.createTime) }}</span>
            </div>
          </div>
          <div class="mobile-card-actions">
            <a-button type="outline" size="small" shape="round" @click="handleEditArticle(article)">
              <template #icon><icon-edit /></template> 编辑
            </a-button>
            <a-button type="primary" status="danger" size="small" shape="round" @click="handleDeleteArticle(article)">
              <template #icon><icon-delete /></template> 删除
            </a-button>
          </div>
        </div>
        <a-empty v-if="articles.length === 0 && !loadingArticles" description="暂无文章" />
      </a-spin>
    </div>

    <!-- 现代标准 Bottom Sheet 编辑文章抽屉 (彻底解决被灵动岛遮挡与老旧PC弹窗问题) -->
    <a-modal 
      :visible="editModalVisible" 
      :width="isMobile ? '100%' : '560px'" 
      :footer="false"
      :header="false"
      :mask-closable="true"
      @cancel="editModalVisible = false" 
      unmount-on-close
    >
      <div class="sheet-modern-container">
        <!-- 移动端顶部拉手横杠 -->
        <div class="sheet-handle-bar" v-if="isMobile"></div>
        
        <!-- 右上角磨砂圆圈关闭按钮 -->
        <button class="sheet-circle-close" @click="editModalVisible = false" aria-label="关闭">
          <icon-close />
        </button>

        <div class="sheet-header">
          <h3 class="sheet-title">编辑日常手记</h3>
          <p class="sheet-subtitle">修改文章标题、正文描述与关联好物</p>
        </div>

        <div class="sheet-body">
          <div class="custom-form-group">
            <div class="custom-form-item">
              <label class="form-label">手记标题</label>
              <a-input v-model="editForm.title" placeholder="输入手记标题..." size="large" class="luxury-form-input" />
            </div>

            <div class="custom-form-item">
              <label class="form-label">手记正文</label>
              <a-textarea 
                v-model="editForm.content" 
                :auto-size="{ minRows: 4, maxRows: 8 }" 
                placeholder="输入手记正文内容..." 
                class="luxury-form-textarea" 
              />
            </div>

            <div class="custom-form-item">
              <label class="form-label">推荐好物 <span class="label-tag">关联商品</span></label>
              <a-select v-model="editForm.productId" allow-clear placeholder="选择手记中提到的商品" class="luxury-select" size="large">
                <a-option
                  v-for="item in products"
                  :key="item.id"
                  :value="item.id"
                  :label="item.name + ' (¥' + item.price + ')'">
                </a-option>
              </a-select>
            </div>

            <div class="custom-form-item">
              <label class="form-label">图片素材</label>
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
            </div>
          </div>
        </div>

        <div class="sheet-footer-action">
          <button class="sheet-main-btn" @click="saveEditedArticle">
            <span>保存修改</span>
          </button>
        </div>
      </div>
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
      return base + '/api/common/upload';
    }
  },
  created() {
    this.fetchArticles();
    this.fetchProducts();
  },
  methods: {
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
        content: '确定要删除这篇日常手记吗？',
        onOk: async () => {
          try {
            await deleteArticle(article.id);
            Message.success('已删除');
            this.fetchArticles();
          } catch (e) {
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
        // ignore
      }
    },
    handleUploadSuccess(fileItem) {
      const res = fileItem.response;
      let url = (res && res.data) ? res.data : ((res && res.url) ? res.url : (typeof res === 'string' ? res : ''));
      fileItem.uploadedUrl = url;
      Message.success('上传成功');
    },
    handleUploadError() {
      Message.error('文件上传失败，请重试');
    },
    handleEditArticle(article) {
      // 提取正文中的纯文本，去除 raw <p> 标签，提供干净的编辑体验
      let cleanContent = article.content || '';
      if (cleanContent.includes('<p>') || cleanContent.includes('<br>')) {
        cleanContent = cleanContent
          .replace(/<p>/gi, '')
          .replace(/<\/p>/gi, '\n')
          .replace(/<br\s*[\/]?>/gi, '\n')
          .replace(/<[^>]+>/g, '')
          .trim();
      }

      this.editForm = {
        id: article.id,
        title: article.title,
        content: cleanContent,
        coverUrl: article.coverUrl || '',
        mediaUrls: article.mediaUrls || '[]',
        productId: article.productId || null
      };

      // 解析已有图片
      try {
        const urls = JSON.parse(article.mediaUrls || '[]');
        this.editFileList = (Array.isArray(urls) ? urls : []).map((url, idx) => ({
          uid: 'edit-img-' + idx,
          name: 'image-' + idx,
          url: url,
          uploadedUrl: url,
          status: 'done'
        }));
      } catch (e) {
        this.editFileList = [];
      }

      this.editModalVisible = true;
    },
    async saveEditedArticle() {
      if (!this.editForm.title || !this.editForm.title.trim()) {
        return Message.warning('请输入手记标题');
      }

      // 提取图片列表
      const mediaList = this.editFileList
        .map(f => f.uploadedUrl || (f.response && (f.response.data || f.response.url)) || f.url)
        .filter(Boolean);

      this.editForm.mediaUrls = JSON.stringify(mediaList);
      if (mediaList.length > 0) {
        this.editForm.coverUrl = mediaList[0];
      }

      try {
        await updateArticle(this.editForm.id, this.editForm);
        Message.success('手记更新成功！');
        this.editModalVisible = false;
        this.fetchArticles();
      } catch (e) {
        Message.error(e.response?.data?.message || '更新失败');
      }
    }
  }
}
</script>

<style scoped>
.article-manager {
  padding: 10px 0;
}

.table-cover-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
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

.mobile-article-cover {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.mobile-article-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.mobile-article-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-article-time {
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

/* 抽屉样式 */
.sheet-modern-container {
  padding: 16px 18px 24px;
  position: relative;
}

.sheet-header {
  text-align: center;
  margin-bottom: 20px;
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

.form-label {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}

.label-tag {
  font-size: 10px;
  font-weight: 500;
  color: #86909C;
  margin-left: 4px;
}

:deep(.luxury-form-input .arco-input-wrapper) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}
:deep(.luxury-form-textarea) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
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
