package com.example.electricbillcalculator_syariful;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    DataHelper dbHelper;

    TextView textViewMonth, textViewUnit, textViewTotalCharges, textViewRebate, textViewFinalCost;
    Button buttonEdit, buttonDelete, buttonBack;

    int billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new DataHelper(this);

        billId = getIntent().getIntExtra("bill_id", 0);

        textViewMonth = findViewById(R.id.textViewMonth);
        textViewUnit = findViewById(R.id.textViewUnit);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewRebate = findViewById(R.id.textViewRebate);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);

        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBack);

        loadBillDetails();

        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, EditActivity.class);
            intent.putExtra("bill_id", billId);
            startActivity(intent);
        });

        buttonDelete.setOnClickListener(v -> confirmDelete());

        buttonBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBillDetails();
    }

    private void loadBillDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DataHelper.TABLE_NAME +
                        " WHERE " + DataHelper.COL_ID + " = ?",
                new String[]{String.valueOf(billId)}
        );

        if (cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DataHelper.COL_MONTH));
            int unit = cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_UNIT));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_TOTAL_CHARGES));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_REBATE));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_FINAL_COST));

            textViewMonth.setText(month);
            textViewUnit.setText(unit + " kWh");
            textViewTotalCharges.setText(String.format("RM %.2f", totalCharges));
            textViewRebate.setText(String.format("%.0f%%", rebate));
            textViewFinalCost.setText(String.format("RM %.2f", finalCost));
        } else {
            Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        cursor.close();
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Record");
        builder.setMessage("Are you sure you want to delete this electricity bill record?");

        builder.setPositiveButton("Delete", (dialog, which) -> deleteRecord());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void deleteRecord() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int deletedRows = db.delete(
                DataHelper.TABLE_NAME,
                DataHelper.COL_ID + " = ?",
                new String[]{String.valueOf(billId)}
        );

        if (deletedRows > 0) {
            Toast.makeText(this, "Record deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show();
        }
    }
}