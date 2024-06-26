<script setup lang="ts">
import { PipelineType } from "@/types/PipelineTypes";
import PvSlider from "@/components/common/pv-slider.vue";
import pvSelect from "@/components/common/pv-select.vue";
import { computed, getCurrentInstance } from "vue";
import { useStateStore } from "@/stores/StateStore";
import type { ActivePipelineSettings } from "@/types/PipelineTypes";
import { useCameraSettingsStore } from "@/stores/settings/CameraSettingsStore";

// TODO fix pipeline typing in order to fix this, the store settings call should be able to infer that only valid pipeline type settings are exposed based on pre-checks for the entire config section
// Defer reference to store access method
const currentPipelineSettings = computed<ActivePipelineSettings>(
  () => useCameraSettingsStore().currentPipelineSettings
);

const currentSelectedModelIndex = computed(() => {
  const cps = currentPipelineSettings.value;
  return cps.pipelineType === PipelineType.RKNN
    ? useCameraSettingsStore().availableModels.indexOf(cps.selectedModel)
    : 0;
});

const getModelName = (index: number) => {
  const cps = currentPipelineSettings.value;
  return cps.pipelineType === PipelineType.RKNN ? useCameraSettingsStore().availableModels[index] : "";
};

const contourArea = computed<[number, number]>({
  get: () => Object.values(useCameraSettingsStore().currentPipelineSettings.contourArea) as [number, number],
  set: (v) => (useCameraSettingsStore().currentPipelineSettings.contourArea = v)
});
const contourRatio = computed<[number, number]>({
  get: () => Object.values(useCameraSettingsStore().currentPipelineSettings.contourRatio) as [number, number],
  set: (v) => (useCameraSettingsStore().currentPipelineSettings.contourRatio = v)
});

console.log("available: " + useCameraSettingsStore().availableModels);
const interactiveCols = computed(
  () =>
    (getCurrentInstance()?.proxy.$vuetify.breakpoint.mdAndDown || false) &&
    (!useStateStore().sidebarFolded || useCameraSettingsStore().isDriverMode)
)
  ? 9
  : 8;
</script>

<template>
  <div v-if="currentPipelineSettings.pipelineType === PipelineType.RKNN">
    <pv-slider
      v-model="currentPipelineSettings.confidenceThreshold"
      class="pt-2"
      :slider-cols="interactiveCols"
      label="Confidence Threshold"
      tooltip="Any detections with a confidence below this threshold will be discarded. Minimum value is 0.1."
      :min="0.1"
      :max="1"
      :step="0.01"
      @input="(value) => useCameraSettingsStore().changeCurrentPipelineSetting({ confidenceThreshold: value }, false)"
    />
    <pv-select
      :select-cols="interactiveCols"
      :value="currentSelectedModelIndex"
      label="RKNN Model"
      tooltip="The RKNN model to use for inference."
      :items="useCameraSettingsStore().availableModels"
      @input="
        (value) => useCameraSettingsStore().changeCurrentPipelineSetting({ selectedModel: getModelName(value) }, false)
      "
    />
    <pv-range-slider
      v-model="contourArea"
      label="Area"
      :min="0"
      :max="100"
      :slider-cols="interactiveCols"
      :step="0.01"
      @input="(value) => useCameraSettingsStore().changeCurrentPipelineSetting({ contourArea: value }, false)"
    />
    <pv-range-slider
      v-model="contourRatio"
      label="Ratio (W/H)"
      tooltip="Min and max ratio between the width and height of a contour's bounding rectangle"
      :min="0"
      :max="100"
      :slider-cols="interactiveCols"
      :step="0.01"
      @input="(value) => useCameraSettingsStore().changeCurrentPipelineSetting({ contourRatio: value }, false)"
    />
    <pv-select
      v-model="useCameraSettingsStore().currentPipelineSettings.contourTargetOrientation"
      label="Target Orientation"
      tooltip="Used to determine how to calculate target landmarks, as well as aspect ratio"
      :items="['Portrait', 'Landscape']"
      :select-cols="interactiveCols"
      @input="
        (value) => useCameraSettingsStore().changeCurrentPipelineSetting({ contourTargetOrientation: value }, false)
      "
    />
    <pv-select
      v-model="currentPipelineSettings.contourSortMode"
      label="Target Sort"
      tooltip="Chooses the sorting mode used to determine the 'best' targets to provide to user code"
      :select-cols="interactiveCols"
      :items="['Largest', 'Smallest', 'Highest', 'Lowest', 'Rightmost', 'Leftmost', 'Centermost']"
      @input="(value) => useCameraSettingsStore().changeCurrentPipelineSetting({ contourSortMode: value }, false)"
    />
    <!-- TODO: confident -->
    <pv-switch
      v-model="useCameraSettingsStore().currentPipelineSettings.outputShowMultipleTargets"
      label="Show Multiple Targets"
      tooltip="If enabled, up to five targets will be displayed and sent via PhotonLib, instead of just one"
      :switch-cols="interactiveCols"
      @input="
        (value) => useCameraSettingsStore().changeCurrentPipelineSetting({ outputShowMultipleTargets: value }, false)
      "
    />
  </div>
</template>
