package vn.tripi.restclient.errors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorResponse<T> {

    @Expose
    @SerializedName("code")
    private Integer code = null;

    @Expose
    @SerializedName("message")
    private String message = null;

    @Expose
    @SerializedName("data")
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
