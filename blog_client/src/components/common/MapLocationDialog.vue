<template>
  <a-modal 
    title="地图定位" 
    :visible="visible" 
    :width="isMobile ? '95%' : '600px'" 
    @cancel="handleCancel" 
    @ok="handleConfirm" 
    :ok-button-props="{disabled: !selectedAddress}" 
    unmount-on-close>
    
    <div v-if="!amapKey" style="margin-bottom: 15px;">
      <a-alert type="warning">
        管理员尚未在后台系统配置中配置“高德地图 Key”与安全密钥，暂时无法使用地图定位功能。<br>
        <span style="font-size: 12px; color: #909399;">若您是管理员，请前往 控制台 -> 首页信息配置 -> 第三方服务配置 填写相关信息。</span>
      </a-alert>
    </div>
    
    <div style="margin-bottom: 10px; display: flex; gap: 10px;">
      <a-input id="map-search-input" v-model="mapSearchKeyword" placeholder="搜索地点..." allow-clear>
        <template #prefix><icon-search /></template>
      </a-input>
      <a-button type="primary" @click="searchMap">搜索</a-button>
    </div>
    
    <div id="amap-container" style="width: 100%; height: 300px; border-radius: 8px; border: 1px solid #E5E6EB;">
      <a-spin :loading="mapLoading" style="width: 100%; height: 100%; display: flex; justify-content: center; align-items: center;" v-if="mapLoading" />
    </div>
    
    <div v-if="selectedAddress" style="margin-top: 15px; background: #F2F6FC; padding: 10px; border-radius: 8px;">
      <span style="color: #909399; font-size: 12px;">已选地址：</span>
      <div style="font-weight: bold; color: #303133; margin-top: 4px;">{{ selectedAddress }}</div>
    </div>
  </a-modal>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import AMapLoader from '@amap/amap-jsapi-loader';

export default {
  name: 'MapLocationDialog',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      mapLoading: false,
      mapSearchKeyword: '',
      selectedAddress: '',
      mapInstance: null,
      markerInstance: null,
      geocoderInstance: null,
      amapKey: '',
      amapSecurityCode: '',
      isMobile: window.innerWidth <= 768
    }
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
    show(newVal) {
      if (newVal) {
        this.selectedAddress = '';
        this.mapSearchKeyword = '';
        if (!this.amapKey) {
          this.fetchMapConfig().then(() => {
            setTimeout(() => {
              this.initMap();
            }, 100);
          });
        } else {
          setTimeout(() => {
            this.initMap();
          }, 100);
        }
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.mapInstance) {
      this.mapInstance.destroy();
    }
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    async fetchMapConfig() {
      try {
        const res = await axios.get('/api/home/config');
        if (res.data && res.data.data) {
          this.amapKey = res.data.data.amapKey || '';
          this.amapSecurityCode = res.data.data.amapSecurityCode || '';
        }
      } catch (e) {
        console.error('获取地图配置失败', e);
      }
    },
    initMap() {
      if (!this.amapKey) return;
      if (document.getElementById('amap-container') == null) return;
      
      this.mapLoading = true;
      window._AMapSecurityConfig = {
        securityJsCode: this.amapSecurityCode,
      };

      AMapLoader.load({
        key: this.amapKey,
        version: "2.0",
        plugins: ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.Geolocation']
      }).then((AMap) => {
        this.mapInstance = new AMap.Map('amap-container', {
          zoom: 14,
          center: [116.397428, 39.90923]
        });

        this.markerInstance = new AMap.Marker({
          map: this.mapInstance
        });

        this.geocoderInstance = new AMap.Geocoder();

        const geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          zoomToAccuracy: true,
          buttonPosition: 'RB'
        });
        this.mapInstance.addControl(geolocation);
        geolocation.getCurrentPosition((status, result) => {
          if (status === 'complete') {
             this.markerInstance.setPosition(result.position);
             this.getAddress(result.position);
          }
        });

        this.mapInstance.on('click', (e) => {
          this.markerInstance.setPosition(e.lnglat);
          this.getAddress(e.lnglat);
        });

        this.mapLoading = false;
      }).catch(e => {
        console.error(e);
        this.mapLoading = false;
        Message.error('地图加载失败，请检查 Key 和网络');
      });
    },
    getAddress(lnglat) {
      if (!this.geocoderInstance) return;
      this.geocoderInstance.getAddress(lnglat, (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          this.selectedAddress = result.regeocode.formattedAddress;
        }
      });
    },
    searchMap() {
      if (!this.mapSearchKeyword || !this.mapInstance) return;
      AMapLoader.load({}).then((AMap) => {
         const placeSearch = new AMap.PlaceSearch({
            map: this.mapInstance
         });
         placeSearch.search(this.mapSearchKeyword, (status, result) => {
            if (status === 'complete' && result.info === 'OK') {
               const poi = result.poiList.pois[0];
               if (poi) {
                  const lnglat = [poi.location.lng, poi.location.lat];
                  this.mapInstance.setCenter(lnglat);
                  this.markerInstance.setPosition(lnglat);
                  this.selectedAddress = poi.pname + poi.cityname + poi.adname + poi.address + poi.name;
               }
            } else {
               Message.warning('未找到相关地点');
            }
         });
      });
    },
    handleCancel() {
      this.visible = false;
    },
    handleConfirm() {
      this.$emit('select', this.selectedAddress);
      this.visible = false;
    }
  }
}
</script>
