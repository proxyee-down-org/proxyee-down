<template>
  <el-row v-if="!selected" type="flex" justify="center">
    <el-col :span="20">
      <el-row>
        <el-col v-for="(tool,index) in tools"
                :key="index"
                :xs="11"
                :sm="9"
                :md="7"
                :lg="5"
                :xl="3">
          <el-tooltip class="item"
                      :content="tool.desc"
                      placement="right">
            <div @click="selected=tool.name">
              <el-card shadow="hover" class="tools-card">
                {{tool.title}}
              </el-card>
            </div>
          </el-tooltip>
        </el-col>
      </el-row>
    </el-col>
  </el-row>
  <div v-else :is="selected" v-bind="args"></div>
</template>

<script>
  import BdyUnzip from './tools/BdyUnzip'
  import Util from '../common/util'

  export default {
    components: {
      BdyUnzip
    },
    data() {
      return {
        tools: [
          {
            name: 'BdyUnzip', 
            title: '百度云解压', 
            desc: '用于百度批量下载的zip文件解压'
          },
        ],
        selected: '',
      }
    },
    props: ['selectTool', 'args'],
    watch: {
      selected(name) {
        let tool = this.tools[Util.inArray(this.tools, name, (tool, name) => tool.name == name)];
        this.$store.commit('tabs/setSecTitle', tool.title)
      }
    },
    created() {
      if (this.selectTool) {
        this.selected = this.selectTool;
      }
    },
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .tools-card {
    text-align: center;
    cursor: pointer;
  }
</style>
