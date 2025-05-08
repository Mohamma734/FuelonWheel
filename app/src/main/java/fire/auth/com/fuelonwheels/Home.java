package fire.auth.com.fuelonwheels;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Home extends AppCompatActivity {

    FirebaseFirestore mFire;
    TextView price;
    Spinner fuel_type;
    EditText etCapacity, etLocation;
    Button mLocationBtn, mOrderBtn;
    FusedLocationProviderClient client;
    LocationRequest locationRequest;
    Geocoder geocoder;
    Location mLocation;
    String finalType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission();

        try {
            price = findViewById(R.id.price);
            fuel_type = findViewById(R.id.hometype);
            etCapacity = findViewById(R.id.et_capacity);
            etLocation = findViewById(R.id.et_location);
            mOrderBtn = findViewById(R.id.orderBtn);
            mLocationBtn = findViewById(R.id.getBtn);
            mFire = FirebaseFirestore.getInstance();

            fuel_type.setOnItemSelectedListener((parent, view, position, id) -> finalType = parent.getSelectedItem().toString());

            mOrderBtn.setOnClickListener(v -> placeOrder());

            geocoder = new Geocoder(this, Locale.getDefault());
            mLocationBtn.setOnClickListener(v -> getLocation());

        } catch (Exception e) {
            Toast.makeText(this, "خطأ في تهيئة الصفحة: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getLocation() {
        try {
            client = LocationServices.getFusedLocationProviderClient(this);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "الصلاحية غير متاحة!", Toast.LENGTH_SHORT).show();
                return;
            }

            client.getLastLocation().addOnSuccessListener(this, location -> {
                if (location == null) {
                    Toast.makeText(Home.this, "لم يتم العثور على الموقع", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocation = location;
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        etLocation.setText(addresses.get(0).getAddressLine(0));
                    }
                } catch (IOException e) {
                    Toast.makeText(Home.this, "خطأ في جلب العنوان: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(Home.this, "حدث خطأ أثناء تحديد الموقع: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void placeOrder() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date resultdate = new Date(System.currentTimeMillis());

            String enteredQty = etCapacity.getText().toString().trim();
            if (enteredQty.isEmpty()) {
                Toast.makeText(Home.this, "الرجاء إدخال الكمية المطلوبة!", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(enteredQty);
            int rate = quantity * (finalType.equals("بترول") ? 85 : 75);
            price.setText(String.valueOf(rate));

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("latitude", (mLocation != null) ? String.valueOf(mLocation.getLatitude()) : "غير متاح");
            orderMap.put("longitude", (mLocation != null) ? String.valueOf(mLocation.getLongitude()) : "غير متاح");
            orderMap.put("address", etLocation.getText().toString());
            orderMap.put("fuel_type", finalType);
            orderMap.put("capacity", enteredQty);
            orderMap.put("dnt", sdf.format(resultdate));
            orderMap.put("price", String.valueOf(rate));
            orderMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            mFire.collection("orders").add(orderMap)
                .addOnSuccessListener(documentReference -> Toast.makeText(Home.this, "تم تنفيذ الطلب بنجاح ✅", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "خطأ في تنفيذ الطلب: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(Home.this, "حدث خطأ غير متوقع أثناء تنفيذ الطلب: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.homeContactUs) {
                startActivity(new Intent(this, ContactUsActivity.class));
            } else {
                Toast.makeText(this, "خيار غير متاح!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }
                           }
