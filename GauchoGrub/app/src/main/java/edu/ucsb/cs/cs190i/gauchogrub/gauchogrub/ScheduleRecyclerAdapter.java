package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.android.QueryRecyclerAdapter;
import io.requery.query.Result;
import io.requery.sql.EntityDataStore;


public class ScheduleRecyclerAdapter extends QueryRecyclerAdapter<RepeatedEvent, ScheduleRecyclerAdapter.ViewHolder> {

    private String diningCommon;
    private Context context;
    private int diningCommonId;
    private EntityDataStore<Persistable> dataStore;

    ScheduleRecyclerAdapter(String diningCommon, Context context) {
        super(RepeatedEvent.$TYPE);
        this.diningCommon = diningCommon;
        this.context = context;
        dataStore = ((GGApp) context.getApplicationContext()).getData();
        this.diningCommonId = dataStore.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first().getId();
    }

    @Override
    public Result<RepeatedEvent> performQuery() {
        return dataStore.select(RepeatedEvent.class).where(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonId)).orderBy(RepeatedEvent.DAY_OF_WEEK).get();
    }

    @Override
    public void onBindViewHolder(RepeatedEvent repeatedEvent, ViewHolder viewHolder, int i) {

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
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
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
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }
    }
}
