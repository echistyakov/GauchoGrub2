package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommonEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MealEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuCategory;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuCategoryEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItemEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEventEntity;
import io.requery.Persistable;
import io.requery.meta.AttributeBuilder;
import io.requery.meta.QueryAttribute;
import io.requery.meta.TypeBuilder;
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
                Log.d(TAG, "PROCESSING DIV ID: " + id);
                parseDiningCommonMenu(idToDiningCommon(id), doc.getElementById(id));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseDiningCommonMenu(String diningCommonStr, Element diningCommonMenus) {
        DiningCommonEntity diningCommonEntity = mDataStore.select(DiningCommonEntity.class).where(DiningCommonEntity.NAME.equal(diningCommonStr)).get().first();
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
            Log.d(TAG, mealTypeStr);
            MealEntity mealEntity = mDataStore.select(MealEntity.class).where(MealEntity.NAME.equal(mealTypeStr)).get().first();
            // check if menu exists in the DB
            LocalDate localDate = new LocalDate(mDate.getTime());
            RepeatedEventEntity repeatedEventEntity = mDataStore.select(RepeatedEventEntity.class)
                    .where(RepeatedEventEntity.DINING_COMMON.equal(diningCommonEntity))
                    .and(RepeatedEventEntity.MEAL.equal(mealEntity)).get().first();
            MenuEntity menuEntity = mDataStore.select(MenuEntity.class)
                    .where(MenuEntity.DATE.equal(localDate))
                    .and(MenuEntity.EVENT.equal(repeatedEventEntity)).get().firstOrNull();
            // if menuEntity null, we found no matching record in the DB and must create and populate a new MenuEntity
            // else, do nothing
            if (menuEntity == null) {
                menuEntity = new MenuEntity();
                menuEntity.setDate(localDate);
                menuEntity.setEvent(repeatedEventEntity);
                mDataStore.insert(menuEntity);
                // iterate through each category of the menu
                for (Element foodListByCategory : mealPanel.getElementsByClass("course-list").first().children()) {
                    String menuCategoryStr = foodListByCategory.getElementsByTag("dt").first().text();
                    Log.d(TAG, menuCategoryStr);
                    MenuCategoryEntity menuCategoryEntity = mDataStore.select(MenuCategoryEntity.class).where(MenuCategoryEntity.NAME.equal(menuCategoryStr)).get().first();
                    for (Element menuItem : foodListByCategory.getElementsByTag("dd")) {
                        String menuItemTitleStr = menuItem.text();
                        Log.d(TAG, menuItemTitleStr);
                        // determine if MenuItem of same title and MenuCategory exists in DB
                        // if not, create new entity and insert it into the database
                        MenuItemEntity menuItemEntity = mDataStore.select(MenuItemEntity.class)
                                .where(MenuItemEntity.TITLE.equal(menuItemTitleStr)
                                .and(MenuItemEntity.MENU_CATEGORY.equal(menuCategoryEntity))).get().firstOrNull();
                        if (menuItemEntity == null) {
                            menuItemEntity = new MenuItemEntity();
                            menuItemEntity.setMenuCategory(menuCategoryEntity);
                            menuItemEntity.setTitle(menuItemTitleStr);
                            menuItemEntity.setIsVegetarian(menuItemTitleStr.contains(VEGETARIAN));
                            menuItemEntity.setIsVegan(menuItemTitleStr.contains(VEGAN));
                            menuItemEntity.setHasNuts(menuItemTitleStr.contains(HAS_NUTS));
                            mDataStore.insert(menuItemEntity);
                        }
                        menuEntity.getMenuItems().add(menuItemEntity);
                    }
                }
                mDataStore.update(menuEntity);
            }
        }
    }

    private String idToDiningCommon(String id) {
        String[] diningCommons = getResources().getStringArray(R.array.dining_commons);
        List<String> ids = Arrays.asList(getResources().getStringArray(R.array.parsable_dining_commons_ids));
        return diningCommons[ids.indexOf(id)];
    }
}
