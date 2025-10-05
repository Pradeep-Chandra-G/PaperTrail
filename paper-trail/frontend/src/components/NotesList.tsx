// src/components/NotesList.tsx
import React, { useCallback, useState } from "react";
import { motion } from "framer-motion";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notesAPI, type NoteDTO } from "../services/api";
import { Trash2, Eye, Share2 } from "lucide-react";

interface NotesListProps {
  type: "my" | "shared";
  onSelectNote?: (noteId: number) => void;
  onShareNote?: (noteId: number) => void; // NEW
}

const NotesList: React.FC<NotesListProps> = ({
  type,
  onSelectNote,
  onShareNote,
}) => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<number | null>(null);

  const { data, isLoading, error } = useQuery<NoteDTO[]>({
    queryKey: type === "my" ? ["my-notes"] : ["shared-notes"],
    queryFn: () =>
      type === "my" ? notesAPI.getMyNotes() : notesAPI.getSharedNotes(),
  });

  const notes = data ?? [];

  const deleteMutation = useMutation({
    mutationFn: (noteId: number) => notesAPI.deleteNote(noteId),
    onSuccess: (_, noteId) => {
      queryClient.setQueryData<NoteDTO[]>(
        [type === "my" ? "my-notes" : "shared-notes"],
        (oldNotes) => oldNotes?.filter((n) => n.id !== noteId) || []
      );
      setPendingDelete(null);
    },
  });

  const handleDelete = useCallback(
    (noteId: number, e: React.MouseEvent) => {
      e.stopPropagation();
      if (pendingDelete === noteId) {
        deleteMutation.mutate(noteId);
      } else {
        setPendingDelete(noteId);
        setTimeout(() => {
          if (pendingDelete === noteId) setPendingDelete(null);
        }, 3000);
      }
    },
    [deleteMutation, pendingDelete]
  );

  const formatDate = useCallback((dateString?: string) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  }, []);

  if (isLoading)
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500" />
      </div>
    );

  if (error)
    return (
      <div className="bg-red-50 text-red-600 p-4 rounded-lg">
        {error?.message || "Failed to load notes"}
      </div>
    );

  if (notes.length === 0)
    return (
      <div className="bg-white rounded-lg shadow p-6 text-center">
        <h2 className="font-semibold text-xl mb-2">Notes</h2>
        <p className="text-gray-500">
          {type === "my"
            ? "Create your first note to get started!"
            : "No notes have been shared with you yet."}
        </p>
      </div>
    );

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: "spring", stiffness: 200, damping: 20 }}
      className="outfit bg-white rounded-lg shadow p-6 md:mt-0 md:p-6 w-full md:h-150 overflow-y-auto"
    >
      <h2 className="flex items-center justify-center font-semibold text-3xl mb-4">
        Notes
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {notes.map((note) => (
          <motion.div
            key={note.id}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.05 }}
            onClick={() => onSelectNote?.(note.id!)}
            className="bg-gray-50 rounded-lg shadow p-4 cursor-pointer hover:shadow-md border border-gray-200 relative"
          >
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-semibold text-lg truncate flex-1 text-black">
                {note.title}
              </h3>
              {type === "shared" && <Eye className="w-4 h-4 text-gray-400" />}
            </div>

            <p className="text-sm text-gray-600 mb-3">
              {note.createdBy && `By ${note.createdBy}`}
            </p>

            <div className="flex justify-between items-center text-xs text-gray-500 gap-2">
              <span>Updated: {formatDate(note.updatedAt)}</span>

              {type === "my" && note.id && (
                <div className="flex gap-2">
                  {/* Delete Button */}
                  <button
                    onClick={(e) => handleDelete(note.id!, e)}
                    className={`p-1 rounded transition relative ${
                      pendingDelete === note.id
                        ? "bg-red-100"
                        : "hover:bg-red-50"
                    }`}
                  >
                    <Trash2
                      className={`w-4 h-4 cursor-pointer ${
                        pendingDelete === note.id
                          ? "text-red-700"
                          : "text-red-500"
                      }`}
                    />
                    {pendingDelete === note.id && (
                      <span className="absolute -top-6 left-1/2 transform -translate-x-1/2 text-xs bg-black text-white px-2 py-1 rounded">
                        Click again to delete
                      </span>
                    )}
                  </button>

                  {/* Share Button */}
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onShareNote?.(note.id!);
                    }}
                    className="p-1 hover:bg-blue-50 rounded transition relative"
                  >
                    <Share2 className="w-4 h-4 cursor-pointer text-blue-500" />
                  </button>
                </div>
              )}
            </div>
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
};

export default NotesList;
