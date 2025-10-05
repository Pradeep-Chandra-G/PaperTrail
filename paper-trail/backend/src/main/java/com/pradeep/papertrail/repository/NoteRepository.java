package com.pradeep.papertrail.repository;

import com.pradeep.papertrail.model.Note;
import com.pradeep.papertrail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
}
