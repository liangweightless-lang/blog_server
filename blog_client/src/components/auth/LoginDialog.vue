<template>
  <el-dialog title="登录 / 注册" :visible.sync="visible" :width="dialogWidth" center append-to-body>
    <el-form :model="loginForm" label-position="top">
      <el-form-item label="手机号">
        <el-input v-model="loginForm.username" placeholder="请输入11位手机号" maxlength="11"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item label="邀请码 (可选)">
        <el-input v-model="loginForm.inviteCode" placeholder="如果有邀请码请填写"></el-input>
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
        inviteCode: ''
      },
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
    async handleLogin() {
      const { username, password } = this.loginForm;
      if (!username || !password) {
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
        localStorage.setItem('token', res.data.token);
        this.visible = false;
        this.$message.success('欢迎回来, ' + res.data.user.nickname);
        // Refresh globally
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '登录失败');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
