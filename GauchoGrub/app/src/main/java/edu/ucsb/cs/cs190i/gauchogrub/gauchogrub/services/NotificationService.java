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
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommonEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.FavoriteEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEventEntity;
import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService extends IntentService {

    public static Logger logger = Logger.getLogger("NotificationService");

    private SingleEntityStore<Persistable> data;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Favorite> favorites = getAllFavoritesToday();
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
        for(Favorite favorite : favorites) {
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
    private List<Favorite> getFavorites(String diningCommon, LocalDate date) {
        ArrayList<Favorite> favorites = new ArrayList<>();
        int dayOfWeek = date.getDayOfWeek();
        // Get DiningCommonEntity from diningCommon string
        DiningCommonEntity diningCommonEntityResult = data.select(DiningCommonEntity.class).where(DiningCommonEntity.NAME.eq(diningCommon)).get().first();
        // Get RepeatedEventEntity result for current day of the week and the right dining common
        List<RepeatedEventEntity> repeatedEvents = data.select(RepeatedEventEntity.class)
                .where(RepeatedEventEntity.DAY_OF_WEEK.equal(dayOfWeek)
                        .and(RepeatedEventEntity.DINING_COMMON.eq(diningCommonEntityResult))).get().toList();
        // Get MenuEntity of the current day
        List<MenuEntity> menusInDiningCommonToday = data.select(MenuEntity.class)
                .where(MenuEntity.DATE.eq(date)).get().toList();
        // Get all favorites
        List<FavoriteEntity> favoriteEntities = data.select(FavoriteEntity.class).get().toList();
        for(MenuEntity menu : menusInDiningCommonToday) {
            for(RepeatedEventEntity repeatedEvent : repeatedEvents) {
                // If menu is today and corresponds to a repeatedEvent for the correct diningCommon
                if(menu.getEvent().id == repeatedEvent.getId()) {
                    // Traverse all items in the menu, add favorites to list of favorites
                    for(FavoriteEntity favoriteEntity : favoriteEntities) {
                        for(MenuItem menuItem : menu.getMenuItems().toList()) {
                            // If menuItem is favorite, add to list of favorites
                            if(menuItem.id == favoriteEntity.getId()) {
                                favorites.add(new Favorite(menuItem, diningCommon, repeatedEvent.getMeal().name));
                            }
                        }
                    }
                }
            }
        }
        return favorites;
    }

    private List<Favorite> getFavoritesToday(String diningCommon) {
        return getFavorites(diningCommon, LocalDate.now());
    }

    private List<Favorite> getAllFavoritesToday() {
        String[] diningCommons = getResources().getStringArray(R.array.dining_commons);
        ArrayList<Favorite> favorites = new ArrayList<>();
        for(String diningCommon : diningCommons) {
            favorites.addAll(getFavoritesToday(diningCommon));
        }
        return favorites;
    }

    /**
     * Helper class Favorite for storing relevant data succinctly
     */
    public class Favorite {

        public MenuItem menuItem;
        public String mealName;
        public String diningCommon;

        public Favorite(MenuItem menuItem, String diningCommon, String mealName) {
            this.menuItem = menuItem;
            this.mealName = mealName;
            this.diningCommon = diningCommon;
        }
    }
}
