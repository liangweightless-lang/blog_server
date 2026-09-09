<template>
  <a-layout id="app" :class="{ 'header-hidden': isMobile && (isLoggedIn || $route.meta.hideHeaderMobile) }">
    <GlobalHeader v-if="!isMobile" />
    <MobileHeader v-else-if="!isLoggedIn && !$route.meta.hideHeaderMobile" />
    <a-layout-content class="main-content">
      <router-view></router-view>
    </a-layout-content>
    <GlobalFooter v-if="!isMobile" />
    <template v-else-if="!$route.meta.hideBottomNav">
      <div class="bottom-nav-spacer"></div>
      <MobileBottomNav />
    </template>
    
    <!-- 全局登录组件 -->
    <LoginDialog :show="loginDialogVisible" @update:show="val => loginDialogVisible = val" />

    <!-- iOS 用户添加到主屏幕指引浮层 -->
    <IosInstallGuide />
  </a-layout>
</template>

<script>
import GlobalHeader from './components/layout/GlobalHeader.vue'
import MobileHeader from './components/layout/MobileHeader.vue'
import MobileBottomNav from './components/layout/MobileBottomNav.vue'
import GlobalFooter from './components/layout/GlobalFooter.vue'
import LoginDialog from './components/auth/LoginDialog.vue'
import IosInstallGuide from './components/common/IosInstallGuide.vue'
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'
import { App as CapApp } from '@capacitor/app'
import { Message } from '@arco-design/web-vue'

