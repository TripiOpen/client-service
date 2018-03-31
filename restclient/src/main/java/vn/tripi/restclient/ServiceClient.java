package vn.tripi.restclient;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import vn.tripi.restclient.adapters.BooleanAdapter;
import vn.tripi.restclient.adapters.IntegerAdapter;
import vn.tripi.restclient.middlewares.RxErrorHandlingCallAdapterFactory;

/**
 * Created by lion on 31/01/2018.
 */

public class ServiceClient {

    private static final int CONNECTION_TIMEOUT = 5;

    public static <T> T createService(Application application, String endPoint, Class<T> serviceClass) {
        return createService(application, endPoint, serviceClass, getGSONConfig(), null);
    }

    public static <T> T createService(Application application, String endPoint, Class<T> serviceClass,
                                      Interceptor interceptor) {
        return createService(application, endPoint, serviceClass, getGSONConfig(), interceptor);
    }

    private static <T> T createService(Application application, String endPoint,
                                       Class<T> serviceClass, @NonNull Gson gson, Interceptor interceptor) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        int cacheSize = 30 * 1024 * 1024; // 10 MiB
        httpClientBuilder.cache(new Cache(application.getCacheDir(), cacheSize));
        if (interceptor != null) {
            ///clear all interceptors before add new
            httpClientBuilder.networkInterceptors().clear();
            httpClientBuilder.addInterceptor(interceptor);
        }

        httpClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(endPoint)
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson));

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            httpClientBuilder.addInterceptor(logging);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        Retrofit retrofit = builder.client(getUnsafeOkHttpClient(httpClientBuilder))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(serviceClass);
    }

    private static OkHttpClient getUnsafeOkHttpClient(OkHttpClient.Builder builder) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((String hostname, SSLSession session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Gson getGSONConfig() {
        BooleanAdapter booleanAdapter = new BooleanAdapter();
        IntegerAdapter integerAdapter = new IntegerAdapter();
        return new GsonBuilder().serializeNulls().registerTypeAdapter(Boolean.class, booleanAdapter)
                .registerTypeAdapter(boolean.class, booleanAdapter)
                .registerTypeAdapter(Integer.class, integerAdapter)
                .registerTypeAdapter(int.class, integerAdapter)
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }
}

