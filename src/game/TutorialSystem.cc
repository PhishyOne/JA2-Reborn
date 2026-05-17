#include "TutorialSystem.h"
#include "Cursors.h"
#include "FileMan.h"
#include "Font.h"
#include "Font_Control.h"
#include "GameRes.h"
#include "HImage.h"
#include "JAScreens.h"
#include "Local.h"
#include "Logger.h"
#include "MouseSystem.h"
#include "Render_Dirty.h"
#include "RustInterface.h"
#include "UILayout.h"
#include "VObject.h"
#include "VSurface.h"
#include "Video.h"
#include "WordWrap.h"

#include <cstdlib>
#include <string>
#include <vector>

#define TUTORIAL_CARD_WIDTH_PCT    70
#define TUTORIAL_CARD_PADDING      16
#define TUTORIAL_CARD_PADDING_TOP  14
#define TUTORIAL_LINE_GAP          3
#define TUTORIAL_PARAGRAPH_GAP     9
#define TUTORIAL_SWIPE_THRESHOLD   42
#define TUTORIAL_DOT_SIZE          10
#define TUTORIAL_DOT_SPACING       16
#define TUTORIAL_CONFIRM_W         124
#define TUTORIAL_CONFIRM_H         28
#define TUTORIAL_CHECKBOX_SIZE     13

#define TUTORIAL_CLR_CARD_BG       Get16BPPColor(FROMRGB(24, 26, 38))
#define TUTORIAL_CLR_CARD_BORDER   Get16BPPColor(FROMRGB(80, 90, 110))
#define TUTORIAL_CLR_BUTTON_BG     Get16BPPColor(FROMRGB(52, 58, 78))
#define TUTORIAL_CLR_BUTTON_HI     Get16BPPColor(FROMRGB(115, 128, 158))
#define TUTORIAL_CLR_BUTTON_SHADOW Get16BPPColor(FROMRGB(10, 12, 18))
#define TUTORIAL_CLR_CHECK_BG      Get16BPPColor(FROMRGB(12, 14, 22))
#define TUTORIAL_CLR_DOT_ACTIVE    Get16BPPColor(FROMRGB(200, 200, 210))
#define TUTORIAL_CLR_DOT_INACTIVE  Get16BPPColor(FROMRGB(60, 60, 70))

TUTORIAL_STATE gTutorial = { false, 0, false, false, false, false, false, TutorialMode::Tactical, nullptr };

static const TUTORIAL_PANEL gTutorialPanelsDE[3] = {
	{
		"Spielfeld",
		"Bewege die Maus, indem du mit dem Finger über das Spielfeld streichst\n\n"
		"Ein einfacher Tap wählt Söldner aus, lässt sie gehen und angreifen, oder führt Aktionen aus\n\n"
		"Ein doppelter Tap lässt Söldner rennen\n\n"
		"Ein doppelter Tap ohne Loslassen lässt dich mehrere Söldner auswählen\n\n"
		"Ein Zwei-Finger Tap aktiviert die rechte Maustaste"
	},
	{
		"Aktionspanel",
		"Tappe Portraits von Söldnern an, um sie auszuwählen\n\n"
		"Halte auf ein Portrait gedrückt, um alle Söldner auszuwählen\n\n"
		"Tappe mit zwei Fingern, um zwischen Söldnerpanel und dem Inventar zu wechseln\n\n"
		"Bediene Buttons direkt im Panel oder sortiere dein Inventar per Drag and Drop"
	},
	{
		"Button Editor",
		"Entsperre das Schloss in der unteren Ecke, um den Button Editor zu öffnen\n\n"
		"Erstelle neue Buttons oder halte einen Button gedrückt, um ihn zu bearbeiten\n\n"
		"Lösche sie, ändere ihre Größe und Form oder verschiebe sie beliebig\n\n"
		"Passe Scroll- und Mausgeschwindigkeit frei an\n\n"
		"Exportiere oder importiere deine perfekte Konfiguration"
	}
};

