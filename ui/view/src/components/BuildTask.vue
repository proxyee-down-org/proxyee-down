<template>
  <el-form ref="form" :rules="rules" :model="form" label-width="80px" size="medium">
    <el-form-item label="链接" prop="url">
      <el-input v-model="form.url"></el-input>
    </el-form-item>
    <el-form-item label="附加">
      <el-checkbox v-model="hasHead">请求头</el-checkbox>
      <el-checkbox v-model="hasBody">请求体</el-checkbox>
    </el-form-item>
    <el-form-item v-show="hasHead" label="请求头">
      <div v-for="(head,index) in form.heads"
           :key="index"
           :class="index==0?null:'head-margin'">
        <el-input class="head-input" v-model="head.key" placeholder="key"></el-input>
        <el-input class="head-input" v-model="head.value" placeholder="value"></el-input>
        <i v-if="index!=0" class="el-icon-remove" @click="delHead(index)"></i>
        <i v-if="index==form.heads.length-1" class="el-icon-circle-plus" @click="addHead"></i>
      </div>
    </el-form-item>
    <el-form-item v-show="hasBody" label="请求体">
      <el-input
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 4}"
        placeholder="请输入内容"
        v-model="form.body">
      </el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">确定</el-button>
      <el-button @click="$emit('onCancel',arguments[0]);">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  export default {
    data() {
      return {
        load: false,
        hasHead: false,
        hasBody: false,
        form: {
          url: '',
          heads: [
            {key: '', value: ''},
          ],
          body: '',
        },
        rules: {
          url: [
            {required: true, message: '不能为空'},
            {pattern: /^https?:\/\/.*$/i, message: '格式不正确'}
          ]
        },
      }
    },
    methods: {
      addHead() {
        this.form.heads.push({key: '', value: ''});
      },
      delHead(index) {
        this.form.heads.splice(index, 1);
      },
      onSubmit() {
        this.$refs['form'].validate(valid => {
          if (valid) {
            this.load = true;
            this.$http.post('api/buildTask', this.form)
            .then(result => {
              this.load = false;
              this.$emit('onSubmit', result);
            }).catch(() => {
              this.load = false;
            });
          }
        });
      }
    }
  }
</script>


<style scoped>
  .head-input {
    width: 40%
  }

  .el-form-item i {
    font-size: 22px;
    margin-left: 10px;
    cursor: pointer;
  }

  .head-margin {
    margin-top: 10px;
  }

  .head-input + .head-input {
    margin-left: 10px;
  }

  .el-checkbox + .el-checkbox {
    margin-left: 10px;
  }
</style>
