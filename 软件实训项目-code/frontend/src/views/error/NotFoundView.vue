<template>
  <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;">
    <h1 style="font-size:72px;color:#dcdfe6;">404</h1>
    <p style="font-size:18px;color:#909399;margin:16px 0;">页面未找到</p>
    <el-button type="primary" @click="$router.push('/student/home')">返回首页</el-button>
  </div>
</template>
