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
            <div style="font-size: 12px; color: #86909C; margin-top: 4px;">{{ formatTime(record.createTime) }}</div>
          </template>
        </a-table-column>
        <a-table-column title="数据" :width="100">
          <template #cell="{ record }">
            <span class="stat-badge"><icon-heart /> {{ record.likesCount || 0 }}</span>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="100" fixed="right">
          <template #cell="{ record }">
            <a-button size="small" type="primary" status="danger" @click="handleDeleteArticle(record)">删除</a-button>
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
              <span class="card-time">{{ formatTime(article.createTime) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <a-button type="primary" status="danger" shape="circle" @click="handleDeleteArticle(article)">
              <template #icon><icon-delete /></template>
            </a-button>
          </div>
        </div>
        <a-empty v-if="articles.length === 0 && !loadingArticles" description="暂无文章" />
      </a-spin>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
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
      articles: []
    }
  },
  created() {
    this.fetchArticles();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return new Date(timeStr).toLocaleString();
    },
    async fetchArticles() {
      this.loadingArticles = true;
      try {
        const res = await axios.get('/api/articles');
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
            await axios.delete(`/api/articles/${article.id}`, { headers: this.getAuthHeader() });
            Message.success('已删除');
            this.fetchArticles();
          } catch (error) {
            Message.error('删除失败');
          }
        }
      });
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
