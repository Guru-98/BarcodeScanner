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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import ml.guru.barcodescanner.adapter.DBAdapter;
import ml.guru.barcodescanner.db.DBHelper;
import ml.guru.barcodescanner.db.DBQueries;
import ml.guru.barcodescanner.model.Items;
import ml.guru.barcodescanner.util.PathUtil;


public class MainActivity extends AppCompatActivity {

    private Button scanbtn;
    private IntentIntegrator barScan;
    private Button loadbtn;
    private Button savebtn;
    private ListView listView;

    private DBQueries dbQueries;
    private DBHelper dbHelper;

    private final int loadCode = 0x000010ad;
    private final int GOT_STORAGE_PERMISION = 0x900d;
    private ArrayList<Items> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getApplicationContext());
        dbQueries =new DBQueries(getApplicationContext());

        barScan = new IntentIntegrator(this);

        listView =findViewById(R.id.listview);
        scanbtn = findViewById(R.id.scanbtn);
        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barScan.initiateScan();
            }
        });
        loadbtn = findViewById(R.id.loadbtn);
        savebtn = findViewById(R.id.savebtn);


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
             if(!dbQueries.findItem(contents)) {
                 Items nItem = new Items(contents);
                 nItem.setCount(1);
                 nItem.setPresent(true);
                 dbQueries.insertItem(nItem);
             }
             else{
                 dbQueries.checkItem(contents);
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
                //TODO: Saving XLS files

            }
        });
    }

    private void populateList(){
        dbQueries.open();
        itemsList = dbQueries.readItems();
        Log.v("POP", String.valueOf(itemsList.size()));
        DBAdapter listAdapter = new DBAdapter(getApplicationContext(), itemsList);
        listView.setAdapter(listAdapter);
        dbQueries.close();
    }
}
