<template>
  <div class="create-campaign-container">
    <a-page-header title="设置团购" subtitle="快团团模式" @back="$router.back()" style="background: white;" />
    
    <a-tabs v-model:active-key="activeTab" class="campaign-tabs">
      <a-tab-pane key="intro" title="介绍">
        <div class="tab-content">
          <a-form :model="form" layout="vertical">
            <a-form-item label="团购标题" required>
              <a-input v-model="form.title" placeholder="例如：小柴面包 3月24号(周二)" />
            </a-form-item>
            <a-form-item label="团购介绍">
              <a-textarea v-model="form.intro" placeholder="填写本次团购的说明、截单时间、注意事项等" :auto-size="{minRows:4}" />
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>
      
      <a-tab-pane key="products" title="商品">
        <div class="tab-content">
          <div class="selected-products" v-if="form.products.length > 0">
            <a-card v-for="(item, index) in form.products" :key="item.product.id" class="product-item-card" :bordered="false">
              <div class="product-row">
                <img :src="item.product.image" class="product-img" />
                <div class="product-info">
                  <div class="p-name">{{ item.product.name }}</div>
                  <div class="p-price">
                    团购价: ¥
                    <a-input-number v-model="item.groupPrice" :min="0" :precision="2" size="mini" style="width: 80px;" hide-button />
                  </div>
                  <div class="p-stock">
                    库存: 
                    <a-input-number v-model="item.stockLimit" :min="-1" size="mini" style="width: 80px;" hide-button placeholder="-1不限" />
                  </div>
                </div>
                <div class="product-actions">
                  <a-button type="text" status="danger" @click="removeProduct(index)"><icon-delete /></a-button>
                </div>
              </div>
            </a-card>
          </div>
          
          <a-button type="outline" long style="margin-top: 15px; border-style: dashed;" @click="showProductSelector">
            <template #icon><icon-plus /></template> 添加商品
          </a-button>
        </div>
      </a-tab-pane>
      
      <a-tab-pane key="settings" title="设置">
        <div class="tab-content">
          <a-form :model="form" layout="vertical">
            <a-card title="活动设置" :bordered="false" style="margin-bottom: 20px;">
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="提货点" field="deliveryLocationId" required>
                    <a-select v-model="form.deliveryLocationId" placeholder="请选择提货地点" :loading="loadingLocations">
                      <a-option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.name }} ({{ loc.address }})</a-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="目标成团人数" field="targetNum">
                    <a-input-number v-model="form.targetNum" :min="0" placeholder="设为0表示不限制" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-card>
            
            <a-form-item label="发货/提货时间">
              <a-date-picker v-model="form.deliveryTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
            </a-form-item>
            
            <a-form-item label="团购开始时间">
              <a-date-picker v-model="form.startTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
            </a-form-item>
            
            <a-form-item label="团购结束时间">
              <a-date-picker v-model="form.endTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="bottom-action-bar">
      <a-button type="primary" status="success" size="large" long shape="round" :loading="saving" @click="publishCampaign">发布团购</a-button>
    </div>

    <!-- 商品选择弹窗 -->
    <a-modal title="选择商品" v-model:visible="productSelectorVisible" width="90%" :footer="false">
      <a-input-search v-model="searchKeyword" placeholder="搜索商品名称" style="margin-bottom: 15px;" @search="searchProducts" />
      <div style="max-height: 60vh; overflow-y: auto;">
        <a-list>
          <a-list-item v-for="p in searchResults" :key="p.id" class="search-product-item" @click="addProduct(p)">
            <a-list-item-meta :title="p.name" :description="`原价: ¥${p.price}`">
              <template #avatar>
                <a-avatar shape="square"><img alt="avatar" :src="p.image" /></a-avatar>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="text" size="small">添加</a-button>
            </template>
          </a-list-item>
        </a-list>
      </div>
    </a-modal>

  </div>
</template>

<script>
import { getActiveDeliveryLocations, createCampaign } from '@/api/campaign';
import { getProducts } from '@/api/product';
import { Message } from '@arco-design/web-vue';
import dayjs from 'dayjs';