static const TUTORIAL_PANEL gTutorialPanelsEN[3] = {
	{
		"Playfield",
		"Move the mouse by sliding your finger across the playfield\n\n"
		"A single tap selects mercenaries, moves them, attacks, or performs actions\n\n"
		"Double tap to make mercenaries run\n\n"
		"Double tap and hold to select multiple mercenaries\n\n"
		"Two-finger tap triggers a right-click"
	},
	{
		"Action Panel",
		"Tap mercenary portraits to select them\n\n"
		"Long press a portrait to select all mercenaries\n\n"
		"Two-finger tap to toggle between the mercenary panel and inventory\n\n"
		"Use buttons directly in the panel or organize your inventory via drag and drop"
	},
	{
		"Button Editor",
		"Unlock the lock in the bottom corner to open the button editor\n\n"
		"Create new buttons or long press a button to edit it\n\n"
		"Delete them, resize and reshape them, or move them anywhere\n\n"
		"Adjust scroll and mouse speed freely\n\n"
		"Export or import your perfect configuration"
	}
};

static const TUTORIAL_PANEL gMainMenuTutorialPanelDE = {
	"Menüsteuerung",
	"Tippe Schaltflächen, die du gut mit dem Finger bedienen kannst, direkt an.\n\n"
	"Bewege deinen Finger am Rand des Bildschirms, um stattdessen den Mauszeiger zu steuern, und tippe, wo dein Finger stoppt, um einen Klick auszulösen.\n\n"
	"Damit kannst du kleinere Schaltflächen präzise erreichen."
};

static const TUTORIAL_PANEL gMainMenuTutorialPanelEN = {
	"Menu Controls",
	"Tap buttons directly when they are easy to reach with your finger.\n\n"
	"Move your finger along the edge of the screen to control the mouse cursor instead, then tap where your finger stops to trigger a click.\n\n"
	"This lets you reach smaller buttons precisely."
};

TUTORIAL_PANEL gTutorialPanels[3];
static bool sTutorialPanelsInitialized = false;
static bool sTutorialLanguageGerman = false;
static bool sTutorialLanguageSetByAndroid = false;

static MOUSE_REGION gTutorialInputRegion;
static bool gTutorialUIInitialized = false;
static bool gTutorialPointerDown = false;
static bool gTutorialSwipeHandled = false;
static int gTutorialSwipeStartX = 0;
static int gTutorialSwipeStartY = 0;

static int gCardX, gCardY, gCardW, gCardH;
static int gCardInnerX, gCardInnerW;
static int gConfirmX, gConfirmY, gConfirmW, gConfirmH;
static int gCheckboxX, gCheckboxY, gCheckboxW, gCheckboxH;

static void InitTutorialPanels()
{
	if (sTutorialPanelsInitialized) return;
	const bool useGerman = sTutorialLanguageSetByAndroid ? sTutorialLanguageGerman : isGermanVersion();
	const TUTORIAL_PANEL* src = useGerman ? gTutorialPanelsDE : gTutorialPanelsEN;
	for (int i = 0; i < 3; i++)
	{
		gTutorialPanels[i] = src[i];
	}
	sTutorialPanelsInitialized = true;
}

static bool IsTutorialGerman()
{
	return sTutorialLanguageSetByAndroid ? sTutorialLanguageGerman : isGermanVersion();
}

void SetTutorialLanguageGerman(bool german)
{
	sTutorialLanguageGerman = german;
	sTutorialLanguageSetByAndroid = true;
	sTutorialPanelsInitialized = false;
	InitTutorialPanels();
}

static ST::string GetTutorialSettingsPath()
{
	RustPointer<char> home(EngineOptions_getStracciatellaHome());
	return FileMan::joinPaths(ST::string(home.get()), "tutorial.set");
}

static ST::string GetMainMenuTutorialSettingsPath()
{
	RustPointer<char> home(EngineOptions_getStracciatellaHome());
	return FileMan::joinPaths(ST::string(home.get()), "mainmenu_tutorial.set");
}

void LoadTutorialSettings()
{
	InitTutorialPanels();

	ST::string path = GetTutorialSettingsPath();
	if (FileMan::isFile(path))
	{
		try
		{
			AutoSGPFile f(FileMan::openForReading(path));
			UINT8 val;
			f->read(&val, sizeof(val));
			gTutorial.fDontShowAgain = (val != 0);
		}
		catch (...)
		{
			SLOGW("Failed to read tutorial.set, using defaults");
		}
	}

	ST::string mainMenuPath = GetMainMenuTutorialSettingsPath();
	if (FileMan::isFile(mainMenuPath))
	{
		try
		{
			AutoSGPFile f(FileMan::openForReading(mainMenuPath));
			UINT8 val;
			f->read(&val, sizeof(val));
			gTutorial.fMainMenuDontShowAgain = (val != 0);
		}
		catch (...)
		{
			SLOGW("Failed to read mainmenu_tutorial.set, using defaults");
		}
	}
}

