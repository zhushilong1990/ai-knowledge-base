# Phase 2 Document Ingestion — Discussion Log

**Phase:** 02
**Date:** 2026-10-01

## Discussion Summary

User selected: 全部讨论（4 areas）

### Area 1: 上传处理模式
**Options presented:** 同步 / 异步
**User decision:** 同步（文件小，简单实现）

### Area 2: Embedding 生成方式
**Options presented:** 本地模型（Ollama/nomic）/ 硅基流动 API
**User decision:** 硅基流动 API（已有 Key，无需额外部署）

### Area 3: Chroma Collection 组织
**Options presented:** 每用户 / 每个知识库 / 全部文档一个
**User decision:** 每用户一个 collection（隔离好，删除用户一并清数据）

### Area 4: 中文文档处理
**Options presented:** PyMuPDF / LayoutPDFReader
**User decision:** PyMuPDF（基本够用，乱序用原始文本行排序补救）

### Additional Specs
- Chunk size: 500 token / 50 token 重叠（10%）
- 支持格式: PDF, Word, TXT
- 文件大小限制: 10MB 前端校验
- 存储: 本地 `uploads/` 临时文件 + `data/chroma/` 向量数据
