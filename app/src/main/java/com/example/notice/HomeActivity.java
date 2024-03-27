package com.example.notice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        display();
        EditText note = findViewById(R.id.add_notice);
        Button btn = findViewById(R.id.add_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Note = note.getText().toString();
                if(Note.isEmpty()){
                    Toast.makeText(HomeActivity.this,"Please enter a notice", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this,"Operation Started", Toast.LENGTH_SHORT).show();
                    addNotice(Note);
                    note.setText("");
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }
    //output
    private void display() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference noticeRef = database.getReference("Notice");
        DatabaseReference countRef = database.getReference("human_counter"); // Assuming count is stored under "Count" node

        TextView noticeText = findViewById(R.id.notice);
        TextView countText = findViewById(R.id.count_num); // Assuming you have a TextView with id "count" in your layout

        // Read notice from Firebase
        noticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                Log.d(TAG, value);
                noticeText.setText(value);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load notice", error.toException());
            }
        });

        // Read count from Firebase
        countRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long count = snapshot.getValue(Long.class);
                Log.d(TAG, "Count: " + count);
                countText.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load count", error.toException());
            }
        });
    }

    //input
    private void addNotice(String note) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference noticeRef = database.getReference("Notice");
        noticeRef.setValue(note);
    }



}

