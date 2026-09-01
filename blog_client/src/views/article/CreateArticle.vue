<template>
  <div class="creator-editor-root">
    <!-- 顶部沉浸式导航栏 -->
    <div class="editor-nav-bar">
      <div class="nav-back-circle" @click="$router.push('/')" title="返回首页">
        <icon-left class="back-icon" />
      </div>
      <div class="nav-center-title">书写灵感日记</div>
      <div class="nav-publish-slot">
        <button class="publish-capsule-btn" :disabled="submitting" @click="onSubmit">
          <icon-loading v-if="submitting" :spin="true" />
          <icon-send v-else />
          <span>{{ submitting ? '正在发布...' : '发布' }}</span>
        </button>
      </div>
    </div>

    <!-- 主体编辑卡片 (纯净轻奢) -->
    <div class="editor-main-card">
      <!-- 1. 现代化横向媒体胶卷上传区 (小红书极简媒体流) -->
      <div class="media-track-section">
        <div class="media-upload-wrapper">
          <a-upload
            :custom-request="customUploadRequest"
            list-type="picture-card"
            v-model:file-list="fileList"
            multiple
            image-preview
            accept="image/*,video/*"
            class="clean-media-uploader"
          >
            <template #upload-button>
              <div class="custom-uploader-btn">
                <icon-camera class="uploader-camera-icon" />
                <span class="uploader-text">添加图片/视频</span>
              </div>
            </template>
          </a-upload>
        </div>
        <div class="media-helper-text">
          <icon-info-circle /> 支持多张高清实拍与短视频，长按可调整封面首图
        </div>
      </div>

      <!-- 图片裁剪弹窗 -->
      <ImageCropperDialog 
        v-model:show="cropModalVisible" 
        :image-url="cropImageUrl" 
        :current-file="currentCropFile"
        @confirm="finishCurrentCrop" 
        @cancel="cancelCrop" 
      />

      <!-- 2. 标题与正文沉浸式书写区 -->
      <div class="text-writing-section">
        <input 
          class="editorial-title-input" 
          v-model="form.title" 
          placeholder="填写吸引人的标题会有更多赞哦~" 
          maxlength="40"
        />
        <div class="editorial-divider"></div>
        
        <!-- 富文本/Markdown/纯文本编辑器 -->
        <div class="editor-container">
          <RichTextEditor v-model="form.content" :custom-upload="customUploadImage" />
        </div>
      </div>

      <!-- 3. 小红书风高定互动卡片列表 (话题、地点、好物关联) -->
      <div class="meta-interactive-group">
        <!-- 参与话题 -->
        <div class="meta-cell" @click="topicVisible = true">
          <div class="cell-left-info">
            <div class="meta-icon-box bg-coral">
              <span class="hash-symbol">#</span>
            </div>
            <div class="meta-text-wrap">
              <span class="meta-title">参与话题</span>
              <span class="meta-value" v-if="form.tags && form.tags.length">
                {{ form.tags.map(t => '#' + t).join(' ') }}
              </span>
              <span class="meta-placeholder" v-else>添加标签提升日记曝光</span>
            </div>
          </div>
          <icon-right class="cell-arrow" />
        </div>

        <!-- 标记地点 -->
        <div class="meta-cell" @click="mapVisible = true">
          <div class="cell-left-info">
            <div class="meta-icon-box bg-green">
              <icon-location />
            </div>
            <div class="meta-text-wrap">
              <span class="meta-title">标记地点</span>
              <span class="meta-value" v-if="form.location">{{ form.location }}</span>
              <span class="meta-placeholder" v-else>分享生活美学打卡地</span>
            </div>
          </div>
          <icon-right class="cell-arrow" />
        </div>

        <!-- 关联推荐好物 (橱窗直购) -->
        <div class="meta-cell product-link-cell">
          <div class="cell-left-info" style="width: 100%;">
            <div class="meta-icon-box bg-gold">
              <icon-gift />
            </div>
            <div class="meta-text-wrap" style="flex: 1;">
              <span class="meta-title">关联橱窗好物</span>
              <div class="product-selector-box">
                <a-select 
                  v-model="form.productId" 
                  allow-clear 
                  placeholder="选择日记中提及的商品，读者可直接购买" 
                  class="luxury-select"
                >
                  <a-option
                    v-for="item in products"
                    :key="item.id"
                    :value="item.id"
                    :label="item.name + ' (¥' + item.price + ')'">
                  </a-option>
                </a-select>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 话题编辑弹窗 -->
    <a-modal v-model:visible="topicVisible" :title="null" :header="false" :footer="false" :width="isMobile ? '100%' : '480px'" unmount-on-close>
      <div class="topic-modal-box">
        <div class="sheet-handle-bar" v-if="isMobile"></div>
        <button class="sheet-circle-close" @click="topicVisible = false">
          <icon-close />
        </button>
        <h3 class="topic-modal-title">添加日记话题</h3>
        <p class="topic-modal-subtitle">输入你想参与的话题名称，按回车即可生成话题胶囊</p>
        <a-input-tag v-model="form.tags" placeholder="输入话题后按回车确认..." allow-clear :max-tag-count="5" size="large" class="topic-input-tag" />
        <button class="topic-confirm-btn" @click="topicVisible = false">完成添加</button>
      </div>
    </a-modal>

    <!-- 高德地图选点组件 -->
    <MapLocationDialog v-model:show="mapVisible" @select="handleMapSelect" />
  </div>
