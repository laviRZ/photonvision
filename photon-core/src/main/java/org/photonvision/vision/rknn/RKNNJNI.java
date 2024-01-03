package org.photonvision.vision.rknn;

import org.photonvision.jni.PhotonJniCommon;

public class RKNNJNI extends PhotonJniCommon {
  public static class BoxRect {
		public int left;
		public int right;
		public int top;
		public int bottom;

		public BoxRect(int left, int right, int top, int bottom) {
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}

		public String toString() {
			return "{" + left + ", " + right + ", " + top + ", " + bottom + "}";
		}
	}
	public static class DetectionResult {
		public String name;
		public BoxRect box;
		public float prop;

		public DetectionResult(String name, BoxRect box, float prop) {
			this.name = name;
			this.box = box;
			this.prop = prop;
		}

		public String toString() {
			return "DetectionResult: " + name + ", " + box + ", " + prop;
		}
	}
	public static class DetectionResultGroup {
		public int id;
		public int count;
		public DetectionResult[] results;

		public DetectionResultGroup(int id, int count, DetectionResult[] results) {
			this.id = id;
			this.count = count;
			this.results = results;
		}

		public String toString() {
			if(results == null || results.length == 0) {
				return "DetectionResultGroup: " + id + ", " + count + ", []";
			}
			
			String s = "DetectionResultGroup: " + id + ", " + count + ", [\n";
			for (int i = 0; i < count; i++)
				s += "\t" + results[i] + (i == count - 1 ? "\n" : ",\n");
			s += "]";
			return s;
		}
	}

	private static native DetectionResultGroup detectAndDisplay(long aiAddr, long frameAddr);
	private static native long initAi(String modelPath);

  private long aiAddr;
  public RKNNJNI() {
		forceLoad(RKNNJNI.class, "jnish");
  }

  public void init() {
    aiAddr = initAi("/home/orangepi/garbest.rknn");
  }

  public DetectionResultGroup detectAndDisplay(long frameAddr) {
		// var timeBefore = System.nanoTime();
    var res = detectAndDisplay(aiAddr, frameAddr);
		// var timeAfter = System.nanoTime();
		// var timeElapsed = timeAfter - timeBefore;
		// System.out.println("JNI: " + String.format("%.2f", timeElapsed / 1000000.0) + "ms");
		return res;
  }
}