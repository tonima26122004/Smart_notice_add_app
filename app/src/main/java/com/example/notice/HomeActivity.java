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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private long previousCount = 0;

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

    // Display notice and count
    private void display() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference noticeRef = database.getReference("Notice");
        DatabaseReference countRef = database.getReference("human_counter");

        TextView noticeText = findViewById(R.id.notice);
        TextView countText = findViewById(R.id.count_num);

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

        countRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long count = snapshot.getValue(Long.class);
                Log.d(TAG, "Count: " + count);
                countText.setText(String.valueOf(count));
                previousCount = count; // Update previous count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load count", error.toException());
            }
        });
    }

    // Add notice and update counter
    private void addNotice(String note) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference noticeRef = database.getReference("Notice");
        DatabaseReference countRef = database.getReference("human_counter");

        // Get the current count value
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long currentCount = snapshot.getValue(Long.class);
                if (currentCount != null) {
                    // Subtract the previous count from the current count
                    long newCount = currentCount - previousCount;

                    // Set the new notice
                    noticeRef.setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Set the new count
                                countRef.setValue(newCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Notice added successfully.");
                                        } else {
                                            Log.e(TAG, "Failed to set count: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "Failed to add notice: " + task.getException().getMessage());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to retrieve current count.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
