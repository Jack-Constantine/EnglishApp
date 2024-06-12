package com.example.englishapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Button searchButton;
    private TableLayout resultTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        searchButton = findViewById(R.id.searchButton);
        resultTable = findViewById(R.id.resultTable);
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        SharedPreferences prefs = getSharedPreferences("com.example.englishapp", MODE_PRIVATE);
        if (prefs.getBoolean("firstRun", true)) {
            // 读取嵌入的 CSV 文件内容
            readEmbeddedCSVFile();
            prefs.edit().putBoolean("firstRun", false).apply();
        }
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                List<String> words = getWords(query);
                adapter.clear();
                adapter.addAll(words);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 设置按钮点击事件监听器
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = autoCompleteTextView.getText().toString().trim();
                searchWord(query);
            }
        });
    }

    private void readEmbeddedCSVFile() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                ContentValues values = new ContentValues();
                values.put("word", columns[0]);
                values.put("frequency", columns[1]);
                StringBuilder locationBuilder = new StringBuilder();
                for (int i = 2; i < columns.length; i++) {
                    locationBuilder.append(columns[i]).append(", ");
                }
                String locationData = locationBuilder.length() > 0 ? locationBuilder.substring(0, locationBuilder.length() - 2) : "";
                values.put("location", locationData);
                db.insert("words", null, values);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchWord(String query) {
        if (query.isEmpty()) {
            return;  // 如果查询词为空，那么就直接返回
        }
        resultTable.removeViews(1, resultTable.getChildCount() - 1);  // 清除之前的搜索结果，但保留表头
        Cursor cursor = db.query("words", null, "word LIKE ? || '%'", new String[]{query}, null, null, null, "1");
        while (cursor.moveToNext()) {
            String[] row = new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2)};
            addTableRow(row);
        }
        cursor.close();
    }
    private List<String> getWords(String query) {
        List<String> words = new ArrayList<>();
        Cursor cursor = db.query("words", new String[]{"word"}, "word LIKE ?", new String[]{query + "%"}, null, null, null);
        while (cursor.moveToNext()) {
            words.add(cursor.getString(0));
        }
        cursor.close();
        return words;
    }
    private void addTableRow(String[] rowData) {
        TableRow tableRow = new TableRow(this);

        // 拼接位置列的数据
        StringBuilder locationBuilder = new StringBuilder();
        for (int i = 2; i < rowData.length; i++) {
            locationBuilder.append(rowData[i]).append(", ");
        }
        // 删除最后一个逗号和空格
        String locationData = locationBuilder.length() > 0 ? locationBuilder.substring(0, locationBuilder.length() - 2) : "";

        // 添加单词、频率和位置列
        for (int i = 0; i < rowData.length; i++) {
            TextView locationTextView = new TextView(this); // 定义 locationTextView
            if (i == 2) { // 如果是“位置”列
                locationTextView.setText(locationData);
                locationTextView.setTextSize(9);
            } else {
                locationTextView.setText(rowData[i]);
                locationTextView.setTextSize(12);
            }
            locationTextView.setPadding(8, 8, 8, 8);
            tableRow.addView(locationTextView);
        }

        resultTable.addView(tableRow);
    }
}
