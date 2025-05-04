<template>
  <div>
    <!-- Search and Filter Area -->
    <div class="card" style="margin-bottom: 10px;">
      <el-input
          v-model="data.filmName"
          placeholder="Please enter the name of the movie to search"
          style="width: 300px; margin-right: 10px;"
      />
      <el-select v-model="data.sentimentFilter" placeholder="Filter by Emotion" clearable style="width: 180px; margin-right: 10px;">
        <el-option label="😊 Positive" :value="2" />
        <el-option label="😐 Neutral" :value="1" />
        <el-option label="😞 Negative" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">Inquire</el-button>
      <el-button type="info" @click="reset">Reset</el-button>
    </div>

    <!-- Comments Table -->
    <div class="card" style="margin-bottom: 10px;">
      <el-table :data="paginatedTableData" stripe>
        <el-table-column prop="filmName" label="The name of the movie" />
        <el-table-column prop="score" label="Movie ratings">
          <template #default="scope">
            <el-rate disabled v-model="scope.row.score" allow-half />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="Movie comment">
          <template #default="scope">
            <el-button @click="preview(scope.row.comment)">View content</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="Username" />
        <el-table-column prop="time" label="Comment time" />
        <el-table-column prop="type" label="Type">
          <template #default="scope">
            <el-tag type="primary" v-if="scope.row.type === 'Short comment'">Short comment</el-tag>
            <el-tag type="success" v-if="scope.row.type === 'Long comment'">Long comment</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Emotion" width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.sentiment === 2" type="success" effect="dark">😊 Positive</el-tag>
            <el-tag v-else-if="scope.row.sentiment === 1" type="warning" effect="dark">😐 Neutral</el-tag>
            <el-tag v-else type="danger" effect="dark">😞 Negative</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Operations" width="110">
          <template #default="scope">
            <el-button type="danger" @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Local Pagination -->
      <el-pagination
          background
          layout="total, prev, pager, next"
          v-model:current-page="data.pageNum"
          :page-size="data.pageSize"
          :total="filteredTableData.length"
          style="margin-top: 10px;"
      />
    </div>

    <!-- Comment Preview Dialog -->
    <el-dialog v-model="data.formVisibleComment" title="Comment content" width="40%">
      <div v-html="data.commentContent"></div>
      <template #footer>
        <el-button @click="data.formVisibleComment = false">Close</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, computed } from "vue";
import request from "../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  rawTableData: [], // store full data
  tableData: [],     // current loaded
  pageNum: 1,
  pageSize: 10,
  filmName: null,
  sentimentFilter: null, // sentiment filter
  formVisibleComment: false,
  commentContent: null,
});

const preview = (comment) => {
  data.commentContent = comment;
  data.formVisibleComment = true;
};

const load = () => {
  request.get("/comment/selectPage", {
    params: {
      pageNum: 1, // load all at once
      pageSize: 1000, // large number to simulate full load
      filmName: data.filmName,
      userId: data.user.role === "ADMIN" ? null : data.user.id,
    },
  }).then((res) => {
    data.rawTableData = res.data.list;
    data.pageNum = 1;
  });
};
load();

const reset = () => {
  data.filmName = null;
  data.sentimentFilter = null;
  load();
};

const del = (id) => {
  ElMessageBox.confirm("Do you confirm the deletion of the data?", "Confirm", { type: "warning" })
      .then(() => {
        request.delete(`comment/delete/${id}`).then((res) => {
          if (res.code === "200") {
            load();
            ElMessage.success("Operation successful");
          } else {
            ElMessage.error(res.msg);
          }
        });
      })
      .catch(() => {});
};

const filteredTableData = computed(() => {
  if (data.sentimentFilter === null) return data.rawTableData;
  return data.rawTableData.filter((item) => item.sentiment === data.sentimentFilter);
});

const paginatedTableData = computed(() => {
  const start = (data.pageNum - 1) * data.pageSize;
  const end = start + data.pageSize;
  return filteredTableData.value.slice(start, end);
});
</script>

<style scoped>
.card {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
</style>
