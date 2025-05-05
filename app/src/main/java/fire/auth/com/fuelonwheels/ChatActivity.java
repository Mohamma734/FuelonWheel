package fire.auth.com.fuelonwheels;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// تأكد من إضافة الاستيرادات الخاصة بالملفات المحلية
import fire.auth.com.fuelonwheels.ChatAdapter;
import fire.auth.com.fuelonwheels.Message;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(messages);
        rvMessages.setAdapter(adapter);

        // جلب الرسائل من Firestore
        db.collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    messages.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        messages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    if (messages.size() > 0) {
                        rvMessages.smoothScrollToPosition(messages.size() - 1);
                    }
                });

        // إرسال الرسالة
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            TextInputEditText etMessage = findViewById(R.id.etMessage);
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Message message = new Message(text, userId, new Date());
                db.collection("chats").add(message)
                        .addOnSuccessListener(aVoid -> etMessage.setText(""));
            }
        });
    }
}
