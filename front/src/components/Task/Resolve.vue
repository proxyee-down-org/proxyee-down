<template>
  <Modal title="创建任务"
    :value="value"
    @input="$emit('input', arguments[0])"
    :closable="false"
    :mask-closable="false"
    @on-visible-change="onReset">
    <Form ref="form"
      :rules="rules"
      :model="form"
      :label-width="60">
      <FormItem label="链接"
        prop="url">
        <Input v-model="form.url" />
      </FormItem>
      <FormItem label="附加">
        <Checkbox v-model="hasHead">请求头</Checkbox>
        <Checkbox v-model="hasBody">请求体</Checkbox>
      </FormItem>
      <FormItem v-show="hasHead"
        label="请求头"
        prop="heads">
        <div v-for="(head, index) in form.heads"
          :key="index"
          :class="index==0?null:'head-margin'">
          <Input class="head-input"
            v-model="head.key"
            placeholder="key" />
          <Input class="head-input"
            v-model="head.value"
            placeholder="value" />
          <Icon v-if="index!=0"
            type="minus-circled"
            @click="delHead(index)"></Icon>
          <Icon v-if="index==form.heads.length-1"
            type="plus-circled"
            @click="addHead"></Icon>
        </div>
      </FormItem>
      <FormItem v-show="hasBody"
        label="请求体"
        prop="body">
        <Input type="textarea"
          :autosize="{ minRows: 2, maxRows: 4}"
          placeholder="请输入内容"
          v-model="form.body" />
      </FormItem>
    </Form>
    <div slot="footer">
      <Button type="primary"
        @click="onSubmit"
        :loading="load">确定</Button>
      <Button @click="$emit('input', false);">取消</Button>
    </div>
    <Spin size="large"
      fix
      v-if="load" />
  </Modal>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  data() {
    return {
      load: false,
      hasHead: false,
      hasBody: false,
      form: {
        url: "",
        heads: [],
        body: "",
        dir: ""
      },
      rules: {
        url: [
          { required: true, message: "不能为空" },
          { pattern: /^https?:\/\/.*$/i, message: "格式不正确" }
        ]
      }
    };
  },
  watch: {
    hasHead(val) {
      if (val && this.form.heads.length == 0) {
        this.addHead();
      }
    }
  },
  methods: {
    addHead() {
      this.form.heads.push({ key: "", value: "" });
    },
    delHead(index) {
      this.form.heads.splice(index, 1);
    },
    onSubmit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const requestData = {
            url: this.form.url,
            heads: {},
            body: ""
          };
          if (this.hasHead) {
            for (let head of this.form.heads) {
              if (head.key && head.value) {
                requestData.heads[head.key] = head.value;
              }
            }
          }
          if (this.hasBody) {
            requestData.body = this.form.body;
          }
          this.load = true;
          this.$http
            .put("http://127.0.0.1:26339/util/resolve", requestData)
            .then(result => {
              console.log(result);
            })
            .finally(() => {
              this.load = false;
            });
        }
      });
    },
    onReset(visible) {
      if (visible) {
        this.$refs["form"].resetFields();
        this.hasHead = false;
        this.hasBody = false;
      }
    }
  }
};
</script>

<style scoped lang="less">
.head-input {
  width: 40%;
  & + .head-input {
    margin-left: 10px;
  }
}

.ivu-icon {
  margin-left: 10px;
  font-size: 22px;
  cursor: pointer;
  position: relative;
  top: 5px;
}

.head-margin {
  margin-top: 10px;
}
</style>