void SaveTutorialSettings()
{
	ST::string path = gTutorial.mode == TutorialMode::MainMenu
		? GetMainMenuTutorialSettingsPath()
		: GetTutorialSettingsPath();

	try
	{
		AutoSGPFile f(FileMan::openForWriting(path));
		UINT8 val = gTutorial.mode == TutorialMode::MainMenu
			? (gTutorial.fMainMenuDontShowAgain ? 1 : 0)
			: (gTutorial.fDontShowAgain ? 1 : 0);
		f->write(&val, sizeof(val));
	}
	catch (...)
	{
		SLOGE("Failed to write tutorial settings");
	}
}

bool ShouldShowMainMenuTutorial()
{
	return !gTutorial.fMainMenuDontShowAgain && !gTutorial.fMainMenuAutoShownThisSession && !gTutorial.fVisible;
}

static std::vector<ST::string> TutorialBodyParagraphs(const char* body)
{
	std::vector<ST::string> paragraphs;
	std::string text(body ? body : "");
	size_t start = 0;
	while (start < text.size())
	{
		size_t end = text.find("\n\n", start);
		std::string paragraph = text.substr(start, end == std::string::npos ? std::string::npos : end - start);
		while (!paragraph.empty() && (paragraph.back() == '\n' || paragraph.back() == '\r' || paragraph.back() == ' '))
		{
			paragraph.pop_back();
		}
		if (!paragraph.empty())
		{
			paragraphs.emplace_back(paragraph.c_str());
		}
		if (end == std::string::npos) break;
		start = end + 2;
	}
	return paragraphs;
}

static UINT16 TutorialBodyHeight(const char* body, UINT16 width)
{
	UINT16 height = 0;
	const auto paragraphs = TutorialBodyParagraphs(body);
	for (size_t i = 0; i < paragraphs.size(); i++)
	{
		height += IanWrappedStringHeight(width, TUTORIAL_LINE_GAP, FONT12ARIAL, paragraphs[i]);
		if (i + 1 < paragraphs.size()) height += TUTORIAL_PARAGRAPH_GAP;
	}
	return height;
}

static void DisplayTutorialBody(UINT16 x, UINT16 y, UINT16 width, const char* body)
{
	UINT16 curY = y;
	const auto paragraphs = TutorialBodyParagraphs(body);
	for (size_t i = 0; i < paragraphs.size(); i++)
	{
		curY += DisplayWrappedString(
			x, curY,
			width, TUTORIAL_LINE_GAP,
			FONT12ARIAL, FONT_FCOLOR_WHITE,
			paragraphs[i],
			FONT_MCOLOR_BLACK,
			CENTER_JUSTIFIED);
		if (i + 1 < paragraphs.size()) curY += TUTORIAL_PARAGRAPH_GAP;
	}
}

static bool PointInRect(int x, int y, int left, int top, int width, int height)
{
	return x >= left && x <= left + width && y >= top && y <= top + height;
}

