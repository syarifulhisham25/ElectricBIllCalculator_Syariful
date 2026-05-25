package com.example.electricbillcalculator_syariful;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth, spinnerRebate;
    EditText editTextUnit;
    TextView textViewTotalCharges, textViewFinalCost;
    Button buttonCalculate, buttonSave, buttonViewRecords, buttonAbout;

    DataHelper dbHelper;

    double totalCharges = 0.0;
    double finalCost = 0.0;
    boolean hasCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DataHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerRebate = findViewById(R.id.spinnerRebate);
        editTextUnit = findViewById(R.id.editTextUnit);

        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);

        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonViewRecords = findViewById(R.id.buttonViewRecords);
        buttonAbout = findViewById(R.id.buttonAbout);

        setupMonthSpinner();
        setupRebateSpinner();

        buttonCalculate.setOnClickListener(v -> calculateBill());

        buttonSave.setOnClickListener(v -> saveBill());

        buttonViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
            startActivity(intent);
        });

        buttonAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void setupMonthSpinner() {
        String[] months = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
        );

        spinnerMonth.setAdapter(adapter);
    }

    private void setupRebateSpinner() {
        String[] rebates = {
                "0", "1", "2", "3", "4", "5"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                rebates
        );

        spinnerRebate.setAdapter(adapter);
    }

    private void calculateBill() {
        String unitText = editTextUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            editTextUnit.setError("Please enter electricity unit");
            editTextUnit.requestFocus();
            return;
        }

        int unit;

        try {
            unit = Integer.parseInt(unitText);
        } catch (NumberFormatException e) {
            editTextUnit.setError("Please enter valid number");
            editTextUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            editTextUnit.setError("Unit must be between 1 and 1000 kWh");
            editTextUnit.requestFocus();
            return;
        }

        double rebate = Double.parseDouble(spinnerRebate.getSelectedItem().toString());

        totalCharges = calculateTotalCharges(unit);
        finalCost = calculateFinalCost(totalCharges, rebate);

        textViewTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
        textViewFinalCost.setText(String.format("Final Cost After Rebate: RM %.2f", finalCost));

        hasCalculated = true;

        Toast.makeText(this, "Bill calculated successfully", Toast.LENGTH_SHORT).show();
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

    private void saveBill() {
        String unitText = editTextUnit.getText().toString().trim();

        if (!hasCalculated) {
            Toast.makeText(this, "Please calculate bill before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unitText.isEmpty()) {
            editTextUnit.setError("Please enter electricity unit");
            editTextUnit.requestFocus();
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();
        int unit = Integer.parseInt(unitText);
        double rebate = Double.parseDouble(spinnerRebate.getSelectedItem().toString());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataHelper.COL_MONTH, month);
        values.put(DataHelper.COL_UNIT, unit);
        values.put(DataHelper.COL_TOTAL_CHARGES, totalCharges);
        values.put(DataHelper.COL_REBATE, rebate);
        values.put(DataHelper.COL_FINAL_COST, finalCost);

        long result = db.insert(DataHelper.TABLE_NAME, null, values);

        if (result != -1) {
            Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextUnit.setText("");
        spinnerMonth.setSelection(0);
        spinnerRebate.setSelection(0);
        textViewTotalCharges.setText("Total Charges: RM 0.00");
        textViewFinalCost.setText("Final Cost After Rebate: RM 0.00");
        totalCharges = 0.0;
        finalCost = 0.0;
        hasCalculated = false;
    }
}