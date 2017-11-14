package lib;

import com.google.gson.Gson;
import com.impinjCtrl.ReaderController;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import model.RaceResult;
import model.ReaderInfoResult;
import model.SocketInput;
import okhttp3.*;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class Api {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static Api instance;
    private String mSocketId; // Session-based data, init at startreader, destroy at terminatereader
    private OkHttpClient mHttpClient;
    private Gson mGson;

    public static synchronized Api getInstance() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }

    public void buildHttpClient () {
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
    }

    private void request (Request request, Callback cb) { mHttpClient.newCall(request).enqueue(cb); }

    // Send reading entry to API server
    // output: { type: 'rxdata', payload: {event: STR, race: STR, records: ARRAY, recordType: STR} }
    public void sendResult(RaceResult result) {
        if (mSocketId != null) {
            Request req = new Request.Builder()
                    .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                    .post(RequestBody.create(Api.MEDIA_TYPE_JSON, mGson.toJson(result)))
                    .build();

            request(req, new Callback() {
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    System.out.println("Sending tag data failed: " + e.getMessage());
                }
                public void onResponse(Call call, Response response) throws IOException {
                    ResponseBody body = response.body();
                    body.close();
                }
            });
        }
    }
    // Socket.io controls
    public void initSocketIOInterface() {
        HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        X509TrustManager mMyX509TrustManager = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[] {}; }
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        };
        final Socket mSocket;
        final String mApiHost = PropertyUtils.getAPiHost();
        final TrustManager[] trustAllCerts= new TrustManager[] {mMyX509TrustManager};
        final SSLContext mySSLContext;
        try {
            if (mApiHost.matches("^(https)://.*$")) {
                mySSLContext = SSLContext.getInstance("TLS");
                mySSLContext.init(null, trustAllCerts, null);
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .hostnameVerifier(myHostnameVerifier)
                        .sslSocketFactory(mySSLContext.getSocketFactory(), mMyX509TrustManager)
                        .build();
                IO.setDefaultOkHttpWebSocketFactory(okHttpClient); // default settings for all sockets
                IO.setDefaultOkHttpCallFactory(okHttpClient);
                IO.Options opts = new IO.Options(); // set as an option
                opts.callFactory = okHttpClient;
                opts.webSocketFactory = okHttpClient;
                mSocket = IO.socket(mApiHost, opts);
            } else {
                mSocket = IO.socket(mApiHost);
            }
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                public void call(Object... args) {
                    mSocketId = mSocket.id();
                    System.out.println("Socket connected: " + mApiHost + ". sid: " + mSocketId);
                    Request sendMsg = new Request.Builder().url(mApiHost + "/api/socket/impinj?sid=" + mSocketId).build();
                    request(sendMsg, new Callback() {
                        public void onFailure(Call call, IOException e) {
                            System.out.println("Socket.EVENT_CONNECT error: " + e.getMessage());
                            e.printStackTrace();
                        }
                        public void onResponse(Call call, Response response) throws IOException {
                            ResponseBody body = response.body();
                            System.out.println(response.body());
                            body.close();
                        }
                    });
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() { // Only disconnect socket, not changing reader status
                public void call(Object... args) {
                    mSocketId = null;
                    System.out.println("Socket disconnected");
                }
            }).on("readercommand", new Emitter.Listener() {
                // It's suppose to get event, race infos
                public void call(Object... args) {
                    // JSONObject mMsg = new JSONObject();
                    ReaderInfoResult infoResult;
                    String input = args[0].toString();

                    SocketInput inputJson = mGson.fromJson(input, SocketInput.class);
                    String command = inputJson.getCommand();
                    String eventId = inputJson.getEventId();
                    String raceId = inputJson.getRaceId();

                    if (command == null || eventId == null || raceId == null) {
                        return;
                    }

                    infoResult = ReaderController.getInstance().controlReader(command, eventId, raceId);

                    // initial log file
                    infoResult.setLogFile(Logging.getInstance().initLogging(eventId, raceId));

                    System.out.println("EVENT_READER_COMMAND Response: " + mGson.toJson(infoResult));
                    Request sendMsg = new Request.Builder()
                            .url(mApiHost + "/api/socket/impinj?sid=" + mSocketId)
                            .post(RequestBody.create(Api.MEDIA_TYPE_JSON, mGson.toJson(infoResult)))
                            .build();

                    request(sendMsg, new Callback() {
                        public void onFailure(Call call, IOException e) {
                            System.out.println("Responding error: " + e.getMessage());
                            e.printStackTrace();
                        }
                        public void onResponse(Call call, Response response) throws IOException {
                            ResponseBody body = response.body();
                            body.close();
                        }
                    });
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
