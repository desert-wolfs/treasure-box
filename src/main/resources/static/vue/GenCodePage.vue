<template>
  <div class="table-container">

    <el-card shadow="hover" class="table-card">

      <el-row style="text-align: center;">
        <span style="font-size: large; font-weight: bold;">代码生成</span>
      </el-row>
      <el-row :gutter="20" class="mb-4">
        <el-col :span="8">
          来源: 本地sit数据库(192.168.2.74)
          <br>
          jar包: (192.168.2.71-后台服务) 目录: /root/jar
          <br>
          条件1: 表名(多个表用逗号隔开)
        </el-col>
        <el-col>
          <el-input
            type="textarea"
            @change="toCamelCase"
            :autosize="{ minRows: 2, maxRows: 5}"
            placeholder="请输入内容"
            v-model="tableNames">
          </el-input>
          生成文件名: <span style="color: #e04646">{{camelCase}}</span>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb-4">
        <el-col :span="9">
          条件2: 文件名 | 文件名指定字符串替换(不分大小写, 可选)
        </el-col>
      </el-row>
      <el-button type="primary" size="mini" @click="addRow" icon="Plus">
        添加替换
      </el-button>
      <el-table
        :data="tableData"
        border
        stripe
        highlight-current-row
        style="width: 800px"
      >
        <el-table-column label="名称">
          <template #default="{ row }">
            <el-input v-model="row.oldName" placeholder="请输入旧字符串" size="small"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="年龄">
          <template #default="{ row }">
            <el-input v-model="row.newName" placeholder="请输入新替换字符串" size="small"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ $index }">
            <el-button
              type="danger"
              icon="Delete"
              size="small"
              @click="deleteRow($index)"
              confirm-type="danger"
              :confirm-message="'确定删除这行吗?'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-row :gutter="20" class="mb-4">
        <el-button type="primary" icon="Plus" @click="handleJump">
          生成下载链接
        </el-button>
      </el-row>
      <el-row :gutter="20" class="mb-4">
        <a></a>
        <span>下载url,点击下载:  <a :href="downloadUrl" target="_blank">{{downloadUrl}}</a> </span>
        <br>
        手动下载: <el-button type="primary" size="small" @click="copyToClipboard(downloadUrl)">复制链接</el-button>
      </el-row>

    </el-card>
  </div>
</template>

<script setup>
import { ref } from "vue"
import { Message } from "element-ui"

// 表格数据
const tableData = ref([])
const tableNames = ref("")
const downloadUrl = ref("")
const camelCase = ref("")

// 生成唯一ID
let nextId = 1

// 添加新行
const addRow = () => {
  tableData.value.push({
    id: nextId++,
    oldName: "",
    newName: ""
  })
}

// 删除行
const deleteRow = (index) => {
  tableData.value.splice(index, 1)
}

const handleJump = () => {
  if (tableNames.value == "" || tableNames.value == null) {
    Message.error("请输入表名")
    return false
  }
  // 替换为实际需要跳转的URL
  const finalTableNames = tableNames.value.replace(/，/g, ',')
  console.log(finalTableNames)
  let finalNames = ""
  if (tableData.value.length > 0) {
    finalNames = tableData.value.map(item => item.oldName + "@" + item.newName).join(",")
  }
  const targetUrl = "http://192.168.2.71:8789/gen-code/download?tableNames=" + finalTableNames + "&newFileNames=" + finalNames
  downloadUrl.value = targetUrl
  console.log(targetUrl)
  // 打开新窗口跳转
  // window.open(targetUrl, "_blank")
}

function toCamelCase(str, separator) {
  if (!str || typeof str !== 'string') return '';

  // 替换中文逗号为英文逗号
  str = str.replace(/，/g, ',')

  // 默认分隔符：横线、下划线和空格
  const sep = separator || /[-_]+/;

  const strings = str.split(",")
  camelCase.value = strings.map(item => {
    // 分割字符串并转换每个单词为首字母大写
    return item.split(sep)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join('');
  }).join(",")
}

const copyToClipboard = (text) => {
  if (!text) {
    Message.error('没有可复制的内容');
    return;
  }

  // 现代浏览器Clipboard API
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      Message.success('复制成功');
    }).catch(() => {
      Message.error("复制失败")
    });
  } else {
    Message.error("复制失败")
  }
}
</script>

<style scoped>
.table-container {
  padding: 20px;
}

.table-card {
  .el-table {
    margin-top: 10px;
  }
}

.mb-4 {
  margin-top: 50px;
}
</style>
