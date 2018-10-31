<template>
  <Form :label-width="70">
    <FormItem v-for="(setting,index) in settings"
      :key="index"
      :label="setting.title"
      :prop="setting.name">
      <Switch v-if="setting.type==='Boolean'"
        v-model="setting.value" />
      <Select v-else-if="setting.options"
        style="width:90%"
        v-model="setting.value">
        <Option v-for="(key,label) in setting.options"
          :key="key"
          :value="key">{{label}}</Option>
      </Select>
      <Input v-else
        style="width:90%"
        v-model="setting.value" />
      <Tooltip class="item"
        placement="right">
        <Icon type="help-circled"
          class="action-icon tip-icon" />
        <div slot="content">
          <p>{{ setting.description }}</p>
        </div>
      </Tooltip>
    </FormItem>
  </Form>
</template>

<script>
export default {
  props: {
    settings: {
      type: Array
    }
  }
}
</script>

