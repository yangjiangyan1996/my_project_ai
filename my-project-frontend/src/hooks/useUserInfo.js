import { reactive } from 'vue'
import { get } from '../net'

const state = reactive({
  data: JSON.parse(localStorage.getItem('userInfo')) || {},
  loading: false
})

const loadUserInfo = async () => {
  console.log("开始全局加载完成userinfo",state)
  state.loading = true
  try {
    const res = await get('/api/auth/user/getCurrentUserInfo')
    state.data = res
    localStorage.setItem('userInfo', JSON.stringify(res))
    console.log("已经全局加载完成userinfo",state)
  } finally {
    state.loading = false
  }
}

export default function useUserInfo() {
  return { state, loadUserInfo }
}
