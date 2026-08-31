<template>
  <Teleport to="body">
    <Transition name="auth-overlay">
      <div v-if="visible" class="auth-overlay" @click.self="handleClose">
        <div 
          class="auth-container" 
          :class="{ 'is-mobile': isMobile, 'is-closing': closing }"
          @click.stop
        >
          <!-- 移动端顶部下拉条 / Handle bar -->
          <div v-if="isMobile" class="sheet-handle-wrapper" @click="handleClose">
            <div class="sheet-handle"></div>
          </div>

          <!-- 关闭按钮 -->
          <button class="close-btn" @click="handleClose" aria-label="关闭">
            <icon-close />
          </button>

          <!-- 品牌头部区域 -->
          <div class="auth-header">
            <div class="brand-badge">
              <icon-sun class="sun-icon" />
            </div>
            <h2 class="auth-title">
              {{ activeTab === 'login' ? '登录小柴包' : '加入小柴包' }}
            </h2>
            <p class="auth-subtitle">
              {{ activeTab === 'login' ? '探索美好创意与生活灵感' : '开启你的独立创意之旅' }}
            </p>
          </div>

          <!-- 胶囊式 Tab 切换 -->
          <div class="tab-switcher">
            <button 
              type="button"
              class="tab-btn" 
              :class="{ active: activeTab === 'login' }"
              @click="switchTab('login')"
            >
              登录
            </button>
            <button 
              type="button"
              class="tab-btn" 
              :class="{ active: activeTab === 'register' }"
              @click="switchTab('register')"
            >
              注册
            </button>
            <div class="tab-slider" :style="{ transform: activeTab === 'login' ? 'translateX(0)' : 'translateX(100%)' }"></div>
          </div>

          <!-- 表单区域 -->
          <form class="auth-form" @submit.prevent="handleAction">
            <!-- 手机号/用户名输入 -->
            <div class="input-group">
              <div class="input-prefix-label">+86</div>
              <input 
                v-model.trim="currentForm.username"
                type="tel"
                maxlength="11"
                placeholder="请输入手机号"
                class="custom-input with-prefix"
                autocomplete="username"
              />
              <button 
                v-if="currentForm.username" 
                type="button" 
                class="clear-input-btn"
                @click="currentForm.username = ''"
              >
                <icon-close-circle-fill />
              </button>
            </div>

            <!-- 密码输入 -->
            <div class="input-group">
              <input 
                v-model="currentForm.password"
                :type="showPassword ? 'text' : 'password'"
                :placeholder="activeTab === 'login' ? '请输入密码' : '请设置密码 (至少6位)'"
                class="custom-input with-suffix"
                autocomplete="current-password"
                @keyup.enter="handleAction"
              />
              <button 
                type="button" 
                class="toggle-eye-btn" 
                @click="showPassword = !showPassword"
              >
                <icon-eye v-if="showPassword" />
                <icon-eye-invisible v-else />
              </button>
            </div>

            <!-- 注册专属：图形验证码 -->
            <Transition name="fade-slide">
              <div v-if="activeTab === 'register'" class="input-group captcha-group">
                <input 
                  v-model.trim="registerForm.captchaCode"
                  type="text"
                  maxlength="6"
                  placeholder="请输入图形验证码"
                  class="custom-input captcha-input"
                  @keyup.enter="handleAction"
                />
                <div class="captcha-box" @click="fetchCaptcha" title="点击刷新验证码">
                  <img v-if="captchaImage" :src="captchaImage" alt="图形验证码" class="captcha-img" />
                  <div v-else class="captcha-loading">
                    <icon-refresh :spin="true" />
                  </div>
                  <span class="refresh-hint">刷新</span>
                </div>
              </div>
            </Transition>

            <!-- 注册专属：邀请码 -->
            <Transition name="fade-slide">
              <div v-if="activeTab === 'register'" class="input-group">
                <input 
                  v-model.trim="registerForm.inviteCode"
                  type="text"
                  placeholder="邀请码 (选填，有邀请人可填写)"
                  class="custom-input"
                  @keyup.enter="handleAction"
                />
              </div>
            </Transition>

            <!-- 协议同意勾选（小红书/美团风格） -->
            <div class="agreement-wrapper" :class="{ 'shake-animation': agreementShake }">
              <label class="custom-checkbox">
                <input type="checkbox" v-model="agreeAgreement" />
                <span class="checkmark">
                  <icon-check class="check-icon" />
                </span>
              </label>
              <span class="agreement-text">
                我已阅读并同意
                <a href="javascript:;" class="agreement-link" @click.stop="openNotice('服务协议')">《用户服务协议》</a>
                和
                <a href="javascript:;" class="agreement-link" @click.stop="openNotice('隐私政策')">《隐私政策》</a>
              </span>
            </div>

            <!-- 提交大按钮（美团/小红书胶囊大按钮） -->
            <button 
              type="submit" 
              class="submit-btn" 
              :class="{ loading: loading }"
              :disabled="loading"
            >
              <icon-loading v-if="loading" :spin="true" class="btn-spinner" />
              <span>{{ loading ? '处理中...' : (activeTab === 'login' ? '登 录' : '立即注册') }}</span>
            </button>
          </form>

          <!-- 底部快速引导 -->
          <div class="auth-footer">
            <span v-if="activeTab === 'login'">
              还没有小柴包账号？
              <a href="javascript:;" class="switch-link" @click="switchTab('register')">立即注册</a>
            </span>
            <span v-else>
              已有小柴包账号？
              <a href="javascript:;" class="switch-link" @click="switchTab('login')">返回登录</a>
            </span>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
