/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.facesdk.callback;


import com.example.facesdk.model.LivenessModel;

public interface ILivenessCallBack {
    public void onTip(int code, String msg);

    public void onCanvasRectCallback(LivenessModel livenessModel);

    public void onCallback(int code, LivenessModel livenessModel);
}
