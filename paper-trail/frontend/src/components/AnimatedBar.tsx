import { motion } from "framer-motion";

interface AnimatedBarProps {
  orientation?: "horizontal" | "vertical";
}

export default function AnimatedBar({
  orientation = "horizontal",
}: AnimatedBarProps) {
  const isHorizontal = orientation === "horizontal";

  return (
    <div className="flex items-center justify-center w-full h-full">
      <motion.div
        className={`
          bg-white 
          ${
            isHorizontal
              ? "w-[85%] md:w-[90%] h-1 md:h-2"
              : "h-[90%] w-1 md:w-2"
          }
        `}
        initial={isHorizontal ? { scaleX: 0 } : { scaleY: 0 }}
        animate={isHorizontal ? { scaleX: 1 } : { scaleY: 1 }}
        transition={{ duration: 1.0, ease: "easeOut" }}
        style={{ transformOrigin: "center" }}
      />
    </div>
  );
}
