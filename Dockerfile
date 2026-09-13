# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# ก๊อปปี้ pom.xml ก่อน แล้วดึง dependency ให้ cache ไว้
# (ทำแบบนี้ ถ้าโค้ดเปลี่ยนแต่ pom.xml ไม่เปลี่ยน จะไม่ต้องโหลด dependency ใหม่ทุกครั้ง -> build เร็วขึ้นมาก)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ดึงมาแค่ jar ที่ build เสร็จแล้ว ไม่เอา Maven/source code ติดไปด้วย
# -> image ขนาดเล็กลงมาก และปลอดภัยกว่า (ไม่มี build tool อยู่ใน production image)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# ต้องเปิด Spring Boot Actuator (spring-boot-starter-actuator) ใน pom.xml ด้วย
# ไม่งั้น endpoint /actuator/health จะไม่มีจริง แล้ว healthcheck จะ fail ตลอด
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]