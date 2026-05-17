package org.libsdl.app;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;


/**
    SDLSurface. This is what we draw on, so we need to know when it's created
    in order to do anything useful.

    Because of this, that's where we set up the SDL thread
*/
public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
    View.OnKeyListener, View.OnTouchListener, SensorEventListener  {

    // Sensors
    protected SensorManager mSensorManager;
    protected Display mDisplay;

    // Keep track of the surface size to normalize touch events
    protected float mWidth, mHeight;

    // Is SurfaceView ready for rendering
    public boolean mIsSurfaceReady;

    private static final String TOUCHSCREEN_MOUSE_MODE_TOUCHSCREEN = "touchscreen";
    private static final String TOUCHSCREEN_MOUSE_MODE_ABSOLUTE = "absolute";
    private static final String TOUCHSCREEN_MOUSE_MODE_TOUCHPAD = "touchpad";
    private static String mTouchscreenMouseMode = TOUCHSCREEN_MOUSE_MODE_TOUCHSCREEN;

    private static final int TOUCHPAD_TAP_TIMEOUT_MS = 250;
    private static final int TOUCHPAD_DOUBLE_TAP_TIMEOUT_MS = 280;
    private static final int TOUCHPAD_DOUBLE_TAP_HOLD_MS = 180;
    private static final int TOUCHPAD_DIRECT_TAP_ARBITRATION_MS = 1800;
    private static final int TOUCHPAD_CLICK_HOLD_MS = 45;
    private static final int TOUCHPAD_DOUBLE_CLICK_GAP_MS = 90;
    private static final int TOUCHPAD_TWO_FINGER_TAP_TIMEOUT_MS = 450;
    private static final int BOTTOM_UI_TWO_FINGER_TAP_TIMEOUT_MS = 450;
    private static final int PORTRAIT_LONG_PRESS_MS = 450;
    private static final int JA2_GAME_SCREEN = 5;
    private static final float TOUCHPAD_TAP_SLOP_PX = 30.0f;
    private static final float TOUCHPAD_DOUBLE_TAP_SLOP_DP = 64.0f;
    private static final float TOUCHPAD_DIRECT_TAP_ARBITRATION_DP = 72.0f;
    private static float sTouchpadSpeed = 1.0f;
    private static boolean sOverlayEditModeActive = false;

    public static void setOverlayEditModeActive(boolean active) {
        sOverlayEditModeActive = active;
    }

    public static boolean isOverlayEditModeActive() {
        return sOverlayEditModeActive;
    }

    public static void setTouchpadMouseSpeed(float speed) {
        sTouchpadSpeed = speed;
    }

    public static float getTouchpadMouseSpeed() {
        return sTouchpadSpeed;
    }

    private int mTouchpadPointerId = -1;
    private float mTouchpadCursorX = -1.0f;
    private float mTouchpadCursorY = -1.0f;
    private float mTouchpadLastX;
    private float mTouchpadLastY;
    private float mTouchpadSettledX;
    private float mTouchpadSettledY;
    private int mTouchpadMoveCount;
    private long mTouchpadDownTime;
    private long mTouchpadLastRelativeMoveTime;
    private float mTouchpadLastRelativeFingerX = -1.0f;
    private float mTouchpadLastRelativeFingerY = -1.0f;
    private boolean mTouchpadMoved;
    private int mTouchpadMaxPointerCount;
    private long mTouchpadLastTapUpTime;
    private float mTouchpadLastTapFingerX = -1.0f;
    private float mTouchpadLastTapFingerY = -1.0f;
    private boolean mTouchpadDoubleTapHoldCandidate;
    private boolean mTouchpadDoubleTapDragActive;
    private boolean mTouchpadSuppressUntilAllUp;
    private int mDeferredTouchpadClickButton;
    private boolean mDeferredTouchpadClickActive;
    private int mPendingTouchpadClickButton;
    private boolean mPendingTouchpadClickActive;
    private int mPendingTouchpadDoubleClickButton;
    private boolean mPendingTouchpadDoubleClickActive;
    private boolean mAbsoluteMouseDown;
    private int mBottomUIPointerId = -1;
    private float mBottomUIDownX;
    private float mBottomUIDownY;
    private float mBottomUILastX;
    private float mBottomUILastY;
    private long mBottomUIDownTime;
    private boolean mBottomUIMoved;
    private boolean mBottomUIMouseDown;
    private boolean mBottomUISuppressUntilAllUp;
    private int mBottomUIMaxPointerCount;
    private int mTeamPanelPortraitPointerId = -1;
    private boolean mTeamPanelPortraitLongPressed;
    private float mTeamPanelPortraitDownXNorm;
    private float mTeamPanelPortraitDownYNorm;
    private static final class LogicalPoint {
        final float xNorm;
        final float yNorm;

        LogicalPoint(float xNorm, float yNorm) {
            this.xNorm = xNorm;
            this.yNorm = yNorm;
        }
    }
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mTeamPanelPortraitLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTeamPanelPortraitPointerId == -1) {
                return;
            }
            if (SDLActivity.selectAllTeamPanelMercs()) {
                mTeamPanelPortraitLongPressed = true;
            }
        }
    };
    private final Runnable mTouchpadClickReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mPendingTouchpadClickActive) {
                return;
            }
            SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
            mPendingTouchpadClickActive = false;
            mPendingTouchpadClickButton = 0;
        }
    };
    private final Runnable mDeferredTouchpadClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mDeferredTouchpadClickActive) {
                return;
            }
            int mouseButton = mDeferredTouchpadClickButton;
            mDeferredTouchpadClickActive = false;
            mDeferredTouchpadClickButton = 0;
            performMouseClick(mouseButton);
        }
    };
    private final Runnable mTouchpadDoubleClickSecondRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mPendingTouchpadDoubleClickActive) {
                return;
            }
            int mouseButton = mPendingTouchpadDoubleClickButton;
            mPendingTouchpadDoubleClickActive = false;
            mPendingTouchpadDoubleClickButton = 0;
            performMouseClick(mouseButton);
        }
    };
    private final Runnable mTouchpadDoubleTapHoldRunnable = new Runnable() {
        @Override
        public void run() {
            beginTouchpadDoubleTapDrag();
        }
    };

    public static void setTouchscreenMouseMode(String mode) {
        if (TOUCHSCREEN_MOUSE_MODE_ABSOLUTE.equals(mode) || TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mode)) {
            mTouchscreenMouseMode = mode;
        } else {
            mTouchscreenMouseMode = TOUCHSCREEN_MOUSE_MODE_TOUCHSCREEN;
        }
        Log.v("SDL", "Touchscreen mouse mode: " + mTouchscreenMouseMode);
    }

    // Startup
    public SDLSurface(Context context) {
        super(context);
        getHolder().addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);

        mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        setOnGenericMotionListener(SDLActivity.getMotionListener());

        // Some arbitrary defaults to avoid a potential division by zero
        mWidth = 1.0f;
        mHeight = 1.0f;

        mIsSurfaceReady = false;
    }

    public void handlePause() {
        cancelDeferredTouchpadClick();
        cancelPendingTouchpadDoubleClick();
        releasePendingTouchpadClick();
        releaseTouchpadDoubleTapDrag();
        enableSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    public void handleResume() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
        enableSensor(Sensor.TYPE_ACCELEROMETER, true);
    }

    public Surface getNativeSurface() {
        return getHolder().getSurface();
    }

    // Called when we have a valid drawing surface
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("SDL", "surfaceCreated()");
        SDLActivity.onNativeSurfaceCreated();
    }

    // Called when we lose the surface
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("SDL", "surfaceDestroyed()");
        cancelDeferredTouchpadClick();
        cancelPendingTouchpadDoubleClick();
        releasePendingTouchpadClick();
        releaseTouchpadDoubleTapDrag();

        // Transition to pause, if needed
        SDLActivity.mNextNativeState = SDLActivity.NativeState.PAUSED;
        SDLActivity.handleNativeState();

        mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();
    }

    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        Log.v("SDL", "surfaceChanged()");

        if (SDLActivity.mSingleton == null) {
            return;
        }

        mWidth = width;
        mHeight = height;
        int nDeviceWidth = width;
        int nDeviceHeight = height;
        try
        {
            if (Build.VERSION.SDK_INT >= 17 /* Android 4.2 (JELLY_BEAN_MR1) */) {
                DisplayMetrics realMetrics = new DisplayMetrics();
                mDisplay.getRealMetrics( realMetrics );
                nDeviceWidth = realMetrics.widthPixels;
                nDeviceHeight = realMetrics.heightPixels;
            }
        } catch(Exception ignored) {
        }

        synchronized(SDLActivity.getContext()) {
            // In case we're waiting on a size change after going fullscreen, send a notification.
            SDLActivity.getContext().notifyAll();
        }

        Log.v("SDL", "Window size: " + width + "x" + height);
        Log.v("SDL", "Device size: " + nDeviceWidth + "x" + nDeviceHeight);
        SDLActivity.nativeSetScreenResolution(width, height, nDeviceWidth, nDeviceHeight, mDisplay.getRefreshRate());
        SDLActivity.onNativeResize();

        // Prevent a screen distortion glitch,
        // for instance when the device is in Landscape and a Portrait App is resumed.
        boolean skip = false;
        int requestedOrientation = SDLActivity.mSingleton.getRequestedOrientation();

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
            if (mWidth > mHeight) {
               skip = true;
            }
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            if (mWidth < mHeight) {
               skip = true;
            }
        }

        // Special Patch for Square Resolution: Black Berry Passport
        if (skip) {
           double min = Math.min(mWidth, mHeight);
           double max = Math.max(mWidth, mHeight);

           if (max / min < 1.20) {
              Log.v("SDL", "Don't skip on such aspect-ratio. Could be a square resolution.");
              skip = false;
           }
        }

        // Don't skip in MultiWindow.
        if (skip) {
            if (Build.VERSION.SDK_INT >= 24 /* Android 7.0 (N) */) {
                if (SDLActivity.mSingleton.isInMultiWindowMode()) {
                    Log.v("SDL", "Don't skip in Multi-Window");
                    skip = false;
                }
            }
        }

        if (skip) {
           Log.v("SDL", "Skip .. Surface is not ready.");
           mIsSurfaceReady = false;
           return;
        }

        /* If the surface has been previously destroyed by onNativeSurfaceDestroyed, recreate it here */
        SDLActivity.onNativeSurfaceChanged();

        /* Surface is ready */
        mIsSurfaceReady = true;

        SDLActivity.mNextNativeState = SDLActivity.NativeState.RESUMED;
        SDLActivity.handleNativeState();
    }

    // Key events
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return SDLActivity.handleKeyEvent(v, keyCode, event, null);
    }

    // Touch events
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (SDLActivity.isTutorialVisible()) {
            cancelDeferredTouchpadClick();
            cancelPendingTouchpadDoubleClick();
            releasePendingTouchpadClick();
            releaseTouchpadDoubleTapDrag();
            dispatchNativeTouch(event, true);
            return true;
        }

        if (sOverlayEditModeActive) {
            return true;
        }

        /* Ref: http://developer.android.com/training/gestures/multi.html */
        dispatchNativeTouch(event, false);
        return true;
   }

    private void dispatchNativeTouch(MotionEvent event, boolean forceNativeTouch) {
        int touchDevId = event.getDeviceId();
        final int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        int pointerFingerId;
        int i = -1;
        float x,y,p;

        /*
         * Prevent id to be -1, since it's used in SDL internal for synthetic events
         * Appears when using Android emulator, eg:
         *  adb shell input mouse tap 100 100
         *  adb shell input touchscreen tap 100 100
         */
        if (touchDevId < 0) {
            touchDevId -= 1;
        }

        // 12290 = Samsung DeX mode desktop mouse
        // 12290 = 0x3002 = 0x2002 | 0x1002 = SOURCE_MOUSE | SOURCE_TOUCHSCREEN
        // 0x2   = SOURCE_CLASS_POINTER
        if (!forceNativeTouch && (event.getSource() == InputDevice.SOURCE_MOUSE || event.getSource() == (InputDevice.SOURCE_MOUSE | InputDevice.SOURCE_TOUCHSCREEN))) {
            int mouseButton = 1;
            try {
                Object object = event.getClass().getMethod("getButtonState").invoke(event);
                if (object != null) {
                    mouseButton = (Integer) object;
                }
            } catch(Exception ignored) {
            }

            // We need to check if we're in relative mouse mode and get the axis offset rather than the x/y values
            // if we are.  We'll leverage our existing mouse motion listener
            SDLGenericMotionListener_API12 motionListener = SDLActivity.getMotionListener();
            x = motionListener.getEventX(event);
            y = motionListener.getEventY(event);

            SDLActivity.onNativeMouse(mouseButton, action, x, y, motionListener.inRelativeMode());
        } else {
            if (!forceNativeTouch && handleTeamPanelPortraitTouch(event)) {
                return;
            }
            if (!forceNativeTouch && TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode) && handleBottomUITouch(event)) {
                return;
            }
            if (!forceNativeTouch && TOUCHSCREEN_MOUSE_MODE_ABSOLUTE.equals(mTouchscreenMouseMode)) {
                handleAbsoluteMouseTouch(event);
                return;
            }
            if (!forceNativeTouch && TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode)) {
                handleTouchpadMouseTouch(event);
                return;
            }

            switch(action) {
                case MotionEvent.ACTION_MOVE:
                    for (i = 0; i < pointerCount; i++) {
                        pointerFingerId = event.getPointerId(i);
                        x = event.getX(i) / mWidth;
                        y = event.getY(i) / mHeight;
                        p = event.getPressure(i);
                        if (p > 1.0f) {
                            // may be larger than 1.0f on some devices
                            // see the documentation of getPressure(i)
                            p = 1.0f;
                        }
                        SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_DOWN:
                    // Primary pointer up/down, the index is always zero
                    i = 0;
                    /* fallthrough */
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Non primary pointer up/down
                    if (i == -1) {
                        i = event.getActionIndex();
                    }

                    pointerFingerId = event.getPointerId(i);
                    x = event.getX(i) / mWidth;
                    y = event.getY(i) / mHeight;
                    p = event.getPressure(i);
                    if (p > 1.0f) {
                        // may be larger than 1.0f on some devices
                        // see the documentation of getPressure(i)
                        p = 1.0f;
                    }
                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    for (i = 0; i < pointerCount; i++) {
                        pointerFingerId = event.getPointerId(i);
                        x = event.getX(i) / mWidth;
                        y = event.getY(i) / mHeight;
                        p = event.getPressure(i);
                        if (p > 1.0f) {
                            // may be larger than 1.0f on some devices
                            // see the documentation of getPressure(i)
                            p = 1.0f;
                        }
                        SDLActivity.onNativeTouch(touchDevId, pointerFingerId, MotionEvent.ACTION_UP, x, y, p);
                    }
                    break;

                default:
                    break;
            }
        }
   }

    private boolean handleTeamPanelPortraitTouch(MotionEvent event) {
        int action = event.getActionMasked();
        if (TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode)
                && event.getPointerCount() >= 2
                && hasPointerInTacticalBottomPanel(event)) {
            if (mTeamPanelPortraitPointerId != -1) {
                mHandler.removeCallbacks(mTeamPanelPortraitLongPressRunnable);
                mTeamPanelPortraitPointerId = -1;
                mTeamPanelPortraitLongPressed = false;
            }
            return false;
        }

        if (mTeamPanelPortraitPointerId != -1) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mHandler.removeCallbacks(mTeamPanelPortraitLongPressRunnable);
                if (action == MotionEvent.ACTION_UP && !mTeamPanelPortraitLongPressed) {
                    SDLActivity.selectTeamPanelMercPortraitAt(mTeamPanelPortraitDownXNorm, mTeamPanelPortraitDownYNorm);
                }
                mTeamPanelPortraitPointerId = -1;
                mTeamPanelPortraitLongPressed = false;
            } else if (action == MotionEvent.ACTION_POINTER_UP) {
                int index = event.getActionIndex();
                if (event.getPointerId(index) == mTeamPanelPortraitPointerId) {
                    mHandler.removeCallbacks(mTeamPanelPortraitLongPressRunnable);
                    mTeamPanelPortraitPointerId = -1;
                    mTeamPanelPortraitLongPressed = false;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                int index = event.findPointerIndex(mTeamPanelPortraitPointerId);
                if (index < 0) {
                    mHandler.removeCallbacks(mTeamPanelPortraitLongPressRunnable);
                    mTeamPanelPortraitPointerId = -1;
                    mTeamPanelPortraitLongPressed = false;
                }
            }
            return true;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            return false;
        }

        LogicalPoint logicalPoint = toLogicalGamePoint(event.getX(0), event.getY(0));
        if (logicalPoint == null) {
            return false;
        }
        if (SDLActivity.isTeamPanelMercPortraitAt(logicalPoint.xNorm, logicalPoint.yNorm)) {
            mTeamPanelPortraitPointerId = event.getPointerId(0);
            mTeamPanelPortraitLongPressed = false;
            mTeamPanelPortraitDownXNorm = logicalPoint.xNorm;
            mTeamPanelPortraitDownYNorm = logicalPoint.yNorm;
            mHandler.postDelayed(mTeamPanelPortraitLongPressRunnable, PORTRAIT_LONG_PRESS_MS);
            return true;
        }
        return false;
    }

    private LogicalPoint toLogicalGamePoint(float surfaceX, float surfaceY) {
        if (mWidth <= 0.0f || mHeight <= 0.0f) {
            return null;
        }

        int logicalWidth;
        int logicalHeight;
        try {
            logicalWidth = SDLActivity.getJa2ScreenWidth();
            logicalHeight = SDLActivity.getJa2ScreenHeight();
        } catch (Throwable ignored) {
            logicalWidth = 0;
            logicalHeight = 0;
        }

        if (logicalWidth <= 0 || logicalHeight <= 0) {
            return new LogicalPoint(surfaceX / mWidth, surfaceY / mHeight);
        }

        float scale = Math.min(mWidth / (float) logicalWidth, mHeight / (float) logicalHeight);
        float viewportWidth = logicalWidth * scale;
        float viewportHeight = logicalHeight * scale;
        float viewportX = (mWidth - viewportWidth) * 0.5f;
        float viewportY = (mHeight - viewportHeight) * 0.5f;

        if (surfaceX < viewportX || surfaceX >= viewportX + viewportWidth ||
                surfaceY < viewportY || surfaceY >= viewportY + viewportHeight) {
            return null;
        }

        return new LogicalPoint(
                (surfaceX - viewportX) / viewportWidth,
                (surfaceY - viewportY) / viewportHeight
        );
    }

    private boolean handleBottomUITouch(MotionEvent event) {
        int action = event.getActionMasked();

        if (mBottomUISuppressUntilAllUp) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mBottomUISuppressUntilAllUp = false;
                resetBottomUITouchState();
            }
            return true;
        }

        if (mBottomUIPointerId == -1) {
            if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) {
                return false;
            }

            int startPointerIndex = findTacticalBottomPanelPointerIndex(event);
            if (startPointerIndex < 0) {
                return false;
            }

            mBottomUIPointerId = event.getPointerId(startPointerIndex);
            mBottomUIDownX = clamp(event.getX(startPointerIndex), 0.0f, mWidth);
            mBottomUIDownY = clamp(event.getY(startPointerIndex), 0.0f, mHeight);
            mBottomUILastX = mBottomUIDownX;
            mBottomUILastY = mBottomUIDownY;
            mBottomUIDownTime = event.getEventTime();
            mBottomUIMoved = false;
            mBottomUIMouseDown = false;
            mBottomUIMaxPointerCount = event.getPointerCount();
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mBottomUIMaxPointerCount = Math.max(mBottomUIMaxPointerCount, event.getPointerCount());
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (mBottomUIMaxPointerCount >= 2
                        && !mBottomUIMouseDown
                        && !mBottomUIMoved
                        && event.getEventTime() - mBottomUIDownTime <= BOTTOM_UI_TWO_FINGER_TAP_TIMEOUT_MS) {
                    SDLActivity.toggleTacticalPanels();
                    resetBottomUITouchState();
                    mBottomUISuppressUntilAllUp = true;
                    return true;
                }
                if (event.getPointerId(event.getActionIndex()) == mBottomUIPointerId) {
                    finishBottomUITouch(event, false);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(mBottomUIPointerId);
                if (pointerIndex < 0) {
                    cancelBottomUITouch();
                    return true;
                }

                float x = clamp(event.getX(pointerIndex), 0.0f, mWidth);
                float y = clamp(event.getY(pointerIndex), 0.0f, mHeight);
                float totalDx = x - mBottomUIDownX;
                float totalDy = y - mBottomUIDownY;
                if (totalDx * totalDx + totalDy * totalDy > TOUCHPAD_TAP_SLOP_PX * TOUCHPAD_TAP_SLOP_PX) {
                    mBottomUIMoved = true;
                    if (!mBottomUIMouseDown && mBottomUIMaxPointerCount < 2) {
                        sendBottomUIMouseDown(mBottomUIDownX, mBottomUIDownY);
                    }
                }
                if (mBottomUIMouseDown) {
                    SDLActivity.onNativeMouse(MotionEvent.BUTTON_PRIMARY, MotionEvent.ACTION_MOVE, x, y, false);
                    mTouchpadCursorX = x;
                    mTouchpadCursorY = y;
                }
                mBottomUILastX = x;
                mBottomUILastY = y;
                return true;
            case MotionEvent.ACTION_UP:
                finishBottomUITouch(event, true);
                return true;
            case MotionEvent.ACTION_CANCEL:
                cancelBottomUITouch();
                return true;
            default:
                return true;
        }
    }

    private boolean isInTacticalBottomPanel(float y) {
        if (mHeight <= 0.0f) {
            return false;
        }
        return isJa2GameScreen() && y >= getTacticalBottomPanelTop();
    }

    private float getTacticalBottomPanelTop() {
        try {
            return mHeight * SDLActivity.getTacticalBottomPanelTopRatio();
        } catch (Throwable ignored) {
            return mHeight;
        }
    }

    private boolean hasPointerInTacticalBottomPanel(MotionEvent event) {
        return findTacticalBottomPanelPointerIndex(event) >= 0;
    }

    private int findTacticalBottomPanelPointerIndex(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (isInTacticalBottomPanel(event.getY(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isJa2GameScreen() {
        try {
            return SDLActivity.getJa2ScreenId() == JA2_GAME_SCREEN;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void finishBottomUITouch(MotionEvent event, boolean allowTapAction) {
        int pointerIndex = event.findPointerIndex(mBottomUIPointerId);
        float x = pointerIndex >= 0 ? clamp(event.getX(pointerIndex), 0.0f, mWidth) : mBottomUILastX;
        float y = pointerIndex >= 0 ? clamp(event.getY(pointerIndex), 0.0f, mHeight) : mBottomUILastY;
        long elapsed = event.getEventTime() - mBottomUIDownTime;

        if (mBottomUIMouseDown) {
            SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, x, y, false);
            mTouchpadCursorX = x;
            mTouchpadCursorY = y;
        } else if (allowTapAction && !mBottomUIMoved) {
            if (mBottomUIMaxPointerCount >= 2 && elapsed <= BOTTOM_UI_TWO_FINGER_TAP_TIMEOUT_MS) {
                SDLActivity.toggleTacticalPanels();
            } else if (elapsed <= TOUCHPAD_TAP_TIMEOUT_MS) {
                sendBottomUIMouseDown(x, y);
                SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, x, y, false);
                mTouchpadCursorX = x;
                mTouchpadCursorY = y;
            }
        }

        resetBottomUITouchState();
    }

    private void cancelBottomUITouch() {
        if (mBottomUIMouseDown) {
            SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mBottomUILastX, mBottomUILastY, false);
        }
        resetBottomUITouchState();
    }

    private void sendBottomUIMouseDown(float x, float y) {
        SDLActivity.onNativeMouse(MotionEvent.BUTTON_PRIMARY, MotionEvent.ACTION_DOWN, x, y, false);
        mBottomUIMouseDown = true;
        mTouchpadCursorX = x;
        mTouchpadCursorY = y;
    }

    private void resetBottomUITouchState() {
        mBottomUIPointerId = -1;
        mBottomUIMoved = false;
        mBottomUIMouseDown = false;
        mBottomUIMaxPointerCount = 0;
    }

    private void handleAbsoluteMouseTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP
                ? event.getActionIndex()
                : 0;
        float x = clamp(event.getX(pointerIndex), 0.0f, mWidth);
        float y = clamp(event.getY(pointerIndex), 0.0f, mHeight);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mAbsoluteMouseDown = true;
                SDLActivity.onNativeMouse(MotionEvent.BUTTON_PRIMARY, action, x, y, false);
                break;
            case MotionEvent.ACTION_UP:
                mAbsoluteMouseDown = false;
                SDLActivity.onNativeMouse(0, action, x, y, false);
                break;
            case MotionEvent.ACTION_MOVE:
                SDLActivity.onNativeMouse(mAbsoluteMouseDown ? MotionEvent.BUTTON_PRIMARY : 0, action, x, y, false);
                break;
            case MotionEvent.ACTION_CANCEL:
                mAbsoluteMouseDown = false;
                SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, x, y, false);
                break;
            default:
                break;
        }
    }

    private void handleTouchpadMouseTouch(MotionEvent event) {
        int action = event.getActionMasked();

        ensureTouchpadCursorInitialized();

        if (mTouchpadSuppressUntilAllUp) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mTouchpadSuppressUntilAllUp = false;
                mTouchpadPointerId = -1;
                mTouchpadMaxPointerCount = 0;
            }
            return;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchpadPointerId = event.getPointerId(0);
                mTouchpadLastX = event.getX(0);
                mTouchpadLastY = event.getY(0);
                mTouchpadDownTime = event.getEventTime();
                mTouchpadMoveCount = 0;
                mTouchpadMoved = false;
                mTouchpadSuppressUntilAllUp = false;
                mTouchpadMaxPointerCount = event.getPointerCount();
                if (isTouchpadDoubleTap(event)) {
                    cancelDeferredTouchpadClick();
                    cancelPendingTouchpadDoubleClick();
                    releasePendingTouchpadClick();
                    mTouchpadDoubleTapHoldCandidate = true;
                    mTouchpadDoubleTapDragActive = false;
                    mHandler.postDelayed(mTouchpadDoubleTapHoldRunnable, TOUCHPAD_DOUBLE_TAP_HOLD_MS);
                } else {
                    cancelPendingTouchpadDoubleClick();
                    cancelTouchpadDoubleTapHoldCandidate();
                    flushDeferredTouchpadClick();
                    releasePendingTouchpadClick();
                    mTouchpadDoubleTapDragActive = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchpadMaxPointerCount = Math.max(mTouchpadMaxPointerCount, event.getPointerCount());
                if (mTouchpadMaxPointerCount >= 2) {
                    cancelDeferredTouchpadClick();
                    cancelPendingTouchpadDoubleClick();
                    releasePendingTouchpadClick();
                    cancelTouchpadDoubleTapHoldCandidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mTouchpadMaxPointerCount >= 2
                        && !mTouchpadMoved
                        && event.getEventTime() - mTouchpadDownTime <= TOUCHPAD_TWO_FINGER_TAP_TIMEOUT_MS) {
                    performMouseClick(MotionEvent.BUTTON_SECONDARY);
                    clearTouchpadLastTap();
                    mTouchpadSuppressUntilAllUp = true;
                    mTouchpadPointerId = -1;
                    return;
                }
                if (event.getPointerId(event.getActionIndex()) == mTouchpadPointerId) {
                    int replacementIndex = event.getActionIndex() == 0 ? 1 : 0;
                    if (replacementIndex < event.getPointerCount()) {
                        mTouchpadPointerId = event.getPointerId(replacementIndex);
                        mTouchpadLastX = event.getX(replacementIndex);
                        mTouchpadLastY = event.getY(replacementIndex);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                releasePendingTouchpadClick();
                int pointerIndex = event.findPointerIndex(mTouchpadPointerId);
                if (pointerIndex < 0) {
                    break;
                }

                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);
                float dx = x - mTouchpadLastX;
                float dy = y - mTouchpadLastY;

                mTouchpadMoveCount++;
                if (mTouchpadMoveCount == 1) {
                    mTouchpadSettledX = x;
                    mTouchpadSettledY = y;
                } else {
                    float totalDx = x - mTouchpadSettledX;
                    float totalDy = y - mTouchpadSettledY;
                    if (totalDx * totalDx + totalDy * totalDy > TOUCHPAD_TAP_SLOP_PX * TOUCHPAD_TAP_SLOP_PX) {
                        if (mTouchpadDoubleTapHoldCandidate) {
                            if (event.getEventTime() - mTouchpadDownTime >= TOUCHPAD_DOUBLE_TAP_HOLD_MS) {
                                mTouchpadMoved = true;
                                beginTouchpadDoubleTapDrag();
                            }
                        } else {
                            mTouchpadMoved = true;
                        }
                    }
                }

                mTouchpadCursorX = clamp(mTouchpadCursorX + dx * sTouchpadSpeed, 0.0f, mWidth);
                mTouchpadCursorY = clamp(mTouchpadCursorY + dy * sTouchpadSpeed, 0.0f, mHeight);
                mTouchpadLastX = x;
                mTouchpadLastY = y;
                if (mTouchpadMoved) {
                    mTouchpadLastRelativeMoveTime = event.getEventTime();
                    mTouchpadLastRelativeFingerX = x;
                    mTouchpadLastRelativeFingerY = y;
                }
                SDLActivity.onNativeMouse(
                        mTouchpadDoubleTapDragActive ? MotionEvent.BUTTON_PRIMARY : 0,
                        MotionEvent.ACTION_MOVE,
                        mTouchpadCursorX,
                        mTouchpadCursorY,
                        false);
                break;
            case MotionEvent.ACTION_UP:
                long elapsed = event.getEventTime() - mTouchpadDownTime;
                if (mTouchpadDoubleTapDragActive) {
                    SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
                    mTouchpadDoubleTapDragActive = false;
                } else if (mTouchpadDoubleTapHoldCandidate) {
                    cancelTouchpadDoubleTapHoldCandidate();
                    performMouseDoubleClick(MotionEvent.BUTTON_PRIMARY);
                    clearTouchpadLastTap();
                } else if (!mTouchpadMoved && elapsed <= TOUCHPAD_TAP_TIMEOUT_MS) {
                    if (mTouchpadMaxPointerCount == 1 && shouldDirectTapAtFinger(event)) {
                        flushDeferredTouchpadClick();
                        performDirectMouseClick(event.getX(0), event.getY(0));
                    } else {
                        int mouseButton = mTouchpadMaxPointerCount >= 2 ? MotionEvent.BUTTON_SECONDARY : MotionEvent.BUTTON_PRIMARY;
                        deferTouchpadClick(mouseButton);
                    }
                    rememberTouchpadTap(event);
                }
                mTouchpadPointerId = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDeferredTouchpadClick();
                cancelPendingTouchpadDoubleClick();
                releasePendingTouchpadClick();
                cancelTouchpadDoubleTapHoldCandidate();
                if (mTouchpadDoubleTapDragActive) {
                    SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
                    mTouchpadDoubleTapDragActive = false;
                }
                mTouchpadPointerId = -1;
                break;
            default:
                break;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void ensureTouchpadCursorInitialized() {
        if (mTouchpadCursorX < 0.0f || mTouchpadCursorY < 0.0f) {
            mTouchpadCursorX = mWidth / 2.0f;
            mTouchpadCursorY = mHeight / 2.0f;
        }
    }

    private boolean shouldDirectTapAtFinger(MotionEvent event) {
        if (isJa2GameScreen()) {
            return false;
        }
        if (mTouchpadLastRelativeMoveTime == 0
                || mTouchpadLastRelativeFingerX < 0.0f
                || mTouchpadLastRelativeFingerY < 0.0f) {
            return true;
        }

        long elapsedSinceRelativeMove = event.getEventTime() - mTouchpadLastRelativeMoveTime;
        if (elapsedSinceRelativeMove > TOUCHPAD_DIRECT_TAP_ARBITRATION_MS) {
            return true;
        }

        float dx = event.getX(0) - mTouchpadLastRelativeFingerX;
        float dy = event.getY(0) - mTouchpadLastRelativeFingerY;
        float radius = getDirectTapArbitrationRadiusPx();
        return dx * dx + dy * dy > radius * radius;
    }

    private boolean isTouchpadDoubleTap(MotionEvent event) {
        if (mTouchpadLastTapUpTime == 0
                || mTouchpadLastTapFingerX < 0.0f
                || mTouchpadLastTapFingerY < 0.0f
                || event.getPointerCount() != 1) {
            return false;
        }

        long elapsedSinceLastTap = event.getEventTime() - mTouchpadLastTapUpTime;
        if (elapsedSinceLastTap > TOUCHPAD_DOUBLE_TAP_TIMEOUT_MS) {
            return false;
        }

        float dx = event.getX(0) - mTouchpadLastTapFingerX;
        float dy = event.getY(0) - mTouchpadLastTapFingerY;
        float radius = Math.max(
                TOUCHPAD_TAP_SLOP_PX * 2.0f,
                TOUCHPAD_DOUBLE_TAP_SLOP_DP * getResources().getDisplayMetrics().density);
        return dx * dx + dy * dy <= radius * radius;
    }

    private void rememberTouchpadTap(MotionEvent event) {
        mTouchpadLastTapUpTime = event.getEventTime();
        mTouchpadLastTapFingerX = event.getX(0);
        mTouchpadLastTapFingerY = event.getY(0);
    }

    private void clearTouchpadLastTap() {
        mTouchpadLastTapUpTime = 0;
        mTouchpadLastTapFingerX = -1.0f;
        mTouchpadLastTapFingerY = -1.0f;
    }

    private float getDirectTapArbitrationRadiusPx() {
        return Math.max(
                TOUCHPAD_TAP_SLOP_PX * 2.0f,
                TOUCHPAD_DIRECT_TAP_ARBITRATION_DP * getResources().getDisplayMetrics().density);
    }

    private void performDirectMouseClick(float x, float y) {
        mTouchpadCursorX = clamp(x, 0.0f, mWidth);
        mTouchpadCursorY = clamp(y, 0.0f, mHeight);
        SDLActivity.onNativeMouse(MotionEvent.BUTTON_PRIMARY, MotionEvent.ACTION_DOWN, mTouchpadCursorX, mTouchpadCursorY, false);
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
    }

    public void performMouseButton(int mouseButton, boolean pressed) {
        if (!TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode)) {
            return;
        }

        ensureTouchpadCursorInitialized();
        if (pressed) {
            flushDeferredTouchpadClick();
            cancelPendingTouchpadDoubleClick();
            releasePendingTouchpadClick();
            releaseTouchpadDoubleTapDrag();
        }
        SDLActivity.onNativeMouse(
                pressed ? mouseButton : 0,
                pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                mTouchpadCursorX,
                mTouchpadCursorY,
                false);
    }

    public void performMouseClick(int mouseButton) {
        if (mTouchpadCursorX >= 0.0f && mTouchpadCursorY >= 0.0f
                && TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode)) {
            releasePendingTouchpadClick();
            mPendingTouchpadClickButton = mouseButton;
            mPendingTouchpadClickActive = true;
            SDLActivity.onNativeMouse(mouseButton, MotionEvent.ACTION_DOWN, mTouchpadCursorX, mTouchpadCursorY, false);
            mHandler.postDelayed(mTouchpadClickReleaseRunnable, TOUCHPAD_CLICK_HOLD_MS);
        }
    }

    private void performMouseDoubleClick(int mouseButton) {
        if (mTouchpadCursorX < 0.0f || mTouchpadCursorY < 0.0f
                || !TOUCHSCREEN_MOUSE_MODE_TOUCHPAD.equals(mTouchscreenMouseMode)) {
            return;
        }
        cancelPendingTouchpadDoubleClick();
        performMouseClick(mouseButton);
        mPendingTouchpadDoubleClickButton = mouseButton;
        mPendingTouchpadDoubleClickActive = true;
        mHandler.postDelayed(mTouchpadDoubleClickSecondRunnable, TOUCHPAD_DOUBLE_CLICK_GAP_MS);
    }

    private void cancelPendingTouchpadDoubleClick() {
        if (!mPendingTouchpadDoubleClickActive) {
            return;
        }
        mHandler.removeCallbacks(mTouchpadDoubleClickSecondRunnable);
        mPendingTouchpadDoubleClickActive = false;
        mPendingTouchpadDoubleClickButton = 0;
    }

    private void deferTouchpadClick(int mouseButton) {
        cancelDeferredTouchpadClick();
        mDeferredTouchpadClickButton = mouseButton;
        mDeferredTouchpadClickActive = true;
        mHandler.postDelayed(mDeferredTouchpadClickRunnable, TOUCHPAD_DOUBLE_TAP_TIMEOUT_MS);
    }

    private void flushDeferredTouchpadClick() {
        if (!mDeferredTouchpadClickActive) {
            return;
        }
        mHandler.removeCallbacks(mDeferredTouchpadClickRunnable);
        int mouseButton = mDeferredTouchpadClickButton;
        mDeferredTouchpadClickActive = false;
        mDeferredTouchpadClickButton = 0;
        performMouseClick(mouseButton);
    }

    private void cancelDeferredTouchpadClick() {
        if (!mDeferredTouchpadClickActive) {
            return;
        }
        mHandler.removeCallbacks(mDeferredTouchpadClickRunnable);
        mDeferredTouchpadClickActive = false;
        mDeferredTouchpadClickButton = 0;
    }

    private void releasePendingTouchpadClick() {
        if (!mPendingTouchpadClickActive) {
            return;
        }
        mHandler.removeCallbacks(mTouchpadClickReleaseRunnable);
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
        mPendingTouchpadClickActive = false;
        mPendingTouchpadClickButton = 0;
    }

    private void beginTouchpadDoubleTapDrag() {
        if (!mTouchpadDoubleTapHoldCandidate || mTouchpadDoubleTapDragActive) {
            return;
        }
        cancelTouchpadDoubleTapHoldCandidate();
        clearTouchpadLastTap();
        cancelPendingTouchpadDoubleClick();
        releasePendingTouchpadClick();
        mTouchpadDoubleTapDragActive = true;
        SDLActivity.onNativeMouse(MotionEvent.BUTTON_PRIMARY, MotionEvent.ACTION_DOWN, mTouchpadCursorX, mTouchpadCursorY, false);
    }

    private void cancelTouchpadDoubleTapHoldCandidate() {
        if (!mTouchpadDoubleTapHoldCandidate) {
            return;
        }
        mHandler.removeCallbacks(mTouchpadDoubleTapHoldRunnable);
        mTouchpadDoubleTapHoldCandidate = false;
    }

    private void releaseTouchpadDoubleTapDrag() {
        cancelTouchpadDoubleTapHoldCandidate();
        if (!mTouchpadDoubleTapDragActive) {
            return;
        }
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_UP, mTouchpadCursorX, mTouchpadCursorY, false);
        mTouchpadDoubleTapDragActive = false;
    }

    public void performOverlayMouseButton(int mouseButton, boolean pressed) {
        ensureTouchpadCursorInitialized();
        if (pressed) {
            flushDeferredTouchpadClick();
            cancelPendingTouchpadDoubleClick();
            releasePendingTouchpadClick();
            releaseTouchpadDoubleTapDrag();
        }
        SDLActivity.onNativeMouse(
                pressed ? mouseButton : 0,
                pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                mTouchpadCursorX >= 0.0f ? mTouchpadCursorX : mWidth / 2.0f,
                mTouchpadCursorY >= 0.0f ? mTouchpadCursorY : mHeight / 2.0f,
                false);
    }

    // Sensor events
    public void enableSensor(int sensortype, boolean enabled) {
        // TODO: This uses getDefaultSensor - what if we have >1 accels?
        if (enabled) {
            mSensorManager.registerListener(this,
                            mSensorManager.getDefaultSensor(sensortype),
                            SensorManager.SENSOR_DELAY_GAME, null);
        } else {
            mSensorManager.unregisterListener(this,
                            mSensorManager.getDefaultSensor(sensortype));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // Since we may have an orientation set, we won't receive onConfigurationChanged events.
            // We thus should check here.
            int newOrientation;

            float x, y;
            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_90:
                    x = -event.values[1];
                    y = event.values[0];
                    newOrientation = SDLActivity.SDL_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    x = event.values[1];
                    y = -event.values[0];
                    newOrientation = SDLActivity.SDL_ORIENTATION_LANDSCAPE_FLIPPED;
                    break;
                case Surface.ROTATION_180:
                    x = -event.values[0];
                    y = -event.values[1];
                    newOrientation = SDLActivity.SDL_ORIENTATION_PORTRAIT_FLIPPED;
                    break;
                case Surface.ROTATION_0:
                default:
                    x = event.values[0];
                    y = event.values[1];
                    newOrientation = SDLActivity.SDL_ORIENTATION_PORTRAIT;
                    break;
            }

            if (newOrientation != SDLActivity.mCurrentOrientation) {
                SDLActivity.mCurrentOrientation = newOrientation;
                SDLActivity.onNativeOrientationChanged(newOrientation);
            }

            SDLActivity.onNativeAccel(-x / SensorManager.GRAVITY_EARTH,
                                      y / SensorManager.GRAVITY_EARTH,
                                      event.values[2] / SensorManager.GRAVITY_EARTH);


        }
    }

    // Captured pointer events for API 26.
    public boolean onCapturedPointerEvent(MotionEvent event)
    {
        int action = event.getActionMasked();

        float x, y;
        switch (action) {
            case MotionEvent.ACTION_SCROLL:
                x = event.getAxisValue(MotionEvent.AXIS_HSCROLL, 0);
                y = event.getAxisValue(MotionEvent.AXIS_VSCROLL, 0);
                SDLActivity.onNativeMouse(0, action, x, y, false);
                return true;

            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_MOVE:
                x = event.getX(0);
                y = event.getY(0);
                SDLActivity.onNativeMouse(0, action, x, y, true);
                return true;

            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_BUTTON_RELEASE:

                // Change our action value to what SDL's code expects.
                if (action == MotionEvent.ACTION_BUTTON_PRESS) {
                    action = MotionEvent.ACTION_DOWN;
                } else { /* MotionEvent.ACTION_BUTTON_RELEASE */
                    action = MotionEvent.ACTION_UP;
                }

                x = event.getX(0);
                y = event.getY(0);
                int button = event.getButtonState();

                SDLActivity.onNativeMouse(button, action, x, y, true);
                return true;
        }

        return false;
    }
}
