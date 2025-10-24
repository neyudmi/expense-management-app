package com.example.expense_management.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.expense_management.BuildConfig;
import com.example.expense_management.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, dateOfBirthInput;
    private AutoCompleteTextView genderDropdown;
    private MaterialButton btnRegister;
    private RequestQueue requestQueue; // Volley request queue
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);

        baseUrl = BuildConfig.BASE_URL;
        // Khởi tạo UI
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirm_Password);
        btnRegister = findViewById(R.id.btnSignup);
        MaterialTextView loginHere = findViewById(R.id.loginHere);
        genderDropdown = findViewById(R.id.genderDropdown);
        dateOfBirthInput = findViewById(R.id.dateOfBirthInput);



        // Khởi tạo Volley queue
        requestQueue = Volley.newRequestQueue(this);

//        // Dropdown giới tính
        String[] genders = new String[]{"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        genderDropdown.setAdapter(adapter);

        // Date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày sinh")
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                        .build())
                .build();

        dateOfBirthInput.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDate = sdf.format(new Date(selection));
            dateOfBirthInput.setText(selectedDate);
        });

        btnRegister.setOnClickListener(v -> signupUser());

        loginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    private void signupUser() {
        String username = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String dob = dateOfBirthInput.getText().toString().trim();
        String gender = genderDropdown.getText().toString().trim();

        // Validate đơn giản
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuẩn bị dữ liệu JSON để gửi lên server
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fullName", username);
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("birthDay", dob);
            requestBody.put("gender", gender);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu JSON!", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL API backend
        String url = baseUrl + "/auth/signup";

        Log.d("MyAppTag", "URL: " + url);
        // Gửi request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Register.this, MainActivity.class);
                    startActivity(intent);
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("RegisterError", "Response body: " + responseBody);
                    }
                    Toast.makeText(this, "Lỗi khi đăng ký!", Toast.LENGTH_LONG).show();
                });

        // Thêm request vào queue
        requestQueue.add(jsonObjectRequest);
    }
}