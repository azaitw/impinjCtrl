package lib;

import com.impinjCtrl.ReaderController;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class Api {
    private static String mSocketId; // Session-based data, init at startreader, destroy at terminatereader
    private static OkHttpClient mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static void request (Request request, Callback cb) { mHttpClient.newCall(request).enqueue(cb); }
    // Send reading entry to API server
    public static void sendResult(JSONObject result, String eventId, String raceId) {
        if (mSocketId != null) {
            JSONObject txData = new JSONObject();
            try {
                result.put("event", eventId);
                result.put("race", raceId);
                txData.put("type", "rxdata");
                txData.put("payload", result);
            } catch (Exception e) {
                System.out.println("sendResult JSON error: " + e.getMessage());
            }
            Request req = new Request.Builder()
                    .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                    .post(RequestBody.create(Api.MEDIA_TYPE_JSON, txData.toJSONString()))
                    .build();

            Api.request(req, new Callback() {
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
    public static void initSocketIOInterface() {
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
        final JSONParser parser = new JSONParser();
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
                    Api.request(sendMsg, new Callback() {
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody body = response.body();
                                body.close();
                            } catch (Exception e) {
                                System.out.println("Socket.EVENT_CONNECT error: " + e.getMessage());
                            }
                        }
                    });
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() { // Only disconnect socket, not changing reader status
                public void call(Object... args) {
                    mSocketId = null;
                    System.out.println("Socket disconnected");
                }
            }).on("readercommand", new Emitter.Listener() {
                public void call(Object... args) {
                    JSONObject mMsg = new JSONObject();
                    try {
                        String input = args[0].toString();
                        JSONObject inputJson = (JSONObject) parser.parse(input);
                        String command = inputJson.get("command").toString();
                        String eventId = inputJson.get("event").toString();
                        String raceId = inputJson.get("race").toString();
                        mMsg = ReaderController.controlReader(command, eventId, raceId);
                    } catch (Exception e) {
                        System.out.println("readercommand error: " + e.getMessage());
                    }
                    System.out.println("EVENT_READER_COMMAND Response: " + mMsg.toJSONString());
                    Request sendMsg = new Request.Builder()
                            .url(mApiHost + "/api/socket/impinj?sid=" + mSocketId)
                            .post(RequestBody.create(Api.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                            .build();
                    Api.request(sendMsg, new Callback() {
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
