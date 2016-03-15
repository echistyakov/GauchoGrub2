package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Favorite;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import io.requery.Persistable;
import io.requery.android.QueryRecyclerAdapter;
import io.requery.query.Result;
import io.requery.sql.EntityDataStore;

public class FavoriteRecyclerAdapter extends QueryRecyclerAdapter<MenuItem, FavoriteRecyclerAdapter.ViewHolder> {

    private EntityDataStore<Persistable> dataStore;
    private String diningCommon;
    private int diningCommonId;
    private Context context;
    private FavoriteRecyclerAdapter thisAdapter;

    public FavoriteRecyclerAdapter(String diningCommon, Context context) {
        super(MenuItem.$TYPE);
        dataStore = ((GGApp) context.getApplicationContext()).getData();
        this.diningCommon = diningCommon;
        this.diningCommonId = dataStore.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first().getId();
        this.thisAdapter = this;
        this.context = context;
    }


    @Override
    public Result<MenuItem> performQuery() {
        return dataStore.select(MenuItem.class)
                .join(Favorite.class)
                .on(MenuItem.ID.eq(Favorite.MENU_ITEM_ID))
                .where(Favorite.DINING_COMMON_ID.eq(diningCommonId))
                .get();
    }

    @Override
    public void onBindViewHolder(final MenuItem menuItem, final ViewHolder viewHolder, int i) {

        // Nuts and V/VGN independent
        if(menuItem.getHasNuts()) {
            viewHolder.favoriteNutsImageview.setImageResource(R.mipmap.ic_nuts);
        } else {
            viewHolder.favoriteNutsImageview.setImageResource(android.R.color.transparent);
        }
        // Vegan icon has priority over vegetarian
        if (menuItem.getIsVegan()) {
            viewHolder.favoriteVegImageView.setImageResource(R.mipmap.ic_vegan);
        } else if(menuItem.getIsVegetarian()) {
            viewHolder.favoriteVegImageView.setImageResource(R.mipmap.ic_vegetarian);
        } else {
            viewHolder.favoriteVegImageView.setImageResource(android.R.color.transparent);
        }

        // Remove reduntant (v), (vgn), or (w/ nuts) from name
        String NUTS_STRING = context.getString(R.string.parsable_has_nuts);
        String VEG_STRING = context.getString(R.string.parsable_veg);
        String VGN_STRING = context.getString(R.string.parsable_vgn);
        viewHolder.favoriteTextView.setText(menuItem.getTitle()
                .replace(NUTS_STRING, "")
                .replace(VEG_STRING, "")
                .replace(VGN_STRING, "")
                .trim());

        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, viewHolder.bottomWrapper);
        if (viewHolder.menuSwipeListener == null) {
            viewHolder.menuSwipeListener = new MenuSwipeListener(viewHolder);
            viewHolder.swipeLayout.addSwipeListener(viewHolder.menuSwipeListener);
        }
        viewHolder.menuSwipeListener.setMenuItem(menuItem);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_favorite, parent, false);
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public SwipeLayout swipeLayout;
        public TextView favoriteTextView;
        public LinearLayout bottomWrapper;
        public ImageView favoriteVegImageView;
        public ImageView favoriteNutsImageview;
        public MenuSwipeListener menuSwipeListener = null;

        public ViewHolder(View v) {
            super(v);
            swipeLayout = (SwipeLayout) v.findViewById(R.id.favorite_swipeLayout);
            favoriteTextView = (TextView) v.findViewById(R.id.favorite_text);
            bottomWrapper = (LinearLayout) v.findViewById(R.id.favorite_bottomWrapper);
            favoriteNutsImageview = (ImageView) v.findViewById(R.id.favorite_hasNuts);
            favoriteVegImageView = (ImageView) v.findViewById(R.id.favorite_isVeg);
        }

    }

    public class MenuSwipeListener implements SwipeLayout.SwipeListener {

        private final ViewHolder viewHolder;
        private MenuItem menuItem;

        public MenuSwipeListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public void onStartOpen(SwipeLayout layout) {

        }

        @Override
        public void onOpen(SwipeLayout layout) {
            Favorite favorite = dataStore.select(Favorite.class)
                    .where(Favorite.DINING_COMMON_ID.eq(diningCommonId)
                            .and(Favorite.MENU_ITEM_ID.eq(menuItem.getId())))
                    .get()
                    .firstOrNull();
            if(favorite != null)
                dataStore.delete(favorite);
            thisAdapter.notifyDataSetChanged();
            thisAdapter.queryAsync();
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

    }
}
