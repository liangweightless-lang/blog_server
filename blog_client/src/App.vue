<template>
  <a-layout id="app" :class="{ 'header-hidden': isMobile && (isLoggedIn || $route.meta.hideHeaderMobile) }">
    <GlobalHeader v-if="!isMobile" />
    <MobileHeader v-else-if="!isLoggedIn && !$route.meta.hideHeaderMobile" />
    <a-layout-content class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="app-page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
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
import { Capacitor } from '@capacitor/core'
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
      isMobile: window.innerWidth <= 768,
      edgeSwipeStartX: 0,
      edgeSwipeStartY: 0,
      isEdgeSwiping: false,
      lastBackTime: 0
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

    // 监听全局触屏边缘右滑返回手势 (支持移动端浏览器与原生容器)
    window.addEventListener('touchstart', this.handleGlobalTouchStart, { passive: true });
    window.addEventListener('touchend', this.handleGlobalTouchEnd, { passive: true });

    // 监听 Android 硬件返回键 / 系统全面屏侧滑手势
    if (Capacitor.isNativePlatform()) {
      CapApp.addListener('backButton', () => {
        this.handleAppBack();
      });
    }
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('open-login', this.showLogin);
    window.removeEventListener('auth-expired', this.handleAuthExpired);
    window.removeEventListener('auth-success', this.fetchUser);
    window.removeEventListener('refresh-user', this.fetchUser);
    window.removeEventListener('touchstart', this.handleGlobalTouchStart);
    window.removeEventListener('touchend', this.handleGlobalTouchEnd);
  },
  methods: {
    ...mapActions(useUserStore, ['fetchUser', 'clearUser']),
    handleGlobalTouchStart(e) {
      if (!e.touches || e.touches.length !== 1) return;
      const touch = e.touches[0];
      // 触碰起点在屏幕左侧 30% 或至少 100px 范围内，均视作侧滑手势起手
      const maxLeft = Math.max(100, (window.innerWidth || 375) * 0.3);
      if (touch.clientX <= maxLeft) {
        this.edgeSwipeStartX = touch.clientX;
        this.edgeSwipeStartY = touch.clientY;
        this.isEdgeSwiping = true;
      } else {
        this.isEdgeSwiping = false;
      }
    },
    handleGlobalTouchEnd(e) {
      if (!this.isEdgeSwiping || !e.changedTouches || e.changedTouches.length === 0) return;
      this.isEdgeSwiping = false;
      const touch = e.changedTouches[0];
      const dx = touch.clientX - this.edgeSwipeStartX;
      const dy = touch.clientY - this.edgeSwipeStartY;
      // 向右滑动位移超过 40px，且水平距离大于垂直距离（判定为向右横滑）
      if (dx > 40 && Math.abs(dx) > Math.abs(dy)) {
        this.handleAppBack();
      }
    },
    handleAppBack() {
      // 1. 如果登录弹窗处于打开状态，仅关闭弹窗
      if (this.loginDialogVisible) {
        this.loginDialogVisible = false;
        return;
      }

      // 2. 检查是否有打开的可见弹窗或抽屉，优先关闭弹窗避免误触切路由
      const openModalCloseBtn = document.querySelector('.arco-modal-container:not([style*="display: none"]) .arco-modal-close-btn, .arco-drawer-container:not([style*="display: none"]) .arco-drawer-close-btn');
      if (openModalCloseBtn) {
        openModalCloseBtn.click();
        return;
      }

      // 3. 如果移动端工作台当前打开了子模块，通知关闭子模块回到工作台面板
      const hasSubModule = document.querySelector('.workbench-submodule-view');
      if (hasSubModule) {
        window.dispatchEvent(new CustomEvent('workbench-back'));
        return;
      }

      const currentPath = this.$route.path;

      // 4. 顶级菜单路由集合（首页、橱窗、我的）：统一执行防误触退出
      const TOP_ROUTES = ['/', '/store', '/profile'];
      if (TOP_ROUTES.includes(currentPath)) {
        const now = Date.now();
        if (this.lastBackTime && (now - this.lastBackTime < 2000)) {
          if (Capacitor.isNativePlatform()) {
            CapApp.exitApp();
          } else {
            Message.info('已是应用最外层');
          }
        } else {
          this.lastBackTime = now;
          Message.info({ content: '再按一次或右滑退出应用', duration: 2000 });
        }
        return;
      }

      // 5. 工作台页面（/admin 开头）：由于是从【我的】进来的，返回直达【我的】
      if (currentPath.startsWith('/admin')) {
        this.$router.push('/profile');
        return;
      }

      // 6. 其他普通子页面（商品详情、文章详情、创建活动等）：返回上一页
      if (window.history.length > 1) {
        this.$router.back();
      } else {
        this.$router.push('/');
      }
    },
    handleAuthExpired() {
      this.clearUser();
      this.showLogin();
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

/* 原生 App 级页面切换平滑转场动画 */
.app-page-fade-enter-active,
.app-page-fade-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}
.app-page-fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.app-page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
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
  .arco-modal:not(.arco-modal-simple):not(.product-buy-sheet-modal),
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

  /* 【市面电商规范】购买与结算底部抽屉 (Bottom Sheet) 移动端贴底滑出 */
  .arco-modal.product-buy-sheet-modal {
    width: 100% !important;
    max-width: 100% !important;
    position: fixed !important;
    bottom: 0 !important;
    left: 0 !important;
    right: 0 !important;
    top: auto !important;
    margin: 0 !important;
    padding: 0 !important;
    border-radius: 20px 20px 0 0 !important;
    background: #FFFFFF !important;
    box-shadow: 0 -8px 36px rgba(0, 0, 0, 0.18) !important;
    max-height: 86dvh !important;
    overflow: hidden !important;
    animation: slideUpBottomSheet 0.3s cubic-bezier(0.25, 1, 0.5, 1) !important;
  }
  .arco-modal.product-buy-sheet-modal .arco-modal-body {
    width: 100% !important;
    padding: 0 !important;
    max-height: 86dvh !important;
    overflow: hidden !important;
    border-radius: 20px 20px 0 0 !important;
    background: #FFFFFF !important;
  }

  /* 对于自定义外置关闭按钮的卡片，背景设为透明，避免双重阴影并保证垂直居中与可视区锁定 */
  .arco-modal.floating-card-modal {
    background: transparent !important;
    box-shadow: none !important;
    display: flex !important;
    flex-direction: column !important;
    justify-content: center !important;
    align-items: center !important;
    max-height: 96dvh !important;
    overflow: hidden !important;
    margin: auto !important;
  }
  .arco-modal.floating-card-modal .arco-modal-body {
    overflow: visible !important;
    max-height: none !important;
    background: transparent !important;
    padding: 0 !important;
    width: 100% !important;
  }

  .arco-modal:not(.arco-modal-simple):not(.product-buy-sheet-modal) .arco-modal-body {
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
  .arco-modal:not(.floating-card-modal):not(.arco-modal-simple):not(.product-buy-sheet-modal) .arco-modal-close-btn,
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

  /* 当已存在专门的 outside-close-wrapper 或内部专属关闭按钮时，隐藏冗余的默认关闭按钮 */
  .floating-card-modal .arco-modal-close-btn,
  .product-buy-sheet-modal .arco-modal-close-btn {
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