import { getCaptcha, authAction } from '@/api/user';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'LoginDialog',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      closing: false,
      loading: false,
      activeTab: 'login',
      showPassword: false,
      agreeAgreement: true,
      agreementShake: false,
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        captchaKey: '',
        captchaCode: '',
        inviteCode: ''
      },
      captchaImage: '',
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    currentForm() {
      return this.activeTab === 'login' ? this.loginForm : this.registerForm;
    }
  },
  watch: {
    show(val) {
      if (val) {
        this.open();
      } else {
        this.close();
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    // 检查 URL 是否带邀请码
    const urlParams = new URLSearchParams(window.location.search);
    const invite = urlParams.get('invite');
    if (invite) {
      this.registerForm.inviteCode = invite;
      this.activeTab = 'register';
    }
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    open() {
      this.visible = true;
      this.closing = false;
      if (this.activeTab === 'register' && !this.captchaImage) {
        this.fetchCaptcha();
      }
    },
    close() {
      this.closing = true;
      setTimeout(() => {
        this.visible = false;
        this.closing = false;
        this.$emit('update:show', false);
      }, 200);
    },
    handleClose() {
      this.close();
    },
    switchTab(tab) {
      this.activeTab = tab;
      this.showPassword = false;
      if (tab === 'register' && !this.captchaImage) {
        this.fetchCaptcha();
      }
    },
    async fetchCaptcha() {
      try {
        const res = await getCaptcha();
        if (res.data && res.data.data) {
          this.registerForm.captchaKey = res.data.data.captchaKey;
          this.captchaImage = res.data.data.captchaImage;
        }
      } catch (error) {
        console.error('获取验证码失败', error);
      }
    },
    async handleAction() {
      const isLogin = this.activeTab === 'login';
      const form = isLogin ? this.loginForm : this.registerForm;

      if (!form.username || !form.password) {
        return Message.warning('请输入手机号和密码');
      }

      const phoneRegex = /^1[3-9]\d{9}$/;
      if (form.username !== 'admin' && !phoneRegex.test(form.username)) {
        return Message.error('请输入正确的11位手机号');
      }

      if (!isLogin && !form.captchaCode) {
        return Message.warning('请输入图形验证码');
      }

      if (!this.agreeAgreement) {
        this.agreementShake = true;
        setTimeout(() => {
          this.agreementShake = false;
        }, 600);
        return Message.warning('请阅读并勾选同意服务协议与隐私政策');
      }

      this.loading = true;
      try {
        const endpoint = isLogin ? '/api/users/login' : '/api/users/register';
        const res = await authAction(endpoint, form);
        const data = res.data.data;
        
        localStorage.setItem('token', data.token);
        Message.success('欢迎回来, ' + (data.user?.nickname || '小柴包探索者'));
        
        this.handleClose();
        window.dispatchEvent(new CustomEvent('auth-success'));
      } catch (error) {
        Message.error(error.response?.data?.message || (isLogin ? '登录失败，请检查账号或密码' : '注册失败'));
        if (!isLogin) {
          this.fetchCaptcha(); // 注册失败自动刷新验证码
        }
      } finally {
        this.loading = false;
      }
    },
    openNotice(type) {
      Modal.info({
        title: `小柴包${type}`,
        content: `欢迎使用小柴包！我们将严格按照法律法规保护您的个人信息安全，仅在为您提供原创内容浏览、灵感互动与商品服务时使用必要权限与数据。`,
        okText: '我知道了'
      });
    }
  }
}
</script>

<style scoped>
/* 遮罩背景 */
.auth-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background-color: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

/* 主卡片容器 (桌面端) */
.auth-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  border-radius: 24px;
  padding: 32px 28px 24px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  animation: modalScaleIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #F4F5F7;
  color: #86909C;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}
.close-btn:hover {
  background: #E5E6EB;
  color: #1D2129;
  transform: rotate(90deg);
}

/* 品牌头部 */
.auth-header {
  text-align: center;
  margin-bottom: 22px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: linear-gradient(135deg, #FFF0EB 0%, #FFE4DC 100%);
  color: #FF5330;
  font-size: 26px;
  margin-bottom: 12px;
  box-shadow: 0 8px 16px rgba(255, 83, 48, 0.15);
}

.auth-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1D2129;
  letter-spacing: -0.5px;
}

.auth-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #86909C;
}

