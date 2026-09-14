<template>
  <div 
    class="slide-verify-container" 
    ref="containerRef"
    :class="{ 'is-success': isSuccess, 'is-sliding': isSliding }"
    @click.stop
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

    <!-- 滑块把手：采用统一原生 Pointer Events (统一兼容 iOS/Android/桌面/触控屏) -->
    <div 
      class="slide-handle"
      ref="handleRef"
      :style="{ 
        transform: `translateX(${currentX}px)`,
        transition: isSliding ? 'none' : 'transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)'
      }"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
      @click.stop
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
      loadingTicket: false,
      activePointerId: null
    };
  },
  computed: {
    progressWidth() {
      if (this.isSuccess) {
        return this.maxSlideWidth + 40;
      }
      return this.currentX > 0 ? this.currentX + 20 : 0;
    }
  },
  mounted() {
    this.updateDimensions();
    window.addEventListener('resize', this.updateDimensions);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateDimensions);
  },
  methods: {
    updateDimensions() {
      if (!this.$refs.containerRef || !this.$refs.handleRef) return;
      const cRect = this.$refs.containerRef.getBoundingClientRect();
      const hRect = this.$refs.handleRef.getBoundingClientRect();
      const containerW = cRect.width || this.$refs.containerRef.offsetWidth || 280;
      const handleW = hRect.width || this.$refs.handleRef.offsetWidth || 40;
      this.maxSlideWidth = Math.max(containerW - handleW - 6, 60);
    },

    // 核心：使用 W3C 现代跨端标准 PointerEvent
    onPointerDown(e) {
      if (this.isSuccess || this.loadingTicket) return;
      
      // 阻止冒泡与系统默认手势
      e.stopPropagation();
      e.preventDefault();

      this.updateDimensions();
      this.isSliding = true;
      this.startX = e.clientX;

      const handle = e.currentTarget || e.target;
      if (handle && handle.setPointerCapture) {
        try {
          handle.setPointerCapture(e.pointerId);
          this.activePointerId = e.pointerId;
        } catch (err) {
          // 部分老版本浏览器降级忽略
        }
      }
    },

    onPointerMove(e) {
      if (!this.isSliding) return;
      e.stopPropagation();
      e.preventDefault();

      const moveX = e.clientX - this.startX;
      if (moveX <= 0) {
        this.currentX = 0;
      } else if (moveX >= this.maxSlideWidth) {
        this.currentX = this.maxSlideWidth;
      } else {
        this.currentX = moveX;
      }
    },

    onPointerUp(e) {
      if (!this.isSliding) return;
      e.stopPropagation();
      this.isSliding = false;

      const handle = e.currentTarget || e.target;
      if (handle && handle.releasePointerCapture && this.activePointerId !== null) {
        try {
          handle.releasePointerCapture(this.activePointerId);
        } catch (err) {}
      }
      this.activePointerId = null;

      this.checkSuccess();
    },

    async checkSuccess() {
      // 滑动距离达到 85% 以上即视为通过（更加流畅宽容）
      const threshold = this.maxSlideWidth > 0 ? this.maxSlideWidth * 0.85 : 150;
      if (this.currentX >= threshold) {
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
        // 未滑到位，平滑回弹
        this.currentX = 0;
      }
    },

    reset() {
      this.isSliding = false;
      this.isSuccess = false;
      this.currentX = 0;
      this.loadingTicket = false;
      this.activePointerId = null;
      this.updateDimensions();
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
  -webkit-touch-callout: none;
  touch-action: none !important; /* 关键：禁止 iOS Safari 原生手势拦截 */
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
  touch-action: none !important; /* 关键：禁止 Safari 拖动延迟与滚动 */
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  user-select: none;
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
  pointer-events: none;
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
