<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.filmName" placeholder="Please enter the name of the movie to search"
                style="width: 300px;margin-right: 10px"></el-input>
      <el-button type="primary" @click="load">Inquire</el-button>
      <el-button type="info" @click="reset">Reset</el-button>

    </div>
    <div class="card" style="margin-bottom: 5px">

      <el-table :data="data.tableData" stripe>
        <el-table-column prop="filmName" label="The name of the movie"/>
        <el-table-column prop="score" label="Movie ratings">
          <template #default="scope">
            <el-rate disabled v-model="scope.row.score" allow-half />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="Movie comment">
        <template #default="scope">
           <el-button @click="preview(scope.row.comment)">
             View content
           </el-button>
        </template>
        </el-table-column>
        <el-table-column prop="userName" label="Username"/>
        <el-table-column prop="time" label="Comment time "/>
        <el-table-column prop="type" label="Type">
          <template #default="scope">
            <el-tag type="primary" v-if="scope.row.type==='Short comment'">Short comment</el-tag>
            <el-tag type="success" v-if="scope.row.type==='Long comment'">Long comment</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Emotion" width="120">
          <template #default="scope">
            <el-tag
                v-if="scope.row.sentiment === 2"
                type="success"
                effect="dark"
            >Positive</el-tag
            >
            <el-tag
                v-else-if="scope.row.sentiment === 1"
                type="warning"
                effect="dark"
            >Neutral</el-tag
            >
            <el-tag
                v-else
                type="danger"
                effect="dark"
            >Negative</el-tag
            >
          </template>
        </el-table-column>


        <el-table-column label="Operations" width="110">
          <template #default="scope">
            <el-button type="danger" @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card">

      <el-pagination background layout="total,prev, pager, next" v-model:current-page="data.pageNum"
                     v-model:page-size="data.pageSize" :total="data.total" @current-change="load"/>

    </div>
    <el-dialog v-model="data.formVisibleComment" title="Comment content" width="40%">
      <div v-html="data.commentContent">

      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisibleComment = false">Shut down</el-button>
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
  user:JSON.parse(localStorage.getItem('system-user') || '{}'),
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  filmName: null,
  formVisible: false,
  form: {},
  formVisibleComment:false,
  commentContent:null
})
const preview = (comment) =>{
data.commentContent=comment
  data.formVisibleComment=true
}
const load = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      filmName: data.filmName,
      userId:data.user.role ==='ADMIN' ? null : data.user.id
    }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  })
}
load()
const reset = () => {
  data.filmName = null
  load()
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const add = () => {
  request.post('/comment/add', data.form).then(res => {
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
  request.put('/comment/update', data.form).then(res => {
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
    request.delete('comment/delete/'+ id).then(res =>{
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