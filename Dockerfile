# 第一阶段：使用 Maven 镜像进行打包构建
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：使用轻量级 JRE 运行构建好的 jar 包
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/blog_server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
