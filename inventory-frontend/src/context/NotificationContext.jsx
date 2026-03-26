import { createContext, useContext, useState, useEffect } from "react";
import { notificationService } from "../services/notificationService";
import { useAuth } from "./AuthContext";

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const { isAuthenticated, isLoading } = useAuth();

  // Fetch notifications and unread count
  const fetchNotifications = async () => {
    // Only fetch if user is authenticated
    if (!isAuthenticated) {
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    try {
      const allNotifications = await notificationService.getNotifications();
      const unreadCount = await notificationService.getUnreadCount();

      setNotifications(allNotifications);
      setUnreadCount(unreadCount);
    } catch (error) {
      console.error("Error fetching notifications:", error);
      // Reset state on error (likely auth issue)
      setNotifications([]);
      setUnreadCount(0);
    }
  };

  // Mark notification as read
  const markAsRead = (notificationId) => {
    notificationService.markAsRead(notificationId);
    setNotifications(
      notifications.map((n) =>
        n.id === notificationId ? { ...n, isRead: true } : n,
      ),
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
  };

  // Mark all notifications as read
  const markAllAsRead = () => {
    notificationService.markAllAsRead(notifications);
    setNotifications(notifications.map((n) => ({ ...n, isRead: true })));
    setUnreadCount(0);
  };

  // Refresh notifications (useful after new alerts are created)
  const refreshNotifications = () => {
    fetchNotifications();
  };

  useEffect(() => {
    // Only start fetching notifications after auth is initialized and user is authenticated
    if (isLoading) return;

    if (isAuthenticated) {
      // Add a small delay to ensure auth token is properly set
      const timer = setTimeout(() => {
        fetchNotifications();
      }, 100);

      // Set up auto-refresh every 30 seconds only when authenticated
      const interval = setInterval(fetchNotifications, 30000);

      return () => {
        clearTimeout(timer);
        clearInterval(interval);
      };
    } else {
      // Clear notifications when not authenticated
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [isAuthenticated, isLoading]);

  const value = {
    notifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    refreshNotifications,
    fetchNotifications,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error(
      "useNotifications must be used within a NotificationProvider",
    );
  }
  return context;
};
