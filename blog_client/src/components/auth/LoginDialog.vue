<template>
  <el-dialog title="登录 / 注册" :visible.sync="visible" :width="dialogWidth" center append-to-body>
    <el-form :model="loginForm" label-position="top">
      <el-form-item label="手机号">
        <el-input v-model="loginForm.username" placeholder="请输入11位手机号" maxlength="11"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item label="验证码">
        <div style="display: flex; gap: 10px; align-items: center;">
          <el-input v-model="loginForm.captchaCode" placeholder="请输入图形验证码" style="flex: 1;" @keyup.enter.native="handleLogin"></el-input>
          <img v-if="captchaImage" :src="captchaImage" @click="fetchCaptcha" style="cursor: pointer; height: 38px; border-radius: 4px; border: 1px solid #DCDFE6;" title="点击刷新">
        </div>
      </el-form-item>
      <el-form-item label="邀请码 (可选)">
        <el-input v-model="loginForm.inviteCode" placeholder="如果有邀请码请填写" @keyup.enter.native="handleLogin"></el-input>
      </el-form-item>
    </el-form>
    <div style="font-size: 12px; color: #909399; margin-bottom: 20px;">
      * 如果账号不存在，将自动为您创建新账号。
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleLogin" :loading="loading">立即进入</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios';

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
      loginForm: {
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
        this.fetchCaptcha(); // 每次打开弹窗时刷新验证码
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    // Check for invite code in URL
    const urlParams = new URLSearchParams(window.location.search);
    const invite = urlParams.get('invite');
    if (invite) {
      this.loginForm.inviteCode = invite;
    }
  },
  beforeDestroy() {
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
          this.loginForm.captchaKey = res.data.data.captchaKey;
          this.captchaImage = res.data.data.captchaImage;
        }
      } catch (error) {
        console.error('获取验证码失败', error);
      }
    },
    async handleLogin() {
      const { username, password, captchaCode } = this.loginForm;
      if (!username || !password || !captchaCode) {
        return this.$message.warning('请填写完整信息');
      }
      
      // Phone number validation (11 digits), allow 'admin' for backend access
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (username !== 'admin' && !phoneRegex.test(username)) {
        return this.$message.error('请输入正确的11位手机号');
      }

      this.loading = true;
      try {
        const res = await axios.post('/api/users/login', this.loginForm);
        const data = res.data.data;
        localStorage.setItem('token', data.token);
        this.visible = false;
        this.$message.success('欢迎回来, ' + data.user.nickname);
        // Refresh globally
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '登录失败');
        this.fetchCaptcha(); // 登录失败后自动刷新验证码
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
