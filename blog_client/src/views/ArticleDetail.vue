<template>
  <div class="xhs-detail-container" v-loading="loading">
    <div class="xhs-detail-header">
      <div class="header-left" @click="$router.push('/')">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-author">
        <img src="/img/avatar.png" class="author-avatar" />
        <span class="author-name">生活家</span>
      </div>
      <div class="header-right">
        <el-button round size="mini" class="follow-btn">关注</el-button>
      </div>
    </div>

    <div class="xhs-media-carousel" v-if="mediaUrls.length > 0">
      <el-carousel trigger="click" height="400px" :autoplay="false">
        <el-carousel-item v-for="(url, index) in mediaUrls" :key="index">
          <div class="carousel-image-wrapper">
            <img :src="url" class="carousel-image" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    <div class="xhs-media-placeholder" v-else :style="{ background: getGradient(article.id) }">
      <span class="cover-icon">✨</span>
    </div>

    <div class="xhs-detail-content">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-text" v-html="formatContent(article.content)"></div>
      <div class="article-meta">
        <span class="post-time">{{ formatDate(article.createTime) }}</span>
      </div>

      <div class="comments-section">
        <h3 class="comments-title">共 {{ comments.length }} 条评论</h3>
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <img src="/img/creamy_avatar.png" class="comment-avatar" onerror="this.src='/img/avatar.png'" />
            <div class="comment-body">
              <div class="comment-author">{{ comment.authorName }}</div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="no-comments">还没有人评论，快来抢沙发~</div>
      </div>
    </div>

    <div class="xhs-detail-footer">
      <div class="footer-input-box">
        <el-input v-model="newComment" placeholder="说点什么..." size="small" class="custom-input"
          @keyup.enter.native="submitComment">
          <el-button slot="append" icon="el-icon-position" @click="submitComment"></el-button>
        </el-input>
      </div>
      <div class="footer-actions">
        <div class="action-btn" @click="likeArticle">
          <i class="el-icon-sugar"></i>
          <span>{{ article.likesCount || '赞' }}</span>
        </div>
        <div class="action-btn">
          <i class="el-icon-star-off"></i>
          <span>收藏</span>
        </div>
        <div class="action-btn">
          <i class="el-icon-chat-dot-round"></i>
          <span>{{ comments.length || '评论' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ArticleDetail',
  data() {
    return {
      loading: true,
      article: {},
      mediaUrls: [],
      comments: [],
      newComment: '',
      submitting: false
    }
  },
  created() {
    this.fetchArticle()
    this.fetchComments()
  },
  methods: {
    async fetchArticle() {
      const id = this.$route.params.id
      try {
        const res = await axios.get(`/api/articles/${id}`)
        this.article = res.data || {}
        if (this.article.mediaUrls) {
          try {
            this.mediaUrls = JSON.parse(this.article.mediaUrls)
          } catch (e) {
            this.mediaUrls = []
          }
        }
      } catch (error) {
        this.$message.error('无法加载日记详情')
      } finally {
        this.loading = false
      }
    },
    formatContent(content) {
      if (!content) return ''
      return content.replace(/\n/g, '<br>')
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}-${date.getDate()}`
    },
    formatTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    },
    async fetchComments() {
      const id = this.$route.params.id
      try {
        const res = await axios.get(`/api/comments/article/${id}`)
        this.comments = res.data || []
      } catch (error) {
        console.error('获取评论失败', error)
      }
    },
    async submitComment() {
      if (!this.newComment.trim() || this.submitting) return;
      this.submitting = true;
      const id = this.$route.params.id;
      try {
        await axios.post('/api/comments', {
          articleId: id,
          content: this.newComment.trim()
        });
        this.newComment = '';
        this.$message.success('评论成功');
        this.fetchComments();
      } catch (error) {
        this.$message.error('评论失败');
      } finally {
        this.submitting = false;
      }
    },
    async likeArticle() {
      const id = this.$route.params.id;
      try {
        await axios.post(`/api/articles/${id}/like`);
        if (this.article.likesCount === undefined || this.article.likesCount === null) {
          this.$set(this.article, 'likesCount', 0);
        }
        this.article.likesCount++;
        this.$message.success('点赞成功');
      } catch (error) {
        this.$message.error('点赞失败');
      }
    },
    getGradient(id) {
      if (!id) return '#FFF0E6';
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
.xhs-detail-container {
  background: #FFFFFF;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.08);
  margin-bottom: 20px;
}

.xhs-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #FFFFFF;
}

.header-left {
  font-size: 24px;
  color: #5C433B;
  cursor: pointer;
  width: 60px;
}

.header-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 700;
  color: #5C433B;
  font-size: 15px;
}

.header-right {
  width: 60px;
  text-align: right;
}

.follow-btn {
  border-color: #FF7E67;
  color: #FF7E67;
  font-weight: 600;
}

.xhs-media-carousel {
  width: 100%;
  background: #FDF0E6;
}

.carousel-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.xhs-media-placeholder {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 60px;
  opacity: 0.8;
}

.xhs-detail-content {
  padding: 24px;
}

.article-title {
  font-size: 22px;
  font-weight: 800;
  color: #5C433B;
  margin-bottom: 16px;
  margin-top: 0;
  line-height: 1.4;
}

.article-text {
  font-size: 16px;
  color: #8C6A5D;
  line-height: 1.8;
  margin-bottom: 20px;
}

.article-meta {
  font-size: 13px;
  color: #D3C1BA;
  border-bottom: 1px solid #FDF0E6;
  padding-bottom: 20px;
}

.xhs-detail-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #FFFFFF;
}

.footer-input-box {
  flex: 1;
  margin-right: 20px;
}

::v-deep .custom-input .el-input__inner {
  border-radius: 20px 0 0 20px;
  background: #FFFDF8;
  border-color: #FDF0E6;
  color: #5C433B;
}

::v-deep .custom-input .el-input-group__append {
  border-radius: 0 20px 20px 0;
  background: #FF7E67;
  color: white;
  border-color: #FF7E67;
}

.footer-actions {
  display: flex;
  gap: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #5C433B;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.action-btn i {
  font-size: 22px;
  color: #FF7E67;
}

/* Comments Section */
.comments-section {
  margin-top: 30px;
  border-top: 1px solid #FDF0E6;
  padding-top: 20px;
}

.comments-title {
  font-size: 14px;
  color: #8C6A5D;
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  margin-bottom: 20px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.comment-body {
  flex: 1;
}

.comment-author {
  font-size: 13px;
  color: #8C6A5D;
  font-weight: 600;
  margin-bottom: 4px;
}

.comment-content {
  font-size: 14px;
  color: #5C433B;
  line-height: 1.5;
  margin-bottom: 6px;
}

.comment-time {
  font-size: 12px;
  color: #D3C1BA;
}

.no-comments {
  text-align: center;
  color: #D3C1BA;
  padding: 30px 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .xhs-detail-container {
    border-radius: 16px;
  }

  .xhs-detail-content {
    padding: 16px;
  }

  .article-title {
    font-size: 18px;
  }

  .xhs-detail-footer {
    padding: 12px 16px;
  }

  .footer-actions {
    gap: 12px;
  }

  .footer-input-box {
    margin-right: 12px;
  }
}
</style>
