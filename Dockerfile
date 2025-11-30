FROM maven:3.9.5-eclipse-temurin-17 AS builder 
#chọn phiên bản Maven kèm Java JDK 17
WORKDIR /app 
#tạo 1 thư mục tên là "app" cho tất cả mã nguồn trong gói docker
COPY pom.xml .      
#Copy file pom.xml vào thư mục hiện tại của gói docker
COPY .mvn/ .mvn     
#Copy thư mục của Maven vào thư mục hiện tại của gói docker (để cache dữ liệu)
COPY mvnw .   
#Copy file maven vào thư mục hiện tại của gói docker
RUN ./mvnw dependency:go-offline      
#download tất cả thư viện (dependency) trong file pom.xml
COPY src ./src      
#copy mã nguồn vào thư mục "src" trong gói docker
RUN ./mvnw package -DskipTests        
#tạo lại file jar cho toàn ứng dụng kèm theo thư viện
FROM eclipse-temurin:17-jdk-focal     
#chọn loại thư viện để tạo image không kèm theo Maven
WORKDIR /app 
#chỉ định thư mục chứa ứng dụng đã build xong
COPY --from=builder /app/target/*.jar app.jar   
#copy file jar vào thư mục thư viện của ứng dụng
EXPOSE 8080  
#thiết lập port của ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"] 
#định nghĩa dòng lệnh để khởi động ứng dụng Spring boot

