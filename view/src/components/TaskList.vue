<template>
  <div v-if="tasks.length>0">
    <el-row type="flex" justify="center">
      <el-col :span="20">
        <el-row v-for="i in Math.ceil(tasks.length/cellSize)">
          <el-col :span="8" v-for="j in cellSize" v-if="taskIndex(i,j)+1<=tasks.length">
            <div>
              <span>{{tasks[taskIndex(i, j)].name}}</span>
              <span>{{tasks[taskIndex(i, j)].speed}}kb/s</span>
              <span>{{tasks[taskIndex(i, j)].total}}kb</span>
            </div>
            <el-progress type="circle"
                         :percentage="tasks[taskIndex(i,j)].progress"
                         :status="tasks[taskIndex(i,j)].status"
                         :width="200"></el-progress>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
  <div v-else>
    <p>暂无下载任务</p>
  </div>
</template>

<script>
  export default {
    name: 'taskList',
    data() {
      return {
        tasks: [],
        cellSize: 3
      }
    },
    methods: {
      down() {
        this.tasks.forEach((task) => {
          if (task.progress < 100) {
            task.progress++;
          } else {
            task.status = 'success';
          }
        });
      },
      taskIndex(i, j) {
        return (i - 1) * 3 + j - 1;
      }
    },
    created() {
      this.$http.get("api/test")
        .then((response) => {
          console.log(response);
        })
//      this.tasks.push({name: '测试任务1', progress: 0, speed: 353, total: 832, status: ''});
//      setInterval(this.down, 100)
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
