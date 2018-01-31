<template>
  <div>
    <el-button type="primary" class="file-choose-button" @click="visible=true">选择</el-button>
    <el-input class="file-choose-input" :value="value" @input="$emit('input',arguments[0])"
              @dblclick.native="visible = true"></el-input>
    <el-dialog title="目录浏览"
               :visible="visible"
               width="30%"
               :append-to-body="true"
               @close="visible = false">
      <el-tree :data="files"
               :props="props"
               :load="loadChild"
               :highlight-current="true"
               lazy
               ref="tree"
               class="file-choose-tree"></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary"
                   @click="visible = false;$emit('input',$refs.tree.getCurrentNode().path)">确 定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import Vue from 'vue'

  export default {
    data() {
      return {
        visible: false,
        files: [],
        props: {
          isLeaf: 'last'
        }
      }
    },
    props: ['value', 'model'],
    methods: {
      loadChild(node, resolve) {
        this.$http.post('api/getChildDirList',
          {
            path: node.data.path || '',
            model: this.model || 'dir',
          })
        .then((result) => {
          if (result.data) {
            resolve(result.data);
          }
        }).catch(() => {
        });
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .file-choose-input {
    width: 50%
  }

  .file-choose-button {
    position: absolute;
    z-index: 1;
    right: 50%
  }

  .file-choose-tree {
    overflow-y: auto;
    height: 300px;
  }
</style>
