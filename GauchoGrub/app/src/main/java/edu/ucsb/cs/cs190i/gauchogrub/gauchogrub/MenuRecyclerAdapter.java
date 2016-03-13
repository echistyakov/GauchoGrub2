package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommonEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.FavoriteEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuCategory;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItemEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.android.QueryRecyclerAdapter;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.EntityDataStore;


public class MenuRecyclerAdapter extends QueryRecyclerAdapter<MenuItemEntity, MenuRecyclerAdapter.ViewHolder> {

    private Context context;
    private DateTime date;
    private List<FavoriteEntity> favorites;
    private View baseView;
    private SingleEntityStore<Persistable> dataStore;

    protected MenuRecyclerAdapter() {
        super(MenuItemEntity.$TYPE);
        this.date = DateTime.now();
        dataStore = ((GGApp) context.getApplicationContext()).getData();
    }

    protected MenuRecyclerAdapter(DateTime date, Context context, View baseView) {
        super(MenuItemEntity.$TYPE);
        this.date = date;
        this.context = context;
        dataStore = ((GGApp) context.getApplicationContext()).getData();
        favorites = getFavorites(context);
        this.baseView = baseView;
    }

    private List<FavoriteEntity> getFavorites(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);
        String currentDiningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON, context.getResources().getString(R.string.DLG));
        return dataStore.select(FavoriteEntity.class).where(FavoriteEntity.DINING_COMMON.like(currentDiningCommon)).get().toList();
}


    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p/>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p/>
     * The new ViewHolder will be used to display items of the adapter using
     * { #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     *  #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_menuitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public Result<MenuItemEntity> performQuery() {
        return null;
    }

    @Override
    public void onBindViewHolder(final MenuItemEntity menuItemEntity, final ViewHolder viewHolder, int i) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources()
                .getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);

        final DiningCommonEntity diningCommon = dataStore.select(DiningCommonEntity.class)
                .where(DiningCommonEntity.NAME.eq(sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON, context.getString(R.string.DLG)))).get().first();

        final FavoriteEntity favoriteEntity = dataStore.select(FavoriteEntity.class)
                .where(FavoriteEntity.DINING_COMMON.eq(diningCommon)
                        .and(FavoriteEntity.MENU_ITEM.eq(menuItemEntity)))
                .get().firstOrNull();

        // Nuts and V/VGN independent
        if(menuItemEntity.getHasNuts()) {
            viewHolder.menuItemNutsImageView.setImageResource(R.mipmap.ic_nuts);
        }
        // Vegan icon has priority over vegetarian
        if (menuItemEntity.getIsVegan()) {
            viewHolder.menuItemVegImageView.setImageResource(R.mipmap.ic_vegan);
        } else if(menuItemEntity.getIsVegetarian()) {
            viewHolder.menuItemVegImageView.setImageResource(R.mipmap.ic_vegetarian);
        }
        viewHolder.menuItemTextView.setText(menuItemEntity.getTitle());
        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                if(favoriteEntity != null) {
                    viewHolder.menuItemFavoriteStar.setImageResource(android.R.drawable.btn_star_big_off);
                } else {
                    viewHolder.menuItemFavoriteStar.setImageResource(android.R.drawable.btn_star_big_on);
                }

            }

            @Override
            public void onOpen(SwipeLayout layout) {

                String favoriteNotification = "";
                // If the favorite exists
                if(favoriteEntity != null) {
                    dataStore.delete(favoriteEntity);
                    favorites.remove(favoriteEntity);
                    favoriteNotification = menuItemEntity.getTitle() + " is removed from your favorites";
                    // Set background color
                    viewHolder.swipeLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                } else {
                    FavoriteEntity newFavorite = new FavoriteEntity();
                    newFavorite.setDiningCommon(diningCommon);
                    newFavorite.setMenuItem(menuItemEntity);
                    dataStore.insert(newFavorite);
                    favorites.add(newFavorite);
                    favoriteNotification = menuItemEntity.getTitle() + " has been added to your favorites";
                    // Set background color
                    viewHolder.swipeLayout.setBackgroundColor(Color.parseColor("#2C8CA1"));
                }

                Snackbar.make(baseView.findViewById(android.R.id.content), favoriteNotification, Snackbar.LENGTH_SHORT);
                layout.close();
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public SwipeLayout swipeLayout;
        public TextView menuItemTextView;
        public ImageView menuItemVegImageView;
        public ImageView menuItemNutsImageView;
        public ImageView menuItemFavoriteStar;

        public ViewHolder(View v) {
            super(v);
            swipeLayout = (SwipeLayout) v.findViewById(R.id.menuItem_swipeLayout);
            menuItemTextView = (TextView) v.findViewById(R.id.menuItem_text);
            menuItemNutsImageView = (ImageView) v.findViewById(R.id.menuItem_hasNuts);
            menuItemVegImageView = (ImageView) v.findViewById(R.id.menuItem_isVeg);
            menuItemFavoriteStar = (ImageView) v.findViewById(R.id.menuItem_favoritesStar);

            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        }
    }
}

