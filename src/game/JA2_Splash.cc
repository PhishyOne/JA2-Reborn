#include "JA2_Splash.h"
#include "VSurface.h"
#include "Timer_Control.h"
#include "MainMenuScreen.h"
#include "UILayout.h"
#include "Video.h"
#include "GameRes.h"

UINT32 guiSplashFrameFade = 10;
UINT32 guiSplashStartTime = 0;


//Simply create videosurface, load image, and draw it to the screen.
void InitJA2SplashScreen(void)
{
	InitializeJA2Clock();

	if(isEnglishVersion() || isChineseVersion())
	{
		ClearMainMenu();
	}
	else
	{
		const char* const ImageFile = GetMLGFilename(MLG_SPLASH);
		BltVideoSurfaceOnce(FRAME_BUFFER, ImageFile, STD_SCREEN_X, STD_SCREEN_Y);
	}

	InvalidateScreen();
	RefreshScreen();

	guiSplashStartTime = GetJA2Clock();
}
