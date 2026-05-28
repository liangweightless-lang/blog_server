# 第一阶段：使用 Maven 镜像进行打包构建
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/settings.xml
COPY pom.xml .
# 利用 Docker 缓存，预先下载依赖
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：使用轻量级 JRE 运行构建好的 jar 包
FROM eclipse-temurin:21-jre
WORKDIR /app
# 使用通配符拷贝 jar 包，增强对版本变动的兼容性
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

# JVM 优化参数：
# -XX:+UseContainerSupport: 确保 JVM 正确感知 Docker 容器的内存限制
# -Duser.timezone: 统一容器时区为上海
# -Xmx: 建议在 docker-compose 中通过 environment 设置，或者此处给个保守值
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.awt.headless=true", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]

