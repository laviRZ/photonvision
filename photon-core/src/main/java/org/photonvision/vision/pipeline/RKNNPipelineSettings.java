package org.photonvision.vision.pipeline;

public class RKNNPipelineSettings extends CVPipelineSettings {
    public double confidenceThreshold = 0.15;

    public RKNNPipelineSettings() {
        super();
        this.pipelineType = PipelineType.RKNN;
    }
}
