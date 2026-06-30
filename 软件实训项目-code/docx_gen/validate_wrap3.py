import subprocess, sys, os
env = os.environ.copy()
env['PYTHONUTF8'] = '1'
path = r"C:\Users\26025\.codebuddy\plugins\marketplaces\codebuddy-plugins-official\plugins\docx\scripts\office\validate.py"
file = r"d:\企业实训作业\codebuddy\gitcode\软件实训项目-code\软件实训项目-code\求职平台前端改造计划书_V1.0.docx"
result = subprocess.run([sys.executable, path, "-v", file], capture_output=True, text=True, encoding='utf-8', errors='replace', env=env)
print("STDOUT:\n", result.stdout)
print("STDERR:\n", result.stderr)
print("RETURN CODE:", result.returncode)
