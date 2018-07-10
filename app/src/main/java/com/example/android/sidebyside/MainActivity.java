package com.example.android.sidebyside;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void redirect(View view){
        Intent intent = new Intent();

        if (view.getId() == R.id.setting){
            intent = new Intent(this,Setting.class);
        } else if (view.getId() == R.id.photo1){
            intent = new Intent(this,Photo1.class);
        } else if (view.getId() == R.id.photo2){
            intent = new Intent(this,Photo2.class);
        }else if (view.getId() == R.id.compare){
            intent = new Intent(this,Compare.class);
        }else if (view.getId() == R.id.share){
            intent = new Intent (this,Share.class);
        }
        startActivity(intent);
    }//redirect() method
}
