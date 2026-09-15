/**
 * 前端通用业务枚举与字典映射
 * 消除魔法数字，统一管理前后端状态、文案及 Tag 颜色展示
 */

// 1. 社区快团跟团订单状态 (CampaignOrder)
export const CAMPAIGN_ORDER_STATUS = {
  UNPAID: { code: 0, label: '待付款', color: 'orange' },
  PAID_PENDING_PICKUP: { code: 1, label: '待提货', color: 'green' },
  COMPLETED: { code: 2, label: '已完成', color: 'blue' },
  CANCELLED: { code: 3, label: '已取消', color: 'gray' }
};

export function getCampaignOrderStatus(status) {
  for (const key in CAMPAIGN_ORDER_STATUS) {
    if (CAMPAIGN_ORDER_STATUS[key].code === status) {
      return CAMPAIGN_ORDER_STATUS[key];
    }
  }
  return { code: status, label: '处理中', color: 'gray' };
}

// 2. 社区快团活动状态 (GroupBuyCampaign)
export const CAMPAIGN_STATUS = {
  DRAFT: { code: 0, label: '未开始', color: 'orange' },
  ACTIVE: { code: 1, label: '进行中', color: 'blue' },
  ENDED: { code: 2, label: '已结束', color: 'gray' }
};

export function getCampaignStatus(status) {
  for (const key in CAMPAIGN_STATUS) {
    if (CAMPAIGN_STATUS[key].code === status) {
      return CAMPAIGN_STATUS[key];
    }
  }
  return { code: status, label: '未知状态', color: 'gray' };
}

// 3. 商城普通商品订单状态 (ProductOrder)
export const PRODUCT_ORDER_STATUS = {
  UNPAID: { code: 0, label: '待付款', color: 'orange' },
  PAID_PENDING_SHIP: { code: 1, label: '待提货/待出炉', color: 'blue' },
  CANCELLED: { code: 2, label: '已取消', color: 'gray' },
  COMPLETED: { code: 3, label: '已交付完成', color: 'green' }
};

export function getProductOrderStatus(status) {
  for (const key in PRODUCT_ORDER_STATUS) {
    if (PRODUCT_ORDER_STATUS[key].code === status) {
      return PRODUCT_ORDER_STATUS[key];
    }
  }
  return { code: status, label: '处理中', color: 'gray' };
}

// 4. 用户角色权限
export const USER_ROLE = {
  USER: { role: 'USER', label: '普通用户' },
  CREATOR: { role: 'CREATOR', label: '认证小柴包酱' },
  ADMIN: { role: 'ADMIN', label: '超级管理员' }
};
