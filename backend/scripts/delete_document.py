"""
Delete document vectors from ChromaDB.
Reads JSON from stdin, writes JSON to stdout.
"""
import sys
import json

import chromadb

DEFAULT_CHROMA_PATH = "backend/data/chroma"


def main():
    try:
        req = json.load(sys.stdin)
    except json.JSONDecodeError as e:
        print(json.dumps({"error": f"Invalid JSON input: {str(e)}"}))
        sys.exit(1)

    kb_id = req.get("kbId", 0)
    user_id = req.get("userId", 0)
    chroma_path = req.get("chromaPath", DEFAULT_CHROMA_PATH)

    if not kb_id:
        print(json.dumps({"error": "kbId is required"}))
        sys.exit(1)

    if not user_id:
        print(json.dumps({"error": "userId is required"}))
        sys.exit(1)

    collection_name = f"user_{user_id}_kb_{kb_id}"

    try:
        client = chromadb.PersistentClient(path=chroma_path)
        collection = client.get_or_create_collection(name=collection_name)

        # Query existing chunks to get count before deleting
        try:
            existing = collection.get(where={"kbId": str(kb_id)})
            deleted_count = len(existing["ids"])
        except Exception:
            # Collection might be empty or filter returns no results
            deleted_count = 0

        # Delete chunks matching kbId filter
        collection.delete(where={"kbId": str(kb_id)})

        print(json.dumps({"deleted": deleted_count}))

    except Exception as e:
        print(json.dumps({"error": f"Failed to delete vectors from Chroma: {str(e)}"}))
        sys.exit(1)


if __name__ == "__main__":
    main()
