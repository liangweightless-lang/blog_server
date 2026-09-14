<template>
  <div 
    class="slide-verify-container" 
    ref="containerRef"
    :class="{ 'is-success': isSuccess, 'is-sliding': isSliding }"
    @click.stop
    @mousedown.stop
    @mouseup.stop
    @touchstart.stop
    @touchmove.stop
    @touchend.stop
  >
    <!-- 背景进度填充条 -->
    <div 
      class="slide-progress" 
      :style="{ width: progressWidth + 'px', transition: isSliding ? 'none' : 'width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)' }"
    ></div>

    <!-- 提示文案 -->
    <div class="slide-label" :class="{ 'is-faded': isSliding && currentX > 20 }">
      <span v-if="isSuccess" class="success-text">
        <icon-check-circle-fill class="success-icon" /> 验证通过
      </span>
      <span v-else class="normal-text">
        向右滑动完成验证
      </span>
    </div>

    <!-- 滑块把手 -->
    <div 
      class="slide-handle"
      ref="handleRef"
      :style="{ 
        transform: `translateX(${currentX}px)`,
        transition: isSliding ? 'none' : 'transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)'
      }"
      @mousedown.stop.prevent="onDragStart"
      @touchstart.stop.prevent="onTouchStart"
    >
      <icon-check v-if="isSuccess" class="handle-icon icon-success" />
      <icon-double-right v-else class="handle-icon" />
    </div>
  </div>
</template>

<script>
import { slideVerify } from '@/api/user';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'SlideVerify',
  emits: ['success', 'reset'],
  data() {
    return {
      isSliding: false,
      isSuccess: false,
      startX: 0,
      currentX: 0,
      maxSlideWidth: 0,
      loadingTicket: false
    };
  },
  computed: {
    progressWidth() {
      // 进度宽度为当前位移 + 滑块宽度的一半
      if (this.isSuccess) {
        return this.maxSlideWidth + 40;
      }
      return this.currentX > 0 ? this.currentX + 20 : 0;
    }
  },
  mounted() {
    this.calcMaxSlideWidth();
    window.addEventListener('resize', this.calcMaxSlideWidth);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.calcMaxSlideWidth);
    this.removeGlobalMouseEvents();
    this.removeGlobalTouchEvents();
  },
  methods: {
    calcMaxSlideWidth() {
      if (!this.$refs.containerRef || !this.$refs.handleRef) return;
      const containerW = this.$refs.containerRef.offsetWidth || 280;
      const handleW = this.$refs.handleRef.offsetWidth || 40;
      // 减去左右 padding (各 3px)
      this.maxSlideWidth = Math.max(containerW - handleW - 6, 0);
    },

    // 鼠标事件
    onDragStart(e) {
      if (e) e.stopPropagation();
      if (this.isSuccess || this.loadingTicket) return;
      this.calcMaxSlideWidth();
      this.isSliding = true;
      this.startX = e.clientX;

      window.addEventListener('mousemove', this.onDragMove);
      window.addEventListener('mouseup', this.onDragEnd);
    },
    onDragMove(e) {
      if (!this.isSliding) return;
      const moveX = e.clientX - this.startX;
      if (moveX < 0) {
        this.currentX = 0;
      } else if (moveX > this.maxSlideWidth) {
        this.currentX = this.maxSlideWidth;
      } else {
        this.currentX = moveX;
      }
    },
    onDragEnd(e) {
      if (e) e.stopPropagation();
      if (!this.isSliding) return;
      this.isSliding = false;
      this.removeGlobalMouseEvents();
      this.checkSuccess();
    },
    removeGlobalMouseEvents() {
      window.removeEventListener('mousemove', this.onDragMove);
      window.removeEventListener('mouseup', this.onDragEnd);
    },

    // 触屏移动端事件
    onTouchStart(e) {
      if (e) e.stopPropagation();
      if (this.isSuccess || this.loadingTicket) return;
      this.calcMaxSlideWidth();
      this.isSliding = true;
      this.startX = e.touches[0].clientX;

      window.addEventListener('touchmove', this.onTouchMove, { passive: false });
      window.addEventListener('touchend', this.onTouchEnd);
      window.addEventListener('touchcancel', this.onTouchEnd);
    },
    onTouchMove(e) {
      if (!this.isSliding) return;
      if (e) e.preventDefault(); // 阻止手机端拖动时的页面跟随滚动
      const moveX = e.touches[0].clientX - this.startX;
      if (moveX < 0) {
        this.currentX = 0;
      } else if (moveX > this.maxSlideWidth) {
        this.currentX = this.maxSlideWidth;
      } else {
        this.currentX = moveX;
      }
    },
    onTouchEnd(e) {
      if (e) e.stopPropagation();
      if (!this.isSliding) return;
      this.isSliding = false;
      this.removeGlobalTouchEvents();
      this.checkSuccess();
    },
    removeGlobalTouchEvents() {
      window.removeEventListener('touchmove', this.onTouchMove);
      window.removeEventListener('touchend', this.onTouchEnd);
      window.removeEventListener('touchcancel', this.onTouchEnd);
    },

    // 校验滑动是否达成
    async checkSuccess() {
      // 滑动距离达到 90% 以上视作拖到终点成功
      if (this.currentX >= this.maxSlideWidth * 0.9) {
        this.currentX = this.maxSlideWidth;
        this.isSuccess = true;
        this.loadingTicket = true;
        try {
          const res = await slideVerify();
          this.loadingTicket = false;
          if (res.data?.data) {
            this.$emit('success', res.data.data);
          }
        } catch (error) {
          this.loadingTicket = false;
          Message.error('验证失效，请重新滑动');
          this.reset();
        }
      } else {
        // 未拖到位，平滑回弹
        this.currentX = 0;
      }
    },

    // 重置滑块
    reset() {
      this.isSliding = false;
      this.isSuccess = false;
      this.currentX = 0;
      this.loadingTicket = false;
      this.$emit('reset');
    }
  }
};
</script>

