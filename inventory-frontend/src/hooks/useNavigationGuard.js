import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export const useNavigationGuard = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    // If user is authenticated and tries to access public routes, redirect to dashboard
    if (isAuthenticated) {
      const publicRoutes = ["/", "/login", "/register", "/forgot-password"];
      if (publicRoutes.includes(location.pathname)) {
        navigate("/dashboard", { replace: true });
      }
    }
  }, [isAuthenticated, location.pathname, navigate]);

  // Handle browser back button for authenticated users
  useEffect(() => {
    if (isAuthenticated) {
      const handlePopState = (event) => {
        const currentPath = window.location.pathname;
        const publicRoutes = ["/", "/login", "/register", "/forgot-password"];

        // If back button leads to a public route while authenticated, redirect to dashboard
        if (publicRoutes.includes(currentPath)) {
          event.preventDefault();
          navigate("/dashboard", { replace: true });
        }
      };

      // Listen for browser back/forward button events
      window.addEventListener("popstate", handlePopState);

      return () => {
        window.removeEventListener("popstate", handlePopState);
      };
    }
  }, [isAuthenticated, navigate]);
};
