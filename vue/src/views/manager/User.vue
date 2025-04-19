//用于用户管理，包括增删改查、头像上传等操作
<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
    <!--通过输入名称，查询与名称相关的用户数据，并展示在用户列表中-->
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="Please enter a name to search"></el-input>
      <el-button type="primary" @click="load">Search</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">Reset</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
      <!--点击新增按钮，管理员填写用户信息后保存-->
        <el-button type="primary" @click="handleAdd">Add</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="Username" prop="username"></el-table-column>
        <el-table-column label="Name" prop="name"></el-table-column>
        <el-table-column label="Avatar">
          <template #default="scope">
            <el-image :src="scope.row.avatar" style="width: 40px; height: 40px; border-radius: 50%"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="Role" prop="role">
          <template #default="scope">
            <span v-if="scope.row.role === 'ADMIN'">Administrator</span>
            <span v-if="scope.row.role === 'USER'">Regular users</span>
          </template>
        </el-table-column>
        <el-table-column label="Actions" align="center" width="160">
          <template #default="scope">
          <!--管理员可以选择某个用户，修改其信息-->
            <el-button type="primary" @click="handleEdit(scope.row)">Edit</el-button>
            <!--管理员可以删除某个用户-->
            <el-button type="danger" @click="handleDelete(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
    <!--按页加载用户数据，每页显示固定数量，并支持翻页-->
      <el-pagination background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>
<!--表单弹窗代码-->
    <el-dialog title="Information" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="Avatar" prop="avatar">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
          <!--管理员可以上传图片作为用户的头像-->
            <el-button type="primary">Upload Image</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="Account" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Save</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

// File upload API endpoint
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null
})

// Pagination query
const load = () => {
  request.get('/user/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}

// Add
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// Edit
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// Add save
const add = () => {
  request.post('/user/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('Operation successful')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// Edit save
const update = () => {
  request.put('/user/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('Operation successful')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// Save in dialog
const save = () => {
  // If data.form has an id, it's an update; otherwise, it's a new addition
  data.form.id ? update() : add()
}

// Delete
const handleDelete = (id) => {
  ElMessageBox.confirm('Data cannot be recovered after deletion. Are you sure you want to delete it?', 'Delete Confirmation', { type: 'warning' }).then(res => {
    request.delete('/user/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('Operation successful')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// Reset
const reset = () => {
  data.name = null
  load()
}

// Handle file upload success
const handleImgSuccess = (res) => {
  data.form.avatar = res.data  // res.data is the file path returned by the upload; assign it to the form property after retrieving the path
}

load()
</script>