static void RecalcCardLayout()
{
	const int layoutX = gTutorial.mode == TutorialMode::MainMenu ? STD_SCREEN_X : 0;
	const int layoutY = gTutorial.mode == TutorialMode::MainMenu ? STD_SCREEN_Y : 0;
	const int layoutW = gTutorial.mode == TutorialMode::MainMenu ? 640 : SCREEN_WIDTH;
	const int layoutH = gTutorial.mode == TutorialMode::MainMenu ? 480 : SCREEN_HEIGHT;

	gCardW = (layoutW * TUTORIAL_CARD_WIDTH_PCT) / 100;
	if (gCardW < 300) gCardW = 300;
	if (gCardW > layoutW - 20) gCardW = layoutW - 20;

	UINT16 maxBodyH = 0;
	const int panelCount = gTutorial.mode == TutorialMode::MainMenu ? 1 : 3;
	for (int i = 0; i < panelCount; i++)
	{
		const TUTORIAL_PANEL& panel = gTutorial.mode == TutorialMode::MainMenu
			? (IsTutorialGerman() ? gMainMenuTutorialPanelDE : gMainMenuTutorialPanelEN)
			: gTutorialPanels[i];
		UINT16 h = TutorialBodyHeight(panel.body, gCardW - 2 * TUTORIAL_CARD_PADDING);
		if (h > maxBodyH) maxBodyH = h;
	}
	UINT16 titleH = GetFontHeight(FONT14ARIAL) + TUTORIAL_LINE_GAP;

	gCardH = TUTORIAL_CARD_PADDING_TOP
		+ titleH + TUTORIAL_CARD_PADDING
		+ maxBodyH + TUTORIAL_CARD_PADDING
		+ (gTutorial.mode == TutorialMode::MainMenu ? 0 : TUTORIAL_DOT_SIZE + TUTORIAL_CARD_PADDING)
		+ TUTORIAL_CONFIRM_H + TUTORIAL_CARD_PADDING;

	if (gCardH > layoutH - 20) gCardH = layoutH - 20;

	gCardX = layoutX + (layoutW - gCardW) / 2;
	gCardY = layoutY + (layoutH - gCardH) / 2;
	gCardInnerX = gCardX + TUTORIAL_CARD_PADDING;
	gCardInnerW = gCardW - 2 * TUTORIAL_CARD_PADDING;

	gConfirmW = TUTORIAL_CONFIRM_W;
	gConfirmH = TUTORIAL_CONFIRM_H;
	gConfirmX = gCardX + (gCardW - gConfirmW) / 2;
	gConfirmY = gCardY + gCardH - TUTORIAL_CARD_PADDING - gConfirmH;

	gCheckboxW = TUTORIAL_CHECKBOX_SIZE;
	gCheckboxH = TUTORIAL_CHECKBOX_SIZE;
	gCheckboxX = gCardX + TUTORIAL_CARD_PADDING;
	gCheckboxY = gConfirmY + (gConfirmH - gCheckboxH) / 2;
}

static void TutorialMoveCallback(MOUSE_REGION* pRegion, UINT32 reason)
{
	if (!gTutorialPointerDown || gTutorialSwipeHandled || !(reason & MSYS_CALLBACK_REASON_MOVE))
	{
		return;
	}

	const int dx = pRegion->MouseXPos - gTutorialSwipeStartX;
	const int dy = pRegion->MouseYPos - gTutorialSwipeStartY;
	if (std::abs(dx) < TUTORIAL_SWIPE_THRESHOLD || std::abs(dx) < std::abs(dy))
	{
		return;
	}

	if (dx < 0) TutorialNextPanel();
	else TutorialPrevPanel();
	gTutorialSwipeHandled = true;
}

static void TutorialButtonCallback(MOUSE_REGION* pRegion, UINT32 reason)
{
	if (reason & MSYS_CALLBACK_REASON_POINTER_DWN)
	{
		gTutorialPointerDown = true;
		gTutorialSwipeHandled = false;
		gTutorialSwipeStartX = pRegion->MouseXPos;
		gTutorialSwipeStartY = pRegion->MouseYPos;
		return;
	}

	if (!(reason & MSYS_CALLBACK_REASON_POINTER_UP))
	{
		return;
	}

	if (gTutorialPointerDown && !gTutorialSwipeHandled)
	{
		const int x = pRegion->MouseXPos;
		const int y = pRegion->MouseYPos;
		const int dx = x - gTutorialSwipeStartX;
		const int dy = y - gTutorialSwipeStartY;

		if (PointInRect(x, y, gConfirmX, gConfirmY, gConfirmW, gConfirmH))
		{
			ExitTutorial();
		}
		else if (PointInRect(x, y, gCheckboxX - 4, gCheckboxY - 5, gCheckboxW + 170, gCheckboxH + 10))
		{
			gTutorial.fCheckboxChecked = !gTutorial.fCheckboxChecked;
		}
		else if (std::abs(dx) >= TUTORIAL_SWIPE_THRESHOLD && std::abs(dx) >= std::abs(dy))
		{
			if (dx < 0) TutorialNextPanel();
			else TutorialPrevPanel();
		}
	}

	gTutorialPointerDown = false;
	gTutorialSwipeHandled = false;
}

