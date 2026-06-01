<template>
  <div class="zone-canvas" ref="containerRef" :style="{ width: width + 'px', height: height + 'px' }">
    <canvas
      ref="canvasRef"
      :width="width"
      :height="height"
      :style="{ cursor: interactive ? 'pointer' : 'default' }"
      @click="handleClick"
      @mousemove="handleMouseMove"
      @mouseleave="hoveredZone = null"
    ></canvas>
    <div v-if="hoveredZone && tooltip" class="canvas-tooltip" :style="tooltipStyle">
      <div class="tooltip-title">{{ hoveredZone.name }}</div>
      <div v-for="(val, key) in hoveredZone.stats" :key="key" class="tooltip-row">
        <span class="tooltip-label">{{ statLabels[key] || key }}:</span>
        <span class="tooltip-value">{{ val }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'

const props = defineProps({
  /** 区域列表: [{ id, name, posX, posY, width, height, visitorCount, avgStayMin, ... }] */
  zones: { type: Array, default: () => [] },
  /** 画布宽度 */
  width: { type: Number, default: 700 },
  /** 画布高度 */
  height: { type: Number, default: 500 },
  /** 是否可交互（点击选择区域） */
  interactive: { type: Boolean, default: true },
  /** 是否显示热力 */
  showHeat: { type: Boolean, default: true },
  /** 热力指标字段名 */
  heatKey: { type: String, default: 'visitorCount' },
  /** 统计信息标签映射 */
  statLabels: { type: Object, default: () => ({}) },
  /** 坐标范围 */
  maxX: { type: Number, default: 100 },
  maxY: { type: Number, default: 100 }
})

const emit = defineEmits(['zone-click', 'zone-hover'])

const containerRef = ref(null)
const canvasRef = ref(null)
const hoveredZone = ref(null)
const tooltip = ref({ x: 0, y: 0 })

const tooltipStyle = computed(() => ({
  left: tooltip.value.x + 12 + 'px',
  top: tooltip.value.y - 10 + 'px'
}))

// 坐标转换：逻辑坐标 → 画布像素
function toPixelX(logicX) {
  return (logicX / props.maxX) * props.width
}
function toPixelY(logicY) {
  return (logicY / props.maxY) * props.height
}

function draw() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = props.width
  const h = props.height

  // 清空
  ctx.clearRect(0, 0, w, h)

  // 背景网格
  ctx.strokeStyle = '#e8e8e8'
  ctx.lineWidth = 0.5
  const gridSize = 50
  for (let x = 0; x <= w; x += gridSize) {
    ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, h); ctx.stroke()
  }
  for (let y = 0; y <= h; y += gridSize) {
    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke()
  }

  // 边框
  ctx.strokeStyle = '#d0d0d0'
  ctx.lineWidth = 2
  ctx.strokeRect(0, 0, w, h)

  if (!props.zones || props.zones.length === 0) return

  // 计算最大热力值
  let maxHeat = 1
  if (props.showHeat) {
    const heats = props.zones.map(z => z[props.heatKey] || 0)
    maxHeat = Math.max(...heats, 1)
  }

  // 绘制每个区域
  props.zones.forEach(zone => {
    const x = toPixelX(zone.posX || 0)
    const y = toPixelY(zone.posY || 0)
    const zw = toPixelX(zone.width || 10)
    const zh = toPixelY(zone.height || 10)

    // 热力颜色
    if (props.showHeat) {
      const heatVal = zone[props.heatKey] || 0
      const ratio = maxHeat > 0 ? heatVal / maxHeat : 0
      // 蓝→绿→黄→红 渐变
      const r = Math.floor(Math.min(255, ratio * 2 * 255))
      const g = Math.floor(Math.min(255, (1 - Math.abs(ratio - 0.5) * 2) * 255))
      const b = Math.floor(Math.max(0, (1 - ratio) * 2 * 255))
      const alpha = 0.15 + ratio * 0.55
      ctx.fillStyle = `rgba(${r},${g},${b},${alpha})`
    } else {
      ctx.fillStyle = 'rgba(64,158,255,0.1)'
    }
    ctx.fillRect(x, y, zw, zh)

    // 边框
    const isHovered = hoveredZone.value && hoveredZone.value.id === zone.id
    ctx.strokeStyle = isHovered ? '#409EFF' : '#909399'
    ctx.lineWidth = isHovered ? 2.5 : 1.5
    ctx.strokeRect(x, y, zw, zh)

    // 区域名称
    ctx.fillStyle = '#303133'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    const cx = x + zw / 2
    const cy = y + zh / 2
    ctx.fillText(zone.name || '', cx, cy)

    // 热力数值
    if (props.showHeat) {
      const heatVal = zone[props.heatKey] || 0
      ctx.fillStyle = '#606266'
      ctx.font = '10px sans-serif'
      ctx.fillText(String(heatVal), cx, cy + 16)
    }
  })
}

function getZoneAt(mouseX, mouseY) {
  if (!props.zones) return null
  for (const zone of props.zones) {
    const x = toPixelX(zone.posX || 0)
    const y = toPixelY(zone.posY || 0)
    const zw = toPixelX(zone.width || 10)
    const zh = toPixelY(zone.height || 10)
    if (mouseX >= x && mouseX <= x + zw && mouseY >= y && mouseY <= y + zh) {
      return zone
    }
  }
  return null
}

function handleClick(e) {
  if (!props.interactive) return
  const rect = canvasRef.value.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  const zone = getZoneAt(mx, my)
  if (zone) {
    emit('zone-click', zone)
  }
}

function handleMouseMove(e) {
  const rect = canvasRef.value.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  const zone = getZoneAt(mx, my)

  if (zone) {
    hoveredZone.value = zone
    tooltip.value = { x: mx, y: my }
    emit('zone-hover', zone)
  } else {
    hoveredZone.value = null
  }
}

watch(() => [props.zones, props.width, props.height, props.heatKey], () => {
  draw()
}, { deep: true })

onMounted(() => {
  draw()
})
</script>

<style scoped>
.zone-canvas {
  position: relative;
  display: inline-block;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.canvas-tooltip {
  position: absolute;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  pointer-events: none;
  z-index: 100;
  white-space: nowrap;
}

.tooltip-title {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 4px;
  color: #409EFF;
}

.tooltip-row {
  display: flex;
  gap: 8px;
}

.tooltip-label {
  color: #c0c4cc;
}

.tooltip-value {
  color: #fff;
  font-weight: 500;
}
</style>
