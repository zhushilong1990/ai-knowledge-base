import sys
import json
import os
import uuid

import chromadb
from pypdf import PdfReader
from docx import Document
import requests

SILICONFLOW_API = "https://api.siliconflow.cn/v1/embeddings"
MODEL = "BAAI/bge-m3"
CHROMA_PATH = "backend/data/chroma"


def extract_text(file_path, ext):
    if ext == "pdf":
        reader = PdfReader(file_path)
        return "\n\n".join(p.extract_text() or "" for p in reader.pages)
    elif ext == "docx":
        doc = Document(file_path)
        return "\n\n".join(p.text for p in doc.paragraphs if p.text.strip())
    elif ext == "txt":
        with open(file_path, encoding="utf-8") as f:
            return f.read()
    return ""


def chunk_text(text, chunk_size=500, overlap=50):
    char_size = int(chunk_size / 0.75)
    overlap_chars = int(overlap / 0.75)
    chunks = []
    start = 0
    while start < len(text):
        end = min(start + char_size, len(text))
        chunks.append(text[start:end])
        start += char_size - overlap_chars
    return chunks


def get_embedding(texts, api_key):
    resp = requests.post(
        SILICONFLOW_API,
        headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
        json={"model": MODEL, "input": texts, "encoding_format": "float"}
    )
    resp.raise_for_status()
    data = resp.json()["data"]
    return [item["embedding"] for item in sorted(data, key=lambda x: x["index"])]


def main():
    try:
        req = json.load(sys.stdin)
    except json.JSONDecodeError as e:
        print(json.dumps({"error": f"Invalid JSON input: {str(e)}"}))
        sys.exit(1)

    file_path = req.get("filePath", "")
    kb_id = req.get("kbId", 0)
    user_id = req.get("userId", 0)
    chroma_path = req.get("chromaPath", CHROMA_PATH)
    api_key = os.environ.get("SILICONFLOW_API_KEY", "")

    if not file_path:
        print(json.dumps({"error": "filePath is required"}))
        sys.exit(1)

    ext = os.path.splitext(file_path)[1].lstrip(".").lower()

    try:
        text = extract_text(file_path, ext)
    except Exception as e:
        print(json.dumps({"error": f"Text extraction failed: {str(e)}"}))
        sys.exit(1)

    if not text.strip():
        print(json.dumps({"error": "No text could be extracted from the file"}))
        sys.exit(1)

    chunks = chunk_text(text)

    embeddings = []
    batch_size = 10
    for i in range(0, len(chunks), batch_size):
        batch = chunks[i:i + batch_size]
        try:
            batch_embeddings = get_embedding(batch, api_key)
            embeddings.extend(batch_embeddings)
        except Exception as e:
            print(json.dumps({"error": f"Embedding generation failed: {str(e)}"}))
            sys.exit(1)

    doc_id = str(uuid.uuid4())
    collection_name = f"user_{user_id}_kb_{kb_id}"

    try:
        client = chromadb.PersistentClient(path=chroma_path)
        collection = client.get_or_create_collection(name=collection_name)
        collection.add(
            ids=[f"{doc_id}_chunk_{j}" for j in range(len(chunks))],
            embeddings=embeddings,
            documents=chunks,
            metadatas=[{"docId": doc_id, "kbId": str(kb_id), "chunkIndex": j} for j in range(len(chunks))]
        )
    except Exception as e:
        print(json.dumps({"error": f"Chroma indexing failed: {str(e)}"}))
        sys.exit(1)

    print(json.dumps({
        "documentId": doc_id,
        "chunkCount": len(chunks),
        "status": "completed"
    }))


if __name__ == "__main__":
    main()
