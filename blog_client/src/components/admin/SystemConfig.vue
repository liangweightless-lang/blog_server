<template>
  <div class="system-config">
    <el-form :model="homeConfigForm" label-width="120px" style="max-width: 600px; margin-top: 20px;" size="small">
      <el-form-item label="首页头像">
        <el-upload
          class="product-image-uploader"
          action="/api/files/upload"
          :show-file-list="false"
          :on-success="handleHomeAvatarSuccess"
          :before-upload="beforeProductImageUpload">
          <img v-if="homeConfigForm.avatarUrl" :src="homeConfigForm.avatarUrl" class="product-upload-preview">
          <div v-else class="product-upload-placeholder">
            <i class="el-icon-plus"></i>
            <span>上传头像</span>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="微信二维码">
        <el-upload
          class="product-image-uploader"
          action="/api/files/upload"
          :show-file-list="false"
          :on-success="handleWechatQrSuccess"
          :before-upload="beforeProductImageUpload">
          <img v-if="homeConfigForm.wechatQrUrl" :src="homeConfigForm.wechatQrUrl" class="product-upload-preview">
          <div v-else class="product-upload-placeholder">
            <i class="el-icon-plus"></i>
            <span>上传二维码</span>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="创作者名称">
        <el-input v-model="homeConfigForm.authorName" placeholder="例如: 小柴包"></el-input>
      </el-form-item>
      <el-form-item label="个性签名/简介">
        <el-input type="textarea" :rows="4" v-model="homeConfigForm.authorBio" placeholder="请输入创作者简介"></el-input>
      </el-form-item>
      <el-form-item label="兴趣标签 (用英文逗号分隔)">
        <el-input v-model="homeConfigForm.tagsString" placeholder="例如: 生活方式,独立品牌,创作手记"></el-input>
      </el-form-item>
      
      <el-divider content-position="left">第三方服务配置</el-divider>
      
      <el-form-item label="高德地图 Key">
        <el-input v-model="homeConfigForm.amapKey" placeholder="用于收货地址定位 (Web端 JS API Key)"></el-input>
      </el-form-item>
      <el-form-item label="高德安全密钥">
        <el-input v-model="homeConfigForm.amapSecurityCode" placeholder="配合高德地图 Key 使用的安全密钥"></el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" :loading="savingConfig" @click="saveHomeConfig">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'SystemConfig',
  data() {
    return {
      savingConfig: false,
      homeConfigForm: {
        avatarUrl: '',
        authorName: '',
        authorBio: '',
        tagsString: '',
        wechatQrUrl: '',
        amapKey: '',
        amapSecurityCode: ''
      }
    }
  },
  created() {
    this.fetchHomeConfig();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchHomeConfig() {
      try {
        const res = await axios.get('/api/home/config');
        if (res.data && res.data.data) {
          const data = res.data.data;
          this.homeConfigForm = {
            avatarUrl: data.avatarUrl,
            authorName: data.authorName,
            authorBio: data.authorBio,
            tagsString: data.tags ? data.tags.join(',') : '',
            wechatQrUrl: data.wechatQrUrl || '',
            amapKey: data.amapKey || '',
            amapSecurityCode: data.amapSecurityCode || ''
          };
        }
      } catch (error) {
        this.$message.error('获取系统配置失败');
      }
    },
    beforeProductImageUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImg) this.$message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt2M) this.$message.error('图片大小不能超过 2MB!');
      return isImg && isLt2M;
    },
    handleHomeAvatarSuccess(res) {
      this.homeConfigForm.avatarUrl = res.url;
      this.$message.success('头像上传成功');
    },
    handleWechatQrSuccess(res) {
      this.homeConfigForm.wechatQrUrl = res.url;
      this.$message.success('二维码上传成功');
    },
    async saveHomeConfig() {
      this.savingConfig = true;
      try {
        const payload = {
          ...this.homeConfigForm,
          tags: this.homeConfigForm.tagsString.split(',').map(s => s.trim()).filter(Boolean)
        };
        delete payload.tagsString;
        
        await axios.post('/api/home/config', payload, { headers: this.getAuthHeader() });
        this.$message.success('系统配置保存成功！');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '保存失败');
      } finally {
        this.savingConfig = false;
      }
    }
  }
}
</script>

<style scoped>
.product-image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.product-image-uploader:hover {
  border-color: #409EFF;
}
.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
}
.product-upload-placeholder i {
  font-size: 24px;
  margin-bottom: 8px;
}
.product-upload-preview {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style>
