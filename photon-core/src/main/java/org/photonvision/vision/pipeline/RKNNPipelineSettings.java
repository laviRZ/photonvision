package org.photonvision.vision.pipeline;

public class RKNNPipelineSettings extends CVPipelineSettings {
    public double confidenceThreshold = 0.15;
    public String selectedModel = "yolov5n-i8-notes-640-single";

    public RKNNPipelineSettings() {
        super();
        this.pipelineType = PipelineType.RKNN;
    }
}
