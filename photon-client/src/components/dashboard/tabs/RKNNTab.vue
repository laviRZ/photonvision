<script setup lang="ts">
import { PipelineType } from "@/types/PipelineTypes";
import PvSlider from "@/components/common/pv-slider.vue";
import { computed, getCurrentInstance } from "vue";
import { useStateStore } from "@/stores/StateStore";
import type { ActivePipelineSettings } from "@/types/PipelineTypes";
import { useCameraSettingsStore } from "@/stores/settings/CameraSettingsStore";

// TODO fix pipeline typing in order to fix this, the store settings call should be able to infer that only valid pipeline type settings are exposed based on pre-checks for the entire config section
// Defer reference to store access method
const currentPipelineSettings = computed<ActivePipelineSettings>(
  () => useCameraSettingsStore().currentPipelineSettings
);

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
      tooltip="Any detections with a confidence below this threshold will be discarded"
      :min="0"
      :max="1"
      :step="0.01"
      @input="(value) => useCameraSettingsStore().changeCurrentPipelineSetting({ confidenceThreshold: value }, false)"
    />
  </div>
</template>
