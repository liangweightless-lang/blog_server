<template>
  <div class="system-config">
    <a-form :model="homeConfigForm" layout="vertical" style="max-width: 600px; margin-top: 20px;">
      <a-form-item label="首页头像">
        <a-upload
          :action="uploadAction"
          :show-file-list="false"
          @success="handleHomeAvatarSuccess"
          @before-upload="beforeProductImageUpload">
          <template #upload-button>
            <div class="product-image-uploader">
              <img v-if="homeConfigForm.avatarUrl" :src="homeConfigForm.avatarUrl" class="product-upload-preview">
              <div v-else class="product-upload-placeholder">
                <icon-plus />
                <span>上传头像</span>
              </div>
            </div>
          </template>
        </a-upload>
      </a-form-item>
      <a-form-item label="微信二维码">
        <a-upload
          :action="uploadAction"
          :show-file-list="false"
          @success="handleWechatQrSuccess"
          @before-upload="beforeProductImageUpload">
          <template #upload-button>
            <div class="product-image-uploader">
              <img v-if="homeConfigForm.wechatQrUrl" :src="homeConfigForm.wechatQrUrl" class="product-upload-preview">
              <div v-else class="product-upload-placeholder">
                <icon-plus />
                <span>上传二维码</span>
              </div>
            </div>
          </template>
        </a-upload>
      </a-form-item>
      <a-form-item label="创作者名称">
        <a-input v-model="homeConfigForm.authorName" placeholder="例如: 小柴包"></a-input>
      </a-form-item>
      <a-form-item label="个性签名/简介">
        <a-textarea :auto-size="{minRows:4}" v-model="homeConfigForm.authorBio" placeholder="请输入创作者简介"></a-textarea>
      </a-form-item>
      <a-form-item label="兴趣标签 (用英文逗号分隔)">
        <a-input v-model="homeConfigForm.tagsString" placeholder="例如: 生活方式,独立品牌,创作手记"></a-input>
      </a-form-item>
      
      <a-divider orientation="left">第三方服务配置</a-divider>
      
      <a-form-item label="高德地图 Key">
        <a-input v-model="homeConfigForm.amapKey" placeholder="用于收货地址定位 (Web端 JS API Key)"></a-input>
      </a-form-item>
      <a-form-item label="高德安全密钥">
        <a-input v-model="homeConfigForm.amapSecurityCode" placeholder="配合高德地图 Key 使用的安全密钥"></a-input>
      </a-form-item>
      
      <a-form-item>
        <a-button type="primary" :loading="savingConfig" @click="saveHomeConfig">保存配置</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script>
import { getHomeConfig, updateHomeConfig } from '@/api/common';
import { Message } from '@arco-design/web-vue';

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
  computed: {
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    async fetchHomeConfig() {
      try {
        const res = await getHomeConfig();
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
        Message.error('获取系统配置失败');
      }
    },
    beforeProductImageUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImg) Message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt2M) Message.error('图片大小不能超过 2MB!');
      return isImg && isLt2M;
    },
    handleHomeAvatarSuccess(fileItem) {
      const res = fileItem.response;
      if (typeof res === 'string' && (res.trim().startsWith('<!DOCTYPE') || res.trim().startsWith('<html'))) {
        Message.error('图片上传失败，服务器返回了错误的格式。');
        return;
      }
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('图片上传失败，服务器返回了错误的格式。');
        return;
      }
      this.homeConfigForm.avatarUrl = url;
      Message.success('头像上传成功');
    },
    handleWechatQrSuccess(fileItem) {
      const res = fileItem.response;
      if (typeof res === 'string' && (res.trim().startsWith('<!DOCTYPE') || res.trim().startsWith('<html'))) {
        Message.error('二维码上传失败，服务器返回了错误的格式。');
        return;
      }
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('二维码上传失败，服务器返回了错误的格式。');
        return;
      }
      this.homeConfigForm.wechatQrUrl = url;
      Message.success('二维码上传成功');
    },
    async saveHomeConfig() {
      this.savingConfig = true;
      try {
        const payload = {
          ...this.homeConfigForm,
          tags: this.homeConfigForm.tagsString.split(',').map(s => s.trim()).filter(Boolean)
        };
        delete payload.tagsString;
        
        await updateHomeConfig(payload);
        Message.success('系统配置保存成功！');
      } catch (error) {
        Message.error(error.response?.data?.message || '保存失败');
      } finally {
        this.savingConfig = false;
      }
    }
  }
}
</script>

<style scoped>
.product-image-uploader {
  border: 1px dashed #E5E6EB;
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
  border-color: #165DFF;
}
.product-upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
}
.product-upload-placeholder svg {
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
