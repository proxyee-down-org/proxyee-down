<template>
  <Modal :title="$t('tasks.createTask')"
    :value="value"
    @input="$emit('input', arguments[0])"
    :closable="false"
    :mask-closable="false"
    @on-visible-change="onReset">
    <Form ref="form"
      :rules="rules"
      :model="form"
      :label-width="60">
      <FormItem :label="$t('tasks.method')"
        prop="method">
        <Select v-model="form.method"
          style="width:70px;">
          <Option value="GET">GET</Option>
          <Option value="POST">POST</Option>
        </Select>
      </FormItem>
      <FormItem :label="$t('tasks.url')"
        prop="url">
        <Input v-model="form.url" />
      </FormItem>
      <FormItem :label="$t('tasks.option')">
        <Checkbox v-model="hasHead">{{ $t('tasks.head') }}</Checkbox>
        <Checkbox v-model="hasBody">{{ $t('tasks.body') }}</Checkbox>
      </FormItem>
      <FormItem v-show="hasHead"
        :label="$t('tasks.head')"
        prop="heads">
        <div v-for="(head, index) in form.heads"
          :key="index"
          :class="index === 0 ? null : 'head-margin' ">
          <Input class="head-input"
            v-model="head.key"
            placeholder="key" />
          <Input class="head-input"
            v-model="head.value"
            placeholder="value" />
          <Icon v-if="index !== 0"
            type="minus-circled"
            @click="delHead(index)"></Icon>
          <Icon v-if="index === form.heads.length - 1"
            type="plus-circled"
            @click="addHead"></Icon>
        </div>
      </FormItem>
      <FormItem v-show="hasBody"
        :label="$t('tasks.body')"
        prop="body">
        <Input type="textarea"
          :autosize="{ minRows: 2, maxRows: 4}"
          v-model="form.body" />
      </FormItem>
    </Form>
    <div slot="footer">
      <Button type="primary"
        @click="onSubmit">{{ $t('tip.ok') }}</Button>
      <Button @click="$emit('input', false)">{{ $t('tip.cancel') }}</Button>
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
      type: Boolean
    }
  },
  data() {
    return {
      load: false,
      hasHead: false,
      hasBody: false,
      form: {
        method: 'GET',
        url: '',
        heads: [],
        body: '',
        dir: ''
      },
      rules: {
        url: [
          { required: true, message: this.$t('tip.notNull') },
          { pattern: /^https?:\/\/.*$/i, message: this.$t('tip.fmtErr') }
        ]
      }
    }
  },
  watch: {
    hasHead(val) {
      if (val && this.form.heads.length === 0) {
        this.addHead()
      }
    }
  },
  methods: {
    addHead() {
      this.form.heads.push({ key: '', value: '' })
    },
    delHead(index) {
      this.form.heads.splice(index, 1)
    },
    onSubmit() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          const requestData = {
            method: this.form.method,
            url: this.form.url,
            heads: {},
            body: ''
          }
          if (this.hasHead) {
            for (let head of this.form.heads) {
              if (head.key && head.value) {
                requestData.heads[head.key] = head.value
              }
            }
          }
          if (this.hasBody) {
            requestData.body = this.form.body
          }
          this.load = true
          this.$http
            .put('http://127.0.0.1:26339/util/resolve', requestData)
            .then(result => {
              this.$emit('input', false)
              const request = JSON.stringify(result.data.request)
              const response = JSON.stringify(result.data.response)
              this.$router.push({
                path: '/',
                query: { request: request, response: response }
              })
            })
            .finally(() => {
              this.load = false
            })
        }
      })
    },
    onReset(visible) {
      if (visible) {
        this.$refs['form'].resetFields()
        this.hasHead = false
        this.hasBody = false
      }
    }
  }
}
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
