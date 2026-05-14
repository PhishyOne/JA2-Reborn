#pragma once

struct SDL_Rect;

namespace TacticalScaling {
	constexpr int kDefaultFovPercent         = 100;
	constexpr int kMinFovPercent             = 80;
	constexpr int kMaxFovPercent             = 130;
	constexpr int kMinResolution             = 640;
	constexpr int kMinHeight                 = 480;

	constexpr int kDefaultPanelScalePercent  = 100;
	constexpr int kMinPanelScalePercent      = 100;
	constexpr int kMaxPanelScalePercent      = 130;

	void ApplyPendingTacticalScaling();
	void SetPendingMapFovPercent(int percent);
	int GetMapFovPercent();

	void SetPendingActionPanelScalePercent(int percent);
	int GetActionPanelScalePercent();

	// Returns the source rect of the current tactical panel in FrameBuffer
	// coordinates. Returns false if no panel is active.
	bool GetPanelSourceRect(SDL_Rect& out);

	// Returns the destination rect of the current tactical panel in
	// ScreenBuffer coordinates after applying panel scale. The rect may extend
	// beyond the screen horizontally; SDL clips the visible portion.
	bool GetPanelDestinationRect(SDL_Rect& out);

	// Maps a screen-space point (in ScreenBuffer coordinates, i.e. current
	// tactical resolution) back to logical FrameBuffer coordinates, inverting
	// the panel presentation scale. Only transforms points that fall within
	// the scaled panel destination rect.
	void ScreenToTacticalLogicalPoint(float& x, float& y);
}
