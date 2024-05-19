import { createRouter, createWebHistory } from 'vue-router';
import indexpage from './page/indexpage.vue';
import TerminosDeUso from './page/TerminosUsos.vue';



const routes = [
  {
    path: '/',
    name: 'Home',
    component:indexpage,
  },
  {
    path: '/terminos',
    name: 'TerminosDeUso',
    component: TerminosDeUso
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
