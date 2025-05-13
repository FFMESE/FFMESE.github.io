<template>
  <div>
    <el-space direction="vertical" alignment="left" style="width: 100%">
      <el-card>
        <el-form ref="searchFormComponents" :model="searchForm" inline>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="searchForm.username" clearable></el-input>
          </el-form-item>
          <el-form-item label="电话" prop="tel">
            <el-input v-model="searchForm.tel" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable filterable style="width: 150px">
              <el-option :label="item" :key="item" :value="item" v-for="item in statusList"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
            <el-button type="" :icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <elspace>
          <el-button type="primary" :icon="Plus" @click="add">添加</el-button>
          <el-button type="danger" :icon="Delete" @click="batchDelete(null)">批量删除</el-button>
        </elspace>
      </el-card>

      <el-card>
        <el-table :data="listData" style="width: 100%">
          <el-table-column type="selection" width="65"/>
          <el-table-column property="id" label="ID"/>
          <el-table-column property="username" label="用户名" width="120"/>
          <el-table-column property="nickname" label="昵称"/>
          <el-table-column property="avatarUrl" label="头像">
            <template #default="scope">
              <el-image v-if="scope.row.avatarUrl"
                        style="width: 50px;height: 50px"
                        :src="scope.row.avatarUrl"
                        :preview-src-list="[scope.row.avatarUrl]"
                        :preview-teleported="true"
              ></el-image>
            </template>
          </el-table-column>
          <el-table-column property="tel" label="电话"/>
          <el-table-column property="email" label="邮箱"/>
          <el-table-column property="createTime" label="创建时间" width="170"/>
          <el-table-column property="status" label="状态"/>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" :icon="Edit" @click="edit(scope.row)" >编辑</el-button>
              <el-button type="danger" :icon="Delete" @click="deleteOne(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px;">
          <el-pagination
              @current-change="currentChange"
              @size-change="sizeChange"
              :page-size="pageInfo.pageSize"
              :current-page="pageInfo.pageNum"
              background
              layout="total,sizes,prev,pager,next"
              :total="pageInfo.total"
          />
        </div>
      </el-card>
    </el-space>
    <el-dialog
        v-model="dialogOpen"
        v-if="dialogOpen"
        :title="formData.id?'编辑':'新增'"
        width="500"
    >
      <el-form ref="formRef" :model="formData" label-width="100">
        <el-form-item label="用户名" prop="username"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <el-input v-model="formData.username"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <el-input v-model="formData.nickname"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="avatarUrl"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <MyUpload type="imageCard" :limit="1" :files="formData.avatarUrl"
                    @setFiles="formData.avatarUrl = $event"
                    v-if="dialogOpen"
          ></MyUpload>
        </el-form-item>
        <el-form-item label="电话" prop="tel"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <el-input v-model="formData.tel"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <el-input v-model="formData.email"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status"
                      :rules="[{required:true,message:'不能为空',trigger:['blur','change']}]">
          <el-radio-group v-model="formData.status">
            <el-radio v-for="item in statusList" :key="item" :label="item">{{item}}</el-radio>

          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :icon="Check" @click="submit">提交</el-button>
          <el-button :icon="Close" @click="closeDialog">取消</el-button>

        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import {ref, toRaw} from "vue";
import {Delete, Edit, Plus, Refresh, Search, Check, Close} from "@element-plus/icons-vue";
import request from "@/utils/http.js"
import MyUpload from "@/components/MyUpload.vue";
const searchFormComponents = ref()
/**
 * 搜索参数
 * @type {Ref<UnwrapRef<{tel: undefined, username: undefined, status: undefined}>, UnwrapRef<{tel: undefined, username: undefined, status: undefined}> | {tel: undefined, username: undefined, status: undefined}>}
 */
const searchForm = ref({
  username: undefined,
  tel: undefined,
  status: undefined
})

//分页
const pageInfo = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 状态
const statusList = ref(['启用', '禁用'])

const listData = ref([
  {
    username: "张三",
    nickname: "里斯"
  }
])

//新增编辑
const formData = ref({})

//控制弹窗开关
const dialogOpen = ref(false)

const formRef = ref()


getPageList()

function getPageList() {
  request.get("/user/page", {
    params: Object.assign(toRaw(searchForm.value), toRaw(pageInfo.value))
  }).then(res => {
    //获取列表
    console.log(res)
    listData.value = res.data.list
    //获取总记录条数
    pageInfo.value.total = res.data.total
  })
}


/**
 * 搜索
 */
function search() {
  //用户点击搜索，分页重置到第一页
  pageInfo.value.pageNum = 1
  //调用分页方法
  getPageList()

}

/**
 * 重置搜索
 */
function resetSearch() {
  searchFormComponents.value.resetFields()
  getPageList()
}

function add() {
  //清空值
  formData.value = {}
  //点击按钮 打开弹窗
  dialogOpen.value = true
}

function edit(row){
  formData.value = Object.assign({},row)
  dialogOpen.value = true
}

function batchDelete() {

}

function deleteOne() {

}

function submit() {
  console.log(formData.value)

  formRef.value.validate((valid) => {
    if(!valid){
      ElMessage({
        message:"验证失败，请检查表单",
        type:'warning'
      })
      return
    }
    const requestMethod = formData.value.id ? request.put : request.post
    const url = formData.value.id ? "/user/update":"/user/add"

    requestMethod(url,formData.value).then(res => {
      if(!res){
        return
      }
      dialogOpen.value = false
      ElMessage({
        message:"操作成功",
        type:'success'
      })
      //重新获取列表
      getPageList()
    })
  })
}

function closeDialog() {
  dialogOpen.value=false
}

/**
 * 选择分页
 * @param num
 */
function currentChange(num) {
  pageInfo.value.pageNum = num
  getPageList()
}

/**
 * 改变分页数量
 * @param num
 */
function sizeChange(num) {
  pageInfo.value.pageSize = num
  getPageList()
}

</script>

<style scoped>

::v-deep .el-table .el-table-column--selection .cell .el-checkbox .el-checkbox__inner {
  border-color: #000; /* 设置边框颜色为纯黑色 */
}
</style>