export default {
  name: 'CreateCampaign',
  data() {
    return {
      activeTab: 'intro',
      saving: false,
      form: {
        title: '',
        intro: '',
        deliveryLocationId: null,
        startTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().add(7, 'day').format('YYYY-MM-DD HH:mm:ss'),
        deliveryTime: '',
        targetNum: 0,
        products: []
      },
      locations: [],
      loadingLocations: false,
      
      productSelectorVisible: false,
      searchKeyword: '',
      allProducts: [],
      searchResults: []
    }
  },
  created() {
    this.fetchLocations();
    this.fetchAllProducts();
  },
  methods: {
    async fetchLocations() {
      this.loadingLocations = true;
      try {
        const res = await getActiveDeliveryLocations();
        this.locations = res.data.data || [];
        if (this.locations.length > 0) {
          this.form.deliveryLocationId = this.locations[0].id;
        }
      } catch (e) {
        Message.error('获取提货点失败');
      } finally {
        this.loadingLocations = false;
      }
    },
    async fetchAllProducts() {
      try {
        const res = await getProducts();
        this.allProducts = res.data.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    showProductSelector() {
      this.searchKeyword = '';
      this.searchResults = this.allProducts;
      this.productSelectorVisible = true;
    },
    searchProducts() {
      if (!this.searchKeyword) {
        this.searchResults = this.allProducts;
      } else {
        this.searchResults = this.allProducts.filter(p => p.name.includes(this.searchKeyword));
      }
    },
    addProduct(product) {
      if (this.form.products.find(item => item.productId === product.id)) {
        Message.warning('该商品已添加');
        return;
      }
      this.form.products.push({
        productId: product.id,
        groupPrice: product.price,
        stockLimit: -1,
        product: product
      });
      Message.success('添加成功');
      this.productSelectorVisible = false;
      this.activeTab = 'products';
    },
    removeProduct(index) {
      this.form.products.splice(index, 1);
    },
    async publishCampaign() {
      if (!this.form.title) {
        this.activeTab = 'intro';
        return Message.warning('请输入团购标题');
      }
      if (this.form.products.length === 0) {
        this.activeTab = 'products';
        return Message.warning('请至少添加一个商品');
      }
      if (!this.form.deliveryLocationId) {
        this.activeTab = 'settings';
        return Message.warning('请选择提货点');
      }
      
      this.saving = true;
      try {
        // Convert dates to ISO or expected format. Since we use LocalDateTime, 'YYYY-MM-DDTHH:mm:ss' is safe.
        const payload = {
          title: this.form.title,
          intro: this.form.intro,
          deliveryLocationId: this.form.deliveryLocationId,
          startTime: this.form.startTime ? dayjs(this.form.startTime).format('YYYY-MM-DDTHH:mm:ss') : null,
          endTime: this.form.endTime ? dayjs(this.form.endTime).format('YYYY-MM-DDTHH:mm:ss') : null,
          deliveryTime: this.form.deliveryTime ? dayjs(this.form.deliveryTime).format('YYYY-MM-DDTHH:mm:ss') : null,
          status: 1, // Directly active
          products: this.form.products.map(p => ({
            productId: p.productId,
            groupPrice: p.groupPrice,
            stockLimit: p.stockLimit
          }))
        };
        await createCampaign(payload);
        Message.success('团购发布成功！');
        this.$router.back();
      } catch (e) {
        Message.error('发布失败');
      } finally {
        this.saving = true;
      }
    }
  }
}
</script>

<style scoped>
.create-campaign-container {
  min-height: 100vh;
  background-color: #F4F6F9;
  padding-bottom: 80px;
}

.campaign-tabs {
  background: white;
  margin-top: 10px;
}
:deep(.arco-tabs-nav-tab) {
  justify-content: center;
}

.tab-content {
  padding: 16px;
  background: white;
  min-height: calc(100vh - 200px);
}

.product-item-card {
  margin-bottom: 12px;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
}
:deep(.product-item-card > .arco-card-body) {
  padding: 12px;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.product-info {
  flex-grow: 1;
}
.p-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
  color: #1D2129;
}
.p-price, .p-stock {
  font-size: 13px;
  color: #4E5969;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 12px 16px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
  box-sizing: border-box;
  padding-bottom: calc(12px + var(--safe-bottom));
}

.search-product-item {
  cursor: pointer;
}
.search-product-item:hover {
  background-color: #F2F3F5;
}
</style>
