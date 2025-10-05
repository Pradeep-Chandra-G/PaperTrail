package com.pradeep.papertrail.controller;

import com.pradeep.papertrail.dto.NoteDTO;
import com.pradeep.papertrail.model.Note;
import com.pradeep.papertrail.model.NotePermission;
import com.pradeep.papertrail.model.User;
import com.pradeep.papertrail.repository.NotePermissionRepository;
import com.pradeep.papertrail.repository.NoteRepository;
import com.pradeep.papertrail.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final NotePermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository,
                          NotePermissionRepository permissionRepository,
                          UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    // Create a new note with JSON content
    @PostMapping("/create")
    public ResponseEntity<?> createNote(@RequestBody NoteDTO noteDTO, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = new Note();
            note.setTitle(noteDTO.getTitle());
            note.setContent(noteDTO.getContent()); // JSON content
            note.setUser(user);
            note.setCreatedBy(user.getName());
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());

            Note savedNote = noteRepository.save(note);
            return ResponseEntity.ok(convertToDTO(savedNote));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating note: " + e.getMessage());
        }
    }

    // Get notes owned by the user
    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<List<NoteDTO>> getMyNotes(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NoteDTO> notes = noteRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notes);
    }

    // Get notes shared with the user (READ permission)
    @GetMapping("/shared")
    @Transactional(readOnly = true)
    public ResponseEntity<List<NoteDTO>> getSharedNotes(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NoteDTO> sharedNotes = permissionRepository
                .findByUserAndPermissionIn(user, List.of(NotePermission.Permission.READ, NotePermission.Permission.EDIT))
                .stream()
                .map(NotePermission::getNote)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::convertToDTO)
                .toList();


        return ResponseEntity.ok(sharedNotes);
    }

    // Get a single note by ID
    @GetMapping("/{noteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            // Check if user owns the note or has read permission
            boolean canRead = note.getUser().getId().equals(user.getId()) ||
                    permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.READ) ||
                    permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.EDIT);

            if (!canRead) {
                return ResponseEntity.status(403).body("No permission to view this note");
            }

            return ResponseEntity.ok(convertToDTO(note));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update note content (only owner or EDIT permission)
    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable Long noteId,
                                        @RequestBody NoteDTO updatedNoteDTO,
                                        Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            boolean canEdit = note.getUser().getId().equals(user.getId()) ||
                    permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.EDIT);

            if (!canEdit) {
                return ResponseEntity.status(403).body("No permission to edit this note");
            }

            note.setTitle(updatedNoteDTO.getTitle());
            note.setContent(updatedNoteDTO.getContent()); // JSON content
            note.setUpdatedAt(LocalDateTime.now());

            Note savedNote = noteRepository.save(note);
            return ResponseEntity.ok(convertToDTO(savedNote));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating note: " + e.getMessage());
        }
    }

    // Share a note with another user
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

            NotePermission notePermission = new NotePermission();
            notePermission.setNote(note);
            notePermission.setUser(targetUser);
            notePermission.setPermission(permission);

            Optional<NotePermission> existing =
                    permissionRepository.findByNoteAndUser(note, targetUser);

            if (existing.isPresent()) {
                existing.get().setPermission(permission); // update instead
                permissionRepository.save(existing.get());
                return ResponseEntity.ok("Permission updated successfully");
            }

            permissionRepository.save(notePermission);
            return ResponseEntity.ok("Note shared successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sharing note: " + e.getMessage());
        }
    }

    // Revoke a user's permission
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

            permissionRepository.deleteByNoteIdAndUserId(noteId, userId);
            return ResponseEntity.ok("Permission revoked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error revoking permission: " + e.getMessage());
        }
    }

    // Delete a note (only owner)
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

            noteRepository.delete(note);
            return ResponseEntity.ok("Note deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting note: " + e.getMessage());
        }
    }

    // Helper method to convert Note entity to DTO
    private NoteDTO convertToDTO(Note note) {
        return new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedBy(),
                note.getUser() != null ? note.getUser().getId() : null,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}