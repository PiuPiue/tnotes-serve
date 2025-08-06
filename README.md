# T-Notes · 可协作笔记与云存储系统

📍 线上地址：[notes.t-music.cn](http://notes.t-music.cn)  
🧑‍💻 独立全栈开发 ｜ 2025.1 - 2025.3

## 📘 项目简介

T-Notes 是一款基于 Spring Boot 构建的智能协作笔记平台，集成云存储与多人实时编辑功能，支持 AI 辅助写作、精细化权限控制及高性能文件上传等特性。适用于个人笔记、资料管理及小组协作场景，已部署上线并稳定运行，支持千级高并发访问。

## ✨ 核心功能

- ✅ 多人实时协作：基于 WebSocket 实现笔记内容实时同步
- ✅ AI 辅助写作：集成 Spring AI + DeepSeek，支持语法检查、摘要生成等功能
- ✅ 文件上传优化：结合 Redis 缓存与 MinIO 实现分片上传、断点续传与秒传
- ✅ 精细化权限控制：基于 ABAC 权限模型 + Spring AOP 实现多维度访问控制
- ✅ 异步任务解耦：使用 RabbitMQ 异步处理用户注册、文件处理等任务

## 🛠️ 技术栈

- 后端：Spring Boot, Spring Security, Spring AOP, MyBatis Plus
- 协作：WebSocket, Redis
- 文件存储：MinIO, Redis
- AI：Spring AI, DeepSeek
- 异步消息：RabbitMQ
- 前端：Vue2、Editor.js

## 🚀 项目成果

- 已部署并投入使用，稳定支持千级并发访问
- 作为开发者日常学习与资料管理工具
