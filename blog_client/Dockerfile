# 第一阶段：构建 Vue 前端产物
FROM node:18-alpine AS build
WORKDIR /app
RUN npm install -g pnpm --registry=https://registry.npmmirror.com
COPY package.json pnpm-lock.yaml ./
RUN pnpm config set registry https://registry.npmmirror.com
RUN pnpm install
COPY . .
# 调整 Node 内存上限为 1024MB，避免 512MB 导致频繁 Full GC 假死
ENV NODE_OPTIONS="--max-old-space-size=1024"
ARG APP_ENV=production
RUN pnpm run build --mode ${APP_ENV}

# 第二阶段：使用 Nginx 部署静态文件
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
