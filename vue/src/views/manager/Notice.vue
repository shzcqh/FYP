<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" placeholder="Please enter a title query"
                style="width: 300px;margin-right: 10px"></el-input>
      <el-button type="primary" @click="load">Inquire</el-button>
      <el-button type="info" @click="reset">Reset</el-button>

    </div>
    <div class="card" style="margin-bottom: 5px">

      <el-button type="primary" style="margin-bottom: 10px" @click="handleAdd">Add</el-button>


      <el-table :data="data.tableData" stripe>
        <el-table-column prop="title" label="title"/>
        <el-table-column prop="content" label="content"/>
        <el-table-column prop="time" label="time"/>
        <el-table-column label="Operations" width="170">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button type="danger" @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card">

      <el-pagination background layout="total,prev, pager, next" v-model:current-page="data.pageNum"
                     v-model:page-size="data.pageSize" :total="data.total" @current-change="load"/>

    </div>
    <el-dialog v-model="data.formVisible" title="System announcements" width="40%">
      <el-form :model="data.form" label-width="80px" style="padding-right:20px ">
        <el-form-item label="Title">
          <el-input v-model="data.form.title" autocomplete="off" placeholder="Please enter a title"/>
        </el-form-item>
        <el-form-item label="Content">
          <el-input type="textarea" v-model="data.form.content" autocomplete="off"
                    placeholder="Please enter your content"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">Cancel</el-button>
          <el-button type="primary" @click="save">Save</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import {reactive} from "vue";
import request from "../../utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  title: null,
  formVisible: false,
  form: {}
})
const load = () => {
  request.get('/notice/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  })
}
load()
const reset = () => {
  data.title = null
  load()
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const add = () => {
  request.post('/notice/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      data.formVisible = false
      ElMessage.success('The operation was successful')
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const update = () => {
  request.put('/notice/update', data.form).then(res => {
    console.log(res)
    if (res.code === '200') {
      load()
      data.formVisible = false
      ElMessage.success('The operation was successful')
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const save = () => {
  data.form.id ? update() : add()
}
const del =(id) => {
  ElMessageBox.confirm('Do you confirm the deletion of the data?','Confirm the deletion',{type:'warning'}).then(res=>{
    request.delete('notice/delete/'+ id).then(res =>{
      if (res.code === '200') {
        load()
        ElMessage.success('The operation was successful')
      } else {
        ElMessage.error(res.msg)
      }
    })
}).catch(err=>{})
}
</script>