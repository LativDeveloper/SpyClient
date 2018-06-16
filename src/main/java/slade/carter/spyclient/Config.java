package slade.carter.spyclient;

import android.content.SharedPreferences;

/**
 * Created by user-pc on 25.05.2017.
 */

public class Config {

    private static SharedPreferences _preferences;

    public static String IP_ADDRESS;
    public static int SERVER_PORT;
    public static int DOWNLOAD_PORT;
    public static String DOWNLOAD_PATH;
    public static String token;

    public static final String INCORRECT_QUERY = "incorrectQuery";
    public static final String INVALID_AUTH = "invalidAuth";
    public static final String VICTIM_OFFLINE = "victimOffline";
    public static final String SERVER_ERROR = "serverError";
    public static final String FILE_ISNT_DIRECTORY = "fileIsntDirectory";
    public static final String FILE_IS_DIRECTORY = "fileIsDirectory";

    public static void load(SharedPreferences preferences) {
        _preferences = preferences;
        //IP_ADDRESS = preferences.getString("IP_ADDRESS", "89.223.28.75");
        IP_ADDRESS = preferences.getString("IP_ADDRESS", "192.168.0.102");
        SERVER_PORT = preferences.getInt("SERVER_PORT", 1121);
        DOWNLOAD_PORT = preferences.getInt("DOWNLOAD_PORT", 1111);
        DOWNLOAD_PATH = preferences.getString("DOWNLOAD_PATH", "/sdcard/Android/");
    }

    public static void setDefaultConfig() {
        setIpAddress("192.168.0.101");
        setServerPort(1121);
        setDownloadPort(1111);
        setDownloadPath("sdcard/");
    }

    public static boolean setIpAddress(String ipAddress) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString("IP_ADDRESS", ipAddress);
        if (editor.commit()) {
            IP_ADDRESS = ipAddress;
            return true;
        }
        return false;
    }

    public static boolean setServerPort(int port) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putInt("SERVER_PORT", port);
        if (editor.commit()) {
            SERVER_PORT = port;
            return true;
        }
        return false;
    }

    public static boolean setDownloadPort(int port) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putInt("DOWNLOAD_PORT", port);
        if (editor.commit()) {
            DOWNLOAD_PORT = port;
            return true;
        }
        return false;
    }

    public static boolean setDownloadPath(String path) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString("DOWNLOAD_PATH", path);
        if (editor.commit()) {
            DOWNLOAD_PATH = path;
            return true;
        }
        return false;
    }
}
