#pragma once

namespace TacticalScaling {
	constexpr int kDefaultFovPercent = 100;
	constexpr int kMinFovPercent     = 80;
	constexpr int kMaxFovPercent     = 130;
	constexpr int kMinResolution     = 640;
	constexpr int kMinHeight         = 480;

	void ApplyPendingTacticalScaling();
	void SetPendingMapFovPercent(int percent);
	int GetMapFovPercent();
}
