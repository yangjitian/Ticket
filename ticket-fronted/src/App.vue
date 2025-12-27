<script setup lang="ts"></script>

<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  height: 100vh;
}

/* 全局样式 */
.el-button {
  font-weight: 500;
}

.el-dialog {
  --el-dialog-padding-primary: 24px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-table th {
  background-color: #f5f5f5;
  color: #606266;
  font-weight: 500;
}

.el-pagination {
  margin-top: 20px;
}
</style>
