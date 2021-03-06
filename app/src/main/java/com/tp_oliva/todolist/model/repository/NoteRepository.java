package com.tp_oliva.todolist.model.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.tp_oliva.todolist.model.model.Note;
import com.tp_oliva.todolist.model.model.NoteDao;
import com.tp_oliva.todolist.model.model.NoteDatabase;
import com.tp_oliva.todolist.model.remoteDataSource.RemoteDb;

import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;
    private RemoteDb remoteDb;

    private LiveData<List<Note>> localAllNotes;
    private LiveData<List<Note>> remoteAllNotes;
    private MutableLiveData<List<Note>> notes;

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        remoteDb = new RemoteDb();
        notes = new MutableLiveData<>();
        remoteAllNotes = remoteDb.getAllNotes();
        localAllNotes = noteDao.getAllNotes();
    }

    public void insert(Note note) {
        new InsertNoteAsyncTask(noteDao, remoteDb).execute(note);
    }

    public void update(Note note) {
        new UpdateNoteAsyncTask(noteDao, remoteDb).execute(note);
    }

    public void delete(Note note) {
        new DeleteNoteAsyncTask(noteDao, remoteDb).execute(note);
    }

    public void deleteAllNotes() {
        new DeleteAllNotesAsyncTask(noteDao, remoteDb).execute();
    }

    public LiveData<List<Note>> getLocalAllNotes() {
        return localAllNotes;
    }

    public LiveData<List<Note>> getRemoteAllNotes() {
        return remoteAllNotes;
    }

    private static class InsertNoteAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;
        private RemoteDb remoteDb;

        private InsertNoteAsyncTask(NoteDao noteDao, RemoteDb remoteDb) {
            this.noteDao = noteDao;
            this.remoteDb = remoteDb;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            long id = noteDao.insert(notes[0]);
            notes[0].setId((int) id);
            remoteDb.insert(notes[0]);
            return null;
        }
    }

    private static class UpdateNoteAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;
        private RemoteDb remoteDb;

        private UpdateNoteAsyncTask(NoteDao noteDao, RemoteDb remoteDb) {
            this.noteDao = noteDao;
            this.remoteDb = remoteDb;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.update(notes[0]);
            remoteDb.update(notes[0]);
            return null;
        }
    }

    private static class DeleteNoteAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;
        private RemoteDb remoteDb;

        private DeleteNoteAsyncTask(NoteDao noteDao, RemoteDb remoteDb) {
            this.noteDao = noteDao;
            this.remoteDb = remoteDb;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.delete(notes[0]);
            remoteDb.delete(notes[0]);
            return null;
        }
    }

    private static class DeleteAllNotesAsyncTask extends AsyncTask<Void, Void, Void> {

        private NoteDao noteDao;
        private RemoteDb remoteDb;

        private DeleteAllNotesAsyncTask(NoteDao noteDao, RemoteDb remoteDb) {
            this.noteDao = noteDao;
            this.remoteDb = remoteDb;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noteDao.deleteAllNotes();
            remoteDb.deleteAllNotes();
            return null;
        }
    }

}
