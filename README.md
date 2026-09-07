# 盛迪嘉支付 · 商户对接支持平台

持牌支付机构 **盛迪嘉支付** 用于对接方企业与内部处理人协同跟进支付 API 接入、交易与结算问题的工单系统。用以替代即时通讯群：问题可检索、状态可追踪、处理过程可留痕。

## 功能

- 对接方提交工单：摘要、详情、分类、影响程度、联系方式、商户号
- 工单列表：按状态、对接企业、处理人、日期、关键词筛选
- 工单详情：变更状态 / 指派处理人、追加备注、时间线
- 角色：对接方仅能查看本企业工单；内部处理人可查看并处理全部工单
- 总览：各状态数量、今日新增、最近更新
- 数据持久化于 MySQL，重启不丢失

## 一键启动（推荐）

需要本机已安装 Docker 与 Docker Compose。

```bash
docker compose up -d --build
```

浏览器访问：**http://localhost:8080**

停止：

```bash
docker compose down
```

数据卷 `sdj_mysql_data` 会保留工单。若要清空演示数据：`docker compose down -v`。

## 演示账号

| 角色 | 账号 | 密码 | 所属 |
| --- | --- | --- | --- |
| 内部管理员 | `admin` | `Sdj@Admin2026` | 盛迪嘉支付 |
| 内部处理人 | `handler` | `Sdj@Handler2026` | 盛迪嘉支付 |
| 对接方 | `xinghui` | `Sdj@Partner2026` | 星辉科技有限公司 |
| 对接方 | `yuntu` | `Sdj@Partner2026` | 云途电子商务有限公司 |

首次启动会写入若干演示工单，打开列表即可看到数据。

## 本地开发

环境：JDK 21、Maven 3.9+、Node.js 20+、MySQL 8。

```sql
CREATE DATABASE sdj_support CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sdj'@'%' IDENTIFIED BY 'sdj_pass_2026';
GRANT ALL ON sdj_support.* TO 'sdj'@'%';
```

启动后端：

```bash
cd backend
mvn spring-boot:run
```

启动前端（开发时代理 `/api` 到 8080）：

```bash
cd frontend
npm install
npm run dev
```

开发界面：http://localhost:5173  
生产打包后由 Spring Boot 在 **8080** 提供同一站点：

```bash
cd frontend
npm install
npm run build
# 产物默认输出到 backend/src/main/resources/static
cd ../backend
mvn spring-boot:run
```

可通过环境变量覆盖数据源：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`（默认 8080）

## 部署说明

1. 在服务器安装 Docker，将本仓库同步到目标目录。
2. 按需修改 `docker-compose.yml` 中的数据库密码与端口映射。
3. 执行 `docker compose up -d --build`。
4. 将 8080 置于 HTTPS 反向代理（Nginx / 网关）之后，再对合作机构开放。
5. 生产环境请立即修改演示账号密码，并关闭或替换种子数据（`APP_SEED=false` 可跳过重复种子；仅在用户表为空时写入）。

应用健康检查：`GET /api/health`。

## 字段说明

| 字段 | 含义 |
| --- | --- |
| 工单编号 | 形如 `SDJ-20260907-0001`，按日递增 |
| 摘要 | 问题一句话标题 |
| 详情 | 现象、影响、已做排查 |
| 发起人 | 提交人姓名与所属企业 |
| 处理人 | 盛迪嘉内部跟进人 |
| 状态 | 待处理 / 处理中 / 已解决 / 已关闭 |
| 影响程度 | 低 / 中 / 高 / 紧急 |
| 分类 | 对接 / 交易 / 结算 / 其他 |
| 商户号 | 相关商户 ID，可选 |
| 备注 | 可多次追加，形成时间线 |
| 创建 / 更新 / 解决时间 | 系统自动维护；进入已解决或已关闭时写入解决时间 |

## 技术栈

- 后端：Java 21、Spring Boot 3.4、Spring Security（Session）、Spring Data JPA、Flyway
- 前端：Vue 3、Vite（中文界面，夜蓝金配色）
- 数据库：MySQL 8.4

## 项目结构

```
backend/     Spring Boot API 与打包后的前端静态资源
frontend/    Vue 3 单页应用
Dockerfile   多阶段构建（Node 打包前端 + Maven 打包后端）
docker-compose.yml   应用 + MySQL
```
