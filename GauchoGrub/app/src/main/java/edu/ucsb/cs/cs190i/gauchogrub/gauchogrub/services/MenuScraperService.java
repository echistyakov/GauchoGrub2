package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.EntityDataStore;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class MenuScraperService extends IntentService {
    private static final String TAG = "MenuScraperService";
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SCRAPE_MENU_AT_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.action.SCRAPE_MENU_AT_DATE";
    private static final String EXTRA_PARAM_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.extra.DATE";
    private EntityDataStore<Persistable> mDataStore;

    public MenuScraperService() {
        super("MenuScraperService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataStore = ((GGApp) getApplication()).getData();
    }

    public static void startActionScrapeMenu(Context context, Date date) {
        Intent intent = new Intent(context, MenuScraperService.class);
        intent.setAction(ACTION_SCRAPE_MENU_AT_DATE);
        intent.putExtra(EXTRA_PARAM_DATE, date.getTime());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Date date;
            // handle case where intent called from startActionScrapeMenu w/ custom date
            // else, assume current date
            if(ACTION_SCRAPE_MENU_AT_DATE.equals(intent.getAction())) {
                date = new Date(intent.getExtras().getLong(EXTRA_PARAM_DATE));
            } else {
                date = new Date(System.currentTimeMillis());
            }
            scrapeMenus(date);
        }
    }

    private void scrapeMenus(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(date);
        try {
            Document doc = Jsoup.connect(getString(R.string.parsable_menu_url) + "?day=" + formattedDate).get();
            String[] diningCommonIds = getResources().getStringArray(R.array.parsable_dining_commons_ids);
            for (String id : diningCommonIds) {
                parseDiningCommonMenu(doc.getElementById(id));
                Log.d(TAG, id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseDiningCommonMenu(Element diningCommonMenu) {
        Elements panelElements = diningCommonMenu.children();
        Elements mealPanelElements = new Elements();
        // filter panels by "panel-success" class so that we do not
        // scrape "meal-warning" panels when the meal is not served
        // at the dining common
        for (Element panel: panelElements) {
            if (panel.hasClass("panel-success"))
                mealPanelElements.add(panel);
        }
        for (Element panelComponent : mealPanelElements) {
            if (panelComponent.hasClass("panel-heading")) {
                String mealType = panelComponent.getElementsByTag("h5").first().text();
                Log.d(TAG, mealType);
                // store meal type
            } else {
                // handle panel-body
                for (Element foodList : panelComponent.children()) {
                    // get first child of foodList (which should be a dt) as a menuCategory
                    String menuCategory = foodList.child(0).text();
                    Log.d(TAG, menuCategory);
                    // store each menuitem, which should be a dl
                    for (Element menuItem : foodList.getElementsByTag("dd")) {
                        String menuItemStr = menuItem.text();
                        Log.d(TAG, menuItemStr);
                    }
                }
            }
        }
    }
}
