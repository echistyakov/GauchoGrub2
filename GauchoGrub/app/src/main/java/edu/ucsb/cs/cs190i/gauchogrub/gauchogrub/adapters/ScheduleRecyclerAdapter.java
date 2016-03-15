package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.GGApp;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.android.QueryRecyclerAdapter;
import io.requery.query.Result;
import io.requery.sql.EntityDataStore;

public class ScheduleRecyclerAdapter extends QueryRecyclerAdapter<RepeatedEvent, ScheduleRecyclerAdapter.ViewHolder> {

    private Context context;
    private int dayOfWeek;
    private String diningCommon;
    private EntityDataStore<Persistable> dataStore;
    private int diningCommonId;

    public ScheduleRecyclerAdapter(String diningCommon, int dayOfWeek, Context context) {
        super(RepeatedEvent.$TYPE);
        this.context = context;
        this.dayOfWeek = dayOfWeek;
        this.diningCommon = diningCommon;
        this.dataStore = ((GGApp) context.getApplicationContext()).getData();
        this.diningCommonId = dataStore.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first().getId();
    }

    @Override
    public Result<RepeatedEvent> performQuery() {
        return dataStore.select(RepeatedEvent.class)
                .join(Meal.class)
                .on(Meal.ID.eq(RepeatedEvent.MEAL_ID))
                .where(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonId)
                        .and(RepeatedEvent.DAY_OF_WEEK.eq(dayOfWeek)))
                .get();
    }

    @Override
    public void onBindViewHolder(RepeatedEvent repeatedEvent, ViewHolder viewHolder, int i) {
        // Always works due to the join condition in the query
        String mealName = dataStore.select(Meal.class).where(Meal.ID.eq(repeatedEvent.getMealId())).get().first().getName();

        String startTime = repeatedEvent.getStartTime().toString("hh:mma");
        String endTime = repeatedEvent.getEndTime().toString("hh:mma");
        String timeText = startTime + " - " + endTime;
        viewHolder.mealNameTextView.setText(mealName);
        viewHolder.mealTimeTextView.setText(timeText);

    }

    @Override
    public ScheduleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_schedule_row, parent, false);
        return new ScheduleRecyclerAdapter.ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mealNameTextView;
        public TextView mealTimeTextView;

        public ViewHolder(View view) {
            super(view);

            mealNameTextView = (TextView) view.findViewById(R.id.ScheduleFragment_mealNameTextView);
            mealTimeTextView = (TextView) view.findViewById(R.id.ScheduleFragment_mealTimeTextView);
        }
    }

}
