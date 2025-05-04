package fire.auth.com.fuelonwheels;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // إرسال الرسالة إلى Firestore
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String message = ((TextInputEditText) findViewById(R.id.etMessage)).getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "الرجاء كتابة الرسالة!", Toast.LENGTH_SHORT).show();
            } else {
                sendMessageToFirestore(message);
            }
        });
    }

    // فتح واتساب
    public void openWhatsApp(View view) {
        String url = "https://wa.me/+967734953951";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // فتح تلجرام
    public void openTelegram(View view) {
        String username = "L1234_l";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=" + username));
        if (intent.resolveActivity(getPackageManager()) == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + username));
        }
        startActivity(intent);
    }

    // فتح الإيميل
    public void openEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:moh734953951@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "دعم تطبيق Fuel on Wheels");
        startActivity(intent);
    }

    // إرسال SMS
    public void sendSMS(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:+967734953951"));
        intent.putExtra("sms_body", "مرحبًا، أود الاستفسار عن...");
        startActivity(intent);
    }

    // الاتصال الهاتفي
    public void makePhoneCall(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:+967734953951"));
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
        }
    }

    // فتح الدردشة
    public void openChat(View view) {
        startActivity(new Intent(this, ChatActivity.class));
    }

    // معالجة طلب الصلاحيات
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall(null);
        }
    }

    // إرسال الرسالة إلى Firestore
    private void sendMessageToFirestore(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("support_messages")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "تم إرسال الرسالة!", Toast.LENGTH_SHORT).show();
                    ((TextInputEditText) findViewById(R.id.etMessage)).setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطأ في الإرسال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
