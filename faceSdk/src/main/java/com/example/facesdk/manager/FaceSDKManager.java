package com.example.facesdk.manager;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.Feature;
import com.example.facesdk.FaceEnvironment;
import com.example.facesdk.GlobalSet;
import com.example.facesdk.api.FaceApi;
import com.example.facesdk.api.LRUCache;
import com.example.facesdk.callback.FaceCallback;
import com.example.facesdk.db.DBManager;
import com.example.facesdk.model.LivenessModel;

import java.util.List;
import java.util.Map;

public class FaceSDKManager {
    private FaceDetector faceDetector;
    private FaceFeatures faceFeature;
    private FaceLiveness faceLiveness;


    private FaceEnvironment faceEnvironment;

    private LRUCache<String, Feature> featureLRUCache = new LRUCache<>(1000);

    private FaceSDKManager() {
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeatures();
        faceLiveness = new FaceLiveness();
        faceEnvironment = new FaceEnvironment();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeatures getFaceFeature() {
        return faceFeature;
    }

    public FaceLiveness getFaceLiveness() {
        return faceLiveness;
    }


    public void initModel(final Context context) {
        faceDetector.initModel(context, "detect_rgb_anakin_2.0.0.bin",
                "",
                "align_2.0.0.anakin.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //ToastUtils.toast(context, response);
                    }
                });
        faceDetector.loadConfig(getFaceEnvironmentConfig());
        faceFeature.initModel(context, "",
                "recognize_rgb_live_anakin_2.0.0.bin",
                "", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //ToastUtils.toast(context, response);
                    }
                });
        faceLiveness.initModel(context, "liveness_rgb_anakin_2.0.0.bin",
                "liveness_nir_anakin_2.0.0.bin",
                "liveness_depth_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //ToastUtils.toast(context, response);
                    }
                });
    }
    public FaceEnvironment getFaceEnvironmentConfig() {
        faceEnvironment.setMinFaceSize(50);
        faceEnvironment.setMaxFaceSize(-1);
        faceEnvironment.setDetectInterval(200);
        faceEnvironment.setTrackInterval(500);
        faceEnvironment.setNoFaceSize(0.5f);
        faceEnvironment.setPitch(30);
        faceEnvironment.setYaw(30);
        faceEnvironment.setRoll(30);
        faceEnvironment.setCheckBlur(true);
        faceEnvironment.setOcclusion(true);
        faceEnvironment.setIllumination(true);
        faceEnvironment.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);
        return faceEnvironment;
    }

    public int setFeature() {
        List<Feature> listFeatures = FaceApi.getInstance().featureQuery();
        if (listFeatures != null && faceFeature != null) {
            faceFeature.setFeature(listFeatures);
            return listFeatures.size();
        }
        return 0;
    }

    public LRUCache<String, Feature> getFeatureLRUCache() {
        return featureLRUCache;
    }

    public Feature getFeature(FaceFeature.FeatureType featureType, byte[] curFeature, LivenessModel liveModel) {
        Log.i("AAA", "getFeature: AAA");
        Log.i("BBB", "getFeature: "+featureLRUCache.getAll().size());
        if (featureLRUCache.getAll().size() > 0) {
            Log.i("AAA", "getFeature: AAA");
            for (Map.Entry<String, Feature> featureEntry : featureLRUCache.getAll()) {
                Feature feature = featureEntry.getValue();
                float similariry;
                if (featureType == FaceFeature.FeatureType.FEATURE_VIS) {
                    Log.i("AAA", "getFeature: BBB");
                    similariry = faceFeature.featureCompare(feature.getFeature(), curFeature);
                    if (similariry > GlobalSet.getFeatureRgbValue()) {
                        Log.i("AAA", "getFeature:CCC");
                        liveModel.setFeatureScore(similariry);
                        featureLRUCache.put(feature.getUserName(), feature);
                        return feature;
                    }
                } else if (featureType == FaceFeature.FeatureType.FEATURE_ID_PHOTO) {
                    Log.i("AAA", "getFeature: DDD");
                    similariry = faceFeature.featureIDCompare(feature.getFeature(), curFeature);
                    if (similariry > GlobalSet.getFeaturePhoneValue()) {
                        Log.i("AAA", "getFeature: EEE");
                        liveModel.setFeatureScore(similariry);
                        featureLRUCache.put(feature.getUserName(), feature);
                        return feature;
                    }
                }
            }
        }

        Feature featureCpp = faceFeature.featureCompareCpp(curFeature, featureType,
                featureType == FaceFeature.FeatureType.FEATURE_VIS ? GlobalSet.getFeatureRgbValue()
                        : GlobalSet.getFeaturePhoneValue());

        if (featureCpp != null) {
            liveModel.setFeatureScore(featureCpp.getScore());
            List<Feature> features = DBManager.getInstance().queryFeatureById(featureCpp.getId());
            if (features != null && features.size() > 0) {
                Feature feature = features.get(0);
                featureLRUCache.put(feature.getUserName(), feature);
                return feature;
            }
        }
        return null;
    }
}