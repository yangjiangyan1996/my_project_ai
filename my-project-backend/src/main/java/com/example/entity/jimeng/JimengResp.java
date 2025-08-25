package com.example.entity.jimeng;

import lombok.Data;

import java.util.List;

/**
 * AI绘画生成响应实体
 * 对应AI绘画算法的生成结果响应
 */
@Data
public class JimengResp {
    private int code;
    private String message;
    private String requestId;
    private int status;
    private String timeElapsed;
    private RespData data; // 重命名为 RespData

    @Data
    public static class AlgorithmBaseResp {
        private int statusCode;
        private String statusMessage;
    }

    @Data
    public static class RespData { // 重命名避免冲突
        private AlgorithmBaseResp algorithmBaseResp;
        private List<String> binaryDataBase64;
        private List<String> imageUrls;
        private InferCtx inferCtx;
        private String llmResult;
        private String peResult;
        private String predictTagsResult;
        private String rephraserResult;
        private String requestId;
        private String vlmResult;

    }

    @Data
    public static class InferCtx {
        private String algorithmKey;
        private String appKey;
        private String createdAt;
        private String generateId;
        private String logId;
        private Params params;
        private String requestId;
        private String sessionId;
        private String timeStamp;

    }

    @Data
    public static class Params {
        private String appId;
        private String aspectRatio;
        private String commonParams;
        private int ddimSteps;
        private String editSessionId;
        private int fps;
        private int frames;
        private String groupName;
        private int height;
        private String inputImageUrl;
        private boolean isOnlySr;
        private boolean isPe;
        private String llmResult;
        private String mediaSource;
        private int nSamples;
        private String negativePrompt;
        private String oriPrompt;
        private String originRequestId;
        private int outputHeight;
        private int outputWidth;
        private String peResult;
        private String predictTagsResult;
        private String rephraserResult;
        private String reqKey;
        private int rescale;
        private String resolution;
        private double scale;
        private int seed;
        private int shift;
        private int srImg2imgFixSteps;
        private double srScale;
        private double srStrength;
        private String srUpscaler;
        private int steps;
        private double strength;
        private String traceId;
        private String translateNegativePrompt;
        private String translatePrompt;
        private boolean usePreLlm;
        private boolean usePromptAug;
        private boolean useSr;
        private String versionId;
        private String videoUrl;
        private String vlmEdit;
        private String vlmInput;
        private String vlmOutput;
        private int width;
    }
}