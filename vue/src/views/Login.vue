<template>
  <div class="login-container">
    <!-- 背景视频 -->
    <video autoplay loop muted class="background-video">
      <source src="/video/jett-valorant2.960x540.mp4" type="video/mp4" />
    </video>


    <!-- 登录框 -->
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">
        Welcome to Login
      </div>
      <el-form :model="formData.form" ref="formRef" :rules="formData.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="formData.form.username" placeholder="Enter your username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="formData.form.password" placeholder="Enter your password" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="formData.form.role">
            <el-option value="ADMIN" label="Administrator"></el-option>
            <el-option value="USER" label="Regular users"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login">Login</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        Don't have an account? Please <a href="/register">register</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

// 使用 Vue 3 `setup` 语法修正 `data`
const formData = reactive({
  form: { username: "", password: "", role: "ADMIN" }, // 修正：确保 `username` 和 `password` 初始化
  rules: {
    username: [{ required: true, message: "Please enter your username", trigger: "blur" }],
    password: [{ required: true, message: "Please enter your password", trigger: "blur" }]
  }
});

const formRef = ref();

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post("/login", formData.form).then((res) => {
        if (res.code === "200") {
          ElMessage.success("Login successful");
          router.push("/");
          localStorage.setItem("system-user", JSON.stringify(res.data));
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  }).catch((error) => {
    console.error(error);
  });
};
</script>

<style scoped>
/* 登录容器 */
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 背景视频 */
.background-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}

/* 登录框 */
.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  z-index: 1;
}
</style>
