package vn.tripi.restclient.middlewares;

import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {

    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return null;
    }
}
