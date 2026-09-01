<template>
  <a-modal
    :visible="visible"
    :footer="false"
    :header="false"
    :width="isMobile ? '100%' : (width || '520px')"
    :mask-closable="maskClosable"
    @cancel="handleClose"
    unmount-on-close
    modal-class="universal-bottom-sheet-modal"
  >
    <div class="sheet-modern-container">
      <!-- 1. 移动端顶部居中拉手横杠 (HandleBar) -->
      <div class="sheet-handle-bar" v-if="isMobile"></div>
      
      <!-- 2. 右上角磨砂圆圈关闭按钮 -->
      <button class="sheet-circle-close" @click="handleClose" aria-label="关闭">
        <icon-close />
      </button>

      <!-- 3. 头部标题与副标题区 -->
      <div class="sheet-header" v-if="title || subtitle || $slots.header">
        <slot name="header">
          <h3 class="sheet-title" v-if="title">{{ title }}</h3>
          <p class="sheet-subtitle" v-if="subtitle">{{ subtitle }}</p>
        </slot>
      </div>

      <!-- 4. 主体内容插槽 -->
      <div class="sheet-body">
        <slot></slot>
      </div>

      <!-- 5. 底部吸底操作栏 (可选自定义插槽或默认全宽大胶囊主按钮) -->
      <div class="sheet-footer-action" v-if="confirmText || $slots.footer">
        <slot name="footer">
          <button 
            class="sheet-main-btn" 
            :disabled="confirmLoading || confirmDisabled" 
            @click="handleConfirm"
          >
            <icon-loading v-if="confirmLoading" :spin="true" />
            <span>{{ confirmLoading ? '正在处理...' : confirmText }}</span>
          </button>
        </slot>
      </div>
    </div>
  </a-modal>
</template>

<script>
export default {
  name: 'AppBottomSheet',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    subtitle: {
      type: String,
      default: ''
    },
    width: {
      type: String,
      default: '520px'
    },
    confirmText: {
      type: String,
      default: ''
    },
    confirmLoading: {
      type: Boolean,
      default: false
    },
    confirmDisabled: {
      type: Boolean,
      default: false
    },
    maskClosable: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:visible', 'cancel', 'confirm'],
  data() {
    return {
      isMobile: window.innerWidth <= 768
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleClose() {
      this.$emit('update:visible', false);
      this.$emit('cancel');
    },
    handleConfirm() {
      this.$emit('confirm');
    }
  }
}
</script>

<style scoped>
.sheet-modern-container {
  padding: 16px 18px 24px;
  position: relative;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.sheet-handle-bar {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: #E5E6EB;
  margin: 0 auto 12px;
  flex-shrink: 0;
}

.sheet-circle-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
}
.sheet-circle-close:active {
  background: #E5E6EB;
  transform: scale(0.92);
}

.sheet-header {
  text-align: center;
  margin-bottom: 18px;
  padding: 0 30px;
  flex-shrink: 0;
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

.sheet-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 2px 2px;
}

.sheet-footer-action {
  margin-top: 20px;
  flex-shrink: 0;
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
  gap: 6px;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.sheet-main-btn:active:not(:disabled) {
  transform: scale(0.96);
}
.sheet-main-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .sheet-modern-container {
    padding: 14px 16px 24px;
  }
}
</style>
