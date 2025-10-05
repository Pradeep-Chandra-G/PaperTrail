// src/components/Dashboard.tsx
import React, { useMemo } from "react";
import { motion } from "framer-motion";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
  Legend,
} from "recharts";

interface DashboardProps {
  notesCreated: number[];
  notesShared: number[];
  labels: string[]; // e.g., ["Mon", "Tue", "Wed", ...]
  userName: string;
}

const Dashboard: React.FC<DashboardProps> = React.memo(
  ({ notesCreated, notesShared, labels, userName }) => {
    // Memoize chart data to avoid recalculation
    const data = useMemo(
      () =>
        labels.map((label, i) => ({
          name: label,
          Created: notesCreated[i] || 0,
          Shared: notesShared[i] || 0,
        })),
      [labels, notesCreated, notesShared]
    );

    // Memoize KPI calculations
    const [totalCreated, totalShared, totalThisWeek] = useMemo(() => {
      const created = notesCreated.reduce((a, b) => a + b, 0);
      const shared = notesShared.reduce((a, b) => a + b, 0);
      return [created, shared, created + shared];
    }, [notesCreated, notesShared]);

    return (
      <motion.div
        initial={{ x: 100, y: 100, opacity: 0 }}
        animate={{ x: 0, y: 0, opacity: 1 }}
        transition={{
          type: "spring",
          stiffness: 120,
          damping: 15,
          mass: 0.8,
        }}
        className="outfit p-6 w-full h-full flex flex-col items-center justify-start bg-white rounded-lg shadow-md"
      >
        <h2 className="text-3xl font-bold mb-4 text-black">
          Welcome, {userName}!
        </h2>
        <p className="text-lg text-gray-700 mb-6 text-center">
          Here's a quick overview of your notes activity
        </p>

        {/* KPI Cards */}
        <div className="flex w-full justify-between mb-6 gap-4">
          <KpiCard value={totalCreated} label="Notes Created" color="blue" />
          <KpiCard value={totalShared} label="Notes Shared" color="green" />
          <KpiCard
            value={totalThisWeek}
            label="Total This Week"
            color="purple"
          />
        </div>

        {/* Line Chart */}
        <div className="flex justify-center items-center w-full h-72">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="Created"
                stroke="#0ea5e9"
                strokeWidth={3}
                dot={{ r: 5 }}
                activeDot={{ r: 7 }}
                animationDuration={1500}
              />
              <Line
                type="monotone"
                dataKey="Shared"
                stroke="#22c55e"
                strokeWidth={3}
                dot={{ r: 5 }}
                activeDot={{ r: 7 }}
                animationDuration={1500}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </motion.div>
    );
  }
);

// KPI card as separate memoized component
interface KpiCardProps {
  value: number;
  label: string;
  color: "blue" | "green" | "purple";
}

const colorMap = {
  blue: { bg: "bg-blue-100", text: "text-blue-800" },
  green: { bg: "bg-green-100", text: "text-green-800" },
  purple: { bg: "bg-purple-100", text: "text-purple-800" },
};

const KpiCard: React.FC<KpiCardProps> = React.memo(
  ({ value, label, color }) => {
    const colors = colorMap[color];
    return (
      <div
        className={`${colors.bg} ${colors.text} rounded-lg p-4 flex flex-col items-center shadow-md flex-1`}
      >
        <span className="text-2xl font-bold">{value}</span>
        <span className="text-sm">{label}</span>
      </div>
    );
  }
);

export default Dashboard;
