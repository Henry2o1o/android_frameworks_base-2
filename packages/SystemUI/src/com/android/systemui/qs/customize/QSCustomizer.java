/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.qs.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;

import com.android.systemui.R;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.qs.QSTile.Host.Callback;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.tuner.QSPagingSwitch;

import java.util.ArrayList;

/**
 * Allows full-screen customization of QS, through show() and hide().
 *
 * This adds itself to the status bar window, so it can appear on top of quick settings and
 * *someday* do fancy animations to get into/out of it.
 */
public class QSCustomizer extends LinearLayout implements OnMenuItemClickListener, Callback {

    private static final int MENU_SAVE = Menu.FIRST;
    private static final int MENU_RESET = Menu.FIRST + 1;

    private PhoneStatusBar mPhoneStatusBar;

    private Toolbar mToolbar;
    private CustomQSPanel mQsPanel;

    private boolean isShown;
    private CustomQSTileHost mHost;

    public QSCustomizer(Context context, AttributeSet attrs) {
        super(new ContextThemeWrapper(context, android.R.style.Theme_Material), attrs);
        mPhoneStatusBar = ((SystemUIApplication) mContext.getApplicationContext())
                .getComponent(PhoneStatusBar.class);
    }

    public void setHost(QSTileHost host) {
        mHost = new CustomQSTileHost(mContext, host);
        mHost.setCallback(this);
        mQsPanel.setTiles(mHost.getTiles());
        mQsPanel.setHost(mHost);
        mHost.setSavedTiles();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mToolbar = (Toolbar) findViewById(com.android.internal.R.id.action_bar);
        TypedValue value = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.homeAsUpIndicator, value, true);
        mToolbar.setNavigationIcon(
                getResources().getDrawable(value.resourceId, mContext.getTheme()));
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Is this all we want...?
                hide();
            }
        });
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.getMenu().add(Menu.NONE, MENU_SAVE, 0, mContext.getString(R.string.save))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mToolbar.getMenu().add(Menu.NONE, MENU_RESET, 0,
                mContext.getString(com.android.internal.R.string.reset));

        mQsPanel = (CustomQSPanel) findViewById(R.id.quick_settings_panel);
    }

    public void show() {
        isShown = true;
        mHost.setSavedTiles();
        // TODO: Fancy shmancy reveal.
        mPhoneStatusBar.getStatusBarWindow().addView(this);
    }

    public void hide() {
        isShown = false;
        // TODO: Similarly awesome or better hide.
        mPhoneStatusBar.getStatusBarWindow().removeView(this);
    }

    public boolean isCustomizing() {
        return isShown;
    }

    private void reset() {
        ArrayList<String> tiles = new ArrayList<>();
        for (String tile : QSPagingSwitch.QS_PAGE_TILES.split(",")) {
            tiles.add(tile);
        }
        mHost.setTiles(tiles);
    }

    private void save() {
        mHost.saveCurrentTiles();
        hide();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE:
                save();
                break;
            case MENU_RESET:
                reset();
                break;
        }
        return true;
    }

    @Override
    public void onTilesChanged() {
        mQsPanel.setTiles(mHost.getTiles());
    }
}