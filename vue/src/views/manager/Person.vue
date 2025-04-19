<!--这是一个用户个人信息管理页面-->
<template>
  <div style="width: 50%">
    <div class="card" style="padding: 30px">
      <el-form :model="data.user" label-width="100px" style="padding-right: 50px">
        <div style="margin: 20px 0; text-align: center">
          <el-upload :show-file-list="false" class="avatar-uploader" :action="uploadUrl" :on-success="handleFileUpload">
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
        <el-form-item label="Account">
          <el-input disabled v-model="data.user.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="Name">
          <el-input v-model="data.user.name" autocomplete="off" />
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

// File upload API endpoint
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'), // Load user information from local storage
});

// Handle file upload success and update user avatar URL
const handleFileUpload = (file) => {
  data.user.avatar = file.data; // Set the uploaded avatar URL to the user object
};

const emit = defineEmits(["updateUser"]);

// Save the updated user information to the backend database
const save = () => {
  if (data.user.role === 'ADMIN') {
    request.put('/admin/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('Update successful'); // Success message
        // Save the updated user information to local storage
        localStorage.setItem('system-user', JSON.stringify(data.user));
        emit('updateUser'); // Emit an event to notify the parent component
      } else {
        ElMessage.error(res.msg); // Show error message from server
      }
    });
  }else if (data.user.role === 'USER') {
    request.put('/user/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('Update successful'); // Success message
        // Save the updated user information to local storage
        localStorage.setItem('system-user', JSON.stringify(data.user));
        emit('updateUser'); // Emit an event to notify the parent component
      } else {
        ElMessage.error(res.msg); // Show error message from server
      }
    });
  }
};

</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>
