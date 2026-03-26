export const API_BASE_URL = "http://localhost:8080/api";

export const ROLES = {
  ADMIN: "ADMIN",
  USER: "USER",
};

export const STOCK_STATUS = {
  IN_STOCK: "IN_STOCK",
  LOW_STOCK: "LOW_STOCK",
  OUT_OF_STOCK: "OUT_OF_STOCK",
};

export const STOCK_STATUS_COLORS = {
  [STOCK_STATUS.IN_STOCK]: "success",
  [STOCK_STATUS.LOW_STOCK]: "warning",
  [STOCK_STATUS.OUT_OF_STOCK]: "danger",
};

export const STOCK_STATUS_LABELS = {
  [STOCK_STATUS.IN_STOCK]: "In Stock",
  [STOCK_STATUS.LOW_STOCK]: "Low Stock",
  [STOCK_STATUS.OUT_OF_STOCK]: "Out of Stock",
};

export const MOVEMENT_TYPES = {
  IN: "IN",
  OUT: "OUT",
  ADJUSTMENT: "ADJUSTMENT",
};

export const AUDIT_ACTIONS = {
  CREATE: "CREATE",
  UPDATE: "UPDATE",
  DELETE: "DELETE",
  LOGIN: "LOGIN",
  LOGOUT: "LOGOUT",
};

export const ROUTES = {
  LOGIN: "/login",
  REGISTER: "/register",
  DASHBOARD: "/dashboard",
  PRODUCTS: "/products",
  LOW_STOCK: "/low-stock",
  ALERTS: "/alerts",
  REPORTS: "/reports",
  USERS: "/users",
  AUDIT_LOGS: "/audit-logs",
  SETTINGS: "/settings",
};

export const NAVIGATION_ITEMS = [
  {
    name: "Dashboard",
    href: ROUTES.DASHBOARD,
    icon: "HomeIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
  {
    name: "Products",
    href: ROUTES.PRODUCTS,
    icon: "CubeIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
  {
    name: "Low Stock",
    href: ROUTES.LOW_STOCK,
    icon: "ExclamationTriangleIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
  {
    name: "Alerts",
    href: ROUTES.ALERTS,
    icon: "BellIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
  {
    name: "Reports",
    href: ROUTES.REPORTS,
    icon: "ChartBarIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
  {
    name: "Users",
    href: ROUTES.USERS,
    icon: "UsersIcon",
    roles: [ROLES.ADMIN],
  },
  {
    name: "Audit Logs",
    href: ROUTES.AUDIT_LOGS,
    icon: "DocumentTextIcon",
    roles: [ROLES.ADMIN],
  },
  {
    name: "Settings",
    href: ROUTES.SETTINGS,
    icon: "CogIcon",
    roles: [ROLES.USER, ROLES.ADMIN],
  },
];

export const DASHBOARD_REFRESH_INTERVAL = 30000; // 30 seconds
