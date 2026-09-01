<template>
  <a-modal 
    :visible="visible" 
    :width="isMobile ? '100%' : '520px'" 
    :footer="false"
    :header="false"
    :mask-closable="true"
    @cancel="handleCancel"
    unmount-on-close
  >
    <div class="sheet-modern-container">
      <!-- 移动端顶部拉手横杠 -->
      <div class="sheet-handle-bar" v-if="isMobile"></div>
      
      <!-- 右上角磨砂圆圈关闭按钮 -->
      <button class="sheet-circle-close" @click="handleCancel" aria-label="关闭">
        <icon-close />
      </button>

      <div class="sheet-header">
        <h3 class="sheet-title">编辑个人资料</h3>
        <p class="sheet-subtitle">完善个人信息，方便主理人沟通与订单配送</p>
      </div>

      <div class="sheet-body">
        <!-- 头像上传区域 -->
        <div class="avatar-edit-section">
          <a-upload
            :action="uploadAction"
            :show-file-list="false"
            @success="handleAvatarSuccess"
            @before-upload="beforeAvatarUpload"
          >
            <template #upload-button>
              <div class="avatar-ring-trigger">
                <img :src="profileForm.avatarUrl || '/img/avatar.png'" class="avatar-preview-img" />
                <div class="avatar-camera-badge">
                  <icon-camera />
                </div>
              </div>
            </template>
          </a-upload>
          <span class="avatar-hint">点击更换专属头像</span>
        </div>

        <div class="custom-form-group">
          <!-- 昵称 -->
          <div class="custom-form-item">
            <label class="form-label">用户昵称</label>
            <a-input 
              v-model="profileForm.nickname" 
              placeholder="请输入您的昵称" 
              size="large" 
              class="luxury-form-input" 
            />
          </div>

          <!-- 微信号 -->
          <div class="custom-form-item">
            <label class="form-label">微信号 <span class="label-tag">选填</span></label>
            <a-input 
              v-model="profileForm.wechatId" 
              placeholder="方便跟团与客服售后沟通" 
              size="large" 
              class="luxury-form-input" 
            />
          </div>

          <!-- 收货地址 -->
          <div class="custom-form-item">
            <div class="label-with-action">
              <label class="form-label">默认收货地址</label>
              <span class="map-picker-link" @click="openMapDialog">
                <icon-location /> 地图快速定位
              </span>
            </div>
            <a-textarea 
              v-model="profileForm.address" 
              placeholder="填写详细收货地址（省/市/区/街道/门牌号）" 
              :auto-size="{ minRows: 2, maxRows: 4 }" 
              class="luxury-form-textarea" 
            />
          </div>
        </div>
      </div>

      <!-- 底部吸底保存大胶囊按钮 -->
      <div class="sheet-footer-action">
        <button class="sheet-main-btn" :disabled="updating" @click="handleUpdate">
          <icon-loading v-if="updating" :spin="true" />
          <span>{{ updating ? '正在保存...' : '保存个人资料' }}</span>
        </button>
      </div>
    </div>

    <!-- 高德地图选点弹窗 -->
    <MapLocationDialog v-model:show="mapDialogVisible" @select="confirmMapLocation" />
  </a-modal>
</template>

<script>
import { updateUserProfile } from '@/api/user';
import { Message } from '@arco-design/web-vue';
import MapLocationDialog from '@/components/common/MapLocationDialog.vue';
import { mapState } from 'pinia';
import { useUserStore } from '@/stores/user';

