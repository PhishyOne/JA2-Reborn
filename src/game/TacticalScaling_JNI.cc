#ifdef __ANDROID__

#include <jni.h>
#include <atomic>
#include "TacticalScaling.h"

static std::atomic<int> g_tacticalMapFovPercent{100};
static std::atomic<int> g_tacticalActionPanelScalePercent{100};

static int clampMapFov(int v) {
	if (v < 80) return 80;
	if (v > 130) return 130;
	return v;
}

static int clampPanelScale(int v) {
	if (v < TacticalScaling::kMinPanelScalePercent) return TacticalScaling::kMinPanelScalePercent;
	if (v > TacticalScaling::kMaxPanelScalePercent) return TacticalScaling::kMaxPanelScalePercent;
	return v;
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setTacticalMapFovPercent(JNIEnv* env, jclass cls, jint percent)
{
	int clamped = clampMapFov(static_cast<int>(percent));
	g_tacticalMapFovPercent.store(clamped, std::memory_order_relaxed);
	TacticalScaling::SetPendingMapFovPercent(clamped);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getTacticalMapFovPercent(JNIEnv* env, jclass cls)
{
	return static_cast<jint>(g_tacticalMapFovPercent.load(std::memory_order_relaxed));
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setTacticalActionPanelScalePercent(JNIEnv* env, jclass cls, jint percent)
{
	int clamped = clampPanelScale(static_cast<int>(percent));
	g_tacticalActionPanelScalePercent.store(clamped, std::memory_order_relaxed);
	TacticalScaling::SetPendingActionPanelScalePercent(clamped);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getTacticalActionPanelScalePercent(JNIEnv* env, jclass cls)
{
	return static_cast<jint>(g_tacticalActionPanelScalePercent.load(std::memory_order_relaxed));
}

#endif
