<template>
  <div 
    class="pull-refresh-container"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <!-- 下拉指示器 -->
    <div 
      class="pull-refresh-indicator" 
      :style="{ 
        height: `${pullDistance}px`,
        opacity: pullDistance > 10 ? 1 : 0,
        transition: isDragging ? 'none' : 'height 0.3s cubic-bezier(0.2, 0, 0, 1), opacity 0.2s ease'
      }"
    >
      <div class="indicator-content" v-if="pullDistance > 10 || isRefreshing">
        <icon-loading v-if="isRefreshing" :spin="true" class="refresh-spinner" />
        <icon-down v-else-if="status === 'pulling'" class="refresh-arrow" />
        <icon-arrow-rise v-else-if="status === 'ready'" class="refresh-arrow ready" />
        <icon-check v-else-if="status === 'success'" class="refresh-check" />
        <span class="refresh-text">{{ statusText }}</span>
      </div>
    </div>

    <!-- 实际页面内容容器 -->
    <div class="pull-refresh-content">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PullToRefresh',
  props: {
    threshold: {
      type: Number,
      default: 65
    },
    maxDistance: {
      type: Number,
      default: 110
    }
  },
  emits: ['refresh'],
  data() {
    return {
      startY: 0,
      currentY: 0,
      pullDistance: 0,
      isDragging: false,
      isRefreshing: false,
      status: 'pulling' // 'pulling' | 'ready' | 'refreshing' | 'success'
    };
  },
  computed: {
    statusText() {
      switch (this.status) {
        case 'ready':
          return '释放立即刷新';
        case 'refreshing':
          return '正在获取最新内容...';
        case 'success':
          return '已更新至最新';
        default:
          return '下拉刷新页面';
      }
    }
  },
  methods: {
    isAtTop() {
      return (window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0) <= 2;
    },
    handleTouchStart(e) {
      if (this.isRefreshing) return;
      if (this.isAtTop()) {
        this.startY = e.touches[0].clientY;
        this.isDragging = true;
        this.status = 'pulling';
      }
    },
    handleTouchMove(e) {
      if (!this.isDragging || this.isRefreshing) return;
      this.currentY = e.touches[0].clientY;
      const diff = this.currentY - this.startY;

      // 仅在向下拖动且处于页面顶部时处理
      if (diff > 0 && this.isAtTop()) {
        // 应用阻尼效果
        const damping = 0.45;
        this.pullDistance = Math.min(diff * damping, this.maxDistance);
        
        if (this.pullDistance >= this.threshold) {
          this.status = 'ready';
        } else {
          this.status = 'pulling';
        }
        
        // 阻止默认橡皮筋滚动，带来更丝滑体验
        if (e.cancelable && this.pullDistance > 15) {
          e.preventDefault();
        }
      } else {
        this.pullDistance = 0;
      }
    },
    async handleTouchEnd() {
      if (!this.isDragging || this.isRefreshing) return;
      this.isDragging = false;

      if (this.status === 'ready') {
        this.triggerRefresh();
      } else {
        this.pullDistance = 0;
        this.status = 'pulling';
      }
    },
    async triggerRefresh() {
      this.isRefreshing = true;
      this.status = 'refreshing';
      this.pullDistance = this.threshold;

      try {
        // 创建 Promise 支持外层 await
        await new Promise((resolve) => {
          this.$emit('refresh', resolve);
          // 兜底 5 秒自动超时关闭
          setTimeout(resolve, 5000);
        });
        this.status = 'success';
      } catch (e) {
        this.status = 'pulling';
      } finally {
        setTimeout(() => {
          this.pullDistance = 0;
          this.isRefreshing = false;
          this.status = 'pulling';
        }, 500);
      }
    }
  }
};
</script>

<style scoped>
.pull-refresh-container {
  position: relative;
  width: 100%;
  min-height: 100%;
  overflow: visible;
}

.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  pointer-events: none;
}

.indicator-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #86909C;
  font-weight: 500;
}

.refresh-spinner {
  font-size: 16px;
  color: #FF4B2B;
}

.refresh-arrow {
  font-size: 14px;
  transition: transform 0.2s ease;
}

.refresh-arrow.ready {
  transform: rotate(180deg);
  color: #FF4B2B;
}

.refresh-check {
  font-size: 16px;
  color: #00B42A;
}

.pull-refresh-content {
  position: relative;
}
</style>
