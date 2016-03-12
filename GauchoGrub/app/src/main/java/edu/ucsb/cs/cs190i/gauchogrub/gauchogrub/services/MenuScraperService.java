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
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import io.requery.Persistable;
import io.requery.meta.AttributeBuilder;
import io.requery.meta.QueryAttribute;
import io.requery.query.Result;
import io.requery.query.Selection;
import io.requery.query.Tuple;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.EntityDataStore;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class MenuScraperService extends IntentService {
    private static final String TAG = "MenuScraperService";
    private final String BRIGHT_MEAL = getString(R.string.parsable_bright_meal);
    private final String VEGETARIAN = getString(R.string.parsable_veg);
    private final String VEGAN = getString(R.string.parsable_vgn);
    private final String HAS_NUTS = getString(R.string.parsable_has_nuts);
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SCRAPE_MENU_AT_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.action.SCRAPE_MENU_AT_DATE";
    private static final String EXTRA_PARAM_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.extra.DATE";
    private EntityDataStore<Persistable> mDataStore;
    private Date mDate;

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
        mDate = date;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(mDate);
        try {
            Document doc = Jsoup.connect(getString(R.string.parsable_menu_url) + "?day=" + formattedDate).get();
            String[] diningCommonIds = getResources().getStringArray(R.array.parsable_dining_commons_ids);
            for (String id : diningCommonIds) {
                Log.d(TAG, id);
                parseDiningCommonMenu(doc.getElementById(id));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseDiningCommonMenu(Element diningCommonMenu) {
        boolean isVegetarian;
        boolean isVegan;
        boolean hasNuts;
        Elements mealPanels = diningCommonMenu.children();
        Elements filteredMealPanels = new Elements();
        // filter panels by "panel-success" class so that we do not
        // scrape warning panels (when meals are not served), and
        // do not include Bright Meal panels
        for (Element panel: mealPanels) {
            if (panel.hasClass("panel-success") && !panel.getElementsByTag("h5").first().text().equals(BRIGHT_MEAL))
                filteredMealPanels.add(panel);
        }
        for (Element mealPanel : filteredMealPanels) {
            String mealTypeStr = mealPanel.getElementsByTag("h5").first().text();
            Log.d(TAG, mealTypeStr);
            for (Element foodListByCategory : mealPanel.getElementsByClass("course-list").first().children()) {
                String menuCategoryStr = foodListByCategory.getElementsByTag("dt").first().text();
                Log.d(TAG, menuCategoryStr);
                for (Element menuItem : foodListByCategory.getElementsByTag("dd")) {
                    String menuItemTitleStr = menuItem.text();
                    // determine if MenuItem of same MenuCategory and title exists in DB
                    AttributeBuilder<> attrBuilder = new AttributeBuilder("title", MenuItem.class);
                    Selection<Result<Tuple>> selectedMenuItems = mDataStore.select(MenuItem.class, );
                    // mDataStore.select(MenuItem.class, )

                    isVegetarian = menuItemTitleStr.contains(VEGETARIAN);
                    isVegan = menuItemTitleStr.contains(VEGAN);
                    hasNuts = menuItemTitleStr.contains(HAS_NUTS);

                    Log.d(TAG, menuItemTitleStr);
                }
            }
        }
    }
}
