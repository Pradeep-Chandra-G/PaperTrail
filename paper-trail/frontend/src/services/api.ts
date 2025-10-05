// paper-trail/frontend/src/services/api.ts
import axios from "axios";
import type { OutputData } from "@editorjs/editorjs";

const API_BASE_URL = "http://localhost:8080";

// Create axios instance with credentials
const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Important for session cookies
  headers: {
    "Content-Type": "application/json",
  },
});

// Types
export interface User {
  id: number;
  name: string;
  email: string;
}

export interface NoteDTO {
  id?: number;
  title: string;
  content: OutputData;
  createdBy?: string;
  userId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface ImageUploadResponse {
  url: string;
  publicId: string;
  format: string;
  userId: number;
  folder: string;
}

// Auth API
export const authAPI = {
  register: async (data: RegisterRequest) => {
    const response = await api.post("/auth/register", data);
    return response.data;
  },

  login: async (data: LoginRequest) => {
    const response = await api.post("/auth/login", data);
    return response.data;
  },

  logout: async () => {
    const response = await api.post("/auth/logout");
    return response.data;
  },

  getSessionInfo: async () => {
    const response = await api.get("/auth/session-info");
    return response.data;
  },
};

// Notes API
// Notes API
export const notesAPI = {
  createNote: async (note: NoteDTO) => {
    const response = await api.post<NoteDTO>("/notes/create", note);
    return response.data;
  },

  getMyNotes: async () => {
    const response = await api.get<NoteDTO[]>("/notes/my");
    return response.data;
  },

  getSharedNotes: async () => {
    const response = await api.get<NoteDTO[]>("/notes/shared");
    return response.data;
  },

  getNoteById: async (noteId: number) => {
    const response = await api.get<NoteDTO>(`/notes/${noteId}`);
    return response.data;
  },

  updateNote: async (noteId: number, note: NoteDTO) => {
    const response = await api.put<NoteDTO>(`/notes/${noteId}`, note);
    return response.data;
  },

  deleteNote: async (noteId: number) => {
    const response = await api.delete(`/notes/${noteId}`);
    return response.data;
  },

  // ✅ Share a note with a user by email
  shareNote: async (
    noteId: number,
    email: string,
    permission: "READ" | "EDIT" = "READ"
  ) => {
    const response = await api.post(`/notes/${noteId}/share`, null, {
      params: { email, permission },
    });
    return response.data;
  },

  revokePermission: async (noteId: number, userId: number) => {
    const response = await api.delete(`/notes/${noteId}/permissions/${userId}`);
    return response.data;
  },
};


// Images API
export const imagesAPI = {
  uploadImage: async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post<ImageUploadResponse>(
      "/api/images/upload",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  deleteImage: async (publicId: string) => {
    const response = await api.delete("/api/images/delete", {
      params: { publicId },
    });
    return response.data;
  },

  getImageUrl: async (publicId: string) => {
    const response = await api.get<{ url: string }>("/api/images/url", {
      params: { publicId },
    });
    return response.data;
  },
};

export default api;
