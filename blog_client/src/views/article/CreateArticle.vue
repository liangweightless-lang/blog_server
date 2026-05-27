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
        action="/api/files/upload"
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
  methods: {
    handleUploadSuccess(fileItem) {
      const res = fileItem.response;
      fileItem.uploadedUrl = (res && res.url) ? res.url : res;
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
        return r ? (typeof r === 'string' ? r : r.url) : f.url;
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
  background: #FFFFFF;
  border-radius: 24px;
  min-height: 80vh;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.08);
}

.xhs-create-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left {
  cursor: pointer;
  color: #5C433B;
  width: 60px;
}
.header-title {
  font-size: 18px;
  font-weight: 800;
  color: #5C433B;
}
.header-right {
  width: 60px;
  text-align: right;
}
.publish-btn {
  background: #FF7E67;
  border: none;
  font-weight: bold;
}
.publish-btn:hover {
  background: #E56A54;
}

.xhs-upload-area {
  margin-bottom: 24px;
}

:deep(.arco-upload-picture-card) {
  width: 100%;
  height: 120px;
  background-color: #F8F9FA;
  border: 1px dashed #E5E6EB;
}
:deep(.arco-upload-picture-card:hover) {
  background: #FFF0ED;
}

.xhs-editor-area {
  display: flex;
  flex-direction: column;
}

.xhs-title-input {
  border: none;
  font-size: 20px;
  font-weight: 700;
  color: #5C433B;
  padding: 10px 0;
  outline: none;
  background: transparent;
}
.xhs-title-input::placeholder {
  color: #D3C1BA;
  font-weight: 600;
}

.xhs-divider {
  height: 1px;
  background: #FDF0E6;
  margin: 10px 0;
}

.xhs-content-input {
  border: none;
  font-size: 16px;
  color: #8C6A5D;
  line-height: 1.6;
  padding: 10px 0;
  outline: none;
  resize: none;
  font-family: inherit;
  background: transparent;
}
.xhs-content-input::placeholder {
  color: #D3C1BA;
}

.xhs-bottom-actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;
  border-top: 1px solid #FDF0E6;
  padding-top: 20px;
  flex-wrap: wrap;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #FFFDF8;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #8C6A5D;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid #FDF0E6;
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
  padding-top: 20px;
  border-top: 1px dashed #FDF0E6;
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
  color: #D3C1BA;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .xhs-create-container {
    padding: 20px 15px;
    border-radius: 16px;
  }
}
</style>
