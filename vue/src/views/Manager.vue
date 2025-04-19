<!--这是一个后台管理系统的布局页面，包含顶部导航栏、左侧菜单栏，以及主要内容区域-->
<template>
  <div>
    <div style="height: 60px; background-color: #fff; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px;color: #91eae4">Movie review system</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
        <span style="margin-left: 5px">{{ data.user.name }}</span>
      </div>
    </div>

    <div style="display: flex">
      <div style="width: 270px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
        <el-menu
            router
            style="border: none"
            :default-active="router.currentRoute.value.path"
            :default-openeds="['1', '2']"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>System Home</span>
          </el-menu-item>
          <el-menu-item index="/filmView">
            <el-icon><Film /></el-icon>
            <span>List of movies</span>
          </el-menu-item>
          <el-menu-item index="/comment">
            <el-icon><Comment /></el-icon>
            <span>Movie Reviews</span>
          </el-menu-item>
          <el-sub-menu index="1" v-if="data.user.role ==='ADMIN'">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>Information management</span>
            </template>
            <el-menu-item index="/category">
              <el-icon><Grid /></el-icon>
              <span>Classification information</span>
            </el-menu-item>
            <el-menu-item index="/film">
              <el-icon><Film /></el-icon>
              <span>Movie information</span>
            </el-menu-item>
            <el-menu-item index="/notice">
              <el-icon><Bell /></el-icon>
              <span>Announcement information</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="2" v-if="data.user.role ==='ADMIN'">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>User Management</span>
            </template>
            <el-menu-item index="/admin">
              <el-icon><User /></el-icon>
              <span>Admin Information</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>Common User Information</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/person">
            <el-icon><User /></el-icon>
            <span>Personal Information</span>
          </el-menu-item>
          <el-menu-item index="/password">
            <el-icon><Lock /></el-icon>
            <span>Change Password</span>
          </el-menu-item>
          <el-menu-item index="login" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>Logout</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px">
        <router-view @updateUser="updateUser" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}') // Load the logged-in user information from local storage
});

// Check if the user is logged in
if (!data.user?.id) {
  ElMessage.error('Please login!'); // Show error message if not logged in
  router.push('/login'); // Redirect to the login page
}

// Update the user information when changes occur
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}'); // Reload the user information from local storage
};

// Handle the logout process
const logout = () => {
  ElMessage.success('Logout successful'); // Show success message on logout
  localStorage.removeItem('system-user'); // Remove user information from local storage
  router.push('/login'); // Redirect to the login page
};
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #e0edfd !important;
}
.el-menu-item:hover {
  color: #1967e3;
}
:deep(th) {
  color: #333;
}
</style>
