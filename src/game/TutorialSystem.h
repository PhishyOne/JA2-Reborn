#pragma once

#include "SGPFile.h"
#include "VSurface.h"

#define TUTORIAL_MAX_PANELS 3

struct TUTORIAL_STATE {
	bool fVisible;
	int  sCurrentPanel;      // 0, 1, 2
	bool fDontShowAgain;     // persisted state
	bool fCheckboxChecked;   // current toggle in UI
	bool fAutoShownThisSession;
	SGPVSurface* pSaveBuffer; // background save for overlay
};

extern TUTORIAL_STATE gTutorial;

struct TUTORIAL_PANEL {
	const char* title;
	const char* body;
};

extern TUTORIAL_PANEL gTutorialPanels[3];

void EnterTutorial();
void ExitTutorial();
void RenderTutorial();
void LoadTutorialSettings();
void SaveTutorialSettings();
void TutorialNextPanel();
void TutorialPrevPanel();
void SetTutorialLanguageGerman(bool german);
