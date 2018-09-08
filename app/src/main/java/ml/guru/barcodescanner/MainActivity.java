package ml.guru.barcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.PathUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.net.URISyntaxException;

import ml.guru.barcodescanner.db.DBHelper;
import ml.guru.barcodescanner.db.DBQueries;
import ml.guru.barcodescanner.util.PathUtil;


public class MainActivity extends AppCompatActivity {

    private Button scanbtn;
    private IntentIntegrator barScan;
    private Button loadbtn;
    private Button savebtn;

    private DBQueries dbQueries;
    private DBHelper dbHelper;

    private final int loadCode = 0x000010ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getApplicationContext());
        dbQueries =new DBQueries(getApplicationContext());

        barScan = new IntentIntegrator(this);

        scanbtn = findViewById(R.id.scanbtn);
        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barScan.initiateScan();
            }
        });

        loadbtn = findViewById(R.id.loadbtn);
        loadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.ms-excel");
                //intent.setType("*/*");

                startActivityForResult(intent, loadCode);
            }
        });

        savebtn = findViewById(R.id.savebtn);
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Saving XLS files

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
         if (result.getContents() ==  null){
             Toast.makeText(this,"Barcode/QR is not recognized", Toast.LENGTH_SHORT).show();
         }
         else {
             Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
         }
        }
        else if(requestCode == loadCode && resultCode == Activity.RESULT_OK && data !=null){
            Uri uri = data.getData();
            assert uri != null;
            String FilePath = null;
            try {
                FilePath = PathUtil.getPath(getApplicationContext(), uri);
            } catch (URISyntaxException e) {
                Log.getStackTraceString(e);
            }

            Log.d("LoadBTN",FilePath);

            //TODO: Load Excel file to UI

            // Using APACHE POI
//            XSSFSheet mSS;
//            XSSFWorkbook mWB;
//            try{
//                FileInputStream file = new FileInputStream(new File(FilePath));
//                mWB = new XSSFWorkbook();
//                mSS = mWB.getSheetAt(0);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            // Using SQLITE2XL
            dbQueries.open();
            ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), DBHelper.DB_NAME, false);
            excelToSQLite.importFromFile(FilePath, new ExcelToSQLite.ImportListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onCompleted(String dbName) {
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
}
