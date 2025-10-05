// src/components/ShareNote.tsx
import React, { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { notesAPI } from "../services/api";

interface ShareNoteProps {
  noteId: number;
  onClose: () => void;
}

const ShareNote: React.FC<ShareNoteProps> = ({ noteId, onClose }) => {
  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState<"READ" | "EDIT">("READ");
  const [status, setStatus] = useState<string | null>(null);

  const shareMutation = useMutation({
    mutationFn: () => notesAPI.shareNote(noteId, email, permission),
    onSuccess: () => {
      setStatus("Note shared successfully!");
      setTimeout(onClose, 1500);
    },
    onError: (err: any) => {
      setStatus(err?.response?.data || "Error sharing note");
    },
  });

  const handleShare = () => {
    if (!email) {
      setStatus("Please enter an email");
      return;
    }
    setStatus(null);
    shareMutation.mutate();
  };

  return (
    <div className="outfit bg-white rounded-lg shadow-md p-6 w-full h-full flex flex-col">
      <h2 className="text-2xl font-semibold mb-4 text-black">Share Note</h2>

      <input
        type="email"
        placeholder="Recipient's email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        className="border border-gray-300 rounded px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-300"
      />

      <label className="mb-2 text-sm text-gray-700">Permission</label>
      <select
        value={permission}
        onChange={(e) => setPermission(e.target.value as "READ" | "EDIT")}
        className="border border-gray-300 rounded px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-300"
      >
        <option value="READ">Read</option>
        <option value="EDIT">Edit</option>
      </select>

      <div className="flex gap-2">
        <button
          onClick={handleShare}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition"
        >
          Share
        </button>
        <button
          onClick={onClose}
          className="bg-gray-300 text-black px-4 py-2 rounded hover:bg-gray-400 transition"
        >
          Cancel
        </button>
      </div>

      {status && <p className="mt-3 text-sm text-red-500">{status}</p>}
    </div>
  );
};

export default ShareNote;
