package com.mobile.eznote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobile.eznote.R;
import com.mobile.eznote.adapters.NotesAdapter;
import com.mobile.eznote.database.NotesDatabase;
import com.mobile.eznote.entities.Note;
import com.mobile.eznote.listeners.NotesListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference root;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReferenceFromUrl("gs://ez-note-ba48e.appspot.com");

    private String EMAIL_KEY = "EMAIL_KEY";
    private String PASSWORD_KEY = "EMAIL_KEY";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        test = findViewById(R.id.test);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://ez-note-ba48e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        root = mDatabase.getReference();

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTE, false); /*hiển thị tất cả các ghi chú từ cơ sở dữ liệu và do đó dưới dạng tham số isNoteDeleted được truyền giá trị false*/

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        findViewById(R.id.imageHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        findViewById(R.id.imageAddWebLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddURLDialog();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                /*if(noteList.size() == 0){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                }
                notesRecyclerView.smoothScrollToPosition(0);*/
                if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);

                    }
                } /*from 105-114, if request code is REQUEST_CODE_UPDATE_NOTE, first we remove note form list,
                 then we checked whether the note is deleted or not,
                 if the note is deleted then notifying adapter about item removed,
                 if the note is not deleted then it must be updated thats why we are adding a newly updated note
                 to that same position where we removed and notifying adapter about item changed*/
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false); /*thêm ghi chú mới vào cơ sở dữ liệu, và do đó dưới dạng tham số isNoteDeleted truyền vào gtri false*/
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
                /*cập nhật ghi chú đã có sẵn từ cơ sở dữ liệu và có thể ghi chú bị xóa do đó dưới dạng tham số isNoteDeleted
                , chúng tôi đang chuyển giá trị từ CreateNoteActivity, cho dù ghi chú bị xóa hay không sử dụng dữ liệu intent với khóa isNoteDeleted*/
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        dialogAddURL.dismiss();
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "URL");
                        intent.putExtra("URL", inputURL.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }


    public void gotoupload(View view) {
        if (noteList.size() == 0)
            return;

        root.child("USERS").child(mAuth.getUid()).removeValue();

        //lay ra uid cua user
        String uid = mAuth.getUid();

        for (int i = 0; i < noteList.size(); i++) {
            Note note = noteList.get(i);

            if (!note.getImagePath().isEmpty()) {
                //upload anh truoc
                String pathimage = "imageofnote" + String.valueOf(noteList.size() - i - 1) + ".png";
                StorageReference imageName = storageReference.child(uid).child(pathimage);

                Bitmap bitmap = BitmapFactory.decodeFile(note.getImagePath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imageName.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
//                    Log.d("binhh", "Upload anh bi that bai!");

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                root.child("USERS").child(mAuth.getUid()).child(String.valueOf(note.getId())).child("urlimage").setValue(imageUrl);
                            }
                        });

                    }
                });
            }


            try {
                //upload idnote, title, datetime, noteText, imagePath, color, webLink
                root.child("USERS").child(uid).child(String.valueOf(note.getId())).child("title").setValue(note.getTitle());
                root.child("USERS").child(uid).child(String.valueOf(note.getId())).child("datetime").setValue(note.getDateTime());
                root.child("USERS").child(uid).child(String.valueOf(note.getId())).child("notetext").setValue(note.getNoteText());
                root.child("USERS").child(uid).child(String.valueOf(note.getId())).child("color").setValue(note.getColor());
                if (!note.getWebLink().isEmpty())
                    root.child("USERS").child(uid).child(String.valueOf(note.getId())).child("weblink").setValue(note.getWebLink());
                Toast.makeText(MainActivity.this, "Đã đồng bộ ghi chú lên server!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, "null");
        editor.putString(PASSWORD_KEY, "null");
        editor.commit();
        //        noteList.clear();
//        notesAdapter.notifyDataSetChanged();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    public void sysdata(View view) {

        //test ham de lay toan bo anh tu realtimedatabase
        DatabaseReference databaseReference = root;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                Iterable<DataSnapshot> listNoteFromServer = snapshot.child("USERS").child(mAuth.getUid()).getChildren();

                for (DataSnapshot data : listNoteFromServer) {
                    String id = data.getKey();
                    String title = data.child("title").getValue(String.class);
                    String datetime = data.child("datetime").getValue(String.class);

//                    String urlimage = data.child("urlimage").getValue(String.class);
                    String urlimage = null;
                    String weblink = data.child("weblink").getValue(String.class);

                    String color = data.child("color").getValue(String.class);
                    String notetext = data.child("notetext").getValue(String.class);

                    noteList.add(new Note(Integer.parseInt(id), title, datetime, notetext, urlimage, color, weblink));
//                    Log.d("abcd", "kiem tra id: " + id);
//                    Log.d("abcd", "kiem tra title: " + title);
//                    Log.d("abcd", "kiem tra datetime: " + datetime);
//                    Log.d("abcd", "kiem tra urlimage: " + urlimage);
//                    Log.d("abcd", "kiem tra weblink: " + weblink);
//                    Log.d("abcd", "kiem tra color: " + color);
//                    Log.d("abcd", "kiem tra notetext: " + notetext);
                }

                notesAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Đã đồng bộ thành công!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}