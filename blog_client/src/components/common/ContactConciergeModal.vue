<template>
  <AppBottomSheet
    v-model:visible="visible"
    title="联系主理人 / 客服微信"
    subtitle="一对一鲜烤出炉咨询 · 校园自提 · 售后无忧"
    width="420px"
  >
    <div class="concierge-modal-body">
      <!-- 主理人专属名片 -->
      <div class="author-badge-card">
        <img :src="config.avatarUrl || '/img/avatar.png'" class="concierge-avatar" alt="Avatar" />
        <div class="concierge-meta">
          <h4 class="concierge-name">{{ config.authorName || '小柴包主理人' }}</h4>
          <span class="concierge-tag">官方认证 · 手作烘焙</span>
        </div>
      </div>

      <!-- 微信二维码展示区 -->
      <div class="qr-showcase-box">
        <div class="qr-img-wrapper">
          <img 
            v-if="config.wechatQrUrl" 
            :src="$formatImageUrl(config.wechatQrUrl)" 
            class="wechat-qr-code" 
            alt="微信二维码"
          />
          <div v-else class="empty-qr">
            <icon-wechat style="font-size: 48px; color: #07C160; opacity: 0.6;" />
            <p class="empty-tip">主理人微信在线</p>
          </div>
        </div>
        <p class="qr-scan-guide">手机截屏后打开微信扫一扫，或长按识别二维码</p>
      </div>

      <!-- 微信号一键复制 -->
      <div class="wechat-id-panel" v-if="wechatId">
        <div class="wechat-id-left">
          <span class="id-label">微信号</span>
          <strong class="id-val">{{ wechatId }}</strong>
        </div>
        <button class="copy-wx-btn" @click="handleCopyWechat">
          <icon-copy /> <span>复制微信号</span>
        </button>
      </div>

      <!-- 服务时间与说明 -->
      <div class="service-notice">
        <div class="notice-item">
          <icon-clock-circle class="notice-icon" />
          <span>服务时间: 09:00 - 22:30 (现烤出炉准时配送)</span>
        </div>
        <div class="notice-item">
          <icon-check-circle-fill class="notice-icon text-green" />
          <span>支持特殊口味定制、宿舍下午茶团餐与急单咨询</span>
        </div>
      </div>
    </div>

    <template #footer>
      <button class="sheet-main-btn btn-wechat-primary" @click="handleCopyWechat">
        <icon-wechat /> <span>一键复制微信号添加好友</span>
      </button>
    </template>
  </AppBottomSheet>
</template>

<script>
import { getHomeConfig } from '@/api/common';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'ContactConciergeModal',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:show'],
  data() {
    return {
      config: {
        avatarUrl: '',
        authorName: '',
        wechatQrUrl: ''
      },
      wechatId: 'caibread_helper' // 默认微信号，有真实配置时优先展示
    };
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  watch: {
    show(val) {
      if (val) {
        this.fetchConfig();
      }
    }
  },
  created() {
    this.fetchConfig();
  },
  methods: {
    async fetchConfig() {
      try {
        const res = await getHomeConfig();
        if (res.data && res.data.data) {
          this.config = res.data.data;
          // 若后端配置了微信号文本字段可同步解析
          if (this.config.wechatId) {
            this.wechatId = this.config.wechatId;
          }
        }
      } catch (e) {
        // ignore
      }
    },
    handleCopyWechat() {
      const textToCopy = this.wechatId || 'caibread_helper';
      if (navigator?.clipboard?.writeText) {
        navigator.clipboard.writeText(textToCopy).then(() => {
          Message.success('微信号已复制，快去微信添加主理人吧！');
        }).catch(() => {
          this.fallbackCopy(textToCopy);
        });
      } else {
        this.fallbackCopy(textToCopy);
      }
    },
    fallbackCopy(text) {
      const input = document.createElement('input');
      input.value = text;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      Message.success('微信号已复制');
    }
  }
};
</script>

<style scoped>
.concierge-modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 4px 0 10px;
}

.author-badge-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #F7F8FA;
  padding: 10px 18px;
  border-radius: 16px;
  width: 100%;
  box-sizing: border-box;
}

.concierge-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #FFFFFF;
}

.concierge-meta {
  display: flex;
  flex-direction: column;
}

.concierge-name {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 800;
  color: #1D2129;
}

.concierge-tag {
  font-size: 11px;
  color: #07C160;
  font-weight: 600;
}

.qr-showcase-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.qr-img-wrapper {
  width: 190px;
  height: 190px;
  border-radius: 18px;
  padding: 10px;
  background: #FFFFFF;
  box-shadow: 0 8px 24px rgba(7, 193, 96, 0.12);
  border: 1.5px solid rgba(7, 193, 96, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.wechat-qr-code {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.empty-qr {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-tip {
  font-size: 12px;
  color: #86909C;
  margin: 0;
}

.qr-scan-guide {
  font-size: 11px;
  color: #86909C;
  margin: 0;
  text-align: center;
}

.wechat-id-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  background: #F0FBF5;
  border: 1px solid rgba(7, 193, 96, 0.25);
  padding: 10px 14px;
  border-radius: 12px;
  box-sizing: border-box;
}

.wechat-id-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.id-label {
  font-size: 11px;
  color: #86909C;
}

.id-val {
  font-size: 14px;
  color: #07C160;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.copy-wx-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #07C160;
  color: #FFFFFF;
  border: none;
  border-radius: 8px;
  padding: 5px 10px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.copy-wx-btn:active {
  transform: scale(0.95);
}

.service-notice {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
  background: #FAFAFA;
  padding: 10px 14px;
  border-radius: 12px;
  box-sizing: border-box;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #4E5969;
}

.notice-icon {
  font-size: 13px;
  color: #86909C;
  flex-shrink: 0;
}

.text-green {
  color: #07C160 !important;
}

.btn-wechat-primary {
  background: linear-gradient(135deg, #07C160 0%, #05A350 100%) !important;
  color: #FFFFFF !important;
  box-shadow: 0 4px 14px rgba(7, 193, 96, 0.3) !important;
}
</style>
