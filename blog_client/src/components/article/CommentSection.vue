<template>
  <div class="comments-section">
    <h3 class="comments-title">共 {{ comments.length }} 条评论</h3>
    <div class="comment-list" v-if="comments.length > 0">
      <div class="comment-item" v-for="comment in comments" :key="comment.id">
        <a-avatar class="comment-avatar" :size="32">
          <img src="/img/creamy_avatar.png" onerror="this.src='/img/avatar.png'" />
        </a-avatar>
        <div class="comment-body">
          <div class="comment-author">{{ comment.authorName }}</div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
        </div>
      </div>
    </div>
    <a-empty v-else description="还没有人评论，快来抢沙发~" style="margin: 40px 0;">
      <template #image><icon-message style="font-size: 40px; color: #D3C1BA; opacity: 0.5;" /></template>
    </a-empty>
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'CommentSection',
  props: {
    articleId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      comments: [],
      submitting: false
    }
  },
  watch: {
    articleId: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.fetchComments();
        }
      }
    }
  },
  methods: {
    async fetchComments() {
      try {
        const res = await axios.get(`/api/comments/article/${this.articleId}`);
        this.comments = (res.data && res.data.data) ? res.data.data : [];
        this.$emit('update-count', this.comments.length);
      } catch (error) {
        console.error('获取评论失败', error);
      }
    },
    async submitComment(content) {
      if (!content.trim() || this.submitting) return false;
      this.submitting = true;
      try {
        await axios.post('/api/comments', {
          articleId: this.articleId,
          content: content.trim()
        });
        Message.success('评论成功');
        await this.fetchComments();
        return true;
      } catch (error) {
        Message.error('评论失败');
        return false;
      } finally {
        this.submitting = false;
      }
    },
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    }
  }
}
</script>

<style scoped>
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
</style>
