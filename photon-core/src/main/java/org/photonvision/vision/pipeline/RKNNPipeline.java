/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.vision.pipeline;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.photonvision.common.configuration.PathManager;
import org.photonvision.common.util.ColorHelper;
import org.photonvision.vision.frame.Frame;
import org.photonvision.vision.frame.FrameThresholdType;
import org.photonvision.vision.pipe.impl.*;
import org.photonvision.vision.pipeline.result.CVPipelineResult;
import org.photonvision.vision.rknn.RKNNJNI;
import org.photonvision.vision.target.TrackedTarget;
import org.photonvision.vision.target.TrackedTarget.TargetCalculationParameters;

public class RKNNPipeline extends CVPipeline<CVPipelineResult, RKNNPipelineSettings> {
    private final CalculateFPSPipe calculateFPSPipe = new CalculateFPSPipe();
    private Mat processed;
    List<TrackedTarget> targetList = new ArrayList<TrackedTarget>();
    List<Long> times = new ArrayList<>();
    private static final FrameThresholdType PROCESSING_TYPE = FrameThresholdType.NONE;
    private double lastTime = 0;

    public RKNNPipeline() {
        super(PROCESSING_TYPE);
        settings = new RKNNPipelineSettings();
    }

    private RKNNJNI rknnjni;

    public RKNNPipeline(RKNNPipelineSettings settings) {
        super(PROCESSING_TYPE);
        this.settings = settings;

        this.rknnjni = new RKNNJNI();
        this.rknnjni.init(PathManager.getInstance().getRootFolder() + "/model.rknn");
    }

    @Override
    protected void setPipeParamsImpl() {}

    @Override
    protected CVPipelineResult process(Frame input_frame, RKNNPipelineSettings settings) {
        long sumPipeNanosElapsed = System.nanoTime();
        if (input_frame.colorImage.getMat().empty()) {
            System.out.println("frame is empty");
            return new CVPipelineResult(0, 0, List.of(), input_frame);
        }
        times.add(System.nanoTime());
        targetList.clear();
        times.clear();
        times.add(System.nanoTime());

        input_frame.processedImage.copyTo(input_frame.colorImage);
        times.add(System.nanoTime());
        processed = input_frame.processedImage.getMat();
        times.add(System.nanoTime());

        times.add(System.nanoTime());
        var results = rknnjni.detectAndDisplay(processed.getNativeObjAddr());
        times.add(System.nanoTime());
        for (int i = 0; results != null && i < results.count; i++) {
            var detection = results.results[i];
            var box = detection.box;

            Imgproc.rectangle(
                    processed,
                    new Point(box.left, box.top),
                    new Point(box.right, box.bottom),
                    new Scalar(0, 0, 255),
                    2);

            var name = String.format("%s (%f)", Short.toString(detection.cls), detection.conf);

            Imgproc.putText(
                    processed,
                    name,
                    new Point(box.left, box.top + 12),
                    0,
                    0.6,
                    ColorHelper.colorToScalar(java.awt.Color.white),
                    2);
            var target =
                    new TrackedTarget(
                            new Rect2d(box.left, box.top, box.right - box.left, box.bottom - box.top),
                            0,
                            detection.conf,
                            new TargetCalculationParameters(
                                    false, null, null, null, null, this.frameStaticProperties));

            targetList.add(target);
        }
        times.add(System.nanoTime());

        Size size = null;
        switch (settings.streamingFrameDivisor) {
            case NONE:
                break;
            case HALF:
                size = new Size(processed.width() / 2, processed.height() / 2);
                break;
            case QUARTER:
                size = new Size(processed.width() / 4, processed.height() / 4);
                break;
            case SIXTH:
                size = new Size(processed.width() / 6, processed.height() / 6);
                break;
            default:
                break;
        }
        if (size != null) {
            Imgproc.resize(processed, processed, size);
            Imgproc.resize(input_frame.colorImage.getMat(), input_frame.colorImage.getMat(), size);
        }

        var fpsResult = calculateFPSPipe.run(null);
        var fps = fpsResult.output;
        return new CVPipelineResult(
                System.nanoTime() - sumPipeNanosElapsed, fps, targetList, input_frame);
    }
}
