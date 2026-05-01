# AI Agent Service - Quick Test

import pytest
from fastapi.testclient import TestClient
from app.main import app


client = TestClient(app)


def test_root_endpoint():
    """Test root endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["service"] == "AI Review Agent"


def test_health_check():
    """Test health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"


def test_review_endpoint():
    """Test review submission endpoint"""
    review_request = {
        "submission_id": 123,
        "work_type": "CODE",
        "file_path": "/uploads/test.zip",
        "work_description": "Test work"
    }

    response = client.post("/api/review", json=review_request)
    assert response.status_code == 200
    assert response.json()["status"] == "processing"


def test_status_endpoint():
    """Test status check endpoint"""
    response = client.get("/api/status/123")
    assert response.status_code == 200
    assert response.json()["submission_id"] == 123


if __name__ == "__main__":
    pytest.main([__file__, "-v"])