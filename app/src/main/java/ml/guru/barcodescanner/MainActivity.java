package ml.guru.barcodescanner;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.*;


public class MainActivity extends AppCompatActivity {

    private Button scanbtn;
    private IntentIntegrator barScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barScan = new IntentIntegrator(this);

        scanbtn = findViewById(R.id.scanbtn);
        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barScan.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
         if (result.getContents() ==  null){
             Toast.makeText(this,"Barcode/QR is not recognized", Toast.LENGTH_SHORT).show();
         }
         else {
             Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
         }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
