<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">Welcome to Register</div>
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="Enter your username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="Enter your password" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" placeholder="Confirm your password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="register">Register</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        Already have an account? Please <a href="/login">Login</a>
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

// Validate that the confirmation password matches the original password
const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('Please confirm your password'));
  } else if (value !== data.form.password) {
    callback(new Error('Passwords do not match'));
  } else {
    callback();
  }
};

const data = reactive({
  form: { role: 'USER' }, // Default role is set to 'USER'
  rules: {
    username: [
      { required: true, message: 'Please enter your username', trigger: 'blur' }, // Username is required
    ],
    password: [
      { required: true, message: 'Please enter your password', trigger: 'blur' }, // Password is required
    ],
    confirmPassword: [
      { validator: validatePass, trigger: 'blur' }, // Validate password confirmation
    ],
  },
});

const formRef = ref();

// Triggered when the register button is clicked
const register = () => {
  formRef.value.validate((valid => {
    if (valid) {
      // Call the backend API
      request.post('/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("Registration successful"); // Show success message
          router.push('/login'); // Redirect to login page
        } else {
          ElMessage.error(res.msg); // Show error message from server
        }
      });
    }
  })).catch(error => {
    console.error(error); // Handle any validation or API call errors
  });
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(to bottom, #a8c0ff, #3f2b96);
  background-size: cover;
}
.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, .1);
  background-color: rgba(255, 255, 255, .5);
}
</style>
