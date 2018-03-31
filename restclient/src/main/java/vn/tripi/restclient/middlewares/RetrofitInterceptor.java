package vn.tripi.restclient.middlewares;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Nguyen Van Do (deverlex)
 * @email: nguyendo94vn@gmail.com
 * @date: 03/2018
 */

public class RetrofitInterceptor implements Interceptor {

    private Map<String, String> mapHeaders;

    private RetrofitInterceptor() {
        mapHeaders = new HashMap<>();
    }

    public RetrofitInterceptor newBuilder() {
        return new RetrofitInterceptor();
    }

    public RetrofitInterceptor header(String key, String value) {
        mapHeaders.put(key, value);
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = initializeHeader(chain);
        Request request = builder.build();
        Response response = chain.proceed(request);
        return response;
    }

    private Request.Builder initializeHeader(Chain chain) {
        Request originRequest = chain.request();
        Request.Builder builder = originRequest.newBuilder();
        for (Map.Entry<String, String> item : mapHeaders.entrySet()) {
            builder.header(item.getKey(), item.getValue());
        }
        return builder.method(originRequest.method(), originRequest.body());
    }
}
