#include "TacticalScaling.h"
#include "JAScreens.h"
#include "ScreenIDs.h"
#include "UILayout.h"
#include "VObject_Blitters.h"
#include "VSurface.h"
#include "RenderWorld.h"
#include "Interface.h"
#include "Video.h"
#include <atomic>

namespace TacticalScaling {
	static bool                s_baseInitialized    = false;
	static int                 s_baseScreenWidth    = 640;
	static int                 s_baseScreenHeight   = 480;
	static std::atomic<int>    s_pendingFovPercent  = kDefaultFovPercent;
	static std::atomic<int>    s_currentFovPercent  = kDefaultFovPercent;

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

		int pending = s_pendingFovPercent.load(std::memory_order_relaxed);
		if (pending == s_currentFovPercent.load(std::memory_order_relaxed)) return;

		s_currentFovPercent.store(pending, std::memory_order_relaxed);

		int effectiveW = s_baseScreenWidth  * pending / 100;
		int effectiveH = s_baseScreenHeight * pending / 100;
		if (effectiveW < kMinResolution) effectiveW = kMinResolution;
		if (effectiveH < kMinHeight)   effectiveH = kMinHeight;

		g_ui.setScreenSize(static_cast<UINT16>(effectiveW), static_cast<UINT16>(effectiveH));
		g_ui.recalculatePositions();

		// Z-Buffer reinit (must happen before VideoReinitSurfaces — the Z buffer
		// relies on FRAME_BUFFER which will be recreated by VideoReinitSurfaces)
		ShutdownZBuffer(gpZBuffer);
		VideoReinitSurfaces();

		// Re-init Z buffer after new framebuffer exists
		SDL_Surface const& fbSurface = FRAME_BUFFER->GetSDLSurface();
		gZBufferPitch = fbSurface.pitch / fbSurface.format->BytesPerPixel;
		gpZBuffer = InitZBuffer(gZBufferPitch, static_cast<UINT32>(effectiveH));
		gZBufferPitch *= sizeof(*gpZBuffer);

		SetRenderFlags(RENDER_FLAG_FULL);
		InvalidateScreen();
		fInterfacePanelDirty = DIRTYLEVEL2;
	}

	void SetPendingMapFovPercent(int percent)
	{
		if (percent < kMinFovPercent) percent = kMinFovPercent;
		if (percent > kMaxFovPercent) percent = kMaxFovPercent;
		s_pendingFovPercent.store(percent, std::memory_order_relaxed);
	}

	int GetMapFovPercent()
	{
		return s_currentFovPercent.load(std::memory_order_relaxed);
	}
}
