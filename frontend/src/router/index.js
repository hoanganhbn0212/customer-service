import { createRouter, createWebHistory } from "vue-router";
import { canEditPages, canManageUsers, isAuthenticated } from "../auth/session";
import LoginView from "../views/LoginView.vue";
import HomeView from "../views/HomeView.vue";
import DashboardView from "../views/DashboardView.vue";
import UsersAdminView from "../views/UsersAdminView.vue";
import LoginThemeAdminView from "../views/LoginThemeAdminView.vue";
import SubscriptionsAdminView from "../views/SubscriptionsAdminView.vue";
import ServicesView from "../views/ServicesView.vue";
import ServiceReviewView from "../views/ServiceReviewView.vue";
import NotificationsView from "../views/NotificationsView.vue";
import AccountView from "../views/AccountView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: () => (isAuthenticated() ? "/home" : "/login")
    },
    { path: "/login", name: "login", component: LoginView, meta: { guest: true } },
    {
      path: "/home",
      name: "home",
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: "/services",
      name: "services",
      component: ServicesView,
      meta: { requiresAuth: true }
    },
    {
      path: "/services/review/:deliverableId",
      name: "service-review",
      component: ServiceReviewView,
      meta: { requiresAuth: true }
    },
    {
      path: "/notifications",
      name: "notifications",
      component: NotificationsView,
      meta: { requiresAuth: true }
    },
    {
      path: "/account",
      name: "account",
      component: AccountView,
      meta: { requiresAuth: true }
    },
    {
      path: "/customers",
      name: "customers",
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: "/admin/users",
      name: "admin-users",
      component: UsersAdminView,
      meta: { requiresAuth: true, requiresManageUsers: true }
    },
    {
      path: "/admin/theme",
      name: "admin-theme",
      component: LoginThemeAdminView,
      meta: { requiresAuth: true, requiresPageEditor: true }
    },
    {
      path: "/admin/subscriptions",
      name: "admin-subscriptions",
      component: SubscriptionsAdminView,
      meta: { requiresAuth: true, requiresManageUsers: true }
    },
    { path: "/dashboard", redirect: "/home" }
  ]
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return { name: "login" };
  }
  if (to.meta.requiresManageUsers && !canManageUsers()) {
    return { name: "home" };
  }
  if (to.meta.requiresPageEditor && !canEditPages()) {
    return { name: "home" };
  }
  if (to.meta.guest && isAuthenticated()) {
    return { name: "home" };
  }
});

export default router;
