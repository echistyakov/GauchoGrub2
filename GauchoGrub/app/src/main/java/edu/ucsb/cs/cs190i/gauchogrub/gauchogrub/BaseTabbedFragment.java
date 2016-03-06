package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.app.Fragment;
import android.content.Context;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost.TabContentFactory;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.menu.DiningCommon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.logging.Logger;

public abstract class BaseTabbedFragment extends Fragment {

    private final static Logger logger = Logger.getLogger("BaseTabbedFragment");

    public abstract void setDisplayContent(int index);

    public abstract TabContentFactory createTabContent();

    /**
     * setUpTabs() handles the creation of tabs on each fragment with tabs
     * @param tabs a TabHost object that the tabs will be added to
     * @param contentCreate a TabContentFactory that will create content for the tabs
     * @param numTabs the number of tabs to create
     */
    public void setUpTabs(TabHost tabs, TabContentFactory contentCreate, int numTabs) {
        tabs.setup();
        // Create tabs and set text & content
        for (int i = 0; i < numTabs; i++) {
            TabSpec tab = tabs.newTabSpec(i + "");
            tab.setContent(contentCreate);
            tab.setIndicator(DiningCommon.READABLE_DINING_COMMONS[i]);
            tabs.addTab(tab);
        }
        // Set tab listeners to change content when triggered
        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setDisplayContent(Integer.parseInt(tabId));
            }
        });
    }
}
