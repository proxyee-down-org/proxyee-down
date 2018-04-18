import Vue from 'vue'
import Util from '../../common/util'

export default {
  namespaced: true,
  state: {
    tasks: [],
    newTaskStatus: 0, //0.无 1.创建任务 2.开始任务
    cellSize: 3,
    initFlag: true,
    newTaskId: null,
  },
  mutations: {
    setInitFlag(state, initFlag) {
      state.initFlag = initFlag;
    },
    setNewTaskStatus(state, newTaskStatus) {
      state.newTaskStatus = newTaskStatus;
    },
    setNewTaskId(state, newTaskId) {
      state.newTaskId = newTaskId;
    },
    delTask(state, taskId) {
      for (let i = 0; i < state.tasks.length; i++) {
        if (state.tasks[i].id == taskId) {
          state.tasks.splice(i, 1);
          break;
        }
      }
    },
    setTasks(state, tasks) {
      if (tasks) {
        tasks.forEach(task1 => {
          let matchIndex = Util.inArray(state.tasks, task1, (arrObj, obj) => {
            return arrObj.id == obj.id
          });
          if (matchIndex != -1) {
            let task2 = state.tasks[matchIndex];
            task1.chunkInfoList.forEach((chunk, index) => {
              chunk.intervalTime = chunk.lastTime
                - task2.chunkInfoList[index].lastTime;
              chunk.intervalDownSize = chunk.downSize
                - task2.chunkInfoList[index].downSize;
              chunk.speedCount = task2.chunkInfoList[index].speedCount;
              if (chunk.intervalDownSize == 0) {
                if (!chunk.speedCount) {
                  chunk.speedCount = 1;
                } else {
                  chunk.speedCount++;
                }
              } else {
                chunk.speedCount = 1;
              }
            });
            Vue.set(state.tasks, matchIndex, task1);
          } else {
            state.tasks.push(task1);
          }
        });
        state.tasks = state.tasks.sort((task1, task2) => {
          return task2.startTime - task1.startTime;
        });
      }
    }
  }
}

