<template>
  <Teleport to="body">
    <Transition name="guide-fade">
      <div v-if="visible" class="ios-guide-container">
        <!-- 遮罩层 (轻度遮罩，允许看清背景) -->
        <div class="ios-guide-backdrop" @click="handleDismiss"></div>

        <!-- 提示卡片 -->
        <div class="ios-guide-card">
          <!-- 关闭按钮 -->
          <button class="close-guide-btn" @click="handleDismiss" aria-label="关闭">
            <icon-close />
          </button>

          <!-- 头部应用信息 -->
          <div class="card-header">
            <div class="app-icon-box">
              <img src="/favicon.png" alt="小柴包" class="app-icon-img" @error="handleImgError" />
              <div v-if="imgFailed" class="fallback-app-icon">
                <icon-sun />
              </div>
            </div>
            <div class="app-info">
              <div class="app-title">添加「小柴包」至主屏幕</div>
              <div class="app-desc">像 App 一样秒开，获得更沉浸的全屏体验</div>
            </div>
          </div>

          <!-- 步骤操作图解 -->
          <div class="steps-box">
            <template v-if="isWeChat">
              <!-- 微信内置浏览器环境 -->
              <div class="step-item">
                <span class="step-badge">1</span>
                <span class="step-text">点击右上角 <strong>「···」</strong></span>
              </div>
              <div class="step-item">
                <span class="step-badge">2</span>
                <span class="step-text">选择 <strong>「在 Safari 浏览器中打开」</strong></span>
              </div>
              <div class="step-item">
                <span class="step-badge">3</span>
                <span class="step-text">在 Safari 中点击底部 <strong>分享</strong> 并 <strong>添加到主屏幕</strong></span>
              </div>
            </template>
            <template v-else>
              <!-- 标准 Safari 环境 -->
              <div class="step-item">
                <span class="step-badge">1</span>
                <span class="step-text">
                  点击下方工具栏中的 
                  <span class="ios-share-tag">
                    <svg class="ios-share-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path>
                      <polyline points="16 6 12 2 8 6"></polyline>
                      <line x1="12" y1="2" x2="12" y2="15"></line>
                    </svg>
                    分享
                  </span>
                  按钮
                </span>
              </div>
              <div class="step-item">
                <span class="step-badge">2</span>
                <span class="step-text">
                  在弹出的菜单中下滑，选择 
                  <span class="ios-add-tag">
                    <svg class="ios-plus-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="4" />
                      <line x1="12" y1="8" x2="12" y2="16" />
                      <line x1="8" y1="12" x2="16" y2="12" />
                    </svg>
                    添加到主屏幕
                  </span>
                </span>
              </div>
            </template>
          </div>

          <!-- 底部按钮 -->
          <div class="card-footer">
            <button class="got-it-btn" @click="handleDismiss">
              我知道了
            </button>
          </div>

          <!-- 指向 Safari 底部工具栏正中分享图标的动态小箭头 (仅在标准 Safari 环境下展示) -->
          <div v-if="!isWeChat" class="pointing-arrow-wrapper">
            <div class="pointing-arrow"></div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
export default {
  name: 'IosInstallGuide',
  data() {
    return {
      visible: false,
      isWeChat: false,
      imgFailed: false
    }
  },
  mounted() {
    this.checkIosEnvironment();
  },
  methods: {
    handleImgError() {
      this.imgFailed = true;
    },
    checkIosEnvironment() {
      const ua = window.navigator.userAgent || '';
      
      // 1. 判断是否是 iOS 设备 (iPhone / iPad / iPod / Mac Touch)
      const isIos = /iPhone|iPad|iPod/i.test(ua) || 
        (window.navigator.platform === 'MacIntel' && window.navigator.maxTouchPoints > 1);

      if (!isIos) {
        return;
      }

      // 2. 判断是否已经在独立 PWA / Web App 模式运行 (如果是，说明已经在主屏幕中打开了)
      const isStandalone = ('standalone' in window.navigator && window.navigator.standalone) ||
        window.matchMedia('(display-mode: standalone)').matches;

      if (isStandalone) {
        return;
      }

      // 3. 检查免打扰设置 (7天内如果关闭过则不再自动弹出)
      const lastDismissed = localStorage.getItem('ios_pwa_guide_dismissed');
      if (lastDismissed) {
        const diffDays = (Date.now() - parseInt(lastDismissed, 10)) / (1000 * 60 * 60 * 24);
        if (diffDays < 7) {
          return;
        }
      }

      // 4. 判断是否在微信等内嵌 Webview
      this.isWeChat = /MicroMessenger/i.test(ua);

      // 延迟 2.5 秒后柔和展示，避免用户刚进入页面感到突兀
      setTimeout(() => {
        this.visible = true;
      }, 2500);
    },
    handleDismiss() {
      this.visible = false;
      // 记录关闭时间，7 天内免打扰
      localStorage.setItem('ios_pwa_guide_dismissed', Date.now().toString());
    }
  }
}
</script>

<style scoped>
.ios-guide-container {
  position: fixed;
  inset: 0;
  z-index: 1050;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  pointer-events: none;
}

.ios-guide-backdrop {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  pointer-events: auto;
}

.ios-guide-card {
  position: relative;
  width: calc(100% - 32px);
  max-width: 380px;
  margin-bottom: max(28px, env(safe-area-inset-bottom, 28px));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 22px;
  padding: 20px 18px 16px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.18), 0 0 1px rgba(0, 0, 0, 0.1);
  pointer-events: auto;
  animation: cardSlideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-sizing: border-box;
}

/* 关闭按钮 */
.close-guide-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: none;
  background: #EAECEF;
  color: #86909C;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.close-guide-btn:hover {
  background: #DFE1E6;
  color: #1D2129;
}

/* 头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-right: 24px;
}

.app-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  background: #FF5330;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.app-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fallback-app-icon {
  font-size: 24px;
  color: #FFFFFF;
}

.app-title {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.3;
}

.app-desc {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}

/* 步骤区 */
.steps-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #F7F8FA;
  border-radius: 14px;
  padding: 12px 14px;
  margin-bottom: 14px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #333842;
  line-height: 1.4;
}

.step-badge {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #FF5330;
  color: #FFFFFF;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-text {
  flex: 1;
}

/* iOS 专属图标标签 */
.ios-share-tag,
.ios-add-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #FFFFFF;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #007AFF;
  border: 1px solid #E5E6EB;
  margin: 0 2px;
  vertical-align: middle;
}

.ios-share-svg {
  width: 14px;
  height: 14px;
}

.ios-plus-svg {
  width: 14px;
  height: 14px;
}

/* 底部操作 */
.card-footer {
  display: flex;
  justify-content: center;
}

.got-it-btn {
  width: 100%;
  height: 38px;
  border-radius: 19px;
  border: none;
  background: linear-gradient(135deg, #FF6842 0%, #FF385C 100%);
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.got-it-btn:active {
  opacity: 0.85;
}

/* 底部指向箭头 */
.pointing-arrow-wrapper {
  position: absolute;
  bottom: -16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  animation: bounceDown 1.2s infinite ease-in-out;
}

.pointing-arrow {
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-top: 14px solid rgba(255, 255, 255, 0.94);
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.15));
}

@keyframes bounceDown {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(6px);
  }
}

@keyframes cardSlideUp {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Vue 过渡动画 */
.guide-fade-enter-active,
.guide-fade-leave-active {
  transition: opacity 0.3s ease;
}

.guide-fade-enter-from,
.guide-fade-leave-to {
  opacity: 0;
}
</style>
