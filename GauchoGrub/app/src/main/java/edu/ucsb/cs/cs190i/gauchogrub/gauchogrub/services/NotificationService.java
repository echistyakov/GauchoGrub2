package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.joda.time.LocalDate;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.MainActivity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.BaseMenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Favorite;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Menu;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService extends IntentService {

    public static Logger logger = Logger.getLogger("NotificationService");

    private EntityDataStore<Persistable> data;

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
        List<FavoriteStruct> favorites = getAllFavoritesToday();
        // Stop if favorites are empty
        if(favorites.size() <= 0) {
            return;
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this).addParentStack(MainActivity.class);
        NotificationCompat.Builder builder;
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        int i = 0;
        for(FavoriteStruct favorite : favorites) {
            builder = new NotificationCompat.Builder(getBaseContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(favorite.menuItem.title + " is at " + favorite.diningCommon + " for " + favorite.mealName);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(i++, builder.build());
        }
    }

    /**
     * Gets favorites for a specific diningCommon, today
     * @param diningCommon
     * @param date
     * @return
     */
    private List<FavoriteStruct> getFavorites(String diningCommon, LocalDate date) {
        ArrayList<FavoriteStruct> favorites = new ArrayList<>();
        int dayOfWeek = date.getDayOfWeek();
        // Get DiningCommon from diningCommon string
        DiningCommon diningCommonResult = data.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first();
        // Get RepeatedEvent result for current day of the week and the right dining common
        List<RepeatedEvent> repeatedEvents = data.select(RepeatedEvent.class)
                .where(RepeatedEvent.DAY_OF_WEEK.equal(dayOfWeek)
                        .and(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonResult.getId()))).get().toList();
        // Get Menu of the current day
        List<Menu> menusInDiningCommonToday = data.select(Menu.class)
                .where(Menu.DATE.eq(date)).get().toList();
        // Get all favorites
        List<Favorite> favoriteEntities = data.select(Favorite.class).get().toList();
        for(Menu menu : menusInDiningCommonToday) {
            for(RepeatedEvent repeatedEvent : repeatedEvents) {
                // If menu is today and corresponds to a repeatedEvent for the correct diningCommon
                if(menu.getEventId() == repeatedEvent.getId()) {
                    // Traverse all items in the menu, add favorites to list of favorites
                    for(Favorite favorite : favoriteEntities) {
                        for(BaseMenuItem menuItem : menu.getMenuItems().toList()) {
                            // If menuItem is favorite, add to list of favorites
                            if(menuItem.id == favorite.getMenuItemId()) {
                                Meal meal = data.select(Meal.class).where(Meal.ID.eq(repeatedEvent.getMealId())).get().first();
                                favorites.add(new FavoriteStruct(menuItem, diningCommon, meal.getName()));
                            }
                        }
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
        for(String diningCommon : diningCommons) {
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
