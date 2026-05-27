<template>
  <a-modal :visible="visible" :width="dialogWidth" @cancel="visible = false" :footer="false" :closable="true" modal-class="auth-dialog">
    <template #title>
      <div style="height: 10px;"></div>
    </template>
    <a-tabs v-model:active-key="activeTab" type="line" justify>
      <a-tab-pane title="登录" key="login">
        <a-form :model="loginForm" layout="vertical">
          <a-form-item label="手机号">
            <a-input v-model="loginForm.username" placeholder="请输入11位手机号" :max-length="11" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model="loginForm.password" placeholder="请输入密码" @press-enter="handleAction" />
          </a-form-item>
        </a-form>
      </a-tab-pane>
      <a-tab-pane title="注册" key="register">
        <a-form :model="registerForm" layout="vertical">
          <a-form-item label="手机号">
            <a-input v-model="registerForm.username" placeholder="请输入11位手机号" :max-length="11" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model="registerForm.password" placeholder="请设置密码" />
          </a-form-item>
          <a-form-item label="验证码">
            <div style="display: flex; gap: 10px; align-items: center; width: 100%;">
              <a-input v-model="registerForm.captchaCode" placeholder="请输入图形验证码" style="flex: 1;" @press-enter="handleAction" />
              <img v-if="captchaImage" :src="captchaImage" @click="fetchCaptcha" style="cursor: pointer; height: 32px; border-radius: 4px; border: 1px solid #E5E6EB;" title="点击刷新">
            </div>
          </a-form-item>
          <a-form-item label="邀请码 (可选)">
            <a-input v-model="registerForm.inviteCode" placeholder="如果有邀请码请填写" @press-enter="handleAction" />
          </a-form-item>
        </a-form>
      </a-tab-pane>
    </a-tabs>
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
      <a-button @click="visible = false">取 消</a-button>
      <a-button type="primary" @click="handleAction" :loading="loading">{{ activeTab === 'login' ? '登 录' : '注 册' }}</a-button>
    </div>
  </a-modal>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';

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
      loading: false,
      activeTab: 'login',
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
    dialogWidth() {
      return this.isMobile ? '90%' : '360px';
    }
  },
  watch: {
    show(val) {
      this.visible = val;
    },
    visible(val) {
      this.$emit('update:show', val);
      if (val) {
        if (this.activeTab === 'register') {
          this.fetchCaptcha();
        }
      }
    },
    activeTab(val) {
      if (val === 'register') {
        this.fetchCaptcha();
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    // Check for invite code in URL
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
    async fetchCaptcha() {
      try {
        const res = await axios.get('/api/auth/captcha');
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
      if (!isLogin && !form.captchaCode) {
        return Message.warning('请输入图形验证码');
      }
      
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (form.username !== 'admin' && !phoneRegex.test(form.username)) {
        return Message.error('请输入正确的11位手机号');
      }

      this.loading = true;
      try {
        const endpoint = isLogin ? '/api/users/login' : '/api/users/register';
        const res = await axios.post(endpoint, form);
        const data = res.data.data;
        localStorage.setItem('token', data.token);
        this.visible = false;
        Message.success('欢迎回来, ' + data.user.nickname);
        this.$emit('update:show', false);
        window.dispatchEvent(new CustomEvent('auth-success'));
      } catch (error) {
        Message.error(error.response?.data?.message || (isLogin ? '登录失败' : '注册失败'));
        if (!isLogin) {
          this.fetchCaptcha(); // 注册失败后自动刷新验证码
        }
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
<style scoped>
:deep(.auth-dialog .arco-modal-header) {
  border-bottom: none;
}
:deep(.auth-dialog .arco-tabs-nav-tab) {
  display: flex;
  justify-content: center;
}
</style>
