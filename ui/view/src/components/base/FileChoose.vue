<template>
  <div style="display: inline">
    <el-input class="file-choose-input"
              :value="value"
              :disabled="disabled"
              @input="$emit('input', arguments[0])"
              @dblclick.native="visible = true"></el-input>
    <el-button type="primary"
               :disabled="disabled"
               @click="visible=true">选择
    </el-button>
    <el-dialog title="目录浏览"
               :visible="visible"
               width="30%"
               :append-to-body="true"
               @close="visible = false">
               <div class="fast-navigation">
                  <el-button plain @click="setFrequentlyUsedDirectory('Desktop')">桌面目录</el-button>
                  <el-button plain @click="setFrequentlyUsedDirectory('Downloads')">下载目录</el-button>
               </div>
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
                   @click="visible = false; $emit('input', $refs.tree.getCurrentNode().path)">确 定
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
    props: ['value', 'model', 'disabled'],
    methods: {
      loadChild (node, resolve) {
        this.$http.post('api/getChildDirList',
          {
            path: node.data.path || '',
            model: this.model || 'dir',
          })
        .then((result) => {
          if (result.data) {
            resolve(result.data);
          }
        }).catch(() => {});
      },
      setFrequentlyUsedDirectory (dir) {
        this.$http.get(`api/getFrequentlyUsedDirectory?dir=${dir}`)
        .then(result => {
          this.visible = false;
          this.$emit('input', result.data)
        })
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .file-choose-input {
    width: 50%
  }

  .file-choose-tree {
    height: 300px;
    overflow-y: auto;
  }

  .fast-navigation {
    margin: -15px 0 20px 0;
  }
</style>