void EnterTutorial()
{
	InitTutorialPanels();
	if (gTutorial.fVisible) return;

	gTutorial.fVisible = true;
	gTutorial.mode = TutorialMode::Tactical;
	gTutorial.sCurrentPanel = 0;
	gTutorial.fCheckboxChecked = gTutorial.fDontShowAgain;
	gTutorialPointerDown = false;
	gTutorialSwipeHandled = false;

	RecalcCardLayout();

	gTutorial.pSaveBuffer = AddVideoSurface(SCREEN_WIDTH, SCREEN_HEIGHT, PIXEL_DEPTH);
	BlitBufferToBuffer(FRAME_BUFFER, gTutorial.pSaveBuffer, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

	MSYS_DefineRegion(&gTutorialInputRegion, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT,
		MSYS_PRIORITY_HIGHEST, VIDEO_NO_CURSOR, TutorialMoveCallback, TutorialButtonCallback);

	gTutorialUIInitialized = true;
}

void EnterMainMenuTutorial()
{
	InitTutorialPanels();
	if (gTutorial.fVisible) return;

	gTutorial.fVisible = true;
	gTutorial.fMainMenuAutoShownThisSession = true;
	gTutorial.mode = TutorialMode::MainMenu;
	gTutorial.sCurrentPanel = 0;
	gTutorial.fCheckboxChecked = gTutorial.fMainMenuDontShowAgain;
	gTutorialPointerDown = false;
	gTutorialSwipeHandled = false;

	RecalcCardLayout();

	gTutorial.pSaveBuffer = AddVideoSurface(SCREEN_WIDTH, SCREEN_HEIGHT, PIXEL_DEPTH);
	BlitBufferToBuffer(FRAME_BUFFER, gTutorial.pSaveBuffer, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

	MSYS_DefineRegion(&gTutorialInputRegion, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT,
		MSYS_PRIORITY_HIGHEST, VIDEO_NO_CURSOR, TutorialMoveCallback, TutorialButtonCallback);

	gTutorialUIInitialized = true;
}

void ExitTutorial()
{
	if (!gTutorialUIInitialized) return;

	MSYS_RemoveRegion(&gTutorialInputRegion);
	gTutorialUIInitialized = false;

	if (gTutorial.pSaveBuffer)
	{
		BlitBufferToBuffer(gTutorial.pSaveBuffer, FRAME_BUFFER, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		DeleteVideoSurface(gTutorial.pSaveBuffer);
		gTutorial.pSaveBuffer = nullptr;
		InvalidateScreen();
	}

	gTutorial.fVisible = false;

	if (gTutorial.fCheckboxChecked)
	{
		if (gTutorial.mode == TutorialMode::MainMenu)
		{
			gTutorial.fMainMenuDontShowAgain = true;
		}
		else
		{
			gTutorial.fDontShowAgain = true;
		}
		SaveTutorialSettings();
	}
}

static void DrawTutorialCheckbox()
{
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gCheckboxX - 1, gCheckboxY - 1,
		gCheckboxX + gCheckboxW + 1, gCheckboxY + gCheckboxH + 1,
		TUTORIAL_CLR_CARD_BORDER);
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gCheckboxX, gCheckboxY,
		gCheckboxX + gCheckboxW, gCheckboxY + gCheckboxH,
		TUTORIAL_CLR_CHECK_BG);

	if (gTutorial.fCheckboxChecked)
	{
		DrawTextToScreen("X", gCheckboxX + 2, gCheckboxY, gCheckboxW,
			FONT10ARIAL, FONT_FCOLOR_WHITE, FONT_MCOLOR_BLACK,
			LEFT_JUSTIFIED | TEXT_SHADOWED);
	}

	const char* label = IsTutorialGerman() ? "Nicht erneut anzeigen" : "Don't show again";
	DrawTextToScreen(label, gCheckboxX + gCheckboxW + 8, gCheckboxY + 1, 0,
		FONT10ARIAL, FONT_FCOLOR_WHITE, FONT_MCOLOR_BLACK,
		LEFT_JUSTIFIED | TEXT_SHADOWED);
}

static void DrawTutorialConfirmButton()
{
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gConfirmX + 1, gConfirmY + 1,
		gConfirmX + gConfirmW + 2, gConfirmY + gConfirmH + 2,
		TUTORIAL_CLR_BUTTON_SHADOW);
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gConfirmX - 1, gConfirmY - 1,
		gConfirmX + gConfirmW + 1, gConfirmY + gConfirmH + 1,
		TUTORIAL_CLR_BUTTON_HI);
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gConfirmX, gConfirmY,
		gConfirmX + gConfirmW, gConfirmY + gConfirmH,
		TUTORIAL_CLR_BUTTON_BG);

	const char* label = IsTutorialGerman() ? "Bestätigen" : "Confirm";
	const int textY = gConfirmY + (gConfirmH - GetFontHeight(FONT12ARIAL)) / 2;
	DrawTextToScreen(label, gConfirmX, textY, gConfirmW,
		FONT12ARIAL, FONT_FCOLOR_WHITE, FONT_MCOLOR_BLACK,
		CENTER_JUSTIFIED | TEXT_SHADOWED);
}