</template>

<script>
import { saveArticle } from '@/api/article'
import { getProducts } from '@/api/product'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import MapLocationDialog from '@/components/common/MapLocationDialog.vue'
import ImageCropperDialog from '@/components/common/ImageCropperDialog.vue'
import { Message } from '@arco-design/web-vue'
import axios from '@/utils/request'

export default {
  name: 'CreateArticle',
  components: {
    RichTextEditor,
    MapLocationDialog,
    ImageCropperDialog
  },
  data() {
    return {
      submitting: false,
      mapVisible: false,
      topicVisible: false,
      cropModalVisible: false,
      cropImageUrl: '',
      currentCropFile: null,
      fileList: [],
      products: [],
      isMobile: window.innerWidth <= 768,
      form: {
        title: '',
        content: '',
        mediaUrls: '[]',
        location: '',
        tags: [],
        productId: null
      }
    }
  },
  created() {
    this.fetchProducts();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    async fetchProducts() {
      try {
        const res = await getProducts();
        this.products = res.data.data || [];
      } catch (e) {
        // ignore
      }
    },
    handleMapSelect(loc) {
      this.form.location = loc.name || loc.address || '';
    },
    async customUploadRequest(options) {
      const { fileItem, onSuccess, onError } = options;
      const isImg = fileItem.file && fileItem.file.type.startsWith('image/');
      
      if (isImg && !fileItem.cropped) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.cropImageUrl = e.target.result;
          this.currentCropFile = fileItem.file;
          this.cropModalVisible = true;
          this.pendingUploadOptions = options;
        };
        reader.readAsDataURL(fileItem.file);
        return;
      }
      this.executeUpload(fileItem.file, onSuccess, onError);
    },
    async finishCurrentCrop(croppedBlob) {
      this.cropModalVisible = false;
      if (this.pendingUploadOptions) {
        const { onSuccess, onError } = this.pendingUploadOptions;
        this.executeUpload(croppedBlob, onSuccess, onError);
      }
    },
    cancelCrop() {
      this.cropModalVisible = false;
      this.fileList = this.fileList.filter(f => f.file !== this.currentCropFile);
    },
    async executeUpload(file, onSuccess, onError) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        const res = await axios.post('/api/common/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        const url = res.data.data;
        onSuccess({ url });
        Message.success('上传成功');
      } catch (e) {
        onError(e);
        Message.error('上传失败');
      }
    },
    async customUploadImage(file) {
      const formData = new FormData();
      formData.append('file', file);
      const res = await axios.post('/api/common/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      return res.data.data;
    },
    async onSubmit() {
      if (!this.form.title || !this.form.title.trim()) {
        return Message.warning('请填写日记标题');
      }
      if (!this.form.content || !this.form.content.trim()) {
        return Message.warning('请填写日记正文内容');
      }
      
      // 提取上传的媒体图片列表
      const urls = this.fileList
        .filter(f => f.response && f.response.url)
        .map(f => f.response.url);
      
      this.form.mediaUrls = JSON.stringify(urls);

      this.submitting = true;
      try {
        await saveArticle(this.form);
        Message.success('灵感日记发布成功！');
        this.$router.push('/');
      } catch (e) {
        Message.error(e.response?.data?.message || '发布失败');
      } finally {
        this.submitting = false;
      }
    }
  }
}
</script>

