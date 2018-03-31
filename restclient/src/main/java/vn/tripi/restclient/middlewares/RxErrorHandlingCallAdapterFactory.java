package vn.tripi.restclient.middlewares;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import vn.tripi.restclient.errors.BaseException;
import vn.tripi.restclient.errors.ErrorResponse;

/**
 * Created by lion on 23/09/2017.
 *
 * ErrorHandling:
 * http://bytes.babbel.com/en/articles/2016-03-16-retrofit2-rxjava-error-handling.html
 * This class only for Call in retrofit 2
 */
public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {

    private static final String TAG = RxErrorHandlingCallAdapterFactory.class.getName();

    private final RxJava2CallAdapterFactory original;

    private RxErrorHandlingCallAdapterFactory() {
        original = RxJava2CallAdapterFactory.create();
    }

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Object> {
        private final Retrofit retrofit;
        private final CallAdapter<R, Object> wrapped;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, Object> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @Override
        public Object adapt(Call<R> call) {
            Object result = wrapped.adapt(call);
            if (result instanceof Single) {
                return ((Single) result).onErrorResumeNext(new Function<Throwable, SingleSource>() {
                    @Override
                    public SingleSource apply(@NonNull Throwable throwable) throws Exception {
                        return Single.error(asRetrofitException(throwable));
                    }
                });
            }
            if (result instanceof Observable) {
                return ((Observable) result).onErrorResumeNext(new Function<Throwable, ObservableSource>() {
                    @Override
                    public ObservableSource apply(@NonNull Throwable throwable) throws Exception {
                        return Observable.error(asRetrofitException(throwable));
                    }
                });
            }

            if (result instanceof Completable) {
                return ((Completable) result).onErrorResumeNext(new Function<Throwable, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@NonNull Throwable throwable) throws Exception {
                        return Completable.error(asRetrofitException(throwable));
                    }
                });
            }

            return result;
        }

        private BaseException asRetrofitException(Throwable throwable) {
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                Response response = httpException.response();
                if (response.errorBody() != null) {
                    try {
                        ErrorResponse errorResponse = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && !TextUtils.isEmpty(errorResponse.getMessage())) {
                            return BaseException.toServerError(errorResponse);
                        } else {
                            return BaseException.toHttpError(response);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    return BaseException.toHttpError(response);
                }
            }

            if (throwable instanceof IOException) {
                return BaseException.toNetworkError((IOException) throwable);
            }

            if (throwable instanceof BaseException) {
                return (BaseException) throwable;
            }

            return BaseException.toUnexpectedError(throwable);
        }
    }
}