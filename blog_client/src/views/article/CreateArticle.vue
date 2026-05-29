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
        :custom-request="customUploadRequest"
        list-type="picture-card"
        v-model:file-list="fileList"
        multiple
        image-preview
        accept="image/*,video/*"
      >
      </a-upload>
      <div class="upload-tip" style="font-size: 12px; color: #D3C1BA; margin-top: 8px;">支持多张图片与短视频上传</div>
    </div>

    <!-- 图片裁剪弹窗 -->
    <ImageCropperDialog 
      v-model:show="cropModalVisible" 
      :image-url="cropImageUrl" 
      :current-file="currentCropFile"
      @confirm="finishCurrentCrop" 
      @cancel="cancelCrop" 
    />

    <!-- 标题与内容输入区 -->
    <div class="xhs-editor-area">
      <input class="xhs-title-input" v-model="form.title" placeholder="填写标题会有更多赞哦~" />
      <div class="xhs-divider"></div>
      
      <!-- 富文本编辑器区 -->
      <RichTextEditor v-model="form.content" :custom-upload="customUploadImage" />
    </div>

    <!-- 小红书特色底部操作栏 -->
    <div class="xhs-bottom-actions">
      <!-- 已添加的话题 -->
      <div 
        class="action-item active-pill" 
        v-for="(tag, index) in form.tags" 
        :key="index"
        @click="topicVisible = true"
      >
        <span class="icon">#</span> {{ tag }}
      </div>
      
      <!-- 话题入口 -->
      <div class="action-item" @click="topicVisible = true" v-if="!form.tags || form.tags.length === 0">
        <span class="icon">#</span> 参与话题
      </div>

      <!-- 暂未开放的 @ 功能 -->
      <div class="action-item" style="opacity: 0.5; cursor: not-allowed;">
        <span class="icon">@</span> 提醒谁看
      </div>

      <!-- 已添加的地点 -->
      <div 
        class="action-item active-pill" 
        v-if="form.location"
        @click="mapVisible = true"
      >
        <icon-location /> <span style="max-width: 150px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">{{ form.location }}</span>
      </div>

      <!-- 地点入口 -->
      <div class="action-item" @click="mapVisible = true" v-else>
        <icon-location /> 添加地点
      </div>
    </div>
    
    <!-- 话题编辑弹窗 -->
    <a-modal v-model:visible="topicVisible" title="添加话题" :footer="false" :width="isMobile ? '90%' : '500px'" unmount-on-close>
      <div style="padding: 10px 0;">
        <div style="margin-bottom: 15px; color: #86909C; font-size: 13px;">输入你想参与的话题，按回车键确认。</div>
        <a-input-tag v-model="form.tags" placeholder="输入话题后按回车..." allow-clear :max-tag-count="5" size="large" />
      </div>
      <div style="margin-top: 30px; display: flex; justify-content: flex-end;">
        <a-button type="primary" shape="round" @click="topicVisible = false">确定</a-button>
      </div>
    </a-modal>

    <!-- 高德地图选点组件 -->
    <MapLocationDialog v-model:show="mapVisible" @select="handleMapSelect" />

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
import { saveArticle } from '@/api/article'
import { getProducts } from '@/api/product'
import { Message } from '@arco-design/web-vue'
import request from '@/utils/request'
import MapLocationDialog from '@/components/common/MapLocationDialog.vue'
import ImageCropperDialog from '@/components/common/ImageCropperDialog.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

