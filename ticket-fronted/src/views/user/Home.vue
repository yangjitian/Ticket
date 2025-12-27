<template>
  <div class="home-page">
    <div style="padding: 50px; text-align: center; background: #f0f8ff; min-height: 100vh;">
      <h1 style="color: #1890ff; font-size: 2rem; margin-bottom: 20px;">旅游分销平台首页</h1>
      <p style="color: #666; font-size: 1.2rem; margin-bottom: 30px;">欢迎来到我们的旅游分销平台！</p>

      <div style="max-width: 600px; margin: 0 auto; margin-bottom: 40px;">
        <el-input
          v-model="searchQuery"
          placeholder="搜索景区..."
          size="large"
          style="width: 100%;"
        />
        <el-button type="primary" size="large" style="margin-top: 20px; width: 200px;">
          搜索
        </el-button>
      </div>

      <div style="max-width: 1200px; margin: 0 auto;">
        <h2 style="color: #333; margin-bottom: 30px;">热门推荐</h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px;">
          <div
            v-for="spot in recommendedSpots"
            :key="spot.id"
            style="background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);"
          >
            <h3 style="color: #333; margin-bottom: 10px;">{{ spot.spotName }}</h3>
            <p style="color: #666; margin-bottom: 15px;">{{ spot.description }}</p>
            <p style="color: #999; font-size: 0.9rem;">{{ spot.address }}</p>
            <div style="margin-top: 15px; color: #f5222d; font-weight: bold;">￥80 起</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 搜索相关
const searchQuery = ref('')

// 推荐景区
const recommendedSpots = ref([
  {
    id: '1',
    spotName: '故宫博物院',
    description: '世界五大宫之一，拥有世界上最大的宫殿建筑群，收藏有众多珍贵文物。',
    address: '北京市东城区景山前街4号'
  },
  {
    id: '2',
    spotName: '长城',
    description: '长城是中国古代伟大的防御工程，被誉为世界七大奇迹之一。',
    address: '北京市延庆区八达岭长城'
  },
  {
    id: '3',
    spotName: '西湖',
    description: '西湖被誉为人间天堂，以其秀丽的湖光山色闻名于世。',
    address: '浙江省杭州市西湖区'
  }
])

onMounted(() => {
  console.log('Home component mounted')
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}
</style>
