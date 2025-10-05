// src/App.tsx (UPDATED)
import "./App.css";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AnimatedBar from "./components/AnimatedBar";
import MainContent from "./components/MainContent";
import Navbar from "./components/Navbar";
import Login from "./components/Login";
import queryClient from "./services/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";

function AppContent() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-purple-600">
        <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-white"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  return (
    <div className="flex flex-col p-2">
      <Navbar />
      <AnimatedBar orientation="horizontal" />
      <MainContent />
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <QueryClientProvider client={queryClient}>
        <AppContent />
      </QueryClientProvider>
    </AuthProvider>
  );
}

export default App;