<style scoped>
.creator-editor-root {
  max-width: 800px;
  margin: 0 auto;
  padding: calc(12px + var(--safe-top, 0px)) 16px 100px;
  min-height: 100vh;
}

/* 沉浸式顶部导航栏 */
.editor-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0 16px;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(250, 250, 250, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.nav-back-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1D2129;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease;
}
.nav-back-circle:active {
  transform: scale(0.92);
}
.back-icon {
  font-size: 18px;
}

.nav-center-title {
  font-size: 16px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.publish-capsule-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(255, 42, 84, 0.3);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.publish-capsule-btn:active:not(:disabled) {
  transform: scale(0.94);
}
.publish-capsule-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 主体卡片 */
.editor-main-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

/* 媒体上传区 */
.media-track-section {
  padding-bottom: 16px;
  border-bottom: 1px solid #F7F8FA;
}

:deep(.arco-upload-list-picture-card) {
  gap: 10px;
}

:deep(.arco-upload-picture-card) {
  width: 100px !important;
  height: 100px !important;
  border-radius: 16px !important;
  background: #F7F8FA !important;
  border: 1px dashed #E5E6EB !important;
  transition: all 0.2s ease;
}
:deep(.arco-upload-picture-card:hover) {
  border-color: #FF5E3A !important;
  background: #FFF9F8 !important;
}

.custom-uploader-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #86909C;
}

.uploader-camera-icon {
  font-size: 24px;
  color: #FF5E3A;
}

.uploader-text {
  font-size: 11px;
  font-weight: 600;
}

.media-helper-text {
  font-size: 12px;
  color: #86909C;
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 标题与正文输入区 */
.text-writing-section {
  padding: 16px 0;
}

.editorial-title-input {
  width: 100%;
  border: none;
  font-size: 20px;
  font-weight: 800;
  color: #1A1D20;
  padding: 10px 0;
  outline: none;
  background: transparent;
  letter-spacing: -0.4px;
}
.editorial-title-input::placeholder {
  color: #C9CDD4;
  font-weight: 600;
}

.editorial-divider {
  height: 1px;
  background: #F7F8FA;
  margin: 8px 0 16px;
}

.editor-container {
  min-height: 220px;
}

/* 互动卡片列表 */
.meta-interactive-group {
  margin-top: 20px;
  border-top: 1px solid #F7F8FA;
  padding-top: 8px;
}

.meta-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 4px;
  border-bottom: 1px solid #F7F8FA;
  cursor: pointer;
  transition: all 0.2s ease;
}
.meta-cell:active {
  transform: scale(0.99);
}

.cell-left-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-icon-box {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.bg-coral { background: #FFF0F0; color: #FF4D4F; }
.bg-green { background: #E8FFEA; color: #00B42A; }
.bg-gold { background: #FFF7E8; color: #FF7D00; }

.hash-symbol {
  font-weight: 900;
  font-size: 18px;
}

.meta-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-title {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
}

.meta-value {
  font-size: 12px;
  color: #FF5E3A;
  font-weight: 600;
}

.meta-placeholder {
  font-size: 12px;
  color: #86909C;
}

.cell-arrow {
  color: #C9CDD4;
  font-size: 14px;
}

.product-selector-box {
  margin-top: 6px;
}

:deep(.luxury-select .arco-select-view-single) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
}

/* 话题弹窗 */
.topic-modal-box {
  padding: 16px 14px;
  position: relative;
  text-align: center;
}

.topic-modal-title {
  margin: 0 0 6px 0;
  font-size: 17px;
  font-weight: 800;
  color: #1A1D20;
}

.topic-modal-subtitle {
  margin: 0 0 16px 0;
  font-size: 12px;
  color: #86909C;
}

.topic-confirm-btn {
  margin-top: 20px;
  width: 100%;
  height: 44px;
  border-radius: 22px;
  background: #1A1D20;
  color: #FFFFFF;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(26, 29, 32, 0.2);
}

@media (max-width: 768px) {
  .creator-editor-root {
    padding: calc(6px + var(--safe-top, 0px)) 12px 100px;
  }
  .editor-main-card {
    border-radius: 18px;
    padding: 16px 14px;
  }
  .editorial-title-input {
    font-size: 18px;
  }
}
</style>