export default {
  name: 'CreateArticle',
  components: { MapLocationDialog, ImageCropperDialog, RichTextEditor },
  data() {
    return {
      fileList: [],
      form: {
        title: '',
        content: '',
        productId: null,
        tags: [],
        location: '',
        coverUrl: '',
        mediaUrls: '[]'
      },
      products: [],
      submitting: false,
      topicVisible: false,
      mapVisible: false,
      isMobile: window.innerWidth <= 768,
      cropQueue: [],
      isCropping: false,
      cropModalVisible: false,
      cropImageUrl: '',
      cropResolve: null,
      cropReject: null,
      currentCropFile: null
    }
  },
  created() {
    this.fetchProducts();
    window.addEventListener('resize', this.handleResize);
  },
  computed: {
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleMapSelect(address) {
      this.form.location = address;
    },
    // ---- 图片裁剪队列逻辑 ----
    startCrop(file) {
      return new Promise((resolve, reject) => {
        this.cropQueue.push({ file, resolve, reject });
        this.processCropQueue();
      });
    },
    processCropQueue() {
      if (this.isCropping || this.cropQueue.length === 0) return;
      this.isCropping = true;
      const { file, resolve, reject } = this.cropQueue[0];
      
      this.currentCropFile = file;
      this.cropResolve = resolve;
      this.cropReject = reject;
      
      // 非图片（比如视频）直接跳过裁剪
      if (file.type && !file.type.startsWith('image/')) {
        this.finishCurrentCrop(file);
        return;
      }
      
      this.cropImageUrl = URL.createObjectURL(file);
      this.cropModalVisible = true;
    },
    finishCurrentCrop(resultFile) {
      this.cropModalVisible = false;
      if (this.cropImageUrl) {
        URL.revokeObjectURL(this.cropImageUrl);
      }
      const item = this.cropQueue.shift();
      if (item && item.resolve && resultFile) item.resolve(resultFile);
      this.isCropping = false;
      this.processCropQueue();
    },
    cancelCrop() {
      const item = this.cropQueue.shift();
      if (item && item.reject) item.reject(new Error('用户取消裁剪'));
      this.cropModalVisible = false;
      if (this.cropImageUrl) {
        URL.revokeObjectURL(this.cropImageUrl);
      }
      this.isCropping = false;
      this.processCropQueue();
    },

    // ---- Arco Upload 自定义上传拦截 ----
    async customUploadRequest(option) {
      const { onProgress, onError, onSuccess, fileItem, name } = option;
      try {
        const file = fileItem.file; // original JS File object
        const croppedFile = await this.startCrop(file);
        
        const formData = new FormData();
        formData.append(name || 'file', croppedFile);
        
        const token = localStorage.getItem('token');
        const res = await request.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': token ? `Bearer ${token}` : ''
          },
          onUploadProgress: (progressEvent) => {
            let percent = 0;
            if (progressEvent.total) {
              percent = progressEvent.loaded / progressEvent.total;
            }
            onProgress(percent);
          }
        });
        if (res.data && res.data.url) {
          onSuccess(res.data);
        } else {
          onError(new Error('服务器未返回URL'));
        }
      } catch (e) {
        if (e.message !== '用户取消裁剪') Message.error('相册上传失败');
        onError(e);
      }
    },

    // ---- WangEditor 富文本图片上传拦截 ----
    async customUploadImage(file, insertFn) {
      try {
        const croppedFile = await this.startCrop(file);
        
        const formData = new FormData();
        formData.append('file', croppedFile);
        const token = localStorage.getItem('token');
        const res = await request.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        if (res.data && res.data.url) {
          let url = res.data.url;
          const base = (request.defaults.baseURL || '').replace(/\/$/, '');
          if (url.startsWith('/uploads/') && base) url = base + url;
          insertFn(url, file.name, url);
        } else {
          Message.error('正文图片上传失败');
        }
      } catch (error) {
        if (error.message !== '用户取消裁剪') Message.error('图片上传异常');
      }
    },
    async fetchProducts() {
      try {
        const res = await getProducts();
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
      
      const payload = {
        ...this.form,
        tags: this.form.tags && this.form.tags.length > 0 ? JSON.stringify(this.form.tags) : null,
        mediaUrls: JSON.stringify(urls)
      };
      payload.coverUrl = urls.length > 0 ? urls[0] : '';

      this.submitting = true
      try {
        const res = await saveArticle(payload)
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
.action-item.active-pill {
  background: rgba(255, 126, 103, 0.1);
  border-color: rgba(255, 126, 103, 0.3);
  color: #FF7E67;
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
