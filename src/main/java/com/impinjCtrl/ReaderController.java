package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.sun.istack.internal.NotNull;
import io.socket.client.IO;
import io.socket.client.Socket;

import io.socket.emitter.Emitter;
import lib.HttpClient;
import lib.PropertyUtils;
import okhttp3.*;
import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.*;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import com.sun.istack.internal.Nullable;


public class ReaderController {
    private static final String EVENT_READER_COMMAND = "readercommand";
    // Race session-based data, init at startreader, destroy at terminatereader
    public static String mSocketId;
    public static String mEventId;
    public static String mRaceId;
    public static Long mValidIntervalMs;
    public static JSONObject mSlaveEpcMap;
    public static String mLogFileName;
    public static JSONObject mRecordsHashTable;
    public static JSONObject mSlaveEpcStat;
    //public static JSONArray mReadResultRaw;
    private static HttpClient mHttpClient;

    private boolean mIsDebugMode;
    private String mReaderHost;
    private String mApiHost;
    private ImpinjReader mReader;
    private Socket mSocket;

    private HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    private X509TrustManager mMyX509TrustManager = new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
    };
    private final TrustManager[] trustAllCerts= new TrustManager[] {mMyX509TrustManager};
    private SSLContext mySSLContext;

    JSONParser parser = new JSONParser();

    ReaderController(@NotNull String readerHost) {
        this.mReaderHost = readerHost;
        this.mApiHost = PropertyUtils.getAPiHost();
        mIsDebugMode = PropertyUtils.isDebugMode();
    }

    public void initialize() {
        mReader = new ImpinjReader();
        if (mIsDebugMode) {
            initReader();
        } else {
            mHttpClient = HttpClient.getInstance();
            try {
                if (mApiHost.matches("^(https)://.*$")) {
                    mySSLContext = SSLContext.getInstance("TLS");
                    mySSLContext.init(null, trustAllCerts, null);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .hostnameVerifier(myHostnameVerifier)
                            .sslSocketFactory(mySSLContext.getSocketFactory(), mMyX509TrustManager)
                            .build();

                    // default settings for all sockets
                    IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
                    IO.setDefaultOkHttpCallFactory(okHttpClient);
                    // set as an option
                    IO.Options opts = new IO.Options();
                    opts.callFactory = okHttpClient;
                    opts.webSocketFactory = okHttpClient;
                    mSocket = IO.socket(mApiHost, opts);
                } else {
                    mSocket = IO.socket(mApiHost);
                }
                mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    public void call(Object... args) {
                        mSocketId = mSocket.id();
                        System.out.println("Connected: " + mApiHost + ". socket ID: " + mSocketId);

                        Request sendMsg = new Request.Builder()
                                    .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                                    .build();
                        mHttpClient.request(sendMsg, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println("Socket.EVENT_CONNECT join chat rooms");
                                try {
                                    ResponseBody body = response.body();
                                    body.close();
                                } catch (Exception e) {
                                    System.out.println("Socket.EVENT_CONNECT error: " + e.getMessage());
                                }
                                initReader();
                            }
                        });
                    }
                }).on(EVENT_READER_COMMAND, new Emitter.Listener() {
                    public void call(Object... args) {
                        if (null == mReader) {
                            return;
                        }
                        JSONObject mMsg = new JSONObject();
                        try {
                            String input = args[0].toString();
                            JSONObject inputJson = (JSONObject) parser.parse(input);
                            String command = inputJson.get("command").toString();
                            System.out.println("Received command: " + command);
                            if (command.equals("START")) {
                                if (!mReader.isConnected()) {
                                    initReader();
                                }
                                mReader.start();
                                if (inputJson.get("eventId") != null) {
                                    mEventId = inputJson.get("eventId").toString();
                                }
                                if (inputJson.get("raceId") != null) {
                                    mRaceId = inputJson.get("raceId").toString();
                                }
                                if (null == mEventId && null == mRaceId) {
                                    System.out.println("Please specify eventId or raceId in parameter");
                                    return;
                                }
                                if (inputJson.get("validIntervalMs") != null) {
                                    mValidIntervalMs = (Long) inputJson.get("validIntervalMs");
                                } else {
                                    mValidIntervalMs = PropertyUtils.getDefaultValidIntervalMs();
                                }
                                if (inputJson.get("slaveEpcMap") != null) {
                                    mSlaveEpcMap = (JSONObject) inputJson.get("slaveEpcMap");
                                } else {
                                    mSlaveEpcMap = new JSONObject();
                                }
                                mRecordsHashTable = new JSONObject();
                                mSlaveEpcStat = new JSONObject();
                                //mReadResultRaw = new JSONArray();
                                mLogFileName = PropertyUtils.getLogFileName();
                                FileWriter file = new FileWriter(ReaderController.mLogFileName);
                                file.write("");
                                file.flush();
                            }
                            if (command.equals("STOP")) {
                                mReader.stop();
                                mEventId = null;
                                mRaceId = null;
                                mValidIntervalMs = null;
                                mSlaveEpcMap = null;
                                mRecordsHashTable = null;
                                mSlaveEpcStat = null;
                                mLogFileName = null;
                            }
                            if (mRaceId != null) {
                                mMsg.put("race", mRaceId);
                            }
                            if (mEventId != null) {
                                mMsg.put("event", mEventId);
                            }
                            mMsg.put("type", "readerstatus");
                            mMsg.put("payload", ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader)));
                        } catch (OctaneSdkException e) {
                            System.out.println("readercommand error: " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("readercommand error: " + e.getMessage());
                        }
                        System.out.println("Responding reader status: " + mMsg.toJSONString());
                        Request sendMsg = new Request.Builder()
                                    .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                                    .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                                    .build();
                        mHttpClient.request(sendMsg, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    ResponseBody body = response.body();
                                    body.close();
                                } catch (Exception e) {
                                    System.out.println("Responding error: " + e.getMessage());
                                }
                            }
                        });
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    public void call(Object... args) {
                        mSocketId = null;
                        dischargeReader();
                        System.out.println("Socket disconnected");
                    }
                });
                System.out.println("Connecting: " + mApiHost);
                mSocket.connect();
            } catch (URISyntaxException e) {
                System.out.println("Socket.io error: " + e.getMessage());
            } catch (NoSuchAlgorithmException e)  {
                System.out.println("Socket.io error: " + e.getMessage());
            } catch (KeyManagementException e) {
                System.out.println("Socket.io error: " + e.getMessage());
            }
        }
    }
    private void initReader() {
        if (null == mReader) {
            System.out.println("reader obj is null: initialReader");
            return;
        }
        System.out.println("Initializing reader");
        try {
            dischargeReader();
            mReader.connect(mReaderHost);
            Settings settings = ReaderSettings.getSettings(mReader);
            mReader.setTagReportListener(new ReportFormat());
            mReader.applySettings(settings);

            // 後門
            Scanner s = new Scanner(System.in);
            while (s.hasNextLine() && mReader.isConnected()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("START")) {
                    mValidIntervalMs = PropertyUtils.getDefaultValidIntervalMs();
                    mRecordsHashTable = new JSONObject();
                    mReader.start();
                } else if (line.equals("STOP")) {
                    mValidIntervalMs = null;
                    mRecordsHashTable = null;
                    mReader.stop();
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getReaderInfo(mReader, settings);
                }
            }
        } catch (OctaneSdkException e) {
            System.out.println("InitReader error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("InitReader error: " + e.getMessage());
        }
    }
    private void dischargeReader() {
        if (mReader.isConnected()) {
            System.out.println("Discharging reader");
            try {
                mReader.removeTagReportListener();
                mReader.stop();
                mReader.disconnect();
            } catch (OctaneSdkException e) {
                System.out.println("dischargeReader error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("dischargeReader error: " + e.getMessage());
            }
        }
    }
}
