<template>
  <Modal title="创建任务"
    :value="visible"
    @input="closeModal"
    :closable="false"
    :mask-closable="false">
    <Form v-if="visible"
      ref="form"
      :model="form"
      :rules="rules"
      :label-width="60">
      <FormItem label="文件名"
        prop="response.fileName">
        <Input v-model="form.response.fileName" />
      </FormItem>
      <FormItem label="文件大小">{{$numeral(form.response.totalSize).format('0.00b')}}</FormItem>
      <FormItem label="连接数"
        prop="config.connections">
        <Slider v-if="response.supportRange"
          v-model="form.config.connections"
          :min="2"
          :max="256"
          :step="2"
          show-input />
        <Slider v-else
          v-model="form.config.connections"
          :min="1"
          :max="1" />
      </FormItem>
      <FormItem label="路径"
        prop="config.filePath">
        <FileChoose v-model="form.config.filePath" />
      </FormItem>
    </Form>
    <div slot="footer">
      <Button type="primary"
        @click="onSubmit">确定</Button>
      <Button @click="closeModal">取消</Button>
    </div>
    <Spin size="large"
      fix
      v-if="load" />
  </Modal>
</template>

<script>
import FileChoose from '../FileChoose'

export default {
  props: {
    request: {
      type: Object
    },
    response: {
      type: Object
    }
  },
  data() {
    return {
      load: false,
      form: {
        request: this.request,
        response: this.response,
        config: {
          filePath: '',
          connections: 32,
          timeout: 0,
          retryCount: 0,
          autoRename: false
        }
      },
      rules: {
        'response.fileName': [{ required: true, message: '不能为空' }],
        'config.filePath': [{ required: true, message: '不能为空' }]
      }
    }
  },
  watch: {
    request() {
      this.form.request = this.request
      this.form.response = this.response
    }
  },
  computed: {
    visible() {
      if (this.request && this.response) {
        return true
      } else {
        return false
      }
    }
  },
  components: {
    FileChoose
  },
  methods: {
    closeModal() {
      this.$emit('close')
    },
    onSubmit() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.load = true
          this.$http
            .post('http://127.0.0.1:26339/tasks', this.form)
            .then(() => {
              this.$router.push('/')
            })
            .finally(() => {
              this.load = false
            })
        }
      })
    }
  }
}
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
