<template>
  <div class="calendar-picker">
    <!-- 月份导航 -->
    <div class="calendar-header">
      <button @click="previousMonth" class="nav-btn">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h3 class="month-title">{{ currentMonthTitle }}</h3>
      <button @click="nextMonth" class="nav-btn">
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 星期标题 -->
    <div class="weekdays">
      <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-grid">
      <!-- 空白填充 -->
      <div
        v-for="blank in leadingBlanks"
        :key="'blank-' + blank"
        class="calendar-day blank"
      ></div>

      <!-- 日期格子 -->
      <div
        v-for="day in daysInMonth"
        :key="day"
        :class="getDayClass(day)"
        @click="selectDate(day)"
      >
        <div class="day-number">{{ day }}</div>
        <div v-if="getStockInfo(day)" class="day-stock">
          <span class="stock-qty">余 {{ getStockInfo(day)?.availableQty }}</span>
          <span class="stock-price">￥{{ getStockInfo(day)?.price }}</span>
        </div>
      </div>
    </div>

    <!-- 图例说明 -->
    <div class="calendar-legend">
      <div class="legend-item">
        <div class="legend-color available"></div>
        <span>可预订</span>
      </div>
      <div class="legend-item">
        <div class="legend-color unavailable"></div>
        <span>无库存</span>
      </div>
      <div class="legend-item">
        <div class="legend-color selected"></div>
        <span>已选择</span>
      </div>
      <div class="legend-item">
        <div class="legend-color past"></div>
        <span>已过期</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { CalendarStockInfo } from '@/types/user'

interface Props {
  modelValue: string
  stockData: CalendarStockInfo[]
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'date-select', date: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 当前显示的月份
const currentDate = ref(new Date())

// 星期名称
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 当前月份标题
const currentMonthTitle = computed(() => {
  return `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`
})

// 当月的天数
const daysInMonth = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  return new Date(year, month + 1, 0).getDate()
})

// 每月第一天是星期几
const firstDayOfMonth = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  return new Date(year, month, 1).getDay()
})

// 前导空白天数
const leadingBlanks = computed(() => firstDayOfMonth.value)

// 获取指定日期的库存信息
const getStockInfo = (day: number) => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`

  return props.stockData.find(stock => stock.date === dateStr)
}

// 获取日期格子的样式类
const getDayClass = (day: number) => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`

  const classes = ['calendar-day']
  const stockInfo = getStockInfo(day)
  const isToday = new Date().toDateString() === new Date(dateStr).toDateString()
  const isPast = new Date(dateStr) < new Date(new Date().toDateString())
  const isSelected = props.modelValue === dateStr

  if (isToday) classes.push('today')
  if (isSelected) classes.push('selected')
  if (isPast) {
    classes.push('past')
  } else if (stockInfo?.isAvailable) {
    classes.push('available')
  } else {
    classes.push('unavailable')
  }

  return classes.join(' ')
}

// 上一个月
const previousMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() - 1,
    1
  )
}

// 下一个月
const nextMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() + 1,
    1
  )
}

// 选择日期
const selectDate = (day: number) => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`

  const stockInfo = getStockInfo(day)
  const isPast = new Date(dateStr) < new Date(new Date().toDateString())

  // 只能选择有库存且未过期的日期
  if (!isPast && stockInfo?.isAvailable) {
    emit('update:modelValue', dateStr)
    emit('date-select', dateStr)
  }
}

// 监听modelValue变化，自动切换到对应月份
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    const selectedDate = new Date(newValue)
    currentDate.value = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), 1)
  }
})
</script>

<style scoped>
.calendar-picker {
  width: 100%;
}

/* 日历头部 */
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.nav-btn {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 50%;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: #e8e8e8;
  color: #333;
}

.month-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 星期标题 */
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.weekday {
  padding: 12px 8px;
  text-align: center;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
}

/* 日历网格 */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

/* 日期格子 */
.calendar-day {
  position: relative;
  min-height: 80px;
  padding: 8px;
  border-right: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.calendar-day.blank {
  background: #fafafa;
  cursor: default;
}

.calendar-day.available:hover {
  background: #e6f7ff;
}

.calendar-day.available {
  background: white;
  cursor: pointer;
}

.calendar-day.unavailable {
  background: #fff2f0;
  cursor: not-allowed;
}

.calendar-day.past {
  background: #f5f5f5;
  cursor: not-allowed;
  color: #ccc;
}

.calendar-day.today {
  border: 2px solid #1890ff;
}

.calendar-day.selected {
  background: #1890ff !important;
  color: white;
}

.calendar-day.selected .day-number,
.calendar-day.selected .stock-qty,
.calendar-day.selected .stock-price {
  color: white;
}

/* 日期数字 */
.day-number {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 4px;
}

/* 库存信息 */
.day-stock {
  font-size: 0.75rem;
  line-height: 1.2;
}

.stock-qty {
  display: block;
  color: #52c41a;
  font-weight: 500;
}

.stock-price {
  display: block;
  color: #f5222d;
  font-weight: 500;
}

/* 图例说明 */
.calendar-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 16px;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: #666;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.legend-color.available {
  background: white;
  border: 1px solid #d9d9d9;
}

.legend-color.unavailable {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.legend-color.selected {
  background: #1890ff;
}

.legend-color.past {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .calendar-legend {
    gap: 12px;
  }

  .legend-item {
    font-size: 0.7rem;
  }

  .calendar-day {
    min-height: 70px;
    padding: 6px;
  }

  .day-number {
    font-size: 1rem;
  }

  .day-stock {
    font-size: 0.7rem;
  }
}
</style>
