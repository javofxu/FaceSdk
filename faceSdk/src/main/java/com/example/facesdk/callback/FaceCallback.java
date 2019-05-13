package com.example.facesdk.callback;

/**
 * Created by 许格 on 2019/5/8.
 */

public interface FaceCallback {
    /**
     *  回调函数 code 0 : 成功；code 1 加载失败
     * @param code
     * @param response
     */
    void onResponse(int code, String response);
}
