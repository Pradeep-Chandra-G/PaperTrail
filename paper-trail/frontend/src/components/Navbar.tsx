// src/components/Navbar.tsx (UPDATED)
import { useState, useRef, useEffect } from "react";
import { Search, X } from "lucide-react";
import { motion, type Variants } from "framer-motion";
import { useAuth } from "../context/AuthContext";

const containerVariants: Variants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.15,
      delayChildren: 0.3,
    },
  },
};

const itemVariants: Variants = {
  hidden: { opacity: 0, y: -20 },
  show: {
    opacity: 1,
    y: 0,
    transition: { type: "spring", stiffness: 300, damping: 15 },
  },
};

function Navbar() {
  const [mobileSearchOpen, setMobileSearchOpen] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated && mobileSearchOpen && inputRef.current) {
      inputRef.current.focus();
    }
  }, [mobileSearchOpen, isAuthenticated]);

  const getUserInitial = () => {
    if (user?.name) {
      return user.name.charAt(0).toUpperCase();
    }
    if (user?.email) {
      return user.email.charAt(0).toUpperCase();
    }
    return "U";
  };

  return (
    <motion.div
      className="font-semibold text-white text-lg md:pl-4 md:pr-4"
      variants={containerVariants}
      initial="hidden"
      animate="show"
    >
      <motion.div
        className="p-8 flex items-center justify-between"
        variants={itemVariants}
      >
        <h1 className="text-2xl md:text-4xl tracking-widest outfit">
          PaperTrail
        </h1>

        {isAuthenticated && (
          <motion.button
            className="md:hidden bg-white p-2 rounded-full text-black"
            onClick={() => setMobileSearchOpen(true)}
            variants={itemVariants}
          >
            <Search className="w-5 h-5" />
          </motion.button>
        )}

        <motion.div className="flex gap-8" variants={itemVariants}>
          {isAuthenticated && (
            <>
              <div className="hidden md:flex items-center bg-white p-2 rounded-full">
                <input
                  type="text"
                  placeholder="Search..."
                  className="px-2 inter bg-white text-black rounded-full focus:outline-none"
                />
                <button className="cursor-pointer">
                  <Search className="text-black w-6 h-6" />
                </button>
              </div>

              <div className="rounded-full w-12 h-12 bg-white flex items-center justify-center">
                <h1 className="text-black font-bold">{getUserInitial()}</h1>
              </div>
            </>
          )}
        </motion.div>
      </motion.div>

      {isAuthenticated && mobileSearchOpen && (
        <motion.div
          className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-start justify-center p-4 md:hidden"
          onClick={() => setMobileSearchOpen(false)}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.5 }}
        >
          <motion.div
            className="w-full bg-white rounded-full flex items-center px-3 py-2 shadow-md"
            onClick={(e) => e.stopPropagation()}
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", stiffness: 250, damping: 15 }}
          >
            <Search className="w-5 h-5 text-gray-500" />
            <input
              ref={inputRef}
              type="text"
              placeholder="Search..."
              className="ml-2 inter flex-1 bg-transparent text-black text-sm focus:outline-none"
            />
            <button onClick={() => setMobileSearchOpen(false)}>
              <X className="w-5 h-5 text-gray-600" />
            </button>
          </motion.div>
        </motion.div>
      )}
    </motion.div>
  );
}

export default Navbar;
