<template>
  <div class="comments-section">
    <h3 class="comments-title">共 {{ comments.length }} 条灵感讨论</h3>
    <div class="comment-list" v-if="comments.length > 0">
      <div class="comment-item" v-for="comment in comments" :key="comment.id">
        <!-- 实名用户头像 (支持渐变文字微徽章与真机头像) -->
        <div class="comment-avatar-wrap">
          <div class="avatar-letter-box" :style="{ background: getAvatarBg(comment.authorName) }">
            {{ getInitial(comment.authorName) }}
          </div>
        </div>
        <div class="comment-body">
          <div class="comment-author-row">
            <span class="comment-author">{{ comment.authorName || '小柴包用户' }}</span>
            <span class="realname-badge">实名</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-time">{{ $formatTime(comment.createTime) }}</div>
        </div>
      </div>
    </div>
    <a-empty v-else description="还没有人评论，快来抢沙发~" style="margin: 40px 0;">
      <template #image><icon-message style="font-size: 40px; color: #D3C1BA; opacity: 0.5;" /></template>
    </a-empty>
  </div>
</template>

<script>
import { getComments, postComment } from '@/api/article';
import { Message } from '@arco-design/web-vue';
import { useUserStore } from '@/stores/user';

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
        const res = await getComments(this.articleId);
        this.comments = (res.data && res.data.data) ? res.data.data : [];
        this.$emit('update-count', this.comments.length);
      } catch (error) {
        console.error('获取评论失败', error);
      }
    },
    async submitComment(content) {
      if (!content || !content.trim() || this.submitting) return false;
      
      const userStore = useUserStore();
      if (!userStore.isLoggedIn) {
        window.dispatchEvent(new CustomEvent('open-login'));
        Message.warning('实名互动：请先登录后再发表评论');
        return false;
      }

      const authorName = userStore.userInfo?.nickname || userStore.userInfo?.username || '用户';

      this.submitting = true;
      try {
        await postComment({
          articleId: this.articleId,
          authorName: authorName,
          content: content.trim()
        });
        Message.success('评论发表成功！');
        await this.fetchComments();
        return true;
      } catch (error) {
        Message.error('评论失败，请重试');
        return false;
      } finally {
        this.submitting = false;
      }
    },
    getInitial(name) {
      if (!name) return '客';
      return name.trim().charAt(0).toUpperCase();
    },
    getAvatarBg(name) {
      const gradients = [
        'linear-gradient(135deg, #FF9A8B 0%, #FF6A88 100%)',
        'linear-gradient(135deg, #FEE140 0%, #FA709A 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      ];
      let hash = 0;
      for (let i = 0; i < (name || '').length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
      }
      return gradients[Math.abs(hash) % gradients.length];
    }
  }
}
</script>

<style scoped>
.comments-section {
  margin-top: 30px;
  border-top: 1px solid rgba(0,0,0,0.05);
  padding-top: 20px;
}

.comments-title {
  font-size: 14px;
  color: #86909C;
  margin-bottom: 20px;
  font-weight: 600;
}

.comment-item {
  display: flex;
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 16px;
  background: #F7F8FA;
  transition: all 0.2s ease;
}

.comment-avatar-wrap {
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar-letter-box {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-weight: 800;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-author-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.comment-author {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}

.realname-badge {
  font-size: 9px;
  font-weight: 700;
  color: #00B42A;
  background: #E8FFEA;
  padding: 1px 4px;
  border-radius: 4px;
}

.comment-content {
  font-size: 14px;
  color: #4E5969;
  line-height: 1.6;
  word-break: break-word;
  margin-bottom: 6px;
}

.comment-time {
  font-size: 11px;
  color: #C9CDD4;
}
</style>
