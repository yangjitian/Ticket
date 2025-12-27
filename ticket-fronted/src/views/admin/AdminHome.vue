<template>
  <div class="admin-home">
    <h2>管理员控制台</h2>

    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">0</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Location /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">0</div>
            <div class="stat-label">景区数量</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Shop /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">0</div>
            <div class="stat-label">经销商数量</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="32"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">0</div>
            <div class="stat-label">门票库存</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="welcome-message">
      <el-card>
        <h3>欢迎使用景点门票分销管理系统</h3>
        <p>请从左侧菜单选择要管理的功能模块。</p>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Location, Shop, Tickets } from '@element-plus/icons-vue'
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

.admin-home h2 {
  margin-bottom: 30px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.welcome-message {
  margin-top: 30px;
}

.welcome-message h3 {
  margin-bottom: 10px;
  color: #333;
}

.welcome-message p {
  color: #666;
  line-height: 1.6;
}
</style>
