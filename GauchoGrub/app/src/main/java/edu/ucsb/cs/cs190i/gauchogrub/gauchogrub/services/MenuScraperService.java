package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuCategory;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Menu;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Menu_MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.sql.EntityDataStore;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class MenuScraperService extends IntentService {
    private static final String TAG = "MenuScraperService";
    private String BRIGHT_MEAL;
    private String VEGETARIAN;
    private String VEGAN;
    private String HAS_NUTS;
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SCRAPE_MENU_AT_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.action.SCRAPE_MENU_AT_DATE";
    private static final String EXTRA_PARAM_DATE = "edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.extra.DATE";
    private EntityDataStore<Persistable> mDataStore;
    private DateTime mDate;

    public MenuScraperService() {
        super("MenuScraperService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataStore = ((GGApp) getApplication()).getData();
        BRIGHT_MEAL = getString(R.string.parsable_bright_meal);
        VEGETARIAN = getString(R.string.parsable_veg);
        VEGAN = getString(R.string.parsable_vgn);
        HAS_NUTS = getString(R.string.parsable_has_nuts);
    }

    /**
     * Starts a MenuScraperService for a specific day
     * @param context
     * @param date the day to get information for
     */
    public static void startActionScrapeMenu(Context context, DateTime date) {
        Intent intent = new Intent(context, MenuScraperService.class);
        intent.setAction(ACTION_SCRAPE_MENU_AT_DATE);
        intent.putExtra(EXTRA_PARAM_DATE, date.getMillis());
        context.startService(intent);
    }

    /**
     * Starts 7 individual MenuScraperService instances to handle the next 7 days
     * @param context
     */
    public static void startActionScrapeMenu(Context context) {
        for(int i = 0; i < 7; i++) {
            startActionScrapeMenu(context, DateTime.now().plusDays(i));
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            DateTime date;
            // handle case where intent called from startActionScrapeMenu w/ custom date
            // else, assume current date
            if(ACTION_SCRAPE_MENU_AT_DATE.equals(intent.getAction())) {
                date = new DateTime(intent.getExtras().getLong(EXTRA_PARAM_DATE));
            } else {
                date = DateTime.now();
            }
            scrapeMenus(date);
        }
    }

    private void scrapeMenus(DateTime date) {
        //Log.d(TAG, date.toString("MM/dd"));
        mDate = date;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        String formattedDate = mDate.toString(dateTimeFormatter);
        try {
            Document doc = Jsoup.connect(getString(R.string.parsable_menu_url) + "?day=" + formattedDate).get();
            String[] diningCommonIds = getResources().getStringArray(R.array.parsable_dining_commons_ids);
            for (String id : diningCommonIds) {
                //Log.d(TAG, "PROCESSING DIV ID: " + id + " using string " + htmlIdToDiningCommon(id));
                parseDiningCommonMenu(htmlIdToDiningCommon(id), doc.getElementById(id));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseDiningCommonMenu(String diningCommonStr, Element diningCommonMenus) {
        DiningCommon diningCommon = mDataStore.select(DiningCommon.class).where(DiningCommon.NAME.equal(diningCommonStr)).get().first();
        Elements mealPanels = diningCommonMenus.children();
        Elements filteredMealPanels = new Elements();
        // filter panels by "panel-success" class so that we do not
        // scrape warning panels (when meals are not served), and
        // do not include Bright Meal panels
        for (Element panel: mealPanels) {
            if (panel.hasClass("panel-success") && !panel.getElementsByTag("h5").first().text().equals(BRIGHT_MEAL))
                filteredMealPanels.add(panel);
        }
        // iterate through each meal, creating a new menu for that
        // menu if it doesn't exist in the DB
        for (Element mealPanel : filteredMealPanels) {
            String mealTypeStr = mealPanel.getElementsByTag("h5").first().text();
            //Log.d(TAG, mealTypeStr);
            Meal meal = mDataStore.select(Meal.class).where(Meal.NAME.equal(mealTypeStr)).get().first();
            // check if menu exists in the DB
            LocalDate localDate = new LocalDate(mDate.getMillis());
            RepeatedEvent repeatedEvent = mDataStore.select(RepeatedEvent.class)
                    .where(RepeatedEvent.DINING_COMMON_ID.equal(diningCommon.getId()))
                    .and(RepeatedEvent.MEAL_ID.equal(meal.getId())).get().first();
            Menu menu = mDataStore.select(Menu.class)
                    .where(Menu.DATE.equal(localDate))
                    .and(Menu.EVENT_ID.equal(repeatedEvent.getId())).get().firstOrNull();
            // if menu null, we found no matching record in the DB and must create and populate a new MenuEntity
            // else, do nothing
            if (menu == null) {
                menu = new Menu();
                menu.setDate(localDate);
                menu.setEventId(repeatedEvent.getId());
                mDataStore.insert(menu);
                // iterate through each category of the menu
                Elements elements = mealPanel.getElementsByClass("course-list").first().children();
                for (Element foodListByCategory : mealPanel.getElementsByClass("course-list").first().children()) {
                    String menuCategoryStr = foodListByCategory.getElementsByTag("dt").first().text();
                    //Log.d(TAG, menuCategoryStr);
                    MenuCategory menuCategory = mDataStore.select(MenuCategory.class).where(MenuCategory.NAME.equal(menuCategoryStr)).get().firstOrNull();
                    if(menuCategory == null) {
                        MenuCategory newMenuCategory = new MenuCategory();
                        newMenuCategory.setName(menuCategoryStr);
                        menuCategory = mDataStore.insert(newMenuCategory);
                    }
                    for (Element menuItemElement : foodListByCategory.getElementsByTag("dd")) {
                        String menuItemTitleStr = menuItemElement.text();
                        //Log.d(TAG, menuItemTitleStr);
                        // determine if MenuItem of same title and MenuCategory exists in DB
                        // if not, create new entity and insert it into the database
                        MenuItem menuItem = mDataStore.select(MenuItem.class)
                                .where(MenuItem.TITLE.equal(menuItemTitleStr)
                                        .and(MenuItem.MENU_CATEGORY_ID.equal(menuCategory.getId()))).get().firstOrNull();
                        boolean newMenuItem = false;
                        if (menuItem == null) {
                            newMenuItem = true;
                            menuItem = new MenuItem();
                            menuItem.setMenuCategoryId(menuCategory.getId());
                            menuItem.setTitle(menuItemTitleStr);
                            menuItem.setIsVegetarian(menuItemTitleStr.contains(VEGETARIAN));
                            menuItem.setIsVegan(menuItemTitleStr.contains(VEGAN));
                            menuItem.setHasNuts(menuItemTitleStr.contains(HAS_NUTS));
                            mDataStore.insert(menuItem);
                        }
                        Menu_MenuItem mm = new Menu_MenuItem();
                        mm.setMenuId(menu.getId());
                        mm.setMenuItemId(menuItem.getId());
                        if(newMenuItem) {
                            mDataStore.insert(mm);
                        }
                    }
                }
            }
        }
    }

    private String htmlIdToDiningCommon(String id) {
        String[] diningCommons = getResources().getStringArray(R.array.dining_commons);
        List<String> ids = Arrays.asList(getResources().getStringArray(R.array.parsable_dining_commons_ids));
        return diningCommons[ids.indexOf(id)];
    }
}
