import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CorpusCollection from "@/views/CorpusCollection";
import LoginView from "@/views/LoginView";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/collect-dialog-corpus',
    name: 'collection',
    component: CorpusCollection
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next)=>{
  if (to.path!="/"){
    let username = localStorage.getItem('username')
    if (!username){
      next('/')
    }
    else {
      next()
    }
  }
  else{
    let username = localStorage.getItem('username')
    if (username){
      next('/collect-dialog-corpus')
    }
  else
    next()
  }
  next()
})

export default router
