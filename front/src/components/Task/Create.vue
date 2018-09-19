<template>
  <Modal :title="$t('tasks.createTask')"
    :value="visible"
    @input="closeModal"
    @on-visible-change="init"
    :closable="false"
    :mask-closable="false">
    <Form v-if="visible"
      ref="form"
      :model="form"
      :rules="rules"
      :label-width="70">
      <FormItem v-if="sameTasks.length>0"
        prop="taskId"
        :label="$t('tasks.sameTaskList')">
        <Select v-model="form.taskId"
          clearable
          @on-change="sameTaskChange"
          :placeholder="$t('tasks.sameTaskPlaceholder')">
          <Option v-for="task in sameTasks"
            :key="task.id"
            :label="task.config.filePath+getFileSeparator()+task.response.fileName"
            :value="task.id">
          </Option>
        </Select>
      </FormItem>
      <template v-if="!selectOldTask">
        <FormItem :label="$t('tasks.fileName')"
          prop="response.fileName">
          <Input :disabled="disabledForm"
            v-model="form.response.fileName" />
        </FormItem>
        <FormItem :label="$t('tasks.fileSize')">{{ form.response.totalSize?$numeral(form.response.totalSize).format('0.00b'):$t('tasks.unknowLeft') }}</FormItem>
        <FormItem :label="$t('tasks.connections')"
          prop="config.connections">
          <Slider v-if="response.supportRange"
            v-model="form.config.connections"
            :disabled="disabledForm"
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
          <FileChoose :disabled="disabledForm"
            v-model="form.config.filePath" />
        </FormItem>
      </template>
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
      selectOldTask: false,
      disabledForm: false,
      form: {
        taskId: undefined,
        request: this.request,
        response: this.response,
        config: {}
      },
      rules: {
        taskId: [{ required: true, message: this.$t('tip.notNull') }],
        'response.fileName': [{ required: true, message: this.$t('tip.notNull') }],
        'config.filePath': [{ required: true, message: this.$t('tip.notNull') }]
      },
      sameTasks: []
    }
  },
  watch: {
    request() {
      this.form.request = this.request
      this.form.response = this.response
      this.setDefaultConfig()
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
          if (this.form.taskId) {
            //refresh download request
            this.$http
              .put('http://127.0.0.1:26339/tasks/' + this.form.taskId, this.form.request)
              .then(() => {
                this.$router.push('/')
              })
              .finally(() => {
                this.load = false
              })
          } else {
            //create download task
            this.$http
              .post('http://127.0.0.1:26339/tasks', this.form)
              .then(() => {
                this.$router.push('/')
              })
              .finally(() => {
                this.load = false
              })
          }
        }
      })
    },
    async init(visible) {
      //reset params
      this.sameTasks = []
      this.form.taskId = undefined
      this.disabledForm = false
      if (visible) {
        //check same task
        const { data: downTasks } = await this.$http.get('http://127.0.0.1:26339/tasks?status=1,2,3')
        this.sameTasks = downTasks
          ? downTasks.filter(task => task.response.supportRange && task.response.totalSize === this.response.totalSize)
          : []
        if (this.sameTasks.length > 0) {
          const _this = this
          this.$Modal.confirm({
            title: _this.$t('tip.tip'),
            content: _this.$t('tasks.checkSameTask'),
            okText: _this.$t('tip.ok'),
            cancelText: _this.$t('tip.cancel'),
            onOk() {
              _this.selectOldTask = true
            },
            onCancel() {
              _this.sameTasks = []
            }
          })
        }
      }
    },
    getFileSeparator() {
      if (window.navigator.platform.indexOf('Win') != -1) {
        return '\\'
      } else {
        return '/'
      }
    },
    sameTaskChange(taskId) {
      const oldTask = this.sameTasks.find(task => task.id == taskId)
      if (oldTask) {
        this.form.config = { ...oldTask.config }
        this.selectOldTask = false
        this.disabledForm = true
      } else {
        this.selectOldTask = true
      }
    },
    setDefaultConfig() {
      this.$noSpinHttp.get('http://127.0.0.1:26339/config').then(result => {
        const serverConfig = result.data
        this.form.config = {
          filePath: serverConfig.filePath,
          connections: serverConfig.connections,
          timeout: serverConfig.timeout,
          retryCount: serverConfig.retryCount,
          autoRename: serverConfig.autoRename,
          speedLimit: serverConfig.speedLimit
        }
      })
    }
  },
  created() {
    this.setDefaultConfig()
    this.init(this.visible)
  }
}
</script>