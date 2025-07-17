<!-- <template>
  <div class="growth-center p-6 max-w-5xl mx-auto">
    <el-card class="mb-6 shadow-md">
      <div class="flex items-center gap-4">
        <el-avatar :size="64" :src="user.avatar" />
        <div>
          <h2 class="text-xl font-bold">{{ user.nickname }}</h2>
          <el-tag type="success" effect="dark">等级：Lv.{{ user.level }}</el-tag>
          <div class="mt-2 text-sm text-gray-500">当前成长值：{{ user.growthValue }} / {{ user.nextLevelValue }}</div>
          <el-progress :percentage="progress" class="w-64 mt-1" />
        </div>
      </div>
    </el-card>

    <el-card class="mb-6 shadow-md">
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-lg font-semibold">🎯 今日任务</h3>
      </div>
      <el-table :data="tasks" style="width: 100%">
        <el-table-column prop="description" label="任务内容" />
        <el-table-column label="奖励">
          <template #default="{ row }">
            🌱 {{ row.reward_growth }} / ✨ {{ row.reward_contribution }}
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.completed ? 'success' : 'info'">
              {{ row.completed ? '已完成' : '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="mb-6 shadow-md">
      <h3 class="text-lg font-semibold mb-2">📊 技能雷达图</h3>
      <v-chart :option="radarOption" autoresize style="height: 400px;" />
    </el-card>

    <el-card class="shadow-md">
      <h3 class="text-lg font-semibold mb-2">🕓 成长记录</h3>
      <el-timeline>
        <el-timeline-item
          v-for="log in growthLogs"
          :key="log.id"
          :timestamp="log.created_at"
          placement="top"
        >
          <div>
            {{ log.action_type }}：+{{ log.growth_change }} 成长 / +{{ log.contribution_change }} 贡献
            <div v-if="log.skill_name" class="text-xs text-gray-500">
              技能经验：{{ log.skill_name }} +{{ log.skill_exp }}
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import 'echarts/lib/chart/radar'

const user = ref({
  avatar: 'https://example.com/avatar.jpg',
  nickname: '副业达人',
  level: 3,
  growthValue: 280,
  nextLevelValue: 400
})

const progress = computed(() => (user.value.growthValue / user.value.nextLevelValue) * 100)

const tasks = ref([
  { description: '发布一个项目', reward_growth: 20, reward_contribution: 10, completed: true },
  { description: '参与一次协作', reward_growth: 15, reward_contribution: 10, completed: false },
  { description: '点赞3个项目', reward_growth: 5, reward_contribution: 2, completed: false }
])

const growthLogs = ref([
  { id: 1, action_type: '发布项目', growth_change: 20, contribution_change: 10, skill_name: 'JavaScript', skill_exp: 5, created_at: '2025-07-09 10:00' },
  { id: 2, action_type: '项目被点赞', growth_change: 5, contribution_change: 2, skill_name: '', skill_exp: 0, created_at: '2025-07-08 21:00' }
])

const radarOption = ref({
  tooltip: {},
  radar: {
    indicator: [
      { name: 'JavaScript', max: 100 },
      { name: 'Vue', max: 100 },
      { name: 'Python', max: 100 },
      { name: 'UI设计', max: 100 },
      { name: '产品思维', max: 100 }
    ]
  },
  series: [
    {
      type: 'radar',
      data: [
        {
          value: [80, 70, 50, 60, 75],
          name: '技能等级'
        }
      ]
    }
  ]
})
</script>

<style scoped>
.growth-center {
  background: #f8f9fa;
  border-radius: 12px;
}
</style> -->
