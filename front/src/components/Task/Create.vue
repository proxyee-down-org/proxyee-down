<template>
  <Modal :title="$t('tasks.createTask')"
    :value="visible"
    @input="closeModal"
    @on-visible-change="loadConfig"
    :closable="false"
    :mask-closable="false">
    <Form v-if="visible"
      ref="form"
      :model="form"
      :rules="rules"
      :label-width="70">
      <FormItem :label="$t('tasks.fileName')"
        prop="response.fileName">
        <Input v-model="form.response.fileName" />
      </FormItem>
      <FormItem :label="$t('tasks.fileSize')">{{ $numeral(form.response.totalSize).format('0.00b') }}</FormItem>
      <FormItem :label="$t('tasks.connections')"
        prop="config.connections">
        <Slider v-if="response.supportRange"
          v-model="form.config.connections"
          :min="2"
          :max="256"
          :step="2"
          show-input />
        <Slider v-else
          disabled
          v-model="form.config.connections"
          :min="1"
          :max="1"
          show-input />
      </FormItem>
      <FormItem :label="$t('tasks.filePath')"
        prop="config.filePath">
        <FileChoose v-model="form.config.filePath" />
      </FormItem>
    </Form>
    <div slot="footer">
      <Button type="primary"
        @click="onSubmit">{{ $t('tip.ok') }}</Button>
      <Button @click="closeModal">{{ $t('tip.cancel') }}</Button>
    </div>
    <Spin size="large"
      fix
      v-if="load" />
  </Modal>
</template>

<script>
import FileChoose from '../FileChoose'

let defaultConfig = {}

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
        config: {}
      },
      rules: {
        'response.fileName': [{ required: true, message: this.$t('tip.notNull') }],
        'config.filePath': [{ required: true, message: this.$t('tip.notNull') }]
      }
    }
  },
  watch: {
    request() {
      this.form.request = this.request
      this.form.response = this.response
      this.form.config = { ...defaultConfig }
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
    },
    loadConfig(visible) {
      if (visible) {
        this.$http.get('http://127.0.0.1:26339/config').then(result => {
          const serverConfig = result.data
          defaultConfig = {
            filePath: serverConfig.filePath,
            connections: serverConfig.connections,
            timeout: serverConfig.timeout,
            retryCount: serverConfig.retryCount,
            autoRename: serverConfig.autoRename,
            speedLimit: serverConfig.speedLimit
          }
          this.form.config = { ...defaultConfig }
        })
      }
    }
  },
  created() {
    this.loadConfig(this.visible)
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
