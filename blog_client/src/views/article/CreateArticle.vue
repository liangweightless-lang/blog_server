<template>
  <div class="xhs-create-container">
    <!-- 顶部导航条 -->
    <div class="xhs-create-header">
      <div class="header-left" @click="$router.push('/')">
        <icon-left style="font-size: 20px" />
      </div>
      <div class="header-title">发布日记</div>
      <div class="header-right">
        <a-button class="publish-btn" type="primary" shape="round" size="small" :loading="submitting" @click="onSubmit">
          发布
        </a-button>
      </div>
    </div>

    <!-- 真实的上传照片区 -->
    <div class="xhs-upload-area">
      <a-upload
        :action="uploadAction"
        list-type="picture-card"
        v-model:file-list="fileList"
        @success="handleUploadSuccess"
        @error="handleUploadError"
        multiple
        image-preview
        accept="image/*,video/*"
      >
      </a-upload>
      <div class="upload-tip" style="font-size: 12px; color: #D3C1BA; margin-top: 8px;">支持多张图片与短视频上传</div>
    </div>

    <!-- 标题与内容输入区 -->
    <div class="xhs-editor-area">
      <input class="xhs-title-input" v-model="form.title" placeholder="填写标题会有更多赞哦~" />
      <div class="xhs-divider"></div>
      <textarea class="xhs-content-input" v-model="form.content" placeholder="添加正文" rows="12"></textarea>
    </div>

    <!-- 小红书特色底部操作栏 -->
    <div class="xhs-bottom-actions">
      <div class="action-item"><span class="icon">#</span> 参与话题</div>
      <div class="action-item"><span class="icon">@</span> 提醒谁看</div>
      <div class="action-item"><icon-location /> 添加地点</div>
    </div>

    <!-- 关联商品选择 -->
    <div class="xhs-product-link">
      <div class="link-header">
        <icon-gift />
        <span>好物推荐</span>
      </div>
      <a-select v-model="form.productId" allow-clear placeholder="选择日记中提到的商品" style="width: 100%">
        <a-option
          v-for="item in products"
          :key="item.id"
          :value="item.id"
          :label="item.name + ' (¥' + item.price + ')'">
        </a-option>
      </a-select>
      <p class="link-tip">关联后，读者将在文末直接看到购买入口</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { Message } from '@arco-design/web-vue'

export default {
  name: 'CreateArticle',
  data() {
    return {
      fileList: [],
      form: {
        title: '',
        content: '',
        coverUrl: '',
        mediaUrls: '[]',
        productId: null
      },
      products: [],
      submitting: false
    }
  },
  created() {
    this.fetchProducts();
  },
  computed: {
    uploadAction() {
      const base = (axios.defaults.baseURL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  methods: {
    handleUploadSuccess(fileItem) {
      const res = fileItem.response;
      if (typeof res === 'string' && (res.trim().startsWith('<!DOCTYPE') || res.trim().startsWith('<html'))) {
        Message.error('文件上传失败，服务器返回了错误的格式。');
        fileItem.status = 'error';
        fileItem.uploadedUrl = '';
        return;
      }
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('文件上传失败，服务器返回了错误的格式。');
        fileItem.status = 'error';
        fileItem.uploadedUrl = '';
        return;
      }
      fileItem.uploadedUrl = url;
    },
    handleUploadError(fileItem) {
      Message.error('文件上传失败，请重试');
    },
    async fetchProducts() {
      try {
        const res = await axios.get('/api/products');
        this.products = (res.data && res.data.data) ? res.data.data : [];
      } catch (error) {
        console.error('获取商品列表失败');
      }
    },
    async onSubmit() {
      if (!this.form.title.trim()) {
        Message.warning('标题不能为空哦~')
        return
      }
      if (!this.form.content.trim()) {
        Message.warning('多写点正文吧~')
        return
      }
      
      const urls = this.fileList.map(f => {
        if (f.uploadedUrl) return f.uploadedUrl;
        const r = f.response;
        let url = r ? (typeof r === 'string' ? r : r.url) : f.url;
        if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
          return null;
        }
        return url;
      }).filter(Boolean);
      this.form.mediaUrls = JSON.stringify(urls);
      this.form.coverUrl = urls.length > 0 ? urls[0] : '';

      this.submitting = true
      try {
        const token = localStorage.getItem('token');
        const res = await axios.post('/api/articles', this.form, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (res.data && res.data.data) {
          Message.success('日记发布成功！')
          this.$router.push('/admin')
        } else {
          Message.error('日记发布失败，请稍后重试')
        }
      } catch (error) {
        Message.error(error.response?.data?.message || '日记发布失败，无管理员权限')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.xhs-create-container {
  background: transparent;
  border-radius: 24px;
  min-height: 80vh;
  position: relative;
  /* removing padding from container, applying it to sections so sticky header works nicely */
}

.xhs-create-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-radius: 24px 24px 0 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}
.header-left {
  cursor: pointer;
  color: #1D2129;
  width: 60px;
  transition: transform 0.2s;
}
.header-left:active {
  transform: scale(0.9);
}
.header-title {
  font-size: 18px;
  font-weight: 800;
  color: #1D2129;
}
.header-right {
  width: 60px;
  text-align: right;
}
.publish-btn {
  background: linear-gradient(135deg, rgba(255, 126, 103, 0.95) 0%, rgba(255, 90, 68, 0.95) 100%);
  border: none;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 126, 103, 0.4);
}
.publish-btn:active {
  transform: scale(0.95);
}

.xhs-upload-area {
  margin-bottom: 24px;
  padding: 0 20px;
}

:deep(.arco-upload-picture-card) {
  width: 100%;
  height: 120px;
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border: 1px dashed rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  transition: all 0.3s ease;
}
:deep(.arco-upload-picture-card:hover) {
  background: rgba(255, 255, 255, 0.9);
  border-color: #FF7E67;
}

.xhs-editor-area {
  display: flex;
  flex-direction: column;
  padding: 0 20px;
}

.xhs-title-input {
  border: none;
  font-size: 20px;
  font-weight: 700;
  color: #1D2129;
  padding: 10px 0;
  outline: none;
  background: transparent;
}
.xhs-title-input::placeholder {
  color: #C9CDD4;
  font-weight: 600;
}

.xhs-divider {
  height: 1px;
  background: rgba(0,0,0,0.05);
  margin: 10px 0;
}

.xhs-content-input {
  border: none;
  font-size: 16px;
  color: #4E5969;
  line-height: 1.8;
  padding: 10px 0;
  outline: none;
  resize: none;
  font-family: inherit;
  background: transparent;
}
.xhs-content-input::placeholder {
  color: #C9CDD4;
}

.xhs-bottom-actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;
  border-top: 1px solid rgba(0,0,0,0.05);
  padding: 20px 20px 0 20px;
  flex-wrap: wrap;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #4E5969;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.action-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.action-item:active {
  transform: scale(0.95);
}
.action-item .icon {
  font-weight: 800;
  color: #FF7E67;
}
.action-item svg {
  color: #FF7E67;
  font-size: 15px;
}

.xhs-product-link {
  margin-top: 30px;
  padding: 20px 20px 0 20px;
  border-top: 1px dashed rgba(0,0,0,0.08);
}
.link-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-weight: bold;
  color: #FF7E67;
}
.link-tip {
  font-size: 12px;
  color: #86909C;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .xhs-create-container {
    border-radius: 0; /* Let it stretch to screen edges on mobile */
  }
  .xhs-create-header {
    margin: 0;
    border-radius: 0;
  }
}
</style>
