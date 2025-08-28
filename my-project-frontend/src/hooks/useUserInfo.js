import { reactive } from 'vue'
import { get } from '../net'

const state = reactive({
  data: JSON.parse(localStorage.getItem('userInfo')) || {},
  loading: false
})

const loadUserInfo = async () => {
  state.loading = true
  try {
    const res = await get('/api/auth/user/getCurrentUserInfo')
    // console.log("loadUserInfo",res)
    if(res) {
      state.data = res
      localStorage.setItem('userInfo', JSON.stringify(res))
    } else {
      // console.log("loadUserInfo 未登录")
    }
  } finally {
    state.loading = false
  }
}

export default function useUserInfo() {
  return { state, loadUserInfo }
}
