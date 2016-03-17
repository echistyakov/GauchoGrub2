package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.joda.time.LocalDate;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.BaseMenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Favorite;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Menu;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService extends IntentService {

    public static Logger logger = Logger.getLogger("NotificationService");

    private EntityDataStore<Persistable> data;

    public NotificationService() {
        super("NotificationService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationService(String name) {
        super(name);
        data = ((GGApp) getApplication()).getData();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        data = ((GGApp) getApplication()).getData();
        List<FavoriteStruct> favorites = getAllFavoritesToday();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(getString(R.string.pref_key_favorites_notification), true)) {
            return;
        }
        // Stop if favorites are empty
        if (favorites.size() <= 0) {
            return;
        }
        NotificationCompat.Builder builder;
        int i = 0;
        for (FavoriteStruct favorite : favorites) {
            builder = new NotificationCompat.Builder(getBaseContext())
                    .setContentTitle(favorite.menuItem.title)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(favorite.mealName + " at " + favorite.diningCommon);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(i++, builder.build());
        }
    }

    /**
     * Gets favorites for a specific diningCommon
     * @param diningCommon the diningCommon to get favorites for
     * @param date a date for menus
     * @return a list of FavoriteStruct objects containing information for building notifications
     */
    private List<FavoriteStruct> getFavorites(String diningCommon, LocalDate date) {
        ArrayList<FavoriteStruct> favorites = new ArrayList<>();

        // Get DiningCommon from diningCommon string
        int diningCommonId = data.select(DiningCommon.class)
                .where(DiningCommon.NAME.eq(diningCommon))
                .get()
                .first()
                .getId();
        // Get Menu of the current day
        List<Menu> menusInDiningCommonToday = data.select(Menu.class)
                .join(RepeatedEvent.class).on(Menu.EVENT_ID.eq(RepeatedEvent.ID))
                .join(DiningCommon.class).on(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonId))
                .join(Meal.class).on(RepeatedEvent.MEAL_ID.eq(Meal.ID))
                .where(DiningCommon.NAME.eq(diningCommon))
                .and(Menu.DATE.eq(date))
                .get()
                .toList();
        // Get all favorites
        List<Favorite> favoriteEntities = data.select(Favorite.class).where(Favorite.DINING_COMMON_ID.eq(diningCommonId)).get().toList();
        for (Menu menu : menusInDiningCommonToday) {
            // Traverse all items in the menu, add favorites to list of favorites
            for (Favorite favorite : favoriteEntities) {
                for (BaseMenuItem menuItem : menu.getMenuItems().toList()) {
                    // If menuItem is favorite, add to list of favorites
                    if (menuItem.id == favorite.getMenuItemId()) {
                        RepeatedEvent repeatedEvent = data
                                .select(RepeatedEvent.class)
                                .where(RepeatedEvent.ID.eq(menu.getEventId()))
                                .get()
                                .first();
                        Meal meal = data
                                .select(Meal.class)
                                .where(Meal.ID.eq(repeatedEvent.getMealId()))
                                .get()
                                .first();
                        favorites.add(new FavoriteStruct(menuItem, diningCommon, meal.getName()));
                    }
                }
            }
        }
        return favorites;
    }

    private List<FavoriteStruct> getFavoritesToday(String diningCommon) {
        return getFavorites(diningCommon, LocalDate.now());
    }

    private List<FavoriteStruct> getAllFavoritesToday() {
        String[] diningCommons = getResources().getStringArray(R.array.dining_commons);
        ArrayList<FavoriteStruct> favorites = new ArrayList<>();
        for (String diningCommon : diningCommons) {
            favorites.addAll(getFavoritesToday(diningCommon));
        }
        return favorites;
    }

    /**
     * Helper class Favorite for storing relevant data succinctly
     */
    public class FavoriteStruct {
        public BaseMenuItem menuItem;
        public String mealName;
        public String diningCommon;

        public FavoriteStruct(BaseMenuItem menuItem, String diningCommon, String mealName) {
            this.menuItem = menuItem;
            this.mealName = mealName;
            this.diningCommon = diningCommon;
        }
    }
}