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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    TextView Name, price;
    Spinner fuel_type, capacity;
    String[] menu = {"بترول", "ديزل"}, amount = {"1000", "2000", "3000", "4000"};
    String finalType, finalQty;
    Button mLocationBtn, mOrderBtn;
    EditText et_mLocation;
    FusedLocationProviderClient client;
    LocationRequest locationRequest;
    Geocoder geocoder;
    Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission();

        Name = findViewById(R.id.homeName);
        price = findViewById(R.id.price);
        fuel_type = findViewById(R.id.hometype);
        capacity = findViewById(R.id.homeCapacity);
        et_mLocation = findViewById(R.id.et_loction);
        mOrderBtn = findViewById(R.id.orderBtn);
        mLocationBtn = findViewById(R.id.getBtn);
        mFire = FirebaseFirestore.getInstance();

        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, menu);
        fuel_type.setAdapter(fuelAdapter);

        ArrayAdapter<String> capacityAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, amount);
        capacity.setAdapter(capacityAdapter);

        fuel_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finalType = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        capacity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finalQty = parent.getSelectedItem().toString();
                int rate = Integer.parseInt(finalQty) * (finalType.equals("بترول") ? 85 : 75);
                price.setText(String.valueOf(rate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mOrderBtn.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.getDefault());
            Date resultdate = new Date(System.currentTimeMillis());

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("latitude", String.valueOf(mLocation.getLatitude()));
            orderMap.put("longitude", String.valueOf(mLocation.getLongitude()));
            orderMap.put("address", et_mLocation.getText().toString());
            orderMap.put("fuel_type", finalType);
            orderMap.put("capacity", finalQty);
            orderMap.put("dnt", sdf.format(resultdate));
            orderMap.put("price", price.getText().toString());
            orderMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            mFire.collection("orders").add(orderMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(Home.this, "تم تنفيذ الطلب بنجاح ✅", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Home.this, ordersuccessful.class);
                        i.putExtra("type", finalType);
                        i.putExtra("amount", finalQty);
                        i.putExtra("location", et_mLocation.getText().toString());
                        i.putExtra("latitude", String.valueOf(mLocation.getLatitude()));
                        i.putExtra("Time", sdf.format(resultdate));
                        i.putExtra("price", price.getText().toString());
                        startActivity(i);
                    });
        });

        geocoder = new Geocoder(this, Locale.getDefault());
        mLocationBtn.setOnClickListener(v -> getLocation());
    }

    private void getLocation() {
        client = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
            }
        }, null);

        client.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(Home.this, "لم يتم العثور على الموقع", Toast.LENGTH_SHORT).show();
                return;
            }
            mLocation = location;
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (!addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    String area = addr.getSubLocality();
                    String city = addr.getLocality();
                    String state = addr.getAdminArea();
                    String country = addr.getCountryName();
                    et_mLocation.setText((area.isEmpty() ? city : area + ", " + city) + ", " + state + ", " + country);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.homeHome) {
            startActivity(new Intent(this, Home.class));
        } else if (id == R.id.homeProfile) {
            startActivity(new Intent(this, Profile.class));
        } else if (id == R.id.homeOrders) {
            startActivity(new Intent(this, Myorder.class));
        } else if (id == R.id.homeChat) { // اضفنا هنا نقر ChatActivity
            startActivity(new Intent(this, ChatActivity.class));
        } else if (id == R.id.homeContactUs) { // اضفنا هنا نقر ContactUsActivity
            startActivity(new Intent(this, ContactUsActivity.class));
        } else if (id == R.id.homeLogout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "حدث خطأ غير متوقع", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