void RenderTutorial()
{
	if (!gTutorial.fVisible || !gTutorialUIInitialized) return;

	RecalcCardLayout();

	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gCardX - 2, gCardY - 2,
		gCardX + gCardW + 2, gCardY + gCardH + 2,
		TUTORIAL_CLR_CARD_BORDER);
	ColorFillVideoSurfaceArea(FRAME_BUFFER,
		gCardX, gCardY,
		gCardX + gCardW, gCardY + gCardH,
		TUTORIAL_CLR_CARD_BG);

	const TUTORIAL_PANEL& panel = gTutorial.mode == TutorialMode::MainMenu
		? (IsTutorialGerman() ? gMainMenuTutorialPanelDE : gMainMenuTutorialPanelEN)
		: gTutorialPanels[gTutorial.sCurrentPanel];
	const int titleY = gCardY + TUTORIAL_CARD_PADDING_TOP;
	DrawTextToScreen(panel.title, gCardX, titleY, gCardW,
		FONT14ARIAL, FONT_FCOLOR_WHITE, FONT_MCOLOR_BLACK,
		CENTER_JUSTIFIED | TEXT_SHADOWED);

	const int titleH = GetFontHeight(FONT14ARIAL) + TUTORIAL_LINE_GAP;
	const int bodyY = gCardY + TUTORIAL_CARD_PADDING_TOP + titleH + TUTORIAL_CARD_PADDING;
	SetFontShadow(NO_SHADOW);
	DisplayTutorialBody(gCardInnerX, bodyY, gCardInnerW, panel.body);
	SetFontShadow(DEFAULT_SHADOW);

	if (gTutorial.mode != TutorialMode::MainMenu)
	{
		const int dotY = gCardY + gCardH - TUTORIAL_CARD_PADDING - TUTORIAL_CONFIRM_H - TUTORIAL_CARD_PADDING - TUTORIAL_DOT_SIZE;
		const int totalDotsW = 3 * TUTORIAL_DOT_SIZE + 2 * TUTORIAL_DOT_SPACING;
		const int dotStartX = gCardX + (gCardW - totalDotsW) / 2;
		for (int i = 0; i < 3; i++)
		{
			UINT16 clr = (i == gTutorial.sCurrentPanel) ? TUTORIAL_CLR_DOT_ACTIVE : TUTORIAL_CLR_DOT_INACTIVE;
			int dx = dotStartX + i * (TUTORIAL_DOT_SIZE + TUTORIAL_DOT_SPACING);
			ColorFillVideoSurfaceArea(FRAME_BUFFER,
				dx, dotY, dx + TUTORIAL_DOT_SIZE, dotY + TUTORIAL_DOT_SIZE,
				clr);
		}
	}

	DrawTutorialCheckbox();
	DrawTutorialConfirmButton();
	InvalidateScreen();
}

void TutorialNextPanel()
{
	if (gTutorial.mode == TutorialMode::MainMenu) return;
	if (gTutorial.sCurrentPanel < 2)
	{
		gTutorial.sCurrentPanel++;
	}
}

void TutorialPrevPanel()
{
	if (gTutorial.mode == TutorialMode::MainMenu) return;
	if (gTutorial.sCurrentPanel > 0)
	{
		gTutorial.sCurrentPanel--;
	}
}
