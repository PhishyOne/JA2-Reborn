#include "TacticalScaling.h"
#include "JAScreens.h"
#include "ScreenIDs.h"
#include "UILayout.h"
#include "VObject_Blitters.h"
#include "VSurface.h"
#include "RenderWorld.h"
#include "Interface.h"
#include "Interface_Panels.h"
#include "Video.h"
#include <atomic>
#include <SDL.h>

namespace TacticalScaling {
	static bool                s_baseInitialized         = false;
	static int                 s_baseScreenWidth         = 640;
	static int                 s_baseScreenHeight        = 480;
	static std::atomic<int>    s_pendingFovPercent       = kDefaultFovPercent;
	static std::atomic<int>    s_currentFovPercent       = kDefaultFovPercent;
	static std::atomic<int>    s_pendingPanelScalePercent = kDefaultPanelScalePercent;
	static std::atomic<int>    s_currentPanelScalePercent = kDefaultPanelScalePercent;

	static void CaptureBaseResolution()
	{
		if (!s_baseInitialized && g_ui.m_screenWidth > 0 && g_ui.m_screenHeight > 0)
		{
			s_baseScreenWidth  = g_ui.m_screenWidth;
			s_baseScreenHeight = g_ui.m_screenHeight;
			s_baseInitialized  = true;
		}
	}

	void ApplyPendingTacticalScaling()
	{
		if (guiCurrentScreen != GAME_SCREEN) return;

		CaptureBaseResolution();

		// Runtime tactical resolution changes invalidate too much active UI state
		// (buttons, mouse regions, panel geometry). Keep the FOV value persisted
		// through JNI, but do not resize the native surfaces while in-game.
		int pendingFov = s_pendingFovPercent.load(std::memory_order_relaxed);
		bool fovChanged = false;

		int pendingPanel = s_pendingPanelScalePercent.load(std::memory_order_relaxed);
		bool panelChanged = (pendingPanel != s_currentPanelScalePercent.load(std::memory_order_relaxed));

		if (!fovChanged && !panelChanged) return;

		if (fovChanged)
		{
			s_currentFovPercent.store(pendingFov, std::memory_order_relaxed);
			if (panelChanged)
			{
				s_currentPanelScalePercent.store(pendingPanel, std::memory_order_relaxed);
			}

			int effectiveW = s_baseScreenWidth  * pendingFov / 100;
			int effectiveH = s_baseScreenHeight * pendingFov / 100;
			if (effectiveW < kMinResolution) effectiveW = kMinResolution;
			if (effectiveH < kMinHeight)   effectiveH = kMinHeight;

			g_ui.setScreenSize(static_cast<UINT16>(effectiveW), static_cast<UINT16>(effectiveH));
			g_ui.recalculatePositions();

			ShutdownZBuffer(gpZBuffer);
			VideoReinitSurfaces();

			SDL_Surface const& fbSurface = FRAME_BUFFER->GetSDLSurface();
			gZBufferPitch = fbSurface.pitch / fbSurface.format->BytesPerPixel;
			gpZBuffer = InitZBuffer(gZBufferPitch, static_cast<UINT32>(effectiveH));
			gZBufferPitch *= sizeof(*gpZBuffer);
		}
		else
		{
			s_currentPanelScalePercent.store(pendingPanel, std::memory_order_relaxed);
		}

		SetRenderFlags(RENDER_FLAG_FULL);
		InvalidateScreen();
		fInterfacePanelDirty = DIRTYLEVEL2;
	}

	void SetPendingMapFovPercent(int percent)
	{
		if (percent < kMinFovPercent) percent = kMinFovPercent;
		if (percent > kMaxFovPercent) percent = kMaxFovPercent;
		s_pendingFovPercent.store(percent, std::memory_order_relaxed);
		s_currentFovPercent.store(percent, std::memory_order_relaxed);
	}

	int GetMapFovPercent()
	{
		return s_currentFovPercent.load(std::memory_order_relaxed);
	}

	void SetPendingActionPanelScalePercent(int percent)
	{
		if (percent < kMinPanelScalePercent) percent = kMinPanelScalePercent;
		if (percent > kMaxPanelScalePercent) percent = kMaxPanelScalePercent;
		s_pendingPanelScalePercent.store(percent, std::memory_order_relaxed);
	}

	int GetActionPanelScalePercent()
	{
		return s_currentPanelScalePercent.load(std::memory_order_relaxed);
	}

	bool GetPanelSourceRect(SDL_Rect& out)
	{
		out = {};
		switch (gsCurInterfacePanel)
		{
			case TEAM_PANEL:
				out.x = static_cast<int>(INTERFACE_START_X);
				out.y = static_cast<int>(INTERFACE_START_Y);
				out.w = static_cast<int>(g_ui.m_teamPanelWidth);
				out.h = TEAMPANEL_HEIGHT;
				return true;
			case SM_PANEL:
				out.x = 0;
				out.y = static_cast<int>(INV_INTERFACE_START_Y);
				out.w = static_cast<int>(SCREEN_WIDTH);
				out.h = INV_INTERFACE_HEIGHT;
				return true;
			default:
				return false;
		}
	}

	bool GetPanelDestinationRect(SDL_Rect& out)
	{
		out = {};

		SDL_Rect src;
		if (!GetPanelSourceRect(src)) return false;

		int scale = GetActionPanelScalePercent();
		int destW = src.w * scale / 100;
		int destH = src.h * scale / 100;

		int destX = (static_cast<int>(SCREEN_WIDTH) - destW) / 2;
		int destY = static_cast<int>(SCREEN_HEIGHT) - destH;

		if (destY < 0) destY = 0;

		out = { destX, destY, destW, destH };
		return destW > 0 && destH > 0;
	}

	void ScreenToTacticalLogicalPoint(float& x, float& y)
	{
		int scale = GetActionPanelScalePercent();
		if (scale == kDefaultPanelScalePercent) return;

		SDL_Rect src;
		if (!GetPanelSourceRect(src)) return;

		SDL_Rect dest;
		if (!GetPanelDestinationRect(dest)) return;

		float fx = x;
		float fy = y;

		if (fx >= static_cast<float>(dest.x) && fx < static_cast<float>(dest.x + dest.w) &&
		    fy >= static_cast<float>(dest.y) && fy < static_cast<float>(dest.y + dest.h))
		{
			x = static_cast<float>(src.x) + (fx - static_cast<float>(dest.x)) * static_cast<float>(src.w) / static_cast<float>(dest.w);
			y = static_cast<float>(src.y) + (fy - static_cast<float>(dest.y)) * static_cast<float>(src.h) / static_cast<float>(dest.h);
		}
	}
}
