<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" placeholder="Please enter a name to enquire"
                style="width: 300px;margin-right: 10px"></el-input>
      <el-button type="primary" @click="load">Inquire</el-button>
      <el-button type="info" @click="reset">Reset</el-button>

    </div>
    <div class="card" style="margin-bottom: 5px">

      <el-button type="primary" style="margin-bottom: 10px" @click="handleAdd">Add</el-button>


      <el-table :data="data.tableData" stripe>
        <el-table-column prop="name" label="Name" width="120"/>
        <el-table-column prop="cover" label="Cover" width="120">
          <template #default="scope">
            <el-image :src="scope.row.cover" style="width: 40px; height: 40px; border-radius: 5px" :preview-src-list="[scope.row.cover]" preview-teleported></el-image>
          </template>
          </el-table-column>
        <el-table-column prop="descr" label="Description" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="year" label="Year" width="80"/>
        <el-table-column prop="director" label="Director" width="150"/>
        <el-table-column prop="actors" label="Actors" min-width="180" show-overflow-tooltip/>
        <el-table-column prop="categoryName" label="Classification ID" min-width="150"/>
        <el-table-column prop="country" label="Country" width="120"/>
        <el-table-column prop="language" label="Language" width="120"/>
        <el-table-column prop="date" label="Date" width="120"/>
        <el-table-column prop="duration" label="Duration" width="100"/>
        <el-table-column prop="imdb" label="IMDb" width="100"/>
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
    <el-dialog v-model="data.formVisible" title="Movie information" width="40%">
      <el-form :model="data.form" label-width="80px" style="padding-right:20px ">
        <el-form-item label="Name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="Please enter a name"/>
        </el-form-item>
        <el-form-item label="Cover" prop="cover">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">Upload Image</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="Description">
          <el-input :rows="5" type="textarea" v-model="data.form.descr" autocomplete="off" placeholder="Please enter a description"/>
        </el-form-item>
        <el-form-item label="Year">
          <el-input v-model="data.form.year" autocomplete="off" placeholder="Please enter a year"/>
        </el-form-item>
        <el-form-item label="Director">
          <el-input v-model="data.form.director" autocomplete="off" placeholder="Please enter a director"/>
        </el-form-item>
        <el-form-item label="Actors">
          <el-input :rows="4" type="textarea" v-model="data.form.actors" autocomplete="off" placeholder="Please enter actors"/>
        </el-form-item>
        <el-form-item label="Classification ID">
          <el-input v-model="data.form.categoryId" autocomplete="off" placeholder="Please enter a calssification id"/>
        </el-form-item>
        <el-form-item label="Country">
          <el-input v-model="data.form.country" autocomplete="off" placeholder="Please enter a country"/>
        </el-form-item>
        <el-form-item label="Language">
          <el-input v-model="data.form.language" autocomplete="off" placeholder="Please enter a language"/>
        </el-form-item>
        <el-form-item label="Date">
          <el-date-picker
              style="width: 100%"
              type="date"
              v-model="data.form.date"
              placeholder="Please select a release date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="Duratiom">
          <el-input v-model="data.form.duration" autocomplete="off" placeholder="Please enter a duration"/>
        </el-form-item>
        <el-form-item label="IMDb">
          <el-input v-model="data.form.imdb" autocomplete="off" placeholder="Please enter a IMDb"/>
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
const uploadUrl=import.meta.env.VITE_BASE_URL + '/files/upload'
const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  name: null,
  formVisible: false,
  form: {}
})
const load = () => {
  request.get('/film/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  })
}
load()
const reset = () => {
  data.name = null
  load()
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const add = () => {
  request.post('/film/add', data.form).then(res => {
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
  request.put('/film/update', data.form).then(res => {
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
    request.delete('film/delete/'+ id).then(res =>{
      if (res.code === '200') {
        load()
        ElMessage.success('The operation was successful')
      } else {
        ElMessage.error(res.msg)
      }
    })
}).catch(err=>{})
}
const handleImgSuccess =(res) =>{
  data.form.cover=res.data
}
</script>