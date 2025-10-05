// src/components/SideBar.tsx
import React from "react";
import { motion, type Variants } from "framer-motion";
import { useAuth } from "../context/AuthContext";

interface SideBarProps {
  onNewNote: () => void;
  onMenuClick: (menu: string) => void;
}

// Variants for sidebar container
const sidebarVariants: Variants = {
  hidden: { x: -100, opacity: 0 },
  show: {
    x: 0,
    opacity: 1,
    transition: {
      type: "spring",
      stiffness: 120,
      damping: 15,
      mass: 0.9,
      staggerChildren: 0.15, // stagger the children
      delayChildren: 0.1,
    },
  },
};

// Variants for children (slide in slightly)
const childVariants: Variants = {
  hidden: { x: -30, opacity: 0 },
  show: {
    x: 0,
    opacity: 1,
    transition: {
      type: "spring",
      stiffness: 150,
      damping: 18,
    },
  },
};

function SideBar({ onNewNote, onMenuClick }: SideBarProps) {
  const { logout } = useAuth();

  const handleMenuClick = (menu: string) => {
    if (menu === "Logout") {
      logout();
    } else {
      onMenuClick(menu);
    }
  };

  return (
    <motion.div
      className="flex flex-col gap-12 p-2"
      variants={sidebarVariants}
      initial="hidden"
      animate="show"
    >
      {/* + Note Button */}
      <motion.div variants={childVariants}>
        <button
          onClick={onNewNote}
          className="flex roboto p-2 rounded-lg text-white md:text-3xl font-light bg-cyan-400 cursor-pointer hover:bg-cyan-500 transition"
        >
          + Note
        </button>
      </motion.div>

      {/* Menu Section */}
      <motion.div variants={childVariants}>
        <div className="w-full outfit flex flex-col bg-white rounded-lg items-left p-2 gap-2 shadow-md">
          <h1 className="flex items-center justify-center font-semibold text-xl text-black">
            Menu
          </h1>
          {["Dashboard", "My Notes", "Shared Notes", "Logout"].map(
            (menu, i) => (
              <button
                key={i}
                onClick={() => handleMenuClick(menu)}
                className="p-2 bg-gray-300 rounded-xl text-black font-medium cursor-pointer hover:bg-gray-400 transition"
              >
                {menu}
              </button>
            )
          )}
        </div>
      </motion.div>

      {/* Activity Section */}
      <motion.div variants={childVariants}>
        <div className="outfit flex flex-col bg-white rounded-lg items-left p-2 gap-2 shadow-md">
          <h1 className="flex items-center justify-center font-semibold text-xl text-black">
            Activity
          </h1>
          {[
            "Edited Note-1 @ 6:30PM",
            "Edited Note-2 @ 7:30PM",
            "Edited Note-3 @ 8:30PM",
            "Edited Note-4 @ 9:30PM",
          ].map((activity, i) => (
            <button
              key={i}
              className="p-2 bg-gray-300 rounded-xl text-black font-medium cursor-pointer hover:bg-gray-400 transition"
            >
              {activity}
            </button>
          ))}
        </div>
      </motion.div>
    </motion.div>
  );
}

export default SideBar;
