import Vue from "vue";
import Router from "vue-router";
import Tasks from "./views/Tasks.vue";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: "/",
      name: "tasks",
      component: Tasks
    }
  ]
});
