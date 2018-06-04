package basenet.better.basenet.bizDemo.handler.utils;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 返回集合
 *
 * @param <T>
 */
public class CommonList<T> implements Serializable {

    public int status;
    public String message;
    public List<T> content;

    public static CommonList fromJson(String json, Class clazz) {
        Type objectType = type(CommonList.class, clazz);
        return new Gson().fromJson(json, objectType);
    }

    public String toJson(Class<T> clazz) {
        Type objectType = type(CommonList.class, clazz);
        return new Gson().toJson(this, objectType);
    }

    static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }

}  