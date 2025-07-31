import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";

const authItemName = "authorize";

const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

const defaultError = (error) => {
    console.log(error);
    const status = error.response?.status;
    if (status === 429) {
        ElMessage.error(error.response.data.message);
    } else {
        //ElMessage.error('发生了一些错误，请联系管理员');
    }
}

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`);
    ElMessage.warning(message);
}

function takeAccessToken() {
    if (typeof window === 'undefined') return null;
    let result = null;

    try {
        // 优先检查 localStorage
        const localTokenStr = localStorage.getItem(authItemName);
        if (localTokenStr) {
            const localToken = JSON.parse(localTokenStr);
            const expireTime = new Date(localToken?.expire).getTime();
            if (expireTime > Date.now()) {
                result = localToken.token;
                // 同步到 sessionStorage 以便当前会话快速访问
                sessionStorage.setItem(authItemName, localTokenStr);
                return result;
            }
        }

        // 其次检查 sessionStorage
        const sessionTokenStr = sessionStorage.getItem(authItemName);
        if (sessionTokenStr) {
            const sessionTokenObj = JSON.parse(sessionTokenStr);
            const expireTime = new Date(sessionTokenObj?.expire).getTime();
            if (expireTime > Date.now()) {
                result = sessionTokenObj.token;
            }
        }
    } catch (e) {
        console.error("读取 token 时出错", e);
        // 如果解析出错，清除无效的 token
        deleteAccessToken();
    }

    return result;
}

function storeAccessToken(remember, token, expire) {
    const authObj = {
        token: token,
        expire: expire,
        time: new Date().getTime()
    };
    const str = JSON.stringify(authObj);
    
    // 总是存储到 localStorage
    localStorage.setItem(authItemName, str);
    
    // 如果用户选择"记住我"，token 会持久化在 localStorage
    // 否则只在当前会话有效（通过 sessionStorage）
    if (remember) {
        localStorage.setItem(authItemName, str);
        sessionStorage.setItem(authItemName, str);
    } else {
        sessionStorage.setItem(authItemName, str);
    }
}

function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
    if (redirect) {
         console.log("去登录1")
        router.push({ name: 'welcome-login' });
    }
}

function internalPost(url, data, headers, success = () => {}, failure = defaultFailure, error = defaultError) {
    return axios.post(url, data, { headers: headers }).then(({ data: responseData }) => {
        console.log("====internalPost",url)

        if (responseData.code === 200) {
            success(responseData.data);
            return responseData.data;
        } else if (responseData.code === 401 && !url.startsWith('/api/unauth') && !url.startsWith('/api/auth/user/getCurrentUserInfo')) {
            // failure('登录状态已过期，请重新登录！');
            failure(responseData.message, responseData.code, url);
            deleteAccessToken(true);
            throw new Error('需要重新认证');
        } else {
            if(!url.startsWith('/api/auth/user/getCurrentUserInfo')) {
                failure(responseData.message, responseData.code, url);
                 throw new Error(responseData.message);
            }
        }
    }).catch(err => {
        console.log("接口调用失败", err);
        error(err);
        throw err;
    });
}

function internalGet(url, headers, success = () => {}, failure = defaultFailure, error = defaultError) {
    return axios.get(url, { headers: headers }).then(({ data: responseData }) => {
        console.log("====internalGet",url)

        if (responseData.code === 200) {
            success(responseData.data);
            return responseData.data;
        } else if (responseData.code === 401 && !url.startsWith('/api/unauth') && !url.startsWith('/api/auth/user/getCurrentUserInfo')) {
            failure('登录状态已过期，请重新登录！');
            deleteAccessToken(true);
            throw new Error('需要重新认证');
        } else if (responseData.code === 999) {
            failure(responseData.message);
        } else {
            if(!url.startsWith('/api/auth/user/getCurrentUserInfo')) {
                failure(responseData.message, responseData.code, url);
                 throw new Error(responseData.message);
            }
        }
    }).catch(err => {
        error(err);
        throw err;
    });
}

function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(remember, data.token, data.expire);
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`);
        success(data);
    }, failure);
}

// 修改post函数
function post(url, data, success, failure = defaultFailure) {
    const isUnauth = url.startsWith('/api/unauth');
    return internalPost(url, data, isUnauth ? {} : accessHeader(), success, failure);
}

// 修改get函数
function get(url, success, failure = defaultFailure) {
    const isUnauth = url.startsWith('/api/unauth');
    return internalGet(url, isUnauth ? {} : accessHeader(), success, failure);
}

import useUserInfo from '@/hooks/useUserInfo';

function logout(success, failure = defaultFailure) {
    const { state, loadUserInfo } = useUserInfo();
    get('/api/auth/logout', () => {
        deleteAccessToken();
        state.data = {}; // 清空用户信息
        localStorage.removeItem('userInfo'); // 清除本地存储
        ElMessage.success(`退出登录成功，欢迎您再次使用`);
        success();
    }, failure);
}

function unauthorized() {
    return !takeAccessToken();
}

// 添加全局 token 检查
router.beforeEach((to, from, next) => {
    // 定义不需要认证的路由名称
    const unauthRoutes = ['welcome-login', 'welcome-register', 'welcome-forget','quanDetail','quanTieDetail'];
    
    // 如果当前路由不需要认证，直接放行
    if (unauthRoutes.includes(to.name)) {
        return next();
    }
    
    // 如果路由需要认证但用户未授权，且不是API路由(/api/unauth)，则跳转登录
    if (to.matched.some(record => record.meta.requiresAuth) && unauthorized()) {
        console.log("去登录2 - 需要认证的路由");
        next({ name: 'welcome-login' });
    } else {
        next();
    }
});

axios.interceptors.request.use(config => {
    const token = takeAccessToken();
    const isUnauth = config.url && config.url.startsWith('/api/unauth');
    
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    } else if (!isUnauth && router.currentRoute.value.name !== 'welcome-login') {
        // 只有非unauth接口且不在登录页时才可能跳转
        // 这里不再自动跳转，由路由守卫处理
        console.log("检测到未授权但不需要跳转");
    }
    return config;
});

export { post, get, login, logout, unauthorized, takeAccessToken };