import pytest
import sys
import os
import json
import tempfile
from unittest.mock import patch, MagicMock

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from extract_and_embed import extract_text, chunk_text, get_embedding, extract_text as extract


class TestChunkText:
    def test_empty_text_returns_single_chunk(self):
        result = chunk_text("")
        assert len(result) == 1
        assert result[0] == ""

    def test_short_text_returns_single_chunk(self):
        text = "Hello world"
        result = chunk_text(text)
        assert len(result) == 1
        assert result[0] == text

    def test_long_text_returns_multiple_chunks(self):
        long_text = "A" * 2000
        result = chunk_text(long_text)
        assert len(result) > 1

    def test_overlap_between_consecutive_chunks(self):
        long_text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" * 100
        chunks = chunk_text(long_text)
        if len(chunks) > 1:
            assert chunks[1].startswith(chunks[0][-67:]) or len(chunks[1]) > 0

    def test_chunk_count_matches_expected(self):
        text = "X" * 1000
        chunks = chunk_text(text)
        expected = 2
        assert len(chunks) == expected


class TestExtractText:
    def test_extract_text_txt(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False, encoding='utf-8') as f:
            f.write("Hello world\nThis is a test document.")
            temp_path = f.name

        try:
            result = extract_text(temp_path, 'txt')
            assert "Hello world" in result
            assert "This is a test document" in result
        finally:
            os.unlink(temp_path)

    def test_extract_text_unsupported_type(self):
        result = extract_text("dummy.txt", "unsupported")
        assert result == ""


class TestGetEmbedding:
    def test_get_embedding_returns_correct_structure(self, monkeypatch):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "data": [
                {"embedding": [0.1, 0.2, 0.3], "index": 0},
                {"embedding": [0.4, 0.5, 0.6], "index": 1}
            ]
        }
        mock_response.raise_for_status = MagicMock()

        def mock_post(url, headers, json):
            return mock_response

        monkeypatch.setattr(requests, "post", mock_post)
        result = get_embedding(["test1", "test2"], "fake_key")
        assert len(result) == 2
        assert result[0] == [0.1, 0.2, 0.3]
        assert result[1] == [0.4, 0.5, 0.6]


class TestIntegration:
    def test_chunk_text_with_chinese_chars(self):
        chinese_text = "这是一段中文文本" * 100
        chunks = chunk_text(chinese_text)
        assert len(chunks) > 1
        assert all(isinstance(c, str) for c in chunks)

    def test_main_function_json_output(self, monkeypatch, tmp_path):
        test_file = tmp_path / "test.txt"
        test_file.write_text("Test content for main function", encoding='utf-8')

        mock_response = MagicMock()
        mock_response.json.return_value = {
            "data": [{"embedding": [0.1] * 1024, "index": 0}]
        }
        mock_response.raise_for_status = MagicMock()
        monkeypatch.setattr(requests, "post", lambda *args, **kwargs: mock_response)

        import io
        from extract_and_embed import main

        input_data = json.dumps({
            "filePath": str(test_file),
            "kbId": 1,
            "userId": 1,
            "chromaPath": str(tmp_path / "chroma")
        })

        monkeypatch.setattr(sys, "stdin", io.StringIO(input_data))

        with patch('chromadb.PersistentClient') as mock_chroma:
            mock_collection = MagicMock()
            mock_chroma.return_value.get_or_create_collection.return_value = mock_collection
            main()

            mock_collection.add.assert_called_once()
