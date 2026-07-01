"""
DeepSeek API 意图分类功能测试脚本
直接调用 DeepSeek API (deepseek-chat) 测试意图分类器的准确性
测试用例来源: intent-classifier-manual-test-cases.md (20个) + 补充用例 (13个)
"""
import json
import requests
import time
import sys
import os
from datetime import datetime

# ============ 配置 ============
API_URL = 'https://api.deepseek.com/v1/chat/completions'
API_KEY = 'sk-5872d52527b9434b9d822754c43078ba'
MODEL = 'deepseek-chat'
TIMEOUT = 30
MAX_TOKENS = 100
TEMPERATURE = 0.0  # 意图分类用 0 温度确保结果稳定

# 意图分类 system prompt (与后端 career_intent_classifier.txt 一致)
SYSTEM_PROMPT = """你是一位意图分类专家。请严格判断以下用户问题的意图，只输出 JSON。

## 意图类别

### RECOMMENDATION（职业方向推荐 / 个性化方案）
用户希望系统根据其个人情况推荐或判断适合的职业方向、岗位，或基于个人画像输出定制化方案。核心特征是"帮我选/帮我判断/帮我规划"。

典型表达：
- "我适合做什么"
- "我适合做前端还是后端"
- "给我推荐几个岗位"
- "最合适的职业方向"
- "基于我的画像推荐职业"
- "我应该选哪个方向"
- "帮我分析一下职业方向"
- "前端和后端我该选哪个"（要求为"我"做选择）
- "有什么推荐学习方案"（基于个人情况的学习计划）
- "我该怎么学"（针对个人的学习路径）
- "针对我的情况制定学习计划"
- "AI应用开发适合我吗"（判断个人适配度）
- "我想做Java后端" / "我想从事前端开发"（明确表达个人职业目标）
- "我对数据分析感兴趣"（明确表达个人兴趣方向）

### GENERAL（通用行业咨询 / 自由问答）
用户询问行业知识、趋势、学习建议等通用信息，不要求系统针对其个人情况做推荐。核心特征是"问知识/问趋势/闲聊"。

典型表达：
- "互联网发展趋势"
- "Java 需要学什么"（通用知识，非个人方案）
- "面试怎么准备"
- "某岗位薪资如何"
- "前端和后端哪个好"（询问行业看法，非个人推荐）
- "如何转行做产品经理"
- "今天星期几"（与职业完全无关的日常问题）

## 判断规则
1. 如果问题中包含"我适合""帮我推荐""帮我选""我该选""适合我吗""我想做""我想从事""我对...感兴趣"等针对个人的推荐请求 → RECOMMENDATION
2. 如果问题涉及个人学习路径、技能提升方案，且用户明确指向自身情况（如"我该怎么学""给我学习方案"） → RECOMMENDATION
3. 如果问题是询问行业趋势、知识点、薪资等通用信息 → GENERAL
4. 如果问题与职业规划完全无关（如日期、天气、闲聊） → GENERAL
5. 模糊时优先判断为 RECOMMENDATION

## 输入

用户问题：{question}

## 输出要求

请只输出以下 JSON 格式，不要包含任何解释、注释或 markdown 代码块：

{{"intent": "RECOMMENDATION"}}
或
{{"intent": "GENERAL"}}"""

