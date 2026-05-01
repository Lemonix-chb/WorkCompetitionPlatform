#!/usr/bin/env python3
"""重新触发AI审核"""
import requests
import json

url = "http://localhost:8001/api/review"
data = {
    "submission_id": 3,
    "work_type": "PPT",
    "file_path": r"E:/JavaProject/WorkCompetitionPlatform/uploads/works/2026/2026年湖南农业大学计算机科学竞赛/PPT/WORK-20260501-D9B69153/WORK-20260501-D9B69153_TEAM-075B95D7_v1_DOCUMENT_20260501181130.pptx",
    "work_description": "作品456"
}

print(f"发送审核请求...")
print(f"数据: {json.dumps(data, ensure_ascii=False, indent=2)}")

try:
    response = requests.post(url, json=data, timeout=5)
    print(f"\n响应状态码: {response.status_code}")
    print(f"响应内容: {response.text}")

    if response.status_code == 200:
        print("\n✓ 审核请求成功，等待Python Agent处理...")
    else:
        print(f"\n✗ 请求失败")

except Exception as e:
    print(f"\n✗ 发送失败: {e}")