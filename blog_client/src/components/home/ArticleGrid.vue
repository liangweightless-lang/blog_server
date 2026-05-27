<template>
  <div class="article-grid-container">

    <a-spin :loading="loading" style="width: 100%; display: block;">
      <a-grid :cols="{ xs: 2, sm: 3, md: 3 }" :colGap="16" :rowGap="16" class="xhs-grid">
        <a-empty v-if="articles.length === 0 && !loading" description="暂无内容，快来书写第一篇日记吧" style="grid-column: 1 / -1;"></a-empty>
        <a-grid-item v-for="article in articles" :key="article.id">
          <a-card class="xhs-card" hoverable :bordered="false" :body-style="{ padding: '0px' }" @click="viewArticle(article)">
            <div class="xhs-cover"
              :style="article.coverUrl ? { backgroundImage: 'url(' + article.coverUrl + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : { background: getGradient(article.id) }">
              <span class="cover-icon" v-if="!article.coverUrl">✨</span>
            </div>
            <div class="xhs-info">
              <div class="xhs-title">{{ article.title }}</div>
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <a-avatar :size="20">
                    <img :src="homeConfig.avatarUrl || '/img/avatar.png'" />
                  </a-avatar>
                  <span class="xhs-author-name">{{ homeConfig.authorName || '小柴包' }}</span>
                </div>
                <div class="xhs-likes">
                  <icon-heart /> {{ article.likesCount || 0 }}
                </div>
              </div>
            </div>
          </a-card>
        </a-grid-item>
      </a-grid>
    </a-spin>

  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ArticleGrid',
  props: {
    articles: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      homeConfig: {
        avatarUrl: '',
        authorName: ''
      }
    }
  },
  created() {
    this.fetchHomeConfig();
  },
  methods: {
    async fetchHomeConfig() {
      try {
        const res = await axios.get('/api/home/config');
        if (res.data && res.data.data) {
          this.homeConfig = res.data.data;
        }
      } catch (error) {
        console.error('获取首页配置失败', error);
      }
    },
    viewArticle(article) {
      this.$router.push(`/article/${article.id}`)
    },
    formatContent(content) {
      if (!content) return ''
      return content.replace(/\n/g, '<br>')
    },
    getGradient(id) {
      const gradients = [
        'linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%)',
        'linear-gradient(135deg, #FFF0E6 0%, #FFD1A9 100%)',
        'linear-gradient(135deg, #E6F0FF 0%, #BFE0FF 100%)',
        'linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%)',
        'linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%)',
      ];
      return gradients[id % gradients.length];
    }
  }
}
</script>

<style scoped>
.xhs-grid {
  margin-top: 24px;
}

.xhs-card {
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.05) !important;
  cursor: pointer;
  background: transparent !important;
}

:deep(.xhs-card > .arco-card-body) {
  padding: 12px;
  background: #FFFFFF;
}

:deep(.xhs-card:hover) {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.15) !important;
}

.xhs-cover {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 40px;
  opacity: 0.8;
}

.xhs-info {
  padding: 12px;
}

.xhs-title {
  font-size: 15px;
  font-weight: 700;
  color: #5C433B;
  line-height: 1.4;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.xhs-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.xhs-author {
  display: flex;
  align-items: center;
  gap: 6px;
}



.xhs-author-name {
  font-size: 12px;
  color: #8C6A5D;
}

.xhs-likes {
  font-size: 12px;
  color: #8C6A5D;
  display: flex;
  align-items: center;
  gap: 4px;
}

.xhs-likes i {
  font-size: 14px;
  color: #FF7E67;
}

.article-content {
  font-size: 15px;
  line-height: 1.7;
  color: #8C7A6B;
}

@media (max-width: 768px) {
  .xhs-cover {
    height: 140px;
  }

  .xhs-title {
    font-size: 14px;
  }

  .xhs-info {
    padding: 10px;
  }
}
</style>
