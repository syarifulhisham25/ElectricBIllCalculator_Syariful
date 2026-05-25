package com.example.electricbillcalculator_syariful;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecordListActivity extends AppCompatActivity {

    DataHelper dbHelper;
    ListView listViewRecords;
    Button buttonBackHome;

    String[] records;
    int[] recordIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        dbHelper = new DataHelper(this);

        listViewRecords = findViewById(R.id.listViewRecords);
        buttonBackHome = findViewById(R.id.buttonBackHome);

        loadRecords();

        buttonBackHome.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DataHelper.TABLE_NAME + " ORDER BY " + DataHelper.COL_ID + " DESC",
                null
        );

        if (cursor.getCount() == 0) {
            records = new String[]{"No saved records yet"};
            recordIds = new int[]{0};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    records
            );

            listViewRecords.setAdapter(adapter);

            listViewRecords.setOnItemClickListener((parent, view, position, id) -> {
                Toast.makeText(this, "No record to view", Toast.LENGTH_SHORT).show();
            });

            cursor.close();
            return;
        }

        records = new String[cursor.getCount()];
        recordIds = new int[cursor.getCount()];

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            int billId = cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_ID));
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DataHelper.COL_MONTH));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_FINAL_COST));

            recordIds[i] = billId;
            records[i] = month + " - RM " + String.format("%.2f", finalCost);
        }

        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                records
        );

        listViewRecords.setAdapter(adapter);

        listViewRecords.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(RecordListActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", recordIds[position]);
            startActivity(intent);
        });
    }
}