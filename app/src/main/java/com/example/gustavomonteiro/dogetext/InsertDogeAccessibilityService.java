package com.example.gustavomonteiro.dogetext;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by gustavomonteiro on 22/08/17.
 */

public class InsertDogeAccessibilityService extends AccessibilityService {

    private Map<String, DogeContainer> dogeMap = new HashMap<>();
    private WindowManager windowManager;

    private int dogeOffsetX;
    private int dogeOffsetY;

    private static String TAG = InsertDogeAccessibilityService.class.getSimpleName();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "CONNECTED");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        dogeOffsetX = getResources().getDimensionPixelSize(R.dimen.doge_offset_x);
        dogeOffsetY = getResources().getDimensionPixelSize(R.dimen.doge_offset_y);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        for (String key : dogeMap.keySet()) {
            dogeMap.get(key).updated = false;
        }

        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source != null) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                AccessibilityNodeInfoCompat rootCompat = new AccessibilityNodeInfoCompat(root);
                List<AccessibilityNodeInfoCompat> textViews =
                        rootCompat.findAccessibilityNodeInfosByText("doge");
                for (AccessibilityNodeInfoCompat node : textViews) {
                    drawingDoge(node);
                }
            }
        }

        Iterator<Map.Entry<String,DogeContainer>> it = dogeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,DogeContainer> entry = it.next();
            if(!entry.getValue().updated){
                windowManager.removeView(entry.getValue().viewGroup);
                it.remove();
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private String getMessageUniqueId(AccessibilityNodeInfoCompat node) {
        AccessibilityNodeInfoCompat parent = node.getParent();
        if(parent != null) {
            List<AccessibilityNodeInfoCompat> list = parent.findAccessibilityNodeInfosByViewId("com.whatsapp:id/date");

            StringBuilder sb = new StringBuilder();
            sb.append(node.getText());

            if(list.size() > 0) {
                sb.append(list.get(0).getText());
            }

            return sb.toString();
        }

        return null;
    }

    private void drawingDoge(AccessibilityNodeInfoCompat node) {
        String nodeId = node.getViewIdResourceName();

        if (!TextUtils.isEmpty(nodeId) && nodeId.equalsIgnoreCase("com.whatsapp:id/message_text")) {
            String key = getMessageUniqueId(node);

            Rect rc = new Rect();
            node.getBoundsInScreen(rc);

            DogeContainer dogeContainer = dogeMap.get(key);

            if(dogeContainer == null) {
                ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.doge_layout, null);

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                layoutParams.gravity = Gravity.TOP | Gravity.START;
                layoutParams.x = rc.right + dogeOffsetX;
                layoutParams.y = rc.centerY() + dogeOffsetY;

                windowManager.addView(viewGroup, layoutParams);

                dogeContainer = new DogeContainer();
                dogeContainer.layoutParams = layoutParams;
                dogeContainer.viewGroup = viewGroup;
                dogeMap.put(key, dogeContainer);
            } else {
                dogeContainer.layoutParams.x = rc.right + dogeOffsetX;
                dogeContainer.layoutParams.y = rc.centerY() + dogeOffsetY;

                windowManager.updateViewLayout(dogeContainer.viewGroup, dogeContainer.layoutParams);
            }

            dogeContainer.updated = true;
        }
    }
}
