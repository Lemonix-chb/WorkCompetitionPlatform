# Test DeepSeek API Connection
import httpx
import json

API_KEY = "your-deepseek-api-key"
API_URL = "https://api.deepseek.com/v1/chat/completions"

def test_deepseek_connection():
    """测试DeepSeek API连接"""
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "deepseek-chat",
        "messages": [
            {"role": "system", "content": "你是一位专业的竞赛作品评审专家。"},
            {"role": "user", "content": "请简单回复：API测试成功"}
        ],
        "temperature": 0.7,
        "max_tokens": 100
    }

    try:
        print("正在测试DeepSeek API连接...")
        with httpx.Client(timeout=10.0) as client:
            response = client.post(API_URL, headers=headers, json=payload)

            if response.status_code == 200:
                data = response.json()
                content = data["choices"][0]["message"]["content"]
                print(f"[OK] API调用成功！")
                print(f"模型响应: {content}")
                print(f"使用模型: {data['model']}")
                return True
            else:
                print(f"[ERROR] API调用失败: HTTP {response.status_code}")
                print(f"错误信息: {response.text}")
                return False

    except Exception as e:
        print(f"[ERROR] 连接错误: {e}")
        return False

if __name__ == "__main__":
    success = test_deepseek_connection()
    if success:
        print("\n[SUCCESS] DeepSeek API配置正确，可以正常使用！")
    else:
        print("\n[WARNING] API配置可能有问题，请检查网络或API Key")