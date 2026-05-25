package com.example.electricbillcalculator_syariful;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    DataHelper dbHelper;

    Spinner spinnerEditMonth, spinnerEditRebate;
    EditText editTextEditUnit;
    TextView textViewEditTotalCharges, textViewEditFinalCost;
    Button buttonUpdate, buttonCancel;

    int billId;
    double totalCharges = 0.0;
    double finalCost = 0.0;

    String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    String[] rebates = {
            "0", "1", "2", "3", "4", "5"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHelper = new DataHelper(this);

        billId = getIntent().getIntExtra("bill_id", 0);

        spinnerEditMonth = findViewById(R.id.spinnerEditMonth);
        spinnerEditRebate = findViewById(R.id.spinnerEditRebate);
        editTextEditUnit = findViewById(R.id.editTextEditUnit);

        textViewEditTotalCharges = findViewById(R.id.textViewEditTotalCharges);
        textViewEditFinalCost = findViewById(R.id.textViewEditFinalCost);

        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancel = findViewById(R.id.buttonCancel);

        setupMonthSpinner();
        setupRebateSpinner();

        loadExistingRecord();

        buttonUpdate.setOnClickListener(v -> updateRecord());

        buttonCancel.setOnClickListener(v -> finish());
    }

    private void setupMonthSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
        );

        spinnerEditMonth.setAdapter(adapter);
    }

    private void setupRebateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                rebates
        );

        spinnerEditRebate.setAdapter(adapter);
    }

    private void loadExistingRecord() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DataHelper.TABLE_NAME +
                        " WHERE " + DataHelper.COL_ID + " = ?",
                new String[]{String.valueOf(billId)}
        );

        if (cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DataHelper.COL_MONTH));
            int unit = cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_UNIT));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_REBATE));

            editTextEditUnit.setText(String.valueOf(unit));

            int monthPosition = getMonthPosition(month);
            spinnerEditMonth.setSelection(monthPosition);

            int rebatePosition = getRebatePosition(String.format("%.0f", rebate));
            spinnerEditRebate.setSelection(rebatePosition);

            totalCharges = calculateTotalCharges(unit);
            finalCost = calculateFinalCost(totalCharges, rebate);

            displayResult();
        } else {
            Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        cursor.close();
    }

    private int getMonthPosition(String month) {
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                return i;
            }
        }

        return 0;
    }

    private int getRebatePosition(String rebate) {
        for (int i = 0; i < rebates.length; i++) {
            if (rebates[i].equals(rebate)) {
                return i;
            }
        }

        return 0;
    }

    private void updateRecord() {
        String unitText = editTextEditUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            editTextEditUnit.setError("Please enter electricity unit");
            editTextEditUnit.requestFocus();
            return;
        }

        int unit;

        try {
            unit = Integer.parseInt(unitText);
        } catch (NumberFormatException e) {
            editTextEditUnit.setError("Please enter valid number");
            editTextEditUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            editTextEditUnit.setError("Unit must be between 1 and 1000 kWh");
            editTextEditUnit.requestFocus();
            return;
        }

        String month = spinnerEditMonth.getSelectedItem().toString();
        double rebate = Double.parseDouble(spinnerEditRebate.getSelectedItem().toString());

        totalCharges = calculateTotalCharges(unit);
        finalCost = calculateFinalCost(totalCharges, rebate);

        displayResult();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataHelper.COL_MONTH, month);
        values.put(DataHelper.COL_UNIT, unit);
        values.put(DataHelper.COL_TOTAL_CHARGES, totalCharges);
        values.put(DataHelper.COL_REBATE, rebate);
        values.put(DataHelper.COL_FINAL_COST, finalCost);

        int updatedRows = db.update(
                DataHelper.TABLE_NAME,
                values,
                DataHelper.COL_ID + " = ?",
                new String[]{String.valueOf(billId)}
        );

        if (updatedRows > 0) {
            Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update record", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateTotalCharges(int unit) {
        double total;

        if (unit <= 200) {
            total = unit * 0.218;
        } else if (unit <= 300) {
            total = (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((unit - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((unit - 600) * 0.546);
        }

        return total;
    }

    private double calculateFinalCost(double totalCharges, double rebatePercent) {
        return totalCharges - (totalCharges * (rebatePercent / 100));
    }

    private void displayResult() {
        textViewEditTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
        textViewEditFinalCost.setText(String.format("Final Cost After Rebate: RM %.2f", finalCost));
    }
}