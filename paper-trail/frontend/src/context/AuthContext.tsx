// paper-trail/frontend/src/context/AuthContext.tsx
import React, { createContext, useContext, useState, useEffect } from "react";
import { authAPI, type User } from "../services/api";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const sessionInfo = await authAPI.getSessionInfo();
      // Parse session info if needed - adjust based on your API response
      if (sessionInfo && !sessionInfo.includes("No authenticated user")) {
        // Extract user info from session
        const emailMatch = sessionInfo.match(/User: ([^,]+)/);
        if (emailMatch) {
          setUser({ id: 0, email: emailMatch[1], name: emailMatch[1] });
        }
      }
    } catch (error) {
      console.error("Auth check failed:", error);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    const response = await authAPI.login({ email, password });
    if (response.includes("Login successful")) {
      // After successful login, check session to get user info
      await checkAuth();
    } else {
      throw new Error("Login failed");
    }
  };

  const register = async (name: string, email: string, password: string) => {
    await authAPI.register({ name, email, password });
    // Auto login after registration
    await login(email, password);
  };

  const logout = async () => {
    await authAPI.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};
