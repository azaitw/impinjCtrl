package lib;

import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static HttpClient instance;
    OkHttpClient mHttpClient;

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static synchronized HttpClient getInstance() {
        if(instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    private HttpClient(){
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void request (Request request, Callback cb) {
        if (null != mHttpClient) {
            mHttpClient.newCall(request).enqueue(cb);
        } else {
            System.out.println("OkHttpClient not exist!");
        }
    }

    public static void parseRespose(Response response) throws IOException {
        System.out.println("--- parseRespose ---");
        ResponseBody responseBody = response.body();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        System.out.println(responseBody.string());
        System.out.println("---------------------");
    }

    public void distory() {
        mHttpClient = null;
    }
}
