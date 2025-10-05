import React, { useEffect, useRef, useState, useCallback } from "react";
import EditorJS from "@editorjs/editorjs";
import type { OutputData } from "@editorjs/editorjs";
import { motion } from "framer-motion";
import { notesAPI, imagesAPI } from "../services/api";

import Header from "@editorjs/header";
import List from "@editorjs/list";
import InlineCode from "@editorjs/inline-code";
import CodeTool from "@editorjs/code";
import Embed from "@editorjs/embed";
import ImageTool from "@editorjs/image";
import LinkTool from "@editorjs/link";
import Table from "@editorjs/table";
import Quote from "@editorjs/quote";
import Marker from "@editorjs/marker";

interface CreateNoteProps {
  noteId?: number;
  onSave?: () => void;
}

const CreateNote: React.FC<CreateNoteProps> = ({ noteId, onSave }) => {
  const editorRef = useRef<EditorJS | null>(null);
  const isInitialized = useRef(false);

  const [title, setTitle] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState("");

  // Stable save handler
  const handleSave = useCallback(async () => {
    if (!editorRef.current) return;

    setIsSaving(true);
    setSaveMessage("");

    try {
      const data: OutputData = await editorRef.current.save();

      if (noteId) {
        await notesAPI.updateNote(noteId, {
          title: title || "Untitled",
          content: data,
        });
        setSaveMessage("Note updated successfully!");
      } else {
        await notesAPI.createNote({
          title: title || "Untitled",
          content: data,
        });
        setSaveMessage("Note created successfully!");
      }

      onSave?.();
      setTimeout(() => setSaveMessage(""), 3000);
    } catch (error: any) {
      console.error("Save failed:", error);
      setSaveMessage(error.response?.data || "Failed to save note");
    } finally {
      setIsSaving(false);
    }
  }, [noteId, title, onSave]);

  // Editor initialization
  useEffect(() => {
    if (isInitialized.current) return;

    const initEditor = async () => {
      if (editorRef.current) return;

      let initialData: OutputData | undefined;
      if (noteId) {
        try {
          const note = await notesAPI.getNoteById(noteId);
          setTitle(note.title);
          initialData = note.content as OutputData;
        } catch (error) {
          console.error("Failed to load note:", error);
        }
      }

      const editor = new EditorJS({
        holder: "editorjs",
        autofocus: true,
        minHeight: 200,
        data: initialData,
        tools: {
          header: {
            class: Header as any,
            inlineToolbar: true,
            config: { placeholder: "Title" },
          },
          paragraph: {
            inlineToolbar: true,
            config: { placeholder: "Write your note..." },
          },
          list: { class: List, inlineToolbar: true },
          inlineCode: InlineCode,
          code: CodeTool,
          embed: Embed,
          image: {
            class: ImageTool,
            config: {
              uploader: {
                async uploadByFile(file: File) {
                  try {
                    const res = await imagesAPI.uploadImage(file);
                    return {
                      success: 1,
                      file: { url: res.url, publicId: res.publicId },
                    };
                  } catch (err) {
                    console.error("Image upload failed:", err);
                    return { success: 0, error: "Upload failed" };
                  }
                },
              },
            },
          },
          linkTool: LinkTool,
          table: Table,
          quote: Quote,
          marker: Marker,
        },
        onReady: () => {
          editorRef.current = editor;
          isInitialized.current = true;
        },
      });
    };

    initEditor();

    return () => {
      if (editorRef.current?.destroy) {
        editorRef.current.destroy();
        editorRef.current = null;
      }
    };
  }, [noteId]);

  useEffect(() => {
    console.log("Mounting editor", noteId);
    return () => {
      console.log("Destroying editor", noteId);
      editorRef.current?.destroy?.();
      editorRef.current = null;
    };
  }, [noteId]);

  return (
    <div className="flex flex-col h-full w-full p-4 sm:p-6 lg:p-8">
      <motion.div
        initial={{ opacity: 0, x: 100, y: 100 }}
        animate={{ opacity: 1, x: 0, y: 0 }}
        transition={{ type: "spring", stiffness: 300, damping: 10, delay: 0.3 }}
        className="bg-white rounded-lg shadow-md flex flex-col h-full max-h-[calc(100vh-8rem)] overflow-hidden"
      >
        <div className="p-4 sm:p-6 border-b border-gray-200 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Note Title"
              className="text-xl sm:text-2xl font-semibold border-none focus:outline-none flex-1 text-black"
            />
            <button
              onClick={handleSave}
              disabled={isSaving}
              className="ml-4 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 transition"
            >
              {isSaving ? "Saving..." : "Save"}
            </button>
          </div>
          {saveMessage && (
            <div
              className={`text-sm ${
                saveMessage.includes("successfully")
                  ? "text-green-600"
                  : "text-red-600"
              }`}
            >
              {saveMessage}
            </div>
          )}
        </div>

        <div className="flex-1 overflow-y-auto p-4 sm:p-6">
          <div
            id="editorjs"
            className="editorjs-container border border-gray-300 rounded-md p-3 sm:p-4 min-h-[400px] max-w-full"
          />
        </div>
      </motion.div>
    </div>
  );
};

export default CreateNote;
