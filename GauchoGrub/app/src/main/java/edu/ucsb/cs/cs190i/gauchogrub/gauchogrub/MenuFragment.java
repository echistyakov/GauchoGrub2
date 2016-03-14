package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MenuItem;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.sql.EntityDataStore;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MenuFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private MenuRecyclerAdapter menuRecyclerAdapter;
    private EntityDataStore<Persistable> data;
    private ExecutorService executorService;
    RecyclerView recyclerView;

    @Bind(R.id.MenuFragment_buttonScrollView)
    HorizontalScrollView buttonScrollView;

    @Bind(R.id.MenuFragment_button_today)
    Button buttonToday;

    @Bind(R.id.MenuFragment_button_tomorrow)
    Button buttonTomorrow;

    @Bind(R.id.MenuFragment_button_2days)
    Button button2days;

    @Bind(R.id.MenuFragment_button_3days)
    Button button3days;

    @Bind(R.id.MenuFragment_button_4days)
    Button button4days;

    @Bind(R.id.MenuFragment_button_5days)
    Button button5days;

    @Bind(R.id.MenuFragment_button_6days)
    Button button6days;

    @Bind(R.id.MenuFragment_mealButton_breakfast)
    Button breakfastButton;

    @Bind(R.id.MenuFragment_mealButton_lunch)
    Button lunchButton;

    @Bind(R.id.MenuFragment_mealButton_dinner)
    Button dinnerButton;

    @Bind(R.id.MenuFragment_mealButton_late_night)
    Button lateNightButton;


    private DateTime displayDate;
    private String mealName;
    private String diningCommon;

    private static final String STATE_DISPLAY_DATE = "STATE_DISPLAY_DATE";
    private static final String STATE_MEAL_NAME = "STATE_MEAL_NAME";

    private final String LOG_TAG = "MenuFragment";
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MenuFragment newInstance(int columnCount) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = ((GGApp) getActivity().getApplication()).getData();
        int savedDateInMillis = 0;
        if(savedInstanceState != null) {
            savedDateInMillis = savedInstanceState.getInt(STATE_DISPLAY_DATE);
            mealName = savedInstanceState.getString(STATE_MEAL_NAME, "");
        }
        if(savedDateInMillis != 0) {
            displayDate = new DateTime(savedDateInMillis);
        } else {
            displayDate = DateTime.now();
        }

        if(mealName == null || mealName.length() == 0) {
            mealName = getString(R.string.MenuFragment_dinner_string);
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);
        diningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON,
                sharedPreferences.getString(getString(R.string.pref_key_default_dining_common),
                        getString(R.string.DLG)));

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.fab.show();
        activity.updateAppBarTitle(getString(R.string.MenuFragment_app_bar_title), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menus, container, false);
        ButterKnife.bind(this, view);
        recyclerView = (RecyclerView) view.findViewById(R.id.MenuFragment_recyclerView);
        setDateButtonsText();
        setMealButtonsText();
        buttonToday.setBackgroundColor(Color.LTGRAY);
        setRecyclerAdapter(diningCommon, displayDate, mealName);
        return view;
    }

    public void updateRecyclerAdapter(String diningCommon) {
        setRecyclerAdapter(diningCommon, displayDate, mealName);
    }

    private void setRecyclerAdapter(String diningCommon, DateTime date, String mealName) {
        Log.d(LOG_TAG, "Setting new recyclerAdapter for " + diningCommon + " " + date.toString("MM/dd") + " " + mealName);
        View view = getActivity().findViewById(android.R.id.content);

        // Use currently set display day
        if(menuRecyclerAdapter != null) {
            recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            menuRecyclerAdapter.close();
            executorService.shutdownNow();
        }
        menuRecyclerAdapter = new MenuRecyclerAdapter(diningCommon, date, mealName, getContext(), view);
        executorService = Executors.newSingleThreadExecutor();
        menuRecyclerAdapter.setExecutor(executorService);
        recyclerView.setAdapter(menuRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuRecyclerAdapter.queryAsync();
        menuRecyclerAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    private void setDateButtonsText() {
        DateTime date = DateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("M/dd");
        buttonToday.setText(date.toString(dateTimeFormatter));
        buttonTomorrow.setText(date.plusDays(1).toString(dateTimeFormatter));
        button2days.setText(date.plusDays(2).toString(dateTimeFormatter));
        button3days.setText(date.plusDays(3).toString(dateTimeFormatter));
        button4days.setText(date.plusDays(4).toString(dateTimeFormatter));
        button5days.setText(date.plusDays(5).toString(dateTimeFormatter));
        button6days.setText(date.plusDays(6).toString(dateTimeFormatter));
    }

    private void setMealButtonsText() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);
        List<Meal> meals =  data.select(Meal.class).join(RepeatedEvent.class).on(Meal.ID.eq(RepeatedEvent.DINING_COMMON_ID))
                .join(DiningCommon.class).on(DiningCommon.ID.eq(RepeatedEvent.DINING_COMMON_ID))
                .where(DiningCommon.NAME.eq(diningCommon)
                        .and(RepeatedEvent.DAY_OF_WEEK.eq(displayDate.getDayOfWeek()))).get().toList();
        ArrayList<String> mealNames = new ArrayList<>();
        for(Meal meal : meals) {
            mealNames.add(meal.getName());
            if(meal.getName().equals(getString(R.string.MenuFragment_breakfast_string)) || meal.getName().equals(getString(R.string.MenuFragment_brunch_string)))
                breakfastButton.setText(meal.getName());
        }
        if(!mealNames.contains(getString(R.string.MenuFragment_breakfast_string)) && !mealNames.contains(getString(R.string.MenuFragment_brunch_string))) {
            breakfastButton.setVisibility(View.INVISIBLE);
        }
        if(!mealNames.contains(getString(R.string.MenuFragment_lunch_string))) {
            lunchButton.setVisibility(View.INVISIBLE);
        }
        if(!mealNames.contains(getString(R.string.MenuFragment_dinner_string))) {
            dinnerButton.setVisibility(View.INVISIBLE);
        }
        if(!mealNames.contains(getString(R.string.MenuFragment_latenight_string))) {
            lateNightButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        menuRecyclerAdapter.queryAsync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        menuRecyclerAdapter.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_DISPLAY_DATE, (int) displayDate.getMillis());
        outState.putString(STATE_MEAL_NAME, mealName);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
    }

    @OnClick({R.id.MenuFragment_button_today, R.id.MenuFragment_button_tomorrow, R.id.MenuFragment_button_2days, R.id.MenuFragment_button_3days, R.id.MenuFragment_button_4days, R.id.MenuFragment_button_5days, R.id.MenuFragment_button_6days})
    public void handleDateButtonClick(Button button) {
        resetDateButtonBackgrounds();
        button.setBackgroundColor(Color.LTGRAY);
        switch(button.getId()) {
            case R.id.MenuFragment_button_today:
                displayDate = DateTime.now();
                break;
            case R.id.MenuFragment_button_tomorrow:
                displayDate = DateTime.now().plusDays(1);
                break;
            case R.id.MenuFragment_button_2days:
                displayDate = DateTime.now().plusDays(2);
                break;
            case R.id.MenuFragment_button_3days:
                displayDate = DateTime.now().plusDays(3);
                break;
            case R.id.MenuFragment_button_4days:
                displayDate = DateTime.now().plusDays(4);
                break;
            case R.id.MenuFragment_button_5days:
                displayDate = DateTime.now().plusDays(5);
                break;
            case R.id.MenuFragment_button_6days:
                displayDate = DateTime.now().plusDays(6);
                break;
        }
        setRecyclerAdapter(diningCommon, displayDate, mealName);
        Snackbar.make(recyclerView, button.getText() + " is now selected", Snackbar.LENGTH_SHORT).show();
    }

    @OnClick({R.id.MenuFragment_mealButton_breakfast, R.id.MenuFragment_mealButton_lunch, R.id.MenuFragment_mealButton_dinner, R.id.MenuFragment_mealButton_late_night})
    public void handleMealButtonClick(Button button) {
        resetMealButtonBackgrounds();
        button.setBackgroundColor(Color.LTGRAY);
        switch(button.getId()) {
            case R.id.MenuFragment_mealButton_breakfast:

                break;
            case R.id.MenuFragment_mealButton_lunch:
                break;
            case R.id.MenuFragment_mealButton_dinner:
                break;
            case R.id.MenuFragment_mealButton_late_night:
                break;
        }
    }

    private void resetDateButtonBackgrounds() {
        buttonToday.setBackgroundColor(Color.WHITE);
        buttonTomorrow.setBackgroundColor(Color.WHITE);
        button2days.setBackgroundColor(Color.WHITE);
        button3days.setBackgroundColor(Color.WHITE);
        button4days.setBackgroundColor(Color.WHITE);
        button5days.setBackgroundColor(Color.WHITE);
        button6days.setBackgroundColor(Color.WHITE);
    }

    private void resetMealButtonBackgrounds() {
        breakfastButton.setBackgroundColor(Color.WHITE);
        lunchButton.setBackgroundColor(Color.WHITE);
        dinnerButton.setBackgroundColor(Color.WHITE);
        lateNightButton.setBackgroundColor(Color.WHITE);
    }
}
