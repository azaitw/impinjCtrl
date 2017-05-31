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
import okhttp3.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ReaderController {

    public static final String EVENT_START_READER = "startreader";
    public static final String EVENT_TERMINATE_READER = "terminatereader";
    public static final String EVENT_TRANSFER_DATA = "rxdata";
    public static final String EVENT_GET_READER_INFO = "getreaderinfo";

    private boolean mIsDebugMode;

    private String mReaderHost;
    private String mApiHost;
    private JSONObject mMsg;
    private ImpinjReader mReader;
    private Socket mSocket;
    private HttpClient mHttpClient;

    ReaderController(@NotNull String readerHost) {
        this.mReaderHost = readerHost;
        this.mApiHost = PropertyUtils.getAPiHost();
        mIsDebugMode = PropertyUtils.isDebugMode();

    }

    public void initialize() {
        mMsg = new JSONObject();
        mReader = new ImpinjReader();
        mHttpClient = HttpClient.getInstance();

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
                            .url(mApiHost + "/race/joinReaderRoom?sid=" + mSocket.id() + "&isSocket=1")
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

                                // init reader
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
                        mMsg.put("message", "start reader");
                        System.out.println(mMsg.toJSONString());

                    } catch (OctaneSdkException ex) {
                        System.out.println(ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace(System.out);
                    }

                }

            }).on(EVENT_GET_READER_INFO, new Emitter.Listener() {
                public void call(Object... args) {
                    if (null == mReader) {
                        return;
                    }
                    // TODO: new api endpoint
                    try {
                        // TODO: send back to api (control panel)
                        ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader));
                    } catch (OctaneSdkException ex) {
                        System.out.println(ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace(System.out);
                    }
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
                        }
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

            if (mIsDebugMode) {
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
