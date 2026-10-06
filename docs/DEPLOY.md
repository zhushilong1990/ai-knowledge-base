# AI Knowledge Base 部署指南

## 服务器信息

- IP: `123.207.69.23`
- 用户: `ubuntu`
- 前端路径: `/var/www/aikb/dist/`
- 后端端口: `8081`
- 后端 JAR: `ai-knowledge-base-1.0.0.jar`

---

## 部署步骤

### 第一步：本地打包后端

```bash
cd D:\code\ai-knowledge-base\backend
mvn clean package -DskipTests
```

### 第二步：上传后端 JAR

```bash
scp target/ai-knowledge-base-1.0.0.jar ubuntu@123.207.69.23:/opt/aikb/
```

### 第三步：在服务器上重启后端

```bash
ssh ubuntu@123.207.69.23

# 杀掉旧进程
pkill -f ai-knowledge-base || true

# 启动新的后端
cd /opt/aikb
nohup java -jar ai-knowledge-base-1.0.0.jar --server.port=8081 > app.log 2>&1 &

# 等待启动
sleep 10

# 验证后端
curl http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"test@test.com\",\"password\":\"test123456\"}"
```

### 第四步：本地打包前端

```bash
cd D:\code\ai-knowledge-base\frontend
npm install
npm run build
```

### 第五步：上传前端

```bash
scp -r dist/* ubuntu@123.207.69.23:/var/www/aikb/dist/
```

### 第六步：重载 Nginx

```bash
ssh ubuntu@123.207.69.23
sudo nginx -t && sudo nginx -s reload
```

### 第七步：访问测试

浏览器访问: http://123.207.69.23/

---

## 一键部署（本地 PowerShell 执行）

```powershell
# 1. 打包上传后端
cd D:\code\ai-knowledge-base\backend; mvn clean package -DskipTests; scp target/ai-knowledge-base-1.0.0.jar ubuntu@123.207.69.23:/opt/aikb/

# 2. 服务器重启后端
ssh ubuntu@123.207.69.23 "pkill -f ai-knowledge-base || true; cd /opt/aikb; nohup java -jar ai-knowledge-base-1.0.0.jar --server.port=8081 > app.log 2>&1 &"

# 3. 打包上传前端
cd D:\code\ai-knowledge-base\frontend; npm install; npm run build; scp -r dist/* ubuntu@123.207.69.23:/var/www/aikb/dist/

# 4. 重载 Nginx
ssh ubuntu@123.207.69.23 "sudo nginx -t && sudo nginx -s reload"
```

---

## 部署检查清单

- [ ] 后端 JAR 已上传 `/opt/aikb/`
- [ ] 旧进程已 kill
- [ ] 新后端启动无报错
- [ ] 前端 dist 已上传 `/var/www/aikb/dist/`
- [ ] Nginx 配置已重载
- [ ] http://123.207.69.23/ 可访问
- [ ] 登录功能正常
- [ ] 聊天功能正常
