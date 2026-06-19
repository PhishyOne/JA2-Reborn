#pragma once

#include "SGPFile.h"
#include "VSurface.h"

#define TUTORIAL_MAX_PANELS 3

enum class TutorialMode {
	Tactical,
	MainMenu,
	TouchPresetUpdate
};

struct TUTORIAL_STATE {
	bool fVisible;
	int  sCurrentPanel;      // 0, 1, 2
	bool fDontShowAgain;     // persisted state
	bool fMainMenuDontShowAgain;
	bool fCheckboxChecked;   // current toggle in UI
	bool fAutoShownThisSession;
	bool fMainMenuAutoShownThisSession;
	int  iTouchPresetUpdateSeenVersion;
	int  iPendingTouchPresetUpdateVersion;
	TutorialMode mode;
	SGPVSurface* pSaveBuffer; // background save for overlay
};

extern TUTORIAL_STATE gTutorial;

struct TUTORIAL_PANEL {
	const char* title;
	const char* body;
};

extern TUTORIAL_PANEL gTutorialPanels[3];

void EnterTutorial();
void EnterMainMenuTutorial();
void RequestTouchPresetUpdateNotice(int version);
bool ShouldShowTouchPresetUpdateNotice();
void EnterTouchPresetUpdateNotice();
void ExitTutorial();
void RenderTutorial();
void LoadTutorialSettings();
void SaveTutorialSettings();
bool ShouldShowMainMenuTutorial();
void TutorialNextPanel();
void TutorialPrevPanel();
void SetTutorialLanguageGerman(bool german);