<style scoped>
.slide-verify-container {
  position: relative;
  width: 100%;
  height: 44px;
  background: #F4F5F8;
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  overflow: hidden;
  user-select: none;
  -webkit-user-select: none;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  transition: border-color 0.3s, background-color 0.3s;
}

.slide-verify-container.is-success {
  background: #E8FFEA;
  border-color: #00B42A;
}

/* 进度填充颜色 */
.slide-progress {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(255, 126, 103, 0.15) 0%, rgba(255, 83, 48, 0.25) 100%);
  pointer-events: none;
  z-index: 1;
}

.is-success .slide-progress {
  background: transparent;
}

/* 提示文字 */
.slide-label {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #86909C;
  z-index: 2;
  pointer-events: none;
  transition: opacity 0.2s;
  letter-spacing: 0.5px;
}

.slide-label.is-faded {
  opacity: 0.35;
}

.normal-text {
  color: #86909C;
}

.success-text {
  color: #00B42A;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.success-icon {
  font-size: 16px;
  color: #00B42A;
}

/* 滑块把手 */
.slide-handle {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 38px;
  height: 36px;
  background: #FFFFFF;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  z-index: 3;
  touch-action: none;
}

.slide-handle:active,
.is-sliding .slide-handle {
  cursor: grabbing;
  box-shadow: 0 4px 12px rgba(255, 83, 48, 0.2);
}

.is-success .slide-handle {
  background: #00B42A;
  box-shadow: 0 2px 8px rgba(0, 180, 42, 0.3);
  cursor: default;
}

.handle-icon {
  font-size: 16px;
  color: #86909C;
  transition: color 0.2s;
}

.is-sliding .handle-icon {
  color: #FF5330;
}

.icon-success {
  color: #FFFFFF !important;
  font-size: 18px;
  font-weight: bold;
}
</style>
