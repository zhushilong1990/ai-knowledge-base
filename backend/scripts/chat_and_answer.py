"""
RAG chat script: query ChromaDB and generate answer via SiliconFlow LLM.
Reads JSON from stdin, writes JSON to stdout.
"""
import sys
import json
import os

import chromadb
import requests

SILICONFLOW_API = "https://api.siliconflow.cn/v1/chat/completions"
MODEL = "deepseek-ai/DeepSeek-V3"
DEFAULT_CHROMA_PATH = "backend/data/chroma"
SIMILARITY_THRESHOLD = 0.7
MAX_DISTANCE = 1 - (2 * SIMILARITY_THRESHOLD)  # 0.6 for threshold=0.7
TOP_K = 10
MAX_SOURCES = 3


def retrieve_context(question, kb_id, user_id, chroma_path):
    """Query ChromaDB for relevant chunks, filter by similarity threshold, return top-3."""
    collection_name = f"user_{user_id}_kb_{kb_id}"

    client = chromadb.PersistentClient(path=chroma_path)
    collection = client.get_or_create_collection(name=collection_name)

    results = collection.query(
        query_texts=[question],
        n_results=TOP_K,
        where={"kbId": str(kb_id)},
        include=["documents", "distances", "metadatas"]
    )

    # Post-filter by similarity threshold (distance > 0.6 means similarity < 0.7)
    filtered = []
    for doc, dist, meta in zip(
            results["documents"][0],
            results["distances"][0],
            results["metadatas"][0]
    ):
        if dist <= MAX_DISTANCE:
            similarity = 1 - (dist / 2)
            filtered.append({
                "document": doc,
                "distance": dist,
                "similarity": similarity,
                "metadata": meta
            })
        if len(filtered) >= MAX_SOURCES:
            break

    return filtered


def build_context_string(sources):
    """Build context string with numbered citations."""
    parts = []
    for i, source in enumerate(sources, 1):
        parts.append(f"【{i}】{source['document']}")
    return "\n\n".join(parts)


def generate_answer(question, context, api_key):
    """Call SiliconFlow LLM to generate answer with citations."""
    system_prompt = f"""你是一个基于知识库的问答助手。

参考信息：
{context}

要求：
1. 仅基于上述参考信息回答问题
2. 每个引用使用【N】格式，例如【1】【2】【3】
3. 如果参考信息不足以回答，请明确说明
4. 回答简洁明了

问题：{question}"""

    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": question}
        ],
        "max_tokens": 2048,
        "temperature": 0.3
    }

    resp = requests.post(
        SILICONFLOW_API,
        headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
        json=payload,
        timeout=120
    )
    resp.raise_for_status()
    return resp.json()["choices"][0]["message"]["content"]


def main():
    try:
        req = json.load(sys.stdin)
    except json.JSONDecodeError as e:
        print(json.dumps({"error": f"Invalid JSON input: {str(e)}"}))
        sys.exit(1)

    question = req.get("question", "")
    kb_id = req.get("kbId", 0)
    user_id = req.get("userId", 0)
    chroma_path = req.get("chromaPath", DEFAULT_CHROMA_PATH)
    api_key = os.environ.get("SILICONFLOW_API_KEY", "")

    if not question:
        print(json.dumps({"error": "question is required"}))
        sys.exit(1)

    if not api_key:
        print(json.dumps({"error": "SILICONFLOW_API_KEY environment variable is not set"}))
        sys.exit(1)

    try:
        # Step 1: Retrieve relevant chunks from ChromaDB
        sources = retrieve_context(question, kb_id, user_id, chroma_path)

        if not sources:
            # No relevant documents found
            print(json.dumps({
                "answer": "抱歉，知识库中没有找到与您问题相关的内容。请尝试上传更多相关文档或调整您的问题。",
                "sources": []
            }))
            sys.exit(0)

        # Step 2: Build context string with citations
        context = build_context_string(sources)

        # Step 3: Generate answer via LLM
        answer = generate_answer(question, context, api_key)

        # Step 4: Format sources for response
        response_sources = []
        for i, source in enumerate(sources, 1):
            response_sources.append({
                "id": str(i),
                "text": source["document"][:200] + "..." if len(source["document"]) > 200 else source["document"],
                "score": round(source["similarity"], 4)
            })

        print(json.dumps({
            "answer": answer,
            "sources": response_sources
        }))

    except chromadb.errors.CollectionNotFoundError:
        print(json.dumps({"error": "知识库为空，请先上传文档"}))
        sys.exit(1)
    except requests.exceptions.RequestException as e:
        print(json.dumps({"error": f"LLM API调用失败: {str(e)}"}))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({"error": f"处理失败: {str(e)}"}))
        sys.exit(1)


if __name__ == "__main__":
    main()
