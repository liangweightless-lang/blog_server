<template>
  <a-modal
    :title="actionType === 'join' ? '加入拼团确认' : '发起拼团确认'"
    :visible="visible"
    :width="isMobile ? '95%' : '450px'"
    @cancel="handleCancel"
    :footer="false"
    modal-class="group-dialog">
    <div class="group-dialog-content" v-if="product && product.id">
      <div class="product-mini-info">
        <a-image :src="product.image" class="mini-img" width="60" height="60" fit="cover" />
        <div class="mini-text">
          <h4>{{ product.name }}</h4>
          <p class="price-row">
            <span class="label">拼团特惠:</span>
            <span class="group-price">¥{{ product.groupPrice || (product.price * 0.8).toFixed(2) }}</span>
            <span class="origin-price">¥{{ product.price }}</span>
          </p>
        </div>
      </div>
      
      <div class="order-form">
        <div class="form-item">
          <p class="form-label">配送地址 <span class="required">*</span></p>
          <a-textarea 
            v-model="orderAddress" 
            placeholder="请输入详细收货地址" 
            :auto-size="{ minRows: 3, maxRows: 5 }">
          </a-textarea>
          <p class="address-tip" v-if="!orderAddress">建议前往“个人资料”设置默认地址</p>
        </div>
        
        <div class="form-item">
          <p class="form-label">预计配送时间</p>
          <a-tag color="gray">成团后 3 个工作日内发货</a-tag>
        </div>

        <div class="rule-box">
          <p><icon-info-circle /> 拼团须知：</p>
          <ul>
            <li>需满 8 人方可成团</li>
            <li>24小时内未成团将自动退款</li>
            <li>成团后不支持取消订单</li>
          </ul>
        </div>
      </div>
    </div>
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
      <a-button @click="handleCancel" shape="round">取 消</a-button>
      <a-button type="primary" status="warning" @click="confirmGroupAction" :loading="processing" shape="round">
        {{ actionType === 'join' ? '立即支付并加入' : '立即支付并开团' }}
      </a-button>
    </div>
  </a-modal>
</template>

<script>
import { joinGroup, startGroup } from '@/api/product';
import { createAlipay } from '@/api/order';
import { Message } from '@arco-design/web-vue';
import { mapState } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'GroupActionModal',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    actionType: {
      type: String,
      default: 'join' // 'start' | 'join'
    },
    product: {
      type: Object,
      default: () => ({})
    },
    groupId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      orderAddress: '',
      processing: false,
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
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        if (this.userInfo && this.userInfo.address) {
          this.orderAddress = this.userInfo.address;
        } else {
          this.orderAddress = '';
        }
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleCancel() {
      this.visible = false;
    },
    async confirmGroupAction() {
      if (!this.orderAddress) {
        return Message.warning('请填写配送地址');
      }
      
      this.processing = true;
      try {
        let orderId = null;
        
        if (this.actionType === 'join') {
          if (!this.groupId) throw new Error("Missing groupId for join");
          const res = await joinGroup(this.groupId, { address: this.orderAddress });
          orderId = res.data.data.orderId;
        } else {
          const res = await startGroup({ productId: this.product.id, address: this.orderAddress });
          orderId = res.data.data.orderId;
        }
        
        const payRes = await createAlipay(orderId);
        const formHtml = payRes.data.data;
        const div = document.createElement('div');
        div.innerHTML = formHtml;
        document.body.appendChild(div);
        
        if (document.forms && document.forms.length > 0) {
           document.forms[document.forms.length - 1].submit();
        }
        
        this.visible = false;
        this.$emit('success', this.actionType);
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        Message.error(error.response?.data?.message || '操作失败');
      } finally {
        this.processing = false;
      }
    }
  }
}
</script>

<style scoped>
.group-dialog-content {
  padding: 10px 0;
}
.product-mini-info {
  display: flex;
  background: #FFFDF8;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #FFE4D6;
}
.mini-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 12px;
}
.mini-text h4 {
  margin: 0 0 5px 0;
  font-size: 15px;
  color: #5C433B;
}
.price-row {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.price-row .label {
  font-size: 12px;
  color: #8C6A5D;
}
.group-price {
  color: #FF7E67;
  font-weight: 800;
  font-size: 18px;
}
.origin-price {
  text-decoration: line-through;
  color: #C0C4CC;
  font-size: 12px;
}
.form-item {
  margin-bottom: 20px;
}
.form-label {
  font-size: 14px;
  font-weight: 800;
  color: #5C433B;
  margin-bottom: 8px;
}
.required {
  color: #F56C6C;
}
.address-tip {
  font-size: 11px;
  color: #FF7E67;
  margin-top: 5px;
}
.rule-box {
  background: #F2F6FC;
  padding: 12px;
  border-radius: 8px;
  margin-top: 20px;
}
.rule-box p {
  margin: 0 0 8px 0;
  font-size: 12px;
  font-weight: 800;
  color: #165DFF;
  display: flex;
  align-items: center;
  gap: 4px;
}
.rule-box ul {
  margin: 0;
  padding-left: 18px;
  font-size: 11px;
  color: #86909C;
}
.rule-box li {
  margin-bottom: 4px;
}
</style>
