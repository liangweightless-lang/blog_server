<template>
  <div class="campus-location-select">
    <div class="select-row">
      <a-select
        v-model="selectedLocationId"
        placeholder="请选择校内提货点/送达点"
        :loading="loading"
        allow-search
        class="location-dropdown"
        @change="handleLocationChange"
      >
        <template #prefix>
          <icon-location class="location-prefix-icon" />
        </template>
        <a-option
          v-for="loc in locations"
          :key="loc.id"
          :value="loc.id"
        >
          <div class="option-item">
            <span class="option-name">{{ loc.name }}</span>
            <span class="option-addr" v-if="loc.address">({{ loc.address }})</span>
          </div>
        </a-option>
      </a-select>
    </div>

    <!-- 选中的提货点提示与选填寝室/门牌号补充 -->
    <div v-if="currentLocation" class="selected-details-card">
      <div class="detail-badge-row">
        <span class="loc-tag"><icon-check-circle /> 校内指定点</span>
        <span class="loc-sub-addr">{{ currentLocation.address }}</span>
      </div>
      <div class="room-input-box">
        <a-input
          v-model="roomDetail"
          placeholder="补充寝室/门牌号 (选填，例：A栋402室)"
          size="small"
          class="room-input"
          @input="emitFullAddress"
        >
          <template #prefix>
            <icon-home style="color: #86909c; font-size: 13px;" />
          </template>
        </a-input>
      </div>
    </div>
    
    <div v-else-if="!loading && locations.length === 0" class="empty-tip">
      <icon-exclamation-circle /> 暂无可用校园提货点，请联系管理员配置
    </div>
  </div>
</template>

<script>
import { getActiveDeliveryLocations } from '@/api/campaign';

export default {
  name: 'CampusLocationSelect',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue', 'change'],
  data() {
    return {
      locations: [],
      selectedLocationId: null,
      roomDetail: '',
      loading: false
    };
  },
  computed: {
    currentLocation() {
      return this.locations.find(l => l.id === this.selectedLocationId) || null;
    }
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(val) {
        if (!val) {
          this.selectedLocationId = null;
          this.roomDetail = '';
        }
      }
    }
  },
  created() {
    this.fetchLocations();
  },
  methods: {
    async fetchLocations() {
      this.loading = true;
      try {
        const res = await getActiveDeliveryLocations();
        this.locations = res.data?.data || [];
        // 如果当前外部已有地址，尝试智能反向匹配
        this.tryMatchInitialValue();
      } catch (e) {
        console.error('加载校园提货点列表失败', e);
      } finally {
        this.loading = false;
      }
    },
    tryMatchInitialValue() {
      if (!this.modelValue || this.locations.length === 0) return;
      const matched = this.locations.find(l => 
        this.modelValue.includes(l.name) || (l.address && this.modelValue.includes(l.address))
      );
      if (matched) {
        this.selectedLocationId = matched.id;
      }
    },
    handleLocationChange(val) {
      this.selectedLocationId = val;
      this.emitFullAddress();
    },
    emitFullAddress() {
      if (!this.currentLocation) {
        this.$emit('update:modelValue', '');
        this.$emit('change', null, '');
        return;
      }
      const baseName = this.currentLocation.name || '校内自提点';
      const baseAddr = this.currentLocation.address ? `(${this.currentLocation.address})` : '';
      const room = this.roomDetail ? ` [${this.roomDetail.trim()}]` : '';
      const fullAddress = `${baseName} ${baseAddr}${room}`.trim();
      
      this.$emit('update:modelValue', fullAddress);
      this.$emit('change', this.currentLocation, fullAddress);
    }
  }
};
</script>

<style scoped>
.campus-location-select {
  width: 100%;
}

.location-dropdown {
  width: 100%;
  border-radius: 12px;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  transition: all 0.2s ease;
}

.location-dropdown:hover, .location-dropdown:focus-within {
  background: #ffffff;
  border-color: #165DFF;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.location-prefix-icon {
  color: #165DFF;
  font-size: 16px;
  margin-right: 4px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.option-name {
  font-weight: 600;
  color: #1d2129;
}

.option-addr {
  font-size: 12px;
  color: #86909c;
}

.selected-details-card {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(22, 93, 255, 0.04);
  border: 1px dashed rgba(22, 93, 255, 0.3);
  border-radius: 10px;
}

.detail-badge-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.loc-tag {
  color: #165DFF;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.loc-sub-addr {
  color: #4e5969;
}

.room-input-box {
  margin-top: 6px;
}

.room-input {
  border-radius: 8px;
  background: #ffffff;
  font-size: 12px;
}

.empty-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #f53f3f;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
