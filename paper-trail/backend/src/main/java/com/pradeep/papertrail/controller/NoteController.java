package com.pradeep.papertrail.controller;

import com.pradeep.papertrail.dto.NoteDTO;
import com.pradeep.papertrail.model.Note;
import com.pradeep.papertrail.model.NotePermission;
import com.pradeep.papertrail.model.User;
import com.pradeep.papertrail.repository.NoteRepository;
import com.pradeep.papertrail.repository.UserRepository;
import com.pradeep.papertrail.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteService noteService; // ADD THIS!

    public NoteController(NoteRepository noteRepository,
                          UserRepository userRepository,
                          NoteService noteService) { // ADD THIS!
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.noteService = noteService; // ADD THIS!
    }

    // Create a new note - USE SERVICE
    @PostMapping("/create")
    public ResponseEntity<?> createNote(@RequestBody NoteDTO noteDTO, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            NoteDTO savedNote = noteService.createNote(
                    noteDTO.getTitle(),
                    noteDTO.getContent(),
                    user
            );
            return ResponseEntity.ok(savedNote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating note: " + e.getMessage());
        }
    }

    // Get notes owned by the user - USE SERVICE WITH CACHE
    @GetMapping("/my")
    public ResponseEntity<List<NoteDTO>> getMyNotes(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // USE CACHED SERVICE METHOD
        List<NoteDTO> notes = noteService.getUserNotes(user);
        return ResponseEntity.ok(notes);
    }

    // Get notes shared with the user - USE SERVICE WITH CACHE
    @GetMapping("/shared")
    public ResponseEntity<List<NoteDTO>> getSharedNotes(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // USE CACHED SERVICE METHOD
        List<NoteDTO> sharedNotes = noteService.getSharedNotes(user);
        return ResponseEntity.ok(sharedNotes);
    }

    // Get a single note by ID - USE SERVICE WITH CACHE
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // USE CACHED SERVICE METHOD
            NoteDTO noteDTO = noteService.getNoteById(noteId);

            if (noteDTO == null) {
                return ResponseEntity.notFound().build();
            }

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            // Check permissions using service
            if (!noteService.canReadNote(note, user)) {
                return ResponseEntity.status(403).body("No permission to view this note");
            }

            return ResponseEntity.ok(noteDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update note - USE SERVICE
    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable Long noteId,
                                        @RequestBody NoteDTO updatedNoteDTO,
                                        Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!noteService.canEditNote(note, user)) {
                return ResponseEntity.status(403).body("No permission to edit this note");
            }

            // USE CACHED SERVICE METHOD
            NoteDTO savedNote = noteService.updateNote(
                    noteId,
                    updatedNoteDTO.getTitle(),
                    updatedNoteDTO.getContent(),
                    note
            );
            return ResponseEntity.ok(savedNote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating note: " + e.getMessage());
        }
    }

    // Share a note - USE SERVICE
    @PostMapping("/{noteId}/share")
    public ResponseEntity<?> shareNote(@PathVariable Long noteId,
                                       @RequestParam String email,
                                       @RequestParam NotePermission.Permission permission,
                                       Authentication auth) {
        try {
            User owner = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(owner.getId())) {
                return ResponseEntity.status(403).body("Only owner can share the note");
            }

            User targetUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            // USE CACHED SERVICE METHOD
            noteService.shareNote(note, targetUser, permission);
            return ResponseEntity.ok("Note shared successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sharing note: " + e.getMessage());
        }
    }

    // Revoke permission - USE SERVICE
    @DeleteMapping("/{noteId}/permissions/{userId}")
    public ResponseEntity<String> revokePermission(@PathVariable Long noteId,
                                                   @PathVariable Long userId,
                                                   Authentication auth) {
        try {
            User owner = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(owner.getId())) {
                return ResponseEntity.status(403).body("Only owner can revoke permissions");
            }

            // USE CACHED SERVICE METHOD
            noteService.revokePermission(noteId, userId);
            return ResponseEntity.ok("Permission revoked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error revoking permission: " + e.getMessage());
        }
    }

    // Delete note - USE SERVICE
    @DeleteMapping("/{noteId}")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteId, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Only owner can delete the note");
            }

            // USE CACHED SERVICE METHOD
            noteService.deleteNote(noteId, note);
            return ResponseEntity.ok("Note deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting note: " + e.getMessage());
        }
    }
}