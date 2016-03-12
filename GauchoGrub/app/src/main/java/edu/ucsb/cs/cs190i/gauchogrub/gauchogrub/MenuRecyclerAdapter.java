package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.util.List;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuCategory;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;


public class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<MenuCategory> sectionTitles;
    private List<List<MenuItem>> menuItems;
    private int menuItemCount;

    public MenuRecyclerAdapter(List<MenuCategory> sectionTitles, List<List<MenuItem>> menuItems, Context context) {
        this.context = context;
        this.sectionTitles = sectionTitles;
        this.menuItems = menuItems;
        menuItemCount = 0;
        for(List<MenuItem> list : menuItems) {
            menuItemCount += list.size();
        }
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
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_menuitem, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p/>
     * Override {#onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle effcient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object menuRow = getObjectAtIndex(position);
        if(menuRow instanceof MenuItem) {
            // TODO: Set text
            // TODO: Handle Logic for icons

            // TODO: Handle logic for swipe listener
            holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {

                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    // TODO: Handle logic for saving favorite

                    // TODO: Show Snackbar upon saving favorite
                    // Close layout upon favorite
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

        } else if (menuRow instanceof MenuCategory) {
            holder.menuItemNutsImageView.setVisibility(View.INVISIBLE);
            holder.menuItemVegImageView.setVisibility(View.INVISIBLE);
            holder.menuItemTextView.setTextSize(28);
            holder.menuItemTextView.setPadding(0, 7, 0, 0);
            // Prevent touch events on category name
            holder.menuItemTextView.setClickable(false);
            holder.swipeLayout.addSwipeDenier(new SwipeLayout.SwipeDenier() {
                @Override
                public boolean shouldDenySwipe(MotionEvent ev) {
                    return true;
                }
            });
            // TODO: Set text
        }
    }

    private Object getObjectAtIndex(int position) {
        int counter = 0;
        for(List<MenuItem> list : menuItems) {
            if(list.size() + counter - 1 <= position) {
                return list.get(position - counter);
            } else {
                counter += list.size();
            }
        }
        return null;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return menuItemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public SwipeLayout swipeLayout;
        public TextView menuItemTextView;
        public ImageView menuItemVegImageView;
        public ImageView menuItemNutsImageView;

        public ViewHolder(View v) {
            super(v);
            swipeLayout = (SwipeLayout) v.findViewById(R.id.menuItem_swipeLayout);
            menuItemTextView = (TextView) v.findViewById(R.id.menuItem_text);
            menuItemNutsImageView = (ImageView) v.findViewById(R.id.menuItem_hasNuts);
            menuItemVegImageView = (ImageView) v.findViewById(R.id.menuItem_isVeg);

            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        }
    }
}

