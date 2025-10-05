package com.pradeep.papertrail.repository;

import com.pradeep.papertrail.model.Note;
import com.pradeep.papertrail.model.NotePermission;
import com.pradeep.papertrail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotePermissionRepository extends JpaRepository<NotePermission, Long> {
    Optional<NotePermission> findByNoteAndUser(Note note, User user);

    // Get all permissions for a user with a specific type
    List<NotePermission> findByUserAndPermissionIn(User user, List<NotePermission.Permission> permissions);


    // Check if a permission exists for a note and user
    boolean existsByNoteAndUserAndPermission(Note note, User user, NotePermission.Permission permission);

    // Delete permission by note id and user id
    void deleteByNoteIdAndUserId(Long noteId, Long userId);

}

