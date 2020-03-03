package com.tp_oliva.todolist.view.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.app.AlertDialog;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog.Builder;
import android.widget.Toast;
import android.content.DialogInterface;


import com.tp_oliva.todolist.R;
import com.tp_oliva.todolist.model.model.Note;
import com.tp_oliva.todolist.view.adapter.NoteAdapter;
import com.tp_oliva.todolist.viewmodel.NoteViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST = 101;
    public static final int EDIT_NOTE_REQUEST = 102;

    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        //swipe suppr
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "La tâche a été supprimé.", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllLocalNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                adapter.submitList(notes);
            }
        });
        noteViewModel.getAllRemoteNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                if (notes != null) {
                    if ((notes.size()) != (adapter.getItemCount())) {
                        int size = ((notes.size() > adapter.getItemCount()) ? (notes.size()) : (adapter.getItemCount()));
                        for (int i = 0; i < size; i++) {
                            boolean foundFlag = false;
                            for (Note note : notes) {
                                for (int l = 0; l < adapter.getItemCount(); l++) {
                                    if ((adapter.getItemId(l) == note.getId())) {
                                        foundFlag = true;
                                    }
                                }
                                if (!foundFlag) {
                                    noteViewModel.insert(note);
                                    foundFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        });

        //editer la tache onclick
        adapter.setOnItemClickListner(new NoteAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());
                startActivityForResult(intent, EDIT_NOTE_REQUEST);
            }
        });
    }

    //back pressed
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Fermeture de l'application")
                .setMessage("Etes-vous sur de vouloir quitter cette application?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
                String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                Note note = new Note(title, description, priority);
                noteViewModel.insert(note);
                Toast.makeText(this, "Mise à jour affectuée.", Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
                int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

                if (id == -1) {
                    Toast.makeText(this, "Une erreur a empêcher la mise à jour de la tâche.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                Note note = new Note(title, description, priority);
                note.setId(id);
                noteViewModel.update(note);

                Toast.makeText(this, "Mise à jour affectuée.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Une erreur a empêcher la mise à jour de la tâche.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                noteViewModel.deleteAllNotes();
                Toast.makeText(MainActivity.this, "Toutes les tâches ont bien été supprimés.", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
