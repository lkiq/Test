# 后端 Dockerfile - OpenJDK 17
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制打包好的 jar 文件
COPY target/*.jar app.jar

# 创建上传文件目录
RUN mkdir -p /app/uploads

# 时区设置
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
