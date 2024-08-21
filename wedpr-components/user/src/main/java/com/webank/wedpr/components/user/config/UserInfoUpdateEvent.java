package com.webank.wedpr.components.user.config;

import org.springframework.context.ApplicationEvent;

/** Created by caryliao on 2024/8/12 21:50 */
public class UserInfoUpdateEvent<T> extends ApplicationEvent {
    private T data;

    public UserInfoUpdateEvent(T source) {
        super(source);
        this.data = source;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
