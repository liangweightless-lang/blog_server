<template>
  <div class="campaign-manager">
    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="list" title="活动列表">
        <a-button type="primary" class="brand-btn" style="margin-bottom: 16px;" :style="{ width: isMobile ? '100%' : 'auto' }" @click="$router.push('/admin/campaign/create')">
          <icon-plus /> 发起新团购
        </a-button>

        <!-- PC端表格 -->
        <a-table v-if="!isMobile" :data="campaigns" :loading="loading" stripe :scroll="{ x: 600 }">
          <template #columns>
            <a-table-column title="团购标题" data-index="title"></a-table-column>
            <a-table-column title="提货点">
              <template #cell="{ record }">
                {{ record.deliveryLocation?.name || '未知' }}
              </template>
            </a-table-column>
            <a-table-column title="状态" :width="100">
              <template #cell="{ record }">
                <a-tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '进行中' : (record.status === 2 ? '已结束' : '未开始') }}</a-tag>
              </template>
            </a-table-column>
              <a-table-column title="操作" :width="280" fixed="right">
              <template #cell="{ record }">
                <a-button type="text" size="small" status="success" @click="shareCampaign(record)"><icon-share-alt /> 分享</a-button>
                <a-button type="text" size="small" @click="viewOrders(record)">查看订单</a-button>
                <a-button type="text" size="small" status="danger" @click="handleDelete(record)">删除</a-button>
              </template>
            </a-table-column>
          </template>
        </a-table>

        <!-- 移动端卡片列表 -->
        <div v-else class="mobile-card-list">
          <a-spin :loading="loading" style="width: 100%;">
            <a-card v-for="campaign in campaigns" :key="campaign.id" class="mobile-card" :bordered="false">
              <div class="m-card-header">
                <div class="m-card-title">{{ campaign.title }}</div>
                <a-tag :color="campaign.status === 1 ? 'green' : 'gray'" size="small">{{ campaign.status === 1 ? '进行中' : (campaign.status === 2 ? '已结束' : '未开始') }}</a-tag>
              </div>
              <div class="m-card-body">
                <div class="m-info-line"><icon-location /> {{ campaign.deliveryLocation?.name || '未知提货点' }}</div>
              </div>
              <div class="m-card-actions">
                <div class="m-action-btn" style="color: #00B42A;" @click="shareCampaign(campaign)"><icon-share-alt /> 分享</div>
                <div class="m-action-btn" style="color: #165DFF;" @click="viewOrders(campaign)"><icon-list /> 订单</div>
                <div class="m-action-btn" style="color: #F53F3F;" @click="handleDelete(campaign)"><icon-delete /> 删除</div>
              </div>
            </a-card>
            <a-empty v-if="campaigns.length === 0" description="暂无快团活动" />
          </a-spin>
        </div>
      </a-tab-pane>

      <a-tab-pane key="orders" title="跟团订单" :disabled="!currentCampaign">
        <div v-if="currentCampaign" style="margin-bottom: 16px;">
          <a-button @click="activeTab = 'list'"><icon-left /> 返回活动列表</a-button>
          <div :style="isMobile ? 'margin-top: 10px;' : 'margin-left: 16px; display: inline-block;'" style="font-weight: bold; color: #1D2129;">当前活动：{{ currentCampaign.title }}</div>
        </div>

        <!-- PC端表格 -->
        <a-table v-if="!isMobile" :data="orders" :loading="loadingOrders" stripe :pagination="{ pageSize: 20 }" :scroll="{ x: 800 }">
          <template #columns>
            <a-table-column title="跟团号" data-index="followNumber" :width="80" align="center">
              <template #cell="{ record }">
                <div style="font-weight: bold; font-size: 16px; color: #165DFF;">{{ record.followNumber }}</div>
              </template>
            </a-table-column>
            <a-table-column title="取货人/电话">
              <template #cell="{ record }">
                <div><icon-user /> {{ record.contactName }}</div>
                <div><icon-phone /> {{ record.contactPhone }}</div>
              </template>
            </a-table-column>
            <a-table-column title="商品明细">
              <template #cell="{ record }">
                <div v-for="item in record.items" :key="item.id" style="font-size: 12px;">
                  {{ item.productName }} x {{ item.quantity }}
                </div>
              </template>
            </a-table-column>
            <a-table-column title="实收总额" :width="100">
              <template #cell="{ record }">
                <span style="color: #F53F3F; font-weight: bold;">¥{{ record.totalAmount }}</span>
              </template>
            </a-table-column>
            <a-table-column title="状态" :width="100">
              <template #cell="{ record }">
                <a-tag :color="getOrderStatusColor(record.status)">{{ getOrderStatusText(record.status) }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="操作" :width="120" fixed="right">
              <template #cell="{ record }">
                <a-button 
                  v-if="record.status === 0 || record.status === 1" 
                  type="primary" status="success" size="small" shape="round" 
                  @click="handleVerifyOrder(record)"
                >核销提货</a-button>
              </template>
            </a-table-column>
          </template>
        </a-table>

        <!-- 移动端卡片列表 -->
        <div v-else class="mobile-card-list">
          <a-spin :loading="loadingOrders" style="width: 100%;">
            <a-card v-for="order in orders" :key="order.id" class="mobile-card order-card" :bordered="false">
              <div class="m-card-header">
                <div class="m-order-no">#{{ order.followNumber }}</div>
                <a-tag :color="getOrderStatusColor(order.status)" size="small">{{ getOrderStatusText(order.status) }}</a-tag>
              </div>
              <div class="m-card-body">
                <div class="m-info-line"><icon-user /> {{ order.contactName }}  <icon-phone style="margin-left:8px;"/> {{ order.contactPhone }}</div>
                <div class="m-order-items">
                  <div v-for="item in order.items" :key="item.id" class="m-item-line">
                    <span class="m-item-name">{{ item.productName }}</span>
                    <span class="m-item-qty">x{{ item.quantity }}</span>
                  </div>
                </div>
              </div>
              <div class="m-card-footer">
                <div class="m-total">实收: <span class="m-price">¥{{ order.totalAmount }}</span></div>
                <a-button v-if="order.status === 0 || order.status === 1" type="primary" size="small" shape="round" class="brand-btn" @click="handleVerifyOrder(order)">核销提货</a-button>
              </div>
            </a-card>
            <a-empty v-if="orders.length === 0" description="暂无订单" />
          </a-spin>
        </div>
      </a-tab-pane>

      <a-tab-pane key="locations" title="提货点管理">
        <div style="margin-bottom: 16px;">
          <a-button type="primary" class="brand-btn" :style="{ width: isMobile ? '100%' : 'auto' }" @click="showAddLocationDialog">
            <template #icon><icon-plus /></template>新增提货点
          </a-button>
        </div>
        
        <!-- PC端表格 -->
        <a-table v-if="!isMobile" :data="locations" :loading="loadingLocations" stripe :scroll="{ x: 600 }">
          <template #columns>
            <a-table-column title="名称" data-index="name"></a-table-column>
            <a-table-column title="详细地址" data-index="address"></a-table-column>
            <a-table-column title="联系人" data-index="contactName"></a-table-column>
            <a-table-column title="联系电话" data-index="contactPhone"></a-table-column>
            <a-table-column title="状态" :width="100">
              <template #cell="{ record }">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column title="操作" :width="150" fixed="right">
              <template #cell="{ record }">
                <a-button type="text" size="small" @click="handleEditLocation(record)">编辑</a-button>
                <a-button type="text" size="small" status="danger" @click="handleDeleteLocation(record)">删除</a-button>
              </template>
            </a-table-column>
          </template>
        </a-table>

        <!-- 移动端卡片列表 -->
        <div v-else class="mobile-card-list">
          <a-spin :loading="loadingLocations" style="width: 100%;">
            <a-card v-for="loc in locations" :key="loc.id" class="mobile-card" :bordered="false">
              <div class="m-card-header">
                <div class="m-card-title">{{ loc.name }}</div>
                <a-tag :color="loc.status === 1 ? 'green' : 'red'" size="small">{{ loc.status === 1 ? '启用' : '禁用' }}</a-tag>
              </div>
              <div class="m-card-body">
                <div class="m-info-line"><icon-location /> {{ loc.address }}</div>
                <div class="m-info-line"><icon-user /> {{ loc.contactName }} ({{ loc.contactPhone }})</div>
              </div>
              <div class="m-card-actions">
                <div class="m-action-btn" style="color: #165DFF;" @click="handleEditLocation(loc)"><icon-edit /> 编辑</div>
                <div class="m-action-btn" style="color: #F53F3F;" @click="handleDeleteLocation(loc)"><icon-delete /> 删除</div>
              </div>
            </a-card>
            <a-empty v-if="locations.length === 0" description="暂无提货点" />
          </a-spin>
        </div>
      </a-tab-pane>
    </a-tabs>

    <!-- 新增/编辑提货点弹窗 -->
    <a-modal :title="locationForm.id ? '编辑提货点' : '新增提货点'" v-model:visible="locationDialogVisible" @ok="saveLocation" unmount-on-close>
      <a-form :model="locationForm" layout="vertical">
        <a-form-item label="提货点名称 (如: 广东工业大学喜之源便利店)">
          <a-input v-model="locationForm.name" />
        </a-form-item>
        <a-form-item label="详细地址">
          <a-input v-model="locationForm.address" />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model="locationForm.contactName" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model="locationForm.contactPhone" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model="locationForm.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </a-switch>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { getCampaigns, deleteCampaign, getCampaignOrders, updateCampaignOrderStatus, getDeliveryLocations, createDeliveryLocation, updateDeliveryLocation, deleteDeliveryLocation } from '@/api/campaign';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'CampaignManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      activeTab: 'list',
      campaigns: [],
      loading: false,
      
      currentCampaign: null,
      orders: [],
      loadingOrders: false,
      
      locations: [],
      loadingLocations: false,
      locationDialogVisible: false,
      locationForm: {
        id: null,
        name: '',
        address: '',
        contactName: '',
        contactPhone: '',
        status: 1
      }
    }
  },
  created() {
    this.fetchCampaigns();
    this.fetchLocations();
  },
  methods: {
    async fetchCampaigns() {
      this.loading = true;
      try {
        const res = await getCampaigns();
        this.campaigns = res.data.data || [];
      } catch (e) {
        Message.error('获取活动列表失败');
      } finally {
        this.loading = false;
      }
    },
    shareCampaign(record) {
      const shareUrl = `${window.location.origin}/campaign/${record.id}`;
      // Use clipboard API
      if (navigator.clipboard) {
        navigator.clipboard.writeText(shareUrl).then(() => {
          Message.success('已复制团购链接，快去微信群分享吧！');
        }).catch(err => {
          Message.error('复制失败，请手动复制: ' + shareUrl);
        });
      } else {
        // Fallback
        const input = document.createElement('input');
        input.value = shareUrl;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        Message.success('已复制团购链接，快去微信群分享吧！');
      }
    },
    handleDelete(record) {
      Modal.confirm({
        title: '警告',
        content: '确定要删除该团购活动吗？删除后相关订单也可能丢失。',
        onOk: async () => {
          try {
            await deleteCampaign(record.id);
            Message.success('删除成功');
            this.fetchCampaigns();
          } catch (e) {
            Message.error('删除失败');
          }
        }
      });
    },
    async viewOrders(campaign) {
      this.currentCampaign = campaign;
      this.activeTab = 'orders';
      this.loadingOrders = true;
      try {
        const res = await getCampaignOrders(campaign.id);
        this.orders = res.data.data || [];
      } catch (e) {
        Message.error('获取订单失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    getOrderStatusColor(status) {
      const map = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'gray' };
      return map[status] || 'gray';
    },
    getOrderStatusText(status) {
      const map = { 0: '待付款', 1: '已付款', 2: '已提货(核销)', 3: '已退款' };
      return map[status] || '未知';
    },
    handleVerifyOrder(order) {
      Modal.confirm({
        title: '核销确认',
        content: `确定顾客已经提货了吗？跟团号: ${order.followNumber}`,
        onOk: async () => {
          try {
            await updateCampaignOrderStatus(order.id, 2); // 2: picked_up
            Message.success('核销成功');
            this.viewOrders(this.currentCampaign); // refresh
          } catch (e) {
            Message.error('核销失败');
          }
        }
      });
    },
    // --- Delivery Locations Methods ---
    async fetchLocations() {
      this.loadingLocations = true;
      try {
        const res = await getDeliveryLocations();
        this.locations = res.data.data || [];
      } catch (error) {
        Message.error('获取提货点失败');
      } finally {
        this.loadingLocations = false;
      }
    },
    showAddLocationDialog() {
      this.locationForm = {
        id: null,
        name: '',
        address: '',
        contactName: '',
        contactPhone: '',
        status: 1
      };
      this.locationDialogVisible = true;
    },
    handleEditLocation(record) {
      this.locationForm = { ...record };
      this.locationDialogVisible = true;
    },
    async saveLocation() {
      if (!this.locationForm.name || !this.locationForm.address) {
        Message.warning('名称和地址不能为空');
        return false;
      }
      try {
        if (this.locationForm.id) {
          await updateDeliveryLocation(this.locationForm.id, this.locationForm);
        } else {
          await createDeliveryLocation(this.locationForm);
        }
        Message.success('保存提货点成功');
        this.fetchLocations();
        this.locationDialogVisible = false;
      } catch (e) {
        Message.error('保存提货点失败');
        return false;
      }
    },
    handleDeleteLocation(record) {
      Modal.confirm({
        title: '提示',
        content: `确定删除提货点 "${record.name}" 吗？`,
        onOk: async () => {
          try {
            await deleteDeliveryLocation(record.id);
            Message.success('删除提货点成功');
            this.fetchLocations();
          } catch (e) {
            Message.error('删除提货点失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.campaign-manager {
  background: transparent;
  min-height: 500px;
}

:deep(.arco-tabs-nav-tab-active) {
  color: var(--brand-primary, #FF4B2B) !important;
  font-weight: 800 !important;
}
:deep(.arco-tabs-nav-ink) {
  background-color: var(--brand-primary, #FF4B2B) !important;
  height: 3px !important;
  border-radius: 2px;
}

.brand-btn {
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%)) !important;
  border: none !important;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 75, 43, 0.2);
}

.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
}

.mobile-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  overflow: hidden;
}
:deep(.mobile-card > .arco-card-body) {
  padding: 0;
}

.m-card-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #F2F3F5;
}
.m-card-title {
  font-size: 16px;
  font-weight: 800;
  color: #1D2129;
}

.m-card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.m-info-line {
  font-size: 13px;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 6px;
}

.m-card-actions {
  display: flex;
  border-top: 1px solid #F2F3F5;
  background: #FAFAFA;
}
.m-action-btn {
  flex: 1;
  padding: 12px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  position: relative;
}
.m-action-btn:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 25%;
  height: 50%;
  width: 1px;
  background: #E5E6EB;
}
.m-action-btn:active {
  background: #F2F3F5;
}

/* Order specific */
.m-order-no {
  font-size: 18px;
  font-weight: 900;
  color: var(--brand-primary, #FF4B2B);
}
.m-order-items {
  background: #F7F8FA;
  border-radius: 8px;
  padding: 10px;
  margin-top: 8px;
}
.m-item-line {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #4E5969;
  margin-bottom: 4px;
}
.m-item-line:last-child {
  margin-bottom: 0;
}
.m-card-footer {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #E5E6EB;
}
.m-total {
  font-size: 13px;
  color: #86909C;
}
.m-price {
  font-size: 18px;
  font-weight: 800;
  color: #1D2129;
}
</style>
