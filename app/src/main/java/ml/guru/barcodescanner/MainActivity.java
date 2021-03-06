package ml.guru.barcodescanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import ml.guru.barcodescanner.adapter.DBAdapter;
import ml.guru.barcodescanner.db.DBConstants;
import ml.guru.barcodescanner.db.DBHelper;
import ml.guru.barcodescanner.db.DBQueries;
import ml.guru.barcodescanner.model.Items;
import ml.guru.barcodescanner.util.PathUtil;


public class MainActivity extends AppCompatActivity {

    private IntentIntegrator barScan;
    private Button loadbtn;
    private Button savebtn;
    private ListView listView;

    private DBQueries dbQueries;

    private final int loadCode = 0x10ad;
    private final int saveCode = 0x05a3;
    private final int GOT_STORAGE_PERMISION = 0x900d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        dbQueries =new DBQueries(getApplicationContext());
        clearDB();

        barScan = new IntentIntegrator(this);
        barScan.setOrientationLocked(true);

        listView =findViewById(R.id.listview);
        loadbtn = findViewById(R.id.loadbtn);
        savebtn = findViewById(R.id.savebtn);

        findViewById(R.id.scanbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barScan.initiateScan();
            }
        });

        findViewById(R.id.clearbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearDB();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        GOT_STORAGE_PERMISION);
        }
        else {setListeners();}
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            String contents = result.getContents();
            if (contents ==  null){
             Toast.makeText(this,"Barcode/QR is not recognized", Toast.LENGTH_SHORT).show();
            } else {
             dbQueries.open();
                if (!dbQueries.checkItem(contents)) {
                 Items nItem = new Items(contents);
                 nItem.setCount(1);
                 nItem.setPresent(true);
                 dbQueries.insertItem(nItem);
             }
             dbQueries.close();

             populateList();

             Toast.makeText(this, contents, Toast.LENGTH_LONG).show();
         }
        }
        else if(requestCode == loadCode && resultCode == Activity.RESULT_OK && data !=null){
            Uri uri = data.getData();
            assert uri != null;
            String FilePath = PathUtil.getPath(getApplicationContext(), uri);

            Log.d("LoadBTN",FilePath);

            dbQueries.open();
            ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), DBHelper.DB_NAME, true);
            assert FilePath != null;
            excelToSQLite.importFromFile(FilePath, new ExcelToSQLite.ImportListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onCompleted(String dbName) {
                    populateList();
                    Toast.makeText(getApplicationContext(),"Import Complete",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Excel Import",e.getMessage());
                    Toast.makeText(getApplicationContext(),"Import Error",Toast.LENGTH_SHORT).show();
                }
            });
            dbQueries.close();
        } else if (requestCode == saveCode && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            assert uri != null;
            String FilePath = PathUtil.getPath(getApplicationContext(), uri);
            assert FilePath != null;
            String DirPath = FilePath.substring(0, FilePath.lastIndexOf('/'));
            String FileName = FilePath.substring(FilePath.lastIndexOf('/'));

            Log.d("SAVBTN", FilePath);

            SQLiteToExcel sqliteToExcel = new SQLiteToExcel(getApplicationContext(), DBHelper.DB_NAME, DirPath);
            sqliteToExcel.exportSingleTable(DBConstants.STOCK_TABLE, FileName, new SQLiteToExcel.ExportListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onCompleted(String filePath) {
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Log.getStackTraceString(e);
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GOT_STORAGE_PERMISION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setListeners();
                } else {
                    loadbtn.setBackgroundColor(Color.RED);
                    savebtn.setBackgroundColor(Color.RED);
                }
            }
        }
    }

    private void setListeners(){
        loadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES,
                        new String[]{"application/vnd.ms-excel",
                                     "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
                startActivityForResult(Intent.createChooser(intent,"ChooseFile"), loadCode);
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES,
                        new String[]{"application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
                intent.putExtra(Intent.EXTRA_TITLE, "New.xls");
                startActivityForResult(intent, saveCode);
            }
        });
    }

    private void populateList(){
        dbQueries.open();
        ArrayList<Items> itemsList = dbQueries.readItems();
        Log.v("POP", String.valueOf(itemsList.size()));
        DBAdapter listAdapter = new DBAdapter(getApplicationContext(), itemsList);
        listView.setAdapter(listAdapter);
        dbQueries.close();
    }

    private void clearDB() {
        dbQueries.open();
        dbQueries.clean();
        dbQueries.close();

        populateList();
    }
}
