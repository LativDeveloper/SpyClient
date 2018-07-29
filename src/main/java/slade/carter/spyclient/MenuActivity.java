package slade.carter.spyclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {
    private static MenuActivity _instance;
    private String _victim;
    private TextView _menuTextView;
    private Button _filesButton;
    private Button _smsButton;
    private Button _wifiButton;
    private Button _cameraManager;

    public static MenuActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        _instance = this;

        _filesButton = (Button) findViewById(R.id.filesButton);
        _smsButton = (Button) findViewById(R.id.smsButton);
        _wifiButton = (Button) findViewById(R.id.wifiButton);
        _cameraManager = (Button) findViewById(R.id.cameraButton);
        _menuTextView = (TextView) findViewById(R.id.menuTextView);
        _victim = getIntent().getStringExtra("victim");
        _menuTextView.setText("Меню жертвы " + _victim);


        initListeners();
    }

    private void initListeners() {
        _filesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_instance, FileManagerActivity.class);
                intent.putExtra("victim", _victim);
                startActivity(intent);
            }
        });

        _wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_instance, WifiManagerActivity.class);
                intent.putExtra("victim", _victim);
                startActivity(intent);
            }
        });

        _cameraManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_instance, CameraManagerActivity.class);
                intent.putExtra("victim", _victim);
                startActivity(intent);
            }
        });


    }

}
