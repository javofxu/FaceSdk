package com.example.facesdk.listener;

/**
 * Created by 许格 on 2018/12/5.
 */

public interface OnImportListener {
    void startImport();

    void onImporting(float progress);

    void endImport(int totalCount, int successCount, int failureCount);

    void showToastMessage(String message);
}
