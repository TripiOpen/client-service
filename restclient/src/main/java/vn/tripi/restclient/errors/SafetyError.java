package vn.tripi.restclient.errors;

import io.reactivex.functions.Consumer;

public abstract class SafetyError implements Consumer<Throwable> {
    /**
     * Don't override this method.
     * Override {@link SafetyError#onSafetyError(BaseException)} instead
     */
    @Override
    public void accept(Throwable throwable) {
        if (throwable == null) {
            onSafetyError(BaseException.toUnexpectedError(new Throwable("Unknown exception")));
            return;
        }
        if (throwable instanceof BaseException) {
            onSafetyError((BaseException) throwable);
        } else {
            onSafetyError(BaseException.toUnexpectedError(throwable));
        }
    }

    public abstract void onSafetyError(BaseException error);
}