/* Tab 胶囊切换器 (小红书轻量风格) */
.tab-switcher {
  position: relative;
  display: flex;
  background: #F2F3F5;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 22px;
}

.tab-btn {
  flex: 1;
  position: relative;
  z-index: 2;
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #4E5969;
  cursor: pointer;
  transition: color 0.2s ease;
}

.tab-btn.active {
  color: #1D2129;
}

.tab-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: #FFFFFF;
  border-radius: 9px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.25s cubic-bezier(0.2, 0, 0, 1);
  z-index: 1;
}

/* 表单输入框组 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: #F7F8FA;
  border: 1.5px solid transparent;
  border-radius: 14px;
  transition: all 0.2s ease;
  overflow: hidden;
}

.input-group:focus-within {
  background: #FFFFFF;
  border-color: #FF5330;
  box-shadow: 0 0 0 3px rgba(255, 83, 48, 0.12);
}

.input-prefix-label {
  padding-left: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #4E5969;
  user-select: none;
  border-right: 1px solid #E5E6EB;
  margin-right: 10px;
  padding-right: 8px;
  line-height: 1.2;
}

.custom-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 46px;
  padding: 0 14px;
  font-size: 15px;
  color: #1D2129;
  outline: none;
}

.custom-input.with-prefix {
  padding-left: 0;
}

.custom-input::placeholder {
  color: #A4ACB9;
  font-size: 14px;
}

.clear-input-btn,
.toggle-eye-btn {
  border: none;
  background: transparent;
  padding: 0 14px;
  color: #86909C;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.clear-input-btn:hover,
.toggle-eye-btn:hover {
  color: #4E5969;
}

/* 验证码特殊样式 */
.captcha-group {
  padding-right: 6px;
}

