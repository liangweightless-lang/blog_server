import { defineStore } from 'pinia';
import axios from '@/utils/request';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    loading: false
  }),
  getters: {
    isLoggedIn: (state) => !!state.userInfo,
    points: (state) => state.userInfo?.points || 0
  },
  actions: {
    async fetchUser() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.userInfo = null;
        return null;
      }
      
      this.loading = true;
      try {
        const res = await axios.get('/api/users/me');
        if (res.data && res.data.data) {
          this.userInfo = res.data.data;
          return this.userInfo;
        }
      } catch (error) {
        this.userInfo = null;
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
        }
      } finally {
        this.loading = false;
      }
    },
    clearUser() {
      this.userInfo = null;
      localStorage.removeItem('token');
    },
    updatePoints(amount) {
      if (this.userInfo) {
        this.userInfo.points -= amount;
      }
    }
  }
});
