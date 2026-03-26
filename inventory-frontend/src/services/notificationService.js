import { alertService } from "./alertService";

class NotificationService {
  constructor() {
    this.readNotifications = new Set(
      JSON.parse(localStorage.getItem("readNotifications") || "[]"),
    );
  }

  // Get all notifications (alerts)
  async getNotifications() {
    try {
      // Check if user is authenticated
      const token = localStorage.getItem("token");
      if (!token) {
        return [];
      }

      const alerts = await alertService.getAllAlerts();
      return alerts.map((alert) => ({
        ...alert,
        isRead: this.readNotifications.has(alert.id),
        type: "alert",
      }));
    } catch (error) {
      // If it's an auth error (401/403), return empty array silently
      if (
        error.message.includes("permission") ||
        error.message.includes("Session expired")
      ) {
        return [];
      }
      console.error("Error fetching notifications:", error);
      return [];
    }
  }

  // Get unread notifications count
  async getUnreadCount() {
    try {
      const notifications = await this.getNotifications();
      return notifications.filter((notification) => !notification.isRead)
        .length;
    } catch (error) {
      // Return 0 on any error
      return 0;
    }
  }

  // Mark notification as read
  markAsRead(notificationId) {
    this.readNotifications.add(notificationId);
    localStorage.setItem(
      "readNotifications",
      JSON.stringify([...this.readNotifications]),
    );
  }

  // Mark all notifications as read
  markAllAsRead(notifications) {
    notifications.forEach((notification) => {
      this.readNotifications.add(notification.id);
    });
    localStorage.setItem(
      "readNotifications",
      JSON.stringify([...this.readNotifications]),
    );
  }

  // Clear read notifications from localStorage (cleanup)
  clearOldReadNotifications() {
    // Keep only the last 100 read notification IDs to prevent localStorage bloat
    const readArray = [...this.readNotifications];
    if (readArray.length > 100) {
      const recent = readArray.slice(-100);
      this.readNotifications = new Set(recent);
      localStorage.setItem("readNotifications", JSON.stringify(recent));
    }
  }
}

export const notificationService = new NotificationService();
