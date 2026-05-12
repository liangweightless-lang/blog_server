<template>
  <div class="article-grid-container">
    <div class="section-header">
      <span class="section-title">📖 创作者日记</span>
      <el-button class="hidden-xs-only" round size="small"
        style="background: #FF7E67; color: white; border: none; font-weight: bold;" @click="$router.push('/create')">
        <i class="el-icon-edit"></i> 记录新日常
      </el-button>
    </div>

    <el-row :gutter="16" class="xhs-grid" v-loading="loading">
      <el-empty v-if="articles.length === 0 && !loading" description="暂无内容，快来书写第一篇日记吧"></el-empty>
      <el-col :xs="12" :sm="8" :md="8" v-for="article in articles" :key="article.id">
        <div class="xhs-card" @click="viewArticle(article)">
          <div class="xhs-cover"
            :style="article.coverUrl ? { backgroundImage: 'url(' + article.coverUrl + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : { background: getGradient(article.id) }">
            <span class="cover-icon" v-if="!article.coverUrl">✨</span>
          </div>
          <div class="xhs-info">
            <div class="xhs-title">{{ article.title }}</div>
            <div class="xhs-bottom">
              <div class="xhs-author">
                <img src="/img/avatar.png" class="xhs-author-avatar" />
                <span class="xhs-author-name">焙刻生活</span>
              </div>
              <div class="xhs-likes">
                <i class="el-icon-sugar"></i> {{ article.likesCount || 0 }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script>
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
    return {}
  },
  methods: {
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
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
  margin-top: 15px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 800;
  color: #5C433B;
}

.xhs-grid {
  margin-top: 24px;
}

.xhs-card {
  background: #FFFFFF;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.05);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.xhs-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.15);
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

.xhs-author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
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
