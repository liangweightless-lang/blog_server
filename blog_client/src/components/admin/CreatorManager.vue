<template>
  <div class="creator-manager">
    <div class="header-actions">
      <div class="title-wrap">
        <h3>主理人入驻审批</h3>
        <span class="count-tip">共收到 {{ applications.length }} 条主理人申请</span>
      </div>
      <a-button @click="fetchApplications" :loading="loading">
        <template #icon><icon-refresh /></template>刷新
      </a-button>
    </div>

    <!-- 筛选工具条 -->
    <div class="filter-bar">
      <a-radio-group v-model="statusFilter" type="button">
        <a-radio value="ALL">全部申请</a-radio>
        <a-radio value="0">待审核 ({{ pendingCount }})</a-radio>
        <a-radio value="1">已通过</a-radio>
        <a-radio value="2">已驳回</a-radio>
      </a-radio-group>
    </div>

    <a-table 
      :data="filteredApplications" 
      :loading="loading" 
      :pagination="{ pageSize: 10 }"
      row-key="id"
      class="custom-table"
    >
      <template #columns>
        <a-table-column title="申请用户" :width="160">
          <template #cell="{ record }">
            <div class="user-cell">
              <a-avatar :size="32" :image-url="record.user_avatar || '/img/default_avatar.png'" />
              <div class="user-info">
                <span class="user-name">{{ record.user_nickname || record.username }}</span>
                <span class="user-acc">ID: {{ record.user_id }}</span>
              </div>
            </div>
          </template>
        </a-table-column>

        <a-table-column title="主理人/品牌名称" data-index="brand_name" :width="160">
          <template #cell="{ record }">
            <span class="brand-badge">{{ record.brand_name }}</span>
          </template>
        </a-table-column>

        <a-table-column title="联系方式" :width="160">
          <template #cell="{ record }">
            <div class="contact-box">
              <span>📞 {{ record.contact_phone }}</span>
              <span v-if="record.wechat_id">💬 {{ record.wechat_id }}</span>
            </div>
          </template>
        </a-table-column>

        <a-table-column title="主理人简介" data-index="intro" :ellipsis="true" :tooltip="true" />

        <a-table-column title="资质/作品" :width="100">
          <template #cell="{ record }">
            <a-image 
              v-if="record.credentials_url" 
              :src="record.credentials_url" 
              width="48" 
              height="48" 
              fit="cover"
              style="border-radius: 6px;"
            />
            <span v-else class="text-muted">无附件</span>
          </template>
        </a-table-column>

        <a-table-column title="申请时间" data-index="create_time" :width="160">
          <template #cell="{ record }">
            {{ formatTime(record.create_time) }}
          </template>
        </a-table-column>

        <a-table-column title="状态" :width="110">
          <template #cell="{ record }">
            <a-tag v-if="record.status === 0" color="orange">待审核</a-tag>
            <a-tag v-else-if="record.status === 1" color="green">已通过 (主理人)</a-tag>
            <a-tag v-else color="red">已驳回</a-tag>
          </template>
        </a-table-column>

        <a-table-column title="操作" :width="150" fixed="right">
          <template #cell="{ record }">
            <a-space v-if="record.status === 0">
              <a-button 
                type="primary" 
                size="small" 
                @click="handleApprove(record)"
                style="background-color: #00B42A;"
              >
                通过
              </a-button>
              <a-button 
                type="outline" 
                status="danger" 
                size="small" 
                @click="openRejectModal(record)"
              >
                驳回
              </a-button>
            </a-space>
            <span v-else-if="record.status === 2" class="reject-tip" :title="record.reject_reason">
              驳回: {{ record.reject_reason || '资料不符' }}
            </span>
            <span v-else class="text-muted">已开通权限</span>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 驳回原因弹窗 -->
    <a-modal 
      v-model:visible="rejectModalVisible" 
      title="驳回主理人申请" 
      @ok="handleRejectConfirm"
      :ok-loading="rejectSubmitting"
    >
      <a-form layout="vertical">
        <a-form-item label="请输入驳回原因 (将通知申请人)">
          <a-textarea 
            v-model="rejectReason" 
            placeholder="例如: 资质资料不完整 / 简介不够详细，请补充后重试" 
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { getCreatorApplications, approveCreatorApplication, rejectCreatorApplication } from '@/api/creator';
import { Message, Modal } from '@arco-design/web-vue';
import dayjs from 'dayjs';

export default {
  name: 'CreatorManager',
  data() {
    return {
      loading: false,
      applications: [],
      statusFilter: 'ALL',
      rejectModalVisible: false,
      rejectSubmitting: false,
      rejectReason: '',
      currentRecord: null
    };
  },
  computed: {
    pendingCount() {
      return this.applications.filter(item => item.status === 0).length;
    },
    filteredApplications() {
      if (this.statusFilter === 'ALL') return this.applications;
      return this.applications.filter(item => String(item.status) === this.statusFilter);
    }
  },
  created() {
    this.fetchApplications();
  },
  methods: {
    async fetchApplications() {
      this.loading = true;
      try {
        const res = await getCreatorApplications();
        this.applications = res.data.data || [];
      } catch (error) {
        Message.error('获取主理人申请列表失败');
      } finally {
        this.loading = false;
      }
    },
    formatTime(time) {
      if (!time) return '-';
      return dayjs(time).format('YYYY-MM-DD HH:mm');
    },
    handleApprove(record) {
      Modal.confirm({
        title: '确认通过审核',
        content: `确定通过【${record.brand_name}】的主理人申请吗？通过后该用户角色将自动升级为主理人，并开通工作台权限。`,
        okText: '确认通过',
        onOk: async () => {
          try {
            await approveCreatorApplication(record.id);
            Message.success('审核通过成功！');
            this.fetchApplications();
          } catch (error) {
            Message.error(error.response?.data?.message || '审核操作失败');
          }
        }
      });
    },
    openRejectModal(record) {
      this.currentRecord = record;
      this.rejectReason = '';
      this.rejectModalVisible = true;
    },
    async handleRejectConfirm() {
      if (!this.rejectReason.trim()) {
        return Message.warning('请输入驳回原因');
      }
      this.rejectSubmitting = true;
      try {
        await rejectCreatorApplication(this.currentRecord.id, this.rejectReason.trim());
        Message.success('已驳回该申请');
        this.rejectModalVisible = false;
        this.fetchApplications();
      } catch (error) {
        Message.error(error.response?.data?.message || '驳回操作失败');
      } finally {
        this.rejectSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
.creator-manager {
  padding: 10px 0;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title-wrap h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1D2129;
}
.count-tip {
  font-size: 12px;
  color: #86909C;
}
.filter-bar {
  margin-bottom: 16px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-info {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-weight: 600;
  font-size: 13px;
  color: #1D2129;
}
.user-acc {
  font-size: 11px;
  color: #86909C;
}
.brand-badge {
  font-weight: 700;
  color: #FF7E67;
}
.contact-box {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  gap: 2px;
}
.text-muted {
  color: #C9CDD4;
  font-size: 12px;
}
.reject-tip {
  font-size: 12px;
  color: #F53F3F;
}
</style>
