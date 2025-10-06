package com.pradeep.papertrail.service;

import com.pradeep.papertrail.dto.NoteDTO;
import com.pradeep.papertrail.model.Note;
import com.pradeep.papertrail.model.NotePermission;
import com.pradeep.papertrail.model.User;
import com.pradeep.papertrail.repository.NotePermissionRepository;
import com.pradeep.papertrail.repository.NoteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NotePermissionRepository permissionRepository;

    public NoteService(NoteRepository noteRepository, NotePermissionRepository permissionRepository) {
        this.noteRepository = noteRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Cache single note by ID
     * Key format: note::noteId
     */
    @Cacheable(value = "note", key = "#noteId", unless = "#result == null")
    @Transactional(readOnly = true)
    public NoteDTO getNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        return note != null ? convertToDTO(note) : null;
    }

    /**
     * Cache user's notes list
     * Key format: userNotes::userId
     */
    @Cacheable(value = "userNotes", key = "#user.id")
    @Transactional(readOnly = true)
    public List<NoteDTO> getUserNotes(User user) {
        return noteRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cache shared notes for a user
     * Key format: sharedNotes::userId
     */
    @Cacheable(value = "sharedNotes", key = "#user.id")
    @Transactional(readOnly = true)
    public List<NoteDTO> getSharedNotes(User user) {
        return permissionRepository
                .findByUserAndPermissionIn(user, List.of(NotePermission.Permission.READ, NotePermission.Permission.EDIT))
                .stream()
                .map(NotePermission::getNote)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Create note and invalidate user's notes cache
     */
    @Caching(evict = {
            @CacheEvict(value = "userNotes", key = "#user.id")
    })
    @Transactional
    public NoteDTO createNote(String title, Object content, User user) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent((java.util.Map<String, Object>) content);
        note.setUser(user);
        note.setCreatedBy(user.getName());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        Note savedNote = noteRepository.save(note);
        return convertToDTO(savedNote);
    }

    /**
     * Update note and invalidate relevant caches
     * - Updates the single note cache
     * - Invalidates owner's userNotes cache
     * - Invalidates sharedNotes cache for all users with permissions
     */
    @Caching(
            put = {
                    @CachePut(value = "note", key = "#noteId")
            },
            evict = {
                    @CacheEvict(value = "userNotes", key = "#note.user.id"),
                    @CacheEvict(value = "sharedNotes", allEntries = true) // Evict all shared notes as we don't know all affected users
            }
    )
    @Transactional
    public NoteDTO updateNote(Long noteId, String title, Object content, Note note) {
        note.setTitle(title);
        note.setContent((java.util.Map<String, Object>) content);
        note.setUpdatedAt(LocalDateTime.now());

        Note savedNote = noteRepository.save(note);
        return convertToDTO(savedNote);
    }

    /**
     * Delete note and invalidate all related caches
     */
    @Caching(evict = {
            @CacheEvict(value = "note", key = "#noteId"),
            @CacheEvict(value = "userNotes", key = "#note.user.id"),
            @CacheEvict(value = "sharedNotes", allEntries = true)
    })
    @Transactional
    public void deleteNote(Long noteId, Note note) {
        noteRepository.delete(note);
    }

    /**
     * Share note - invalidates shared notes cache for target user
     */
    @CacheEvict(value = "sharedNotes", key = "#targetUser.id")
    @Transactional
    public void shareNote(Note note, User targetUser, NotePermission.Permission permission) {
        NotePermission notePermission = permissionRepository
                .findByNoteAndUser(note, targetUser)
                .orElse(new NotePermission());

        notePermission.setNote(note);
        notePermission.setUser(targetUser);
        notePermission.setPermission(permission);

        permissionRepository.save(notePermission);
    }

    /**
     * Revoke permission - invalidates shared notes cache
     */
    @Caching(evict = {
            @CacheEvict(value = "sharedNotes", allEntries = true)
    })
    @Transactional
    public void revokePermission(Long noteId, Long userId) {
        permissionRepository.deleteByNoteIdAndUserId(noteId, userId);
    }

    /**
     * Check if user can read note (with caching)
     */
    public boolean canReadNote(Note note, User user) {
        if (note.getUser().getId().equals(user.getId())) {
            return true;
        }

        return permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.READ) ||
                permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.EDIT);
    }

    /**
     * Check if user can edit note
     */
    public boolean canEditNote(Note note, User user) {
        if (note.getUser().getId().equals(user.getId())) {
            return true;
        }

        return permissionRepository.existsByNoteAndUserAndPermission(note, user, NotePermission.Permission.EDIT);
    }

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