.captcha-box {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 8px;
  background: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
}

.captcha-img {
  height: 28px;
  width: auto;
  border-radius: 4px;
}

.captcha-loading {
  font-size: 14px;
  color: #FF5330;
}

.refresh-hint {
  font-size: 11px;
  color: #86909C;
}

/* 协议同意行 (美团 / 小红书规范) */
.agreement-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;
  margin-top: 2px;
}

.custom-checkbox {
  position: relative;
  display: inline-flex;
  cursor: pointer;
  margin-top: 1px;
}

.custom-checkbox input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  width: 16px;
  height: 16px;
  border: 1.5px solid #C9CDD4;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: #FFFFFF;
}

.check-icon {
  display: none;
  font-size: 10px;
  color: #FFFFFF;
  stroke-width: 3;
}

.custom-checkbox input:checked ~ .checkmark {
  background: #FF5330;
  border-color: #FF5330;
}

.custom-checkbox input:checked ~ .checkmark .check-icon {
  display: block;
}

.agreement-text {
  flex: 1;
}

.agreement-link {
  color: #FF5330;
  text-decoration: none;
  font-weight: 500;
}

.agreement-link:hover {
  text-decoration: underline;
}

/* 登录/注册主大按钮 (大胶囊满宽) */
.submit-btn {
  height: 48px;
  border-radius: 24px;
  border: none;
  background: linear-gradient(135deg, #FF6842 0%, #FF385C 100%);
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  margin-top: 8px;
  box-shadow: 0 8px 20px rgba(255, 67, 76, 0.28);
  transition: all 0.25s cubic-bezier(0.2, 0, 0, 1);
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.95;
  box-shadow: 0 10px 24px rgba(255, 67, 76, 0.38);
  transform: translateY(-1px);
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-spinner {
  font-size: 18px;
}

/* 底部快捷链接 */
.auth-footer {
  text-align: center;
  font-size: 13px;
  color: #86909C;
  margin-top: 18px;
}

.switch-link {
  color: #FF5330;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}

.switch-link:hover {
  text-decoration: underline;
}

/* ================= 移动端小红书 / 美团 Bottom Sheet 样式 ================= */
@media (max-width: 768px) {
  .auth-overlay {
    align-items: flex-end;
    padding: 0;
  }

  .auth-container.is-mobile {
    max-width: 100%;
    border-radius: 24px 24px 0 0;
    padding: 12px 20px max(24px, env(safe-area-inset-bottom, 24px));
    animation: sheetSlideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1);
    max-height: 90vh;
    overflow-y: auto;
  }

  .auth-container.is-closing {
    animation: sheetSlideDown 0.2s ease forwards !important;
  }

  .sheet-handle-wrapper {
    display: flex;
    justify-content: center;
    padding: 6px 0 14px;
    cursor: pointer;
  }

  .sheet-handle {
    width: 36px;
    height: 4px;
    background: #E5E6EB;
    border-radius: 2px;
  }

  .close-btn {
    top: 14px;
    right: 16px;
    width: 28px;
    height: 28px;
  }

  .brand-badge {
    width: 46px;
    height: 46px;
    font-size: 22px;
    border-radius: 14px;
    margin-bottom: 8px;
  }

  .auth-title {
    font-size: 20px;
  }

  .submit-btn {
    height: 46px;
  }
}

/* 动效 */
@keyframes modalScaleIn {
  0% {
    opacity: 0;
    transform: scale(0.92);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes sheetSlideUp {
  0% {
    transform: translateY(100%);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes sheetSlideDown {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(100%);
  }
}

.shake-animation {
  animation: shake 0.5s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-4px); }
  40%, 80% { transform: translateX(4px); }
}

/* Vue 过渡动画 */
.auth-overlay-enter-active,
.auth-overlay-leave-active {
  transition: opacity 0.25s ease;
}

.auth-overlay-enter-from,
.auth-overlay-leave-to {
  opacity: 0;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