export default {
  name: 'ProfileEditDialog',
  components: {
    MapLocationDialog
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      updating: false,
      profileForm: {
        nickname: '',
        avatarUrl: '',
        wechatId: '',
        age: 18,
        gender: 'OTHER',
        address: ''
      },
      mapDialogVisible: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    },
    uploadAction() {
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
      return base + '/api/common/upload';
    }
  },
  watch: {
    show(newVal) {
      if (newVal && this.userInfo) {
        this.profileForm = {
          nickname: this.userInfo.nickname || '',
          avatarUrl: this.userInfo.avatarUrl || '',
          wechatId: this.userInfo.wechatId || '',
          age: this.userInfo.age || 18,
          gender: this.userInfo.gender || 'OTHER',
          address: this.userInfo.address || ''
        };
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    beforeAvatarUpload(file) {
      const isImg = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 5;
      if (!isImg) {
        Message.error('只能上传图片文件');
        return false;
      }
      if (!isLt2M) {
        Message.error('头像大小不能超过 5MB');
        return false;
      }
      return true;
    },
    handleAvatarSuccess(fileItem) {
      if (fileItem && fileItem.response && fileItem.response.data) {
        this.profileForm.avatarUrl = fileItem.response.data;
        Message.success('头像已更新');
      }
    },
    openMapDialog() {
      this.mapDialogVisible = true;
    },
    confirmMapLocation(loc) {
      if (loc && (loc.address || loc.name)) {
        this.profileForm.address = (loc.address || '') + ' ' + (loc.name || '');
      }
    },
    handleCancel() {
      this.visible = false;
    },
    async handleUpdate() {
      if (!this.profileForm.nickname || !this.profileForm.nickname.trim()) {
        return Message.warning('用户昵称不能为空');
      }
      this.updating = true;
      try {
        await updateUserProfile(this.profileForm);
        Message.success('个人资料已成功保存！');
        this.$emit('updated');
        this.visible = false;
      } catch (error) {
        Message.error(error.response?.data?.message || '保存失败');
      } finally {
        this.updating = false;
      }
    }
  }
}
</script>

<style scoped>
.sheet-modern-container {
  padding: 16px 18px 24px;
  position: relative;
}

.sheet-header {
  text-align: center;
  margin-bottom: 20px;
}

.sheet-title {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.sheet-subtitle {
  margin: 0;
  font-size: 12px;
  color: #86909C;
}

.avatar-edit-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-ring-trigger {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  padding: 2.5px;
  background: linear-gradient(135deg, #FF9A8B 0%, #FF6A88 100%);
  box-shadow: 0 4px 16px rgba(255, 106, 136, 0.25);
  cursor: pointer;
  transition: transform 0.2s ease;
}
.avatar-ring-trigger:active {
  transform: scale(0.95);
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #FFFFFF;
  display: block;
}

.avatar-camera-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #1A1D20;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border: 2px solid #FFFFFF;
}

.avatar-hint {
  font-size: 11px;
  color: #86909C;
  margin-top: 8px;
}

.custom-form-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.custom-form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
}

.label-tag {
  font-size: 10px;
  font-weight: 500;
  color: #86909C;
  margin-left: 4px;
}

.label-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-picker-link {
  font-size: 12px;
  font-weight: 600;
  color: #FF5E3A;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
}

:deep(.luxury-form-input .arco-input-wrapper) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
  padding: 8px 14px !important;
  transition: all 0.2s ease;
}
:deep(.luxury-form-input .arco-input-wrapper:focus-within) {
  border-color: rgba(255, 94, 58, 0.4) !important;
  background: #FFFFFF !important;
  box-shadow: 0 0 0 3px rgba(255, 94, 58, 0.08) !important;
}

:deep(.luxury-form-textarea) {
  border-radius: 12px !important;
  background: #F7F8FA !important;
  border: 1px solid transparent !important;
  padding: 10px 14px !important;
  transition: all 0.2s ease;
}
:deep(.luxury-form-textarea:focus-within) {
  border-color: rgba(255, 94, 58, 0.4) !important;
  background: #FFFFFF !important;
  box-shadow: 0 0 0 3px rgba(255, 94, 58, 0.08) !important;
}

.sheet-footer-action {
  margin-top: 24px;
}

.sheet-main-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(255, 42, 84, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.sheet-main-btn:active:not(:disabled) {
  transform: scale(0.96);
}
.sheet-main-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