export default {
  name: 'App',
  components: {
    GlobalHeader,
    MobileHeader,
    MobileBottomNav,
    GlobalFooter,
    LoginDialog,
    IosInstallGuide
  },
  data() {
    return {
      loginDialogVisible: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo', 'isLoggedIn']),
    user() {
      return this.userInfo;
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('open-login', this.showLogin);
    window.addEventListener('auth-expired', this.handleAuthExpired);
    
    // 监听授权成功事件，触发获取用户信息
    window.addEventListener('auth-success', this.fetchUser);
    window.addEventListener('refresh-user', this.fetchUser);
    
    this.fetchUser();

    // 拦截 Android 硬件返回键 / 侧滑返回手势 (解决滑动手势误退应用问题)
    const isCapacitor = typeof window !== 'undefined' && window.Capacitor;
    if (isCapacitor) {
      this.lastBackTime = 0;
      CapApp.addListener('backButton', () => {
        // 1. 如果登录弹窗处于打开状态，仅关闭弹窗
        if (this.loginDialogVisible) {
          this.loginDialogVisible = false;
          return;
        }

        const currentPath = this.$route.path;

        // 2. 如果当前不是首页根路径，优先返回上一页或返回首页
        if (currentPath !== '/') {
          if (currentPath === '/store' || currentPath === '/profile') {
            // 如果在商城页或个人主页等主 Tab，统一平滑返回到首页
            this.$router.push('/');
          } else {
            // 其他子页面优先回退上一页
            this.$router.back();
          }
          return;
        }

        // 3. 如果当前已经在首页根路径，采用现代 App 双击防误触退出机制
        const now = Date.now();
        if (this.lastBackTime && (now - this.lastBackTime < 2000)) {
          CapApp.exitApp();
        } else {
          this.lastBackTime = now;
          Message.info({ content: '再按一次或右划退出应用', duration: 2000 });
        }
      });
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('open-login', this.showLogin);
    window.removeEventListener('auth-expired', this.handleAuthExpired);
    window.removeEventListener('auth-success', this.fetchUser);
    window.removeEventListener('refresh-user', this.fetchUser);
  },
  methods: {
    ...mapActions(useUserStore, ['fetchUser', 'clearUser']),
    handleAuthExpired() {
      this.clearUser();
      this.showLogin();
      // 可选：如果处于受保护的路由中，则重定向到首页
      if (this.$route.path === '/profile' || this.$route.path.startsWith('/admin')) {
        this.$router.push('/');
      }
    },
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    showLogin() {
      this.loginDialogVisible = true;
    }
  }
}
</script>

<style>
:root {
  /* 全局安全区高度变量，后续任何新页面都可以直接使用 var(--safe-top) */
  --safe-top: env(safe-area-inset-top, 0px);
  --safe-bottom: env(safe-area-inset-bottom, 0px);
  --brand-primary: #FF4B2B;
  --brand-gradient: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-blur: blur(30px);
}

body {
  margin: 0;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Helvetica, "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background: linear-gradient(180deg, #FAFAFA 0%, #F4F6F9 100%); /* 现代感柔和渐变背景 */
  background-attachment: fixed;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #1D2129; /* 深灰色以提高可读性 */
  letter-spacing: 0.2px;
  box-sizing: border-box;
}

*, *::before, *::after {
  box-sizing: inherit;
}

.main-content {
  padding: 20px 10px 100px 10px; /* 增强移动端内边距，底部留白以适配悬浮导航栏 */
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.header-hidden .main-content {
  padding-top: 0;
}
@media (min-width: 768px) {
  .main-content {
    padding: 30px 15px 40px 15px;
  }
}

/* ===== 全局移动端体验优化 ===== */
* {
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

@media (hover: none) and (pointer: coarse), (max-width: 768px) {
  /* 移动端极速轻量反馈 (硬件加速，零延迟) */
  button, 
  .arco-btn,
  .brand-btn,
  .clickable-card,
  .action-btn {
    transition: transform 0.08s ease;
  }
  
  button:active:not([disabled]),
  .arco-btn:active:not([disabled]):not(.arco-btn-disabled),
  .brand-btn:active:not([disabled]),
  .clickable-card:active {
    transform: scale(0.96);
  }
}

@media (max-width: 768px) {
  /* 调整 Toast 容器位置，避免被刘海/灵动岛遮挡 */
  .arco-message-list {
    top: max(60px, env(safe-area-inset-top, 60px)) !important;
  }

  /* 优化 Toast 提示：更大、更圆润、阴影更深 */
  .arco-message {
    padding: 12px 20px !important;
    border-radius: 30px !important;
    font-size: 14px !important;
    box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
    background-color: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
  }
  
  /* 屏蔽除确认框以外的移动端 Arco 默认 PC 头部 (自建卡片弹窗已有内部标题) */
  .arco-modal:not(.arco-modal-simple) .arco-modal-header {
    display: none !important;
  }

  /* 遮罩层高质感半透明暗光与高斯模糊 */
  .arco-modal-mask {
    background-color: rgba(0, 0, 0, 0.52) !important;
    backdrop-filter: blur(6px) !important;
    -webkit-backdrop-filter: blur(6px) !important;
  }

  /* 移动端弹窗容器统一垂直居中，底部留出空间放置悬浮关闭按钮 */
  .arco-modal-wrapper {
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 24px 0 68px !important;
  }

  /* 全局屏蔽移动端顶部拉手条 (已全面摒弃贴底抽屉，统一为标准浮动卡片) */
  .sheet-handle-bar,
  .handle-bar,
  .sheet-handle-wrapper {
    display: none !important;
  }

  /* 解决长弹窗滚动条体验 */
  .arco-modal-body::-webkit-scrollbar,
  .arco-drawer-body::-webkit-scrollbar {
    width: 4px;
    background-color: transparent;
  }
  .arco-modal-body::-webkit-scrollbar-thumb,
  .arco-drawer-body::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 10px;
  }

  /* 【市面手机主流规范】全站移动端卡片弹窗一律统一为：左右有间距、四周24px全圆角、悬浮卡片 */
  .arco-modal:not(.arco-modal-simple),
  .checkout-modal,
  .creator-modal-mobile,
  .group-dialog,
  .custom-share-modal,
  .wechat-pay-modal,
  .universal-cashier-modal,
  .universal-bottom-sheet-modal,
  .login-dialog-modal {
    width: calc(100% - 32px) !important;
    max-width: 440px !important;
    position: relative !important;
    bottom: auto !important;
    left: auto !important;
    right: auto !important;
    margin: auto !important;
    padding: 0 !important;
    border-radius: 24px !important;
    border: none !important;
    background: #FFFFFF !important;
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.22) !important;
    max-height: calc(85vh - 70px) !important;
    overflow: visible !important;
    display: flex !important;
    flex-direction: column !important;
    align-items: center !important;
    animation: floatingScaleIn 0.28s cubic-bezier(0.16, 1, 0.3, 1) !important;
  }

  /* 对于自定义外置关闭按钮的卡片，背景设为透明，避免双重阴影 */
  .arco-modal.floating-card-modal {
    background: transparent !important;
    box-shadow: none !important;
  }
  .arco-modal.floating-card-modal .arco-modal-body {
    overflow: visible !important;
    max-height: none !important;
    background: transparent !important;
  }

  .arco-modal:not(.arco-modal-simple) .arco-modal-body {
    width: 100% !important;
    border-radius: 24px !important;
    overflow-y: auto !important;
    max-height: calc(85vh - 70px) !important;
    padding: 0 !important;
  }

  /* 【系统确认弹窗专项优化】Modal.confirm / Modal.warning 弹窗排版，彻底解决文字贴边与溢出问题 */
  .arco-modal-simple {
    width: calc(100% - 48px) !important;
    max-width: 360px !important;
    border-radius: 24px !important;
    background: #FFFFFF !important;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.26) !important;
    padding: 24px 22px 20px 22px !important;
    box-sizing: border-box !important;
    overflow: hidden !important;
    margin: auto !important;
    border: none !important;
    animation: floatingScaleIn 0.25s cubic-bezier(0.16, 1, 0.3, 1) !important;
  }

  .arco-modal-simple .arco-modal-header {
    display: flex !important;
    align-items: center !important;
    height: auto !important;
    padding: 0 0 12px 0 !important;
    border: none !important;
    margin: 0 !important;
  }

  .arco-modal-simple .arco-modal-title {
    font-size: 17px !important;
    font-weight: 800 !important;
    color: #1D2129 !important;
    line-height: 1.4 !important;
    display: flex !important;
    align-items: center !important;
    gap: 8px !important;
  }

  .arco-modal-simple .arco-modal-body {
    width: 100% !important;
    padding: 0 0 20px 0 !important;
    font-size: 14px !important;
    line-height: 1.65 !important;
    color: #4E5969 !important;
    word-break: break-word !important;
    overflow-wrap: break-word !important;
    white-space: normal !important;
    box-sizing: border-box !important;
  }

  .arco-modal-simple .arco-modal-footer {
    display: flex !important;
    justify-content: flex-end !important;
    gap: 10px !important;
    padding: 0 !important;
    border: none !important;
    margin: 0 !important;
    width: 100% !important;
    box-sizing: border-box !important;
  }

  .arco-modal-simple .arco-modal-footer .arco-btn {
    flex: 1 !important;
    height: 40px !important;
    border-radius: 20px !important;
    font-size: 14px !important;
    font-weight: 600 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
  }

  /* 确认提示框仅靠操作按钮关闭，隐藏下方悬浮的独立小关闭按钮 */
  .arco-modal-simple .arco-modal-close-btn {
    display: none !important;
  }

  /* 【核心交互】移动端 Arco 默认业务弹窗关闭按钮统一转换为：居中悬浮在白色卡片正下方外部的圆形关闭按钮 */
  .arco-modal:not(.floating-card-modal):not(.arco-modal-simple) .arco-modal-close-btn,
  .sheet-circle-close {
    display: flex !important;
    position: absolute !important;
    top: auto !important;
    right: auto !important;
    bottom: -58px !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
    width: 40px !important;
    height: 40px !important;
    border-radius: 50% !important;
    background: rgba(30, 30, 30, 0.45) !important;
    backdrop-filter: blur(8px) !important;
    -webkit-backdrop-filter: blur(8px) !important;
    border: 1.5px solid rgba(255, 255, 255, 0.85) !important;
    color: #FFFFFF !important;
    font-size: 18px !important;
    align-items: center !important;
    justify-content: center !important;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25) !important;
    cursor: pointer !important;
    z-index: 9999 !important;
    transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
  }
  .arco-modal:not(.floating-card-modal):not(.arco-modal-simple) .arco-modal-close-btn:active,
  .sheet-circle-close:active {
    transform: translateX(-50%) scale(0.9) !important;
    background: rgba(0, 0, 0, 0.75) !important;
  }
  .arco-modal .arco-modal-close-btn .arco-icon,
  .sheet-circle-close .arco-icon {
    font-size: 18px !important;
    color: #FFFFFF !important;
  }
  .arco-modal .arco-modal-close-btn .arco-icon-hover {
    background: transparent !important;
  }

  /* 当已存在专门的 outside-close-wrapper 时，隐藏冗余的关闭按钮 */
  .floating-card-modal .arco-modal-close-btn {
    display: none !important;
  }

  @keyframes floatingScaleIn {
    from {
      opacity: 0;
      transform: scale(0.92) translateY(16px);
    }
    to {
      opacity: 1;
      transform: scale(1) translateY(0);
    }
  }
  
  .arco-modal-body {
    padding: 0 !important;
    max-height: 85vh !important;
    overflow-y: auto !important;
    -webkit-overflow-scrolling: touch !important;
    word-break: break-word !important;
  }
}

@keyframes slideUpBottomSheet {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
</style>
