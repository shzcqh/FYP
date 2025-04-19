<!--这是一个用户修改密码的页面，用户可以输入原密码、新密码和确认新密码-->
<template>
  <div style="width: 50%">
    <div class="card" style="padding: 30px">
      <el-form :model="data.user" label-width="100px" style="padding-right: 50px">
        <el-form-item label="Current Password">
          <el-input v-model="data.user.password" show-password />
        </el-form-item>
        <el-form-item label="New Password">
          <el-input v-model="data.user.newPassword" show-password />
        </el-form-item>
        <el-form-item label="Confirm New Password">
          <el-input v-model="data.user.confirmPasword" show-password />
        </el-form-item>
        <div style="text-align: center">
          <el-button type="primary" @click="save">Save</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'), // Load user information from local storage
});

// Save the updated password to the backend database
const save = () => {
  if (data.user.newPassword !== data.user.confirmPasword) {
    ElMessage.error('The new password confirmation does not match'); // Error if passwords do not match
    return;
  }
  request.put('/updatePassword', data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('Password updated successfully'); // Success message
      // Clear cached user information
      localStorage.removeItem('system-user');
      router.push('/login'); // Redirect to login page
    } else {
      ElMessage.error(res.msg); // Show error message from server
    }
  });
};
</script>