# ============ 测试用例 ============
# A类: 伪装成闲聊的职业问题 (7个)
# B类: 含"岗位"字眼但实为技术探讨 (6个)
# C类: 纯粹闲聊 (7个)
# D类: 补充边界用例 (10个)
# E类: 回归测试 (3个)
TEST_CASES = [
    # A类: 伪装成闲聊的职业问题
    {"id": "A1", "question": "嗨，能聊聊后端开发那些事儿吗", "expected": "GENERAL", "category": "A-伪装闲聊"},
    {"id": "A2", "question": "随便问问，Java工程师平时都干啥", "expected": "GENERAL", "category": "A-伪装闲聊"},
    {"id": "A3", "question": "好奇问下，做前端有前途吗", "expected": "GENERAL", "category": "A-伪装闲聊"},
    {"id": "A4", "question": "闲聊一下，你觉得我搞数据分析怎么样", "expected": "RECOMMENDATION", "category": "A-伪装闲聊"},
    {"id": "A5", "question": "打发时间，聊聊我适合产品还是技术", "expected": "RECOMMENDATION", "category": "A-伪装闲聊"},
    {"id": "A6", "question": "无聊问问，帮我看看我做运维行不行", "expected": "RECOMMENDATION", "category": "A-伪装闲聊"},
    {"id": "A7", "question": "顺嘴问一句，我是学计算机的能做什么", "expected": "RECOMMENDATION", "category": "A-伪装闲聊"},
    # B类: 含岗位字眼但实为技术探讨
    {"id": "B1", "question": "Java后端岗位需要掌握哪些技术栈", "expected": "GENERAL", "category": "B-含岗位字眼"},
    {"id": "B2", "question": "前端岗位的React和Vue哪个更值得学", "expected": "GENERAL", "category": "B-含岗位字眼"},
    {"id": "B3", "question": "算法岗位平时用Python多还是C++多", "expected": "GENERAL", "category": "B-含岗位字眼"},
    {"id": "B4", "question": "测试岗位的自动化测试框架怎么选", "expected": "GENERAL", "category": "B-含岗位字眼"},
    {"id": "B5", "question": "DevOps岗位的CI/CD一般用什么工具", "expected": "GENERAL", "category": "B-含岗位字眼"},
    {"id": "B6", "question": "后端岗位的微服务架构怎么拆分合理", "expected": "GENERAL", "category": "B-含岗位字眼"},
    # C类: 纯粹闲聊
    {"id": "C1", "question": "你吃了吗", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C2", "question": "今天几号", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C3", "question": "明天天气怎么样", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C4", "question": "讲个笑话听听", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C5", "question": "你会唱歌吗", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C6", "question": "你喜欢什么颜色", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    {"id": "C7", "question": "讲个故事吧", "expected": "GENERAL", "category": "C-纯粹闲聊"},
    # D类: 补充边界与复合兴趣用例
    {"id": "D1", "question": "推荐适合我的方向", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    {"id": "D2", "question": "我想做AI算法方向", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    {"id": "D3", "question": "产品经理的日常工作是什么", "expected": "GENERAL", "category": "D-补充边界"},
    {"id": "D4", "question": "帮我推荐Java后端相关的方向", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    {"id": "D5", "question": "后端开发和前端开发哪个前景更好", "expected": "GENERAL", "category": "D-补充边界"},
    {"id": "D6", "question": "我适合做数据分析还是算法", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    {"id": "D7", "question": "Java后端开发面试应该怎么准备", "expected": "GENERAL", "category": "D-补充边界"},
    {"id": "D8", "question": "推荐匹配度高于80的岗位", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    {"id": "D9", "question": "请介绍一下测试工程师这个岗位", "expected": "GENERAL", "category": "D-补充边界"},
    {"id": "D10", "question": "我想从事云计算相关的工作，有什么建议", "expected": "RECOMMENDATION", "category": "D-补充边界"},
    # E类: 回归测试
    {"id": "E1", "question": "推荐匹配度高于80的岗位", "expected": "RECOMMENDATION", "category": "E-回归测试"},
    {"id": "E2", "question": "推荐Java相关的方向", "expected": "RECOMMENDATION", "category": "E-回归测试"},
    {"id": "E3", "question": "推荐适合我的方向", "expected": "RECOMMENDATION", "category": "E-回归测试"},
]


def call_deepseek_api(question):
    """
    调用 DeepSeek API 进行意图分类
    
    Args:
        question: 用户问题文本
        
    Returns:
        dict: 包含 intent(意图), raw_response(原始响应), latency_ms(耗时), error(错误信息)
    """
    user_prompt = SYSTEM_PROMPT.replace("{question}", question)
    
    headers = {
        'Authorization': f'Bearer {API_KEY}',
        'Content-Type': 'application/json; charset=utf-8'
    }
    
    payload = {
        'model': MODEL,
        'messages': [
            {'role': 'system', 'content': '你是意图分类专家，只输出JSON。'},
            {'role': 'user', 'content': user_prompt}
        ],
        'temperature': TEMPERATURE,
        'max_tokens': MAX_TOKENS,
        'stream': False
    }
    
    start = time.time()
    try:
        resp = requests.post(API_URL, headers=headers, json=payload, timeout=TIMEOUT)
        latency = int((time.time() - start) * 1000)
        
        if resp.status_code != 200:
            return {
                'intent': None,
                'raw_response': resp.text[:500],
                'latency_ms': latency,
                'error': f'HTTP {resp.status_code}',
                'status_code': resp.status_code
            }
        
        data = resp.json()
        content = data.get('choices', [{}])[0].get('message', {}).get('content', '').strip()
        
        # 解析 JSON 响应 (去除可能的 markdown 代码块标记)
        clean = content
        if clean.startswith('```'):
            clean = clean.split('\n', 1)[-1] if '\n' in clean else clean[3:]
        if clean.endswith('```'):
            clean = clean[:-3]
        clean = clean.strip()
        
        try:
            parsed = json.loads(clean)
            intent = parsed.get('intent', '').upper().strip()
        except json.JSONDecodeError:
            # 尝试正则提取
            import re
            m = re.search(r'RECOMMENDATION|GENERAL', content, re.IGNORECASE)
            intent = m.group(0).upper() if m else 'PARSE_FAIL'
        
        usage = data.get('usage', {})
        
        return {
            'intent': intent,
            'raw_response': content[:200],
            'latency_ms': latency,
            'error': None,
            'status_code': 200,
            'prompt_tokens': usage.get('prompt_tokens'),
            'completion_tokens': usage.get('completion_tokens'),
            'total_tokens': usage.get('total_tokens')
        }
    except requests.exceptions.Timeout:
        return {'intent': None, 'raw_response': '', 'latency_ms': int((time.time()-start)*1000), 'error': 'TIMEOUT', 'status_code': 0}
    except Exception as e:
        return {'intent': None, 'raw_response': '', 'latency_ms': int((time.time()-start)*1000), 'error': str(e), 'status_code': 0}


def main():
    """
    主测试函数: 遍历所有测试用例,调用 DeepSeek API,记录结果并输出报告
    """
    print('=' * 80)
    print('DeepSeek API 意图分类功能测试')
    print(f'API: {API_URL}')
    print(f'Model: {MODEL}')
    print(f'API Key: {API_KEY[:8]}...{API_KEY[-4:]}')
    print(f'用例数: {len(TEST_CASES)}')
    print(f'开始时间: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}')
    print('=' * 80)
    
    # 连通性预检
    print('\n[预检] 测试 API 连通性...')
    pre = call_deepseek_api('我适合做什么工作')
    if pre.get('error'):
        print(f'[预检] 失败: {pre["error"]} - {pre.get("raw_response", "")}')
        print('[预检] API 不可用,终止测试。')
        sys.exit(1)
    print(f'[预检] 成功 | intent={pre["intent"]} | latency={pre["latency_ms"]}ms | tokens={pre.get("total_tokens")}')
    print()
    
    results = []
    pass_count = 0
    fail_count = 0
    error_count = 0
    
    for case in TEST_CASES:
        q = case['question']
        expected = case['expected']
        
        result = call_deepseek_api(q)
        actual = result.get('intent')
        error = result.get('error')
        
        if error:
            status = 'ERROR'
            error_count += 1
        elif actual == expected:
            status = 'PASS'
            pass_count += 1
        else:
            status = 'FAIL'
            fail_count += 1
        
        record = {
            'id': case['id'],
            'category': case['category'],
            'question': q,
            'expected': expected,
            'actual': actual,
            'status': status,
            'latency_ms': result.get('latency_ms'),
            'raw_response': result.get('raw_response'),
            'error': error,
            'tokens': result.get('total_tokens')
        }
        results.append(record)
        
        # 控制台输出
        marker = '✓' if status == 'PASS' else ('✗' if status == 'FAIL' else '!')
        print(f'  [{case["id"]}] {marker} {status:5} | 预期:{expected:14} 实际:{str(actual):14} | {result.get("latency_ms",0):5}ms | {q[:30]}')
        
        if status != 'PASS':
            print(f'         原始响应: {result.get("raw_response", "")[:100]}')
            if error:
                print(f'         错误: {error}')
        
        time.sleep(0.3)  # 避免 API 限流
    
    # 汇总统计
    total = len(results)
    print('\n' + '=' * 80)
    print('测试结果汇总')
    print('=' * 80)
    print(f'  总用例: {total}')
    print(f'  通过:   {pass_count}')
    print(f'  失败:   {fail_count}')
    print(f'  错误:   {error_count}')
    print(f'  通过率: {pass_count/total*100:.1f}%')
    
    # 按类别统计
    print('\n按类别统计:')
    categories = {}
    for r in results:
        cat = r['category']
        if cat not in categories:
            categories[cat] = {'total': 0, 'pass': 0}
        categories[cat]['total'] += 1
        if r['status'] == 'PASS':
            categories[cat]['pass'] += 1
    
    for cat, stats in sorted(categories.items()):
        rate = stats['pass']/stats['total']*100
        print(f'  {cat:20} {stats["pass"]}/{stats["total"]} ({rate:.0f}%)')
    
    # 延迟统计
    latencies = [r['latency_ms'] for r in results if r.get('latency_ms') and r['status'] != 'ERROR']
    if latencies:
        print(f'\n延迟统计 (ms):')
        print(f'  平均: {sum(latencies)/len(latencies):.0f}')
        print(f'  最小: {min(latencies)}')
        print(f'  最大: {max(latencies)}')
    
    # 失败用例详情
    fails = [r for r in results if r['status'] != 'PASS']
    if fails:
        print(f'\n失败/错误用例详情 ({len(fails)}个):')
        for r in fails:
            print(f'  [{r["id"]}] {r["question"]}')
            print(f'         预期: {r["expected"]} | 实际: {r["actual"]} | 错误: {r.get("error","-")}')
    
    # 保存 JSON 结果
    out_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'deepseek_api_intent_test_results.json')
    report = {
        'test_time': datetime.now().isoformat(),
        'api_url': API_URL,
        'model': MODEL,
        'api_key_masked': f'{API_KEY[:8]}...{API_KEY[-4:]}',
        'summary': {
            'total': total,
            'pass': pass_count,
            'fail': fail_count,
            'error': error_count,
            'pass_rate': f'{pass_count/total*100:.1f}%'
        },
        'by_category': {cat: stats for cat, stats in categories.items()},
        'latency': {
            'avg_ms': round(sum(latencies)/len(latencies)) if latencies else 0,
            'min_ms': min(latencies) if latencies else 0,
            'max_ms': max(latencies) if latencies else 0
        },
        'results': results
    }
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(f'\n结果已保存: {out_path}')


if __name__ == '__main__':
    main()
