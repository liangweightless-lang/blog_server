<template>
  <a-modal 
    v-model:visible="visible" 
    :title="null"
    :header="false"
    :footer="false"
    :closable="false"
    :mask-closable="!submitting"
    :modal-class="isMobile ? 'creator-modal-mobile' : 'creator-modal-pc'"
    @close="handleClose"
  >
    <div class="apply-container">
      <div class="sheet-handle-bar" v-if="isMobile"></div>
      <button class="sheet-circle-close" @click="handleClose" aria-label="关闭">
        <icon-close />
      </button>

      <div class="apply-header">
        <div class="badge-icon">🌟</div>
        <h3 class="apply-title">入驻成为小柴包主理人</h3>
        <p class="apply-subtitle">开启独立创作主理空间，自主发布商品、策划活动与管理订单</p>
      </div>

      <a-form :model="form" layout="vertical" class="apply-form" @submit.prevent="handleSubmit">
        <a-form-item label="主理人 / 品牌名称" required>
          <a-input 
            v-model="form.brandName" 
            placeholder="例如: 柴柴手作工作室 / 小柴咖啡" 
            size="large"
            class="custom-input"
          />
        </a-form-item>

        <a-form-item label="联系手机号" required>
          <a-input 
            v-model="form.contactPhone" 
            placeholder="请输入管理员联络手机" 
            size="large"
            class="custom-input"
          />
        </a-form-item>

        <a-form-item label="联系微信号 (选填)">
          <a-input 
            v-model="form.wechatId" 
            placeholder="便于平台进行一对一入驻对接" 
            size="large"
            class="custom-input"
          />
        </a-form-item>

        <a-form-item label="主理人简介 / 主营品类" required>
          <a-textarea 
            v-model="form.intro" 
            :auto-size="{ minRows: 3, maxRows: 5 }" 
            placeholder="请简要介绍您的创作风格、手作特色或主营商品品类..."
            class="custom-textarea"
          />
        </a-form-item>

        <a-form-item label="作品或资质图片 (选填)">
          <a-upload
            :action="uploadAction"
            :show-file-list="false"
            @success="handleUploadSuccess"
            @before-upload="beforeUpload"
          >
            <template #upload-button>
              <div class="credentials-uploader">
                <img v-if="form.credentialsUrl" :src="form.credentialsUrl" class="credentials-preview" />
                <div v-else class="credentials-placeholder">
                  <icon-plus />
                  <span>上传作品或资质图</span>
                </div>
              </div>
            </template>
          </a-upload>
        </a-form-item>

        <div class="form-actions">
          <button 
            type="submit" 
            class="submit-apply-btn" 
            :disabled="submitting"
          >
            <icon-loading v-if="submitting" :spin="true" />
            <span>{{ submitting ? '提交中...' : '立即提交入驻申请' }}</span>
          </button>
        </div>
      </a-form>
    </div>
  </a-modal>
</template>

<script>
import { applyCreator } from '@/api/creator';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'CreatorApplyDialog',
  props: {
    show: Boolean
  },
  data() {
    return {
      submitting: false,
      form: {
        brandName: '',
        contactPhone: '',
        wechatId: '',
        intro: '',
        credentialsUrl: ''
      },
      isMobile: window.innerWidth <= 768
    };
  },
  computed: {
    visible: {
      get() { return this.show; },
      set(val) { this.$emit('update:show', val); }
    },
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  methods: {
    beforeUpload(file) {
      const isImg = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isLt2M = file.size / 1024 / 1024 < 3;
      if (!isImg) Message.error('只能上传 JPG/PNG/WebP 格式图片!');
      if (!isLt2M) Message.error('图片大小不能超过 3MB!');
      return isImg && isLt2M;
    },
    handleUploadSuccess(fileItem) {
      const res = fileItem.response;
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url) {
        this.form.credentialsUrl = url;
        Message.success('资质图片上传成功！');
      }
    },
    async handleSubmit() {
      if (!this.form.brandName || !this.form.brandName.trim()) {
        return Message.warning('请输入主理人或品牌名称');
      }
      if (!this.form.contactPhone || !this.form.contactPhone.trim()) {
        return Message.warning('请输入联系电话');
      }
      if (!this.form.intro || !this.form.intro.trim()) {
        return Message.warning('请输入主理人简介与主营说明');
      }

      this.submitting = true;
      try {
        await applyCreator(this.form);
        Message.success('主理人申请已提交，管理员将尽快审核！');
        this.visible = false;
        this.$emit('success');
      } catch (error) {
        Message.error(error.response?.data?.message || '提交申请失败');
      } finally {
        this.submitting = false;
      }
    },
    handleClose() {
      this.visible = false;
    }
  }
};
</script>

<style scoped>
.apply-container {
  padding: 10px 14px;
  position: relative;
}
.sheet-handle-bar {
  width: 36px;
  height: 4px;
  background: #E5E6EB;
  border-radius: 2px;
  margin: 0 auto 14px auto;
}
.sheet-circle-close {
  position: absolute;
  right: 14px;
  top: 14px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}
.sheet-circle-close:active {
  background: #E5E6EB;
  transform: scale(0.92);
}
.apply-header {
  text-align: center;
  margin-bottom: 20px;
}
.badge-icon {
  font-size: 32px;
  margin-bottom: 8px;
  display: inline-block;
  animation: bounce 2s infinite ease-in-out;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
.apply-title {
  font-size: 20px;
  font-weight: 800;
  color: #1D2129;
  margin: 0 0 6px 0;
}
.apply-subtitle {
  font-size: 13px;
  color: #86909C;
  margin: 0;
}
.custom-input,
.custom-textarea {
  border-radius: 12px;
  background: #F7F8FA;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}
.custom-input:focus,
.custom-textarea:focus {
  background: #FFFFFF;
  border-color: #FF7E67;
  box-shadow: 0 0 0 3px rgba(255, 126, 103, 0.15);
}
.credentials-uploader {
  width: 100%;
  height: 110px;
  border: 1.5px dashed #E5E6EB;
  border-radius: 12px;
  background: #F7F8FA;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s ease;
}
.credentials-uploader:hover {
  border-color: #FF7E67;
  background: #FFF9F6;
}
.credentials-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #86909C;
  font-size: 13px;
  gap: 6px;
}
.credentials-placeholder svg {
  font-size: 24px;
  color: #FF7E67;
}
.credentials-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.form-actions {
  margin-top: 24px;
}
.submit-apply-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FF9A8B 0%, #FF6A88 55%, #FF99AC 100%);
  color: #FFFFFF;
  border: none;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 106, 136, 0.35);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.submit-apply-btn:active {
  transform: scale(0.96);
  box-shadow: 0 2px 8px rgba(255, 106, 136, 0.25);
}

@media (max-width: 768px) {
  :deep(.creator-modal-mobile) {
    position: fixed !important;
    bottom: 0 !important;
    left: 0 !important;
    right: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    border-radius: 24px 24px 0 0 !important;
    padding-bottom: max(16px, env(safe-area-inset-bottom));
    animation: sheetSlideUp 0.35s cubic-bezier(0.25, 1, 0.5, 1);
    max-height: 88vh;
    overflow-y: auto;
  }
  .apply-container {
    padding-bottom: 20px;
  }
}

@keyframes sheetSlideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
</style>
