package com.mobile.eznote.listeners;

import com.mobile.eznote.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note, int position);

}
