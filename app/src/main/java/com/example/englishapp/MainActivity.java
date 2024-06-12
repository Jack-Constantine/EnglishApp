package com.example.englishapp;

import android.os.Bundle;
import android.view.View;
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

    private EditText inputEditText;
    private Button searchButton;
    private TableLayout resultTable;
    private List<String[]> csvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEditText = findViewById(R.id.inputEditText);
        searchButton = findViewById(R.id.searchButton);
        resultTable = findViewById(R.id.resultTable);

        // 读取嵌入的 CSV 文件内容
        readEmbeddedCSVFile();

        // 设置按钮点击事件监听器
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = inputEditText.getText().toString().trim();
                searchWord(query);
            }
        });
    }

    private void readEmbeddedCSVFile() {
        csvData = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                csvData.add(columns);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchWord(String query) {
        resultTable.removeViews(1, resultTable.getChildCount() - 1);  // 清除之前的搜索结果，但保留表头
        for (String[] row : csvData) {
            for (String data : row) {
                if (data.toLowerCase().startsWith(query.toLowerCase())) {
                    addTableRow(row);
                    break;
                }
            }
        }
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
            TextView textView = new TextView(this);
            if (i == 2) { // 如果是“位置”列
                textView.setText(locationData);
                textView.setTextSize(9);
            } else {
                textView.setText(rowData[i]);
                textView.setTextSize(12);
            }
            textView.setPadding(8, 8, 8, 8);
            tableRow.addView(textView);
        }

        resultTable.addView(tableRow);
    }
}
