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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ReaderController {

    public static final String EVENT_START_READER = "startreader";
    public static final String EVENT_TERMINATE_READER = "terminatereader";
    public static final String EVENT_TRANSFER_DATA = "rxdata";
    public static final String EVENT_GET_READER_STATUS = "getreaderstatus";
    public static final String EVENT_READER_STATUS = "readerstatus";

    private boolean mIsStarted = false;
    private boolean mIsDebugMode;

    private String mReaderHost;
    private String mApiHost;
    private JSONObject mMsg;
    private ImpinjReader mReader;
    private Socket mSocket;
    private HttpClient mHttpClient;
    private Integer mEventId = PropertyUtils.getEventId();


    ReaderController(@NotNull String readerHost) {
        this.mReaderHost = readerHost;
        this.mApiHost = PropertyUtils.getAPiHost();

        mIsDebugMode = PropertyUtils.isDebugMode();

    }

    public void initialize() {
        if (null == mEventId) {
            System.out.println("Please specify the eventId in parameter");
            return;
        }
        mMsg = new JSONObject();
        mReader = new ImpinjReader();
        mHttpClient = HttpClient.getInstance();
        System.out.println("Try to connect to socket: " + mApiHost);

        try {
            mSocket = IO.socket(mApiHost);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                public void call(Object... args) {
                    if (mIsDebugMode) {
                        System.out.println("socket connected");
                        System.out.println("socket ID" + mSocket.id());
                    }
                    // join / register id to socket io
                    Request req = new Request.Builder()
                            .url(mApiHost + "/api/race/joinReaderRoom?sid=" + mSocket.id() + "&isSocket=1")
                            .build();

                    mHttpClient.request(req, new Callback() {
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                if (mIsDebugMode) {
                                    HttpClient.parseRespose(response);
                                }
                                initialReader();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).on(EVENT_START_READER, new Emitter.Listener() {
                public void call(Object... args) {
                    System.out.println("start reader");
                    if (null == mReader) {
                        return;
                    }
                    try {
                        if (!mReader.isConnected()) {
                            initialReader();
                        }
                        mReader.start();
                        mIsStarted = true;
                        mMsg.put("event", PropertyUtils.getEventId());
                        mMsg.put("message", "start reader");
                        System.out.println(mMsg.toJSONString());

                    } catch (OctaneSdkException ex) {
                        System.out.println(ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace(System.out);
                    }

                }

            }).on(EVENT_GET_READER_STATUS, new Emitter.Listener() {
                public void call(Object... args) {
                    System.out.println("get reader status");
                    if (null == mReader) {
                        return;
                    }
                    mMsg.put("event", PropertyUtils.getEventId());
                    mMsg.put("type", EVENT_READER_STATUS);
                    if (mIsStarted) {
                        if (mIsDebugMode) {
                            mMsg.put("payload", "debug");
                        } else {
                            mMsg.put("payload", "started");
                        }
                    } else {
                        mMsg.put("payload", "idle");
                    }
                    System.out.println(mMsg.toJSONString());

                    Request sendMsg = new Request.Builder()
                    .url(PropertyUtils.getAPiHost() + "/api/race/readerRoom?isSocket=1")
                    .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                    .build();

                    mHttpClient.request(sendMsg, new Callback() {
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                if (mIsDebugMode) {
                                    HttpClient.parseRespose(response);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }).on(EVENT_TERMINATE_READER, new Emitter.Listener() {
                public void call(Object... args) {
                    System.out.println("stop reader");
                    if (null == mReader) {
                        return;
                    }

                    try {
                        if (mReader.isConnected()) {
                            mReader.stop();
                            mIsStarted = false;
                        }
                        mMsg.put("event", PropertyUtils.getEventId());
                        mMsg.put("message", "stop reader");
                        System.out.println(mMsg.toJSONString());

                    } catch (OctaneSdkException ex) {
                        System.out.println(ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace(System.out);
                    }

                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                public void call(Object... args) {
                    System.out.println("socket disconnect");
                }

            });

            mSocket.connect();

        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        mMsg.put("message", "Connecting");

    }

    private void initialReader() {
        if (null == mReader) {
            System.out.println("reader obj is null: initialReader");
            return;
        }

        try {
            checkReaderConnection();
            mReader.connect(mReaderHost);
            Settings settings = ReaderSettings.getSettings(mReader);
            mReader.setTagReportListener(new ReportFormat());
            mReader.applySettings(settings);

            // 後門
            Scanner s = new Scanner(System.in);
            while (s.hasNextLine() && mReader.isConnected()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("STOP")) {
                    break;
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getReaderInfo(mReader, settings);
                }
            }

        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private void checkReaderConnection() {
        if (mReader.isConnected()) {
            try {
                mReader.removeTagReportListener();
                mReader.stop();
                mReader.disconnect();

            } catch (OctaneSdkException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    private void destory() {
        mSocket.disconnect();
        checkReaderConnection();
        mHttpClient.distory();

        mReader.removeTagReportListener();
        mSocket = null;
        mReader = null;
    }
}
