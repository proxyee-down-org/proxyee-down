<template>
  <Form ref="form"
    :rules="rules"
    :model="form"
    :label-width="60">
    <FormItem label="文件名"
      prop="fileName">
      <Input v-model="form.fileName" />
    </FormItem>
    <FormItem label="文件大小">{{totalSizeText}}</FormItem>
    <FormItem label="连接数">
      <Slider v-model="form.connections"
        :min="2"
        :max="256"
        :step="2"
        show-input />
    </FormItem>
    <FormItem label="路径"
      prop="filePath">
      <!-- <file-choose v-model="form.filePath" /> -->
      <FormItem>
        <Button type="primary"
          @click="onSubmit"
          :loading="load">创建</Button>
        <Button @click="onCancel">取消</Button>
      </FormItem>
  </Form>
</template>

<script>
export default {
  data() {
    return {
      load: true,
      newTask: null,
      sameTasks: [],
      form: {
        id: this.taskId,
        oldId: "",
        fileName: "",
        totalSize: 0,
        supportRange: false,
        connections: 1,
        filePath: "",
        unzip: false,
        unzipPath: ""
      },
      rules: {
        url: [
          { required: true, message: "不能为空" },
          { type: "url", message: "格式不正确" }
        ],
        fileName: [{ required: true, message: "不能为空" }],
        filePath: [
          { required: true, message: "不能为空" },
          { pattern: /^([a-z]:)?[/\\].*$/i, message: "格式不正确" }
        ]
      }
    };
  },
  props: ["taskId"],
  /*  components: {
    FileChoose
  }, */
  computed: {
    supportRangeText() {
      return this.form.supportRange ? "支持" : "不支持";
    },
    totalSizeText() {
      return "0kb";
      //return Util.sizeFmt(this.form.totalSize, "未知");
    }
  },
  methods: {
    onSubmit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.load = true;
          this.$http
            .post("api/startTask", this.form)
            .then(result => {
              this.load = false;
              this.$emit("onSubmit", result);
            })
            .catch(() => {
              this.load = false;
            });
        }
      });
    },
    onCancel() {
      this.$confirm("确认要取消吗", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消"
      })
        .then(() => {
          this.$http
            .get(`api/delNewTask?id=${this.form.id}`)
            .then(() => {
              this.$emit("onCancel", arguments[0]);
            })
            .catch(() => {});
        })
        .catch(() => {});
    }
  }
};
</script>


<style scoped>
.same-task-label {
  float: left;
}

.same-task-value {
  float: right;
  padding-left: 20px;
  color: #8492a6;
  font-size: 14px;
}
</style>
