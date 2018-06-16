package slade.carter.spyclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private static SettingsActivity _instance;

    private EditText _ipEditText;
    private EditText _serverPortEditText;
    private EditText _downloadPortEditText;
    private EditText _downloadPathEditText;
    private Button _updateButton;
    private Button _clearButton;

    public static SettingsActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _instance = this;

        _ipEditText = (EditText) findViewById(R.id.ipEditText);
        _serverPortEditText = (EditText) findViewById(R.id.serverPortEditText);
        _downloadPortEditText = (EditText) findViewById(R.id.downloadEditText);
        _downloadPathEditText = (EditText) findViewById(R.id.downloadPathEditText);

        _updateButton = (Button) findViewById(R.id.updateButton);
        _clearButton = (Button) findViewById(R.id.clearButton);

        initSettings();
        initListeners();
    }

    private void initSettings() {
        _ipEditText.setText(Config.IP_ADDRESS);
        _serverPortEditText.setText(Integer.toString(Config.SERVER_PORT));
        _downloadPortEditText.setText(Integer.toString(Config.DOWNLOAD_PORT));
        _downloadPathEditText.setText(Config.DOWNLOAD_PATH);
    }

    private void initListeners() {
        _updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.setIpAddress(_ipEditText.getText().toString())
                    || !Config.setServerPort(Integer.parseInt(_serverPortEditText.getText().toString()))
                    || !Config.setDownloadPort(Integer.parseInt(_downloadPortEditText.getText().toString()))
                    || !Config.setDownloadPath(_downloadPathEditText.getText().toString()))
                    MainActivity.getInstance().showText("Ошибка изменения настроек!");
                else
                    MainActivity.getInstance().showText("Настройки обновлены!");
            }
        });

        _clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.setDefaultConfig();
                MainActivity.getInstance().showText("Настройки сброшены!");
                initSettings();
            }
        });
    }
}
