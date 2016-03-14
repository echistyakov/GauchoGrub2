package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Menu;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
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
    private String mealName = "";
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);
        diningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON,
                sharedPreferences.getString(getString(R.string.pref_key_default_dining_common),
                        getString(R.string.DLG)));
        int savedDateInMillis = 0;
        // Attempt to restore date and mealName from savedInstanceState, if it exists
        if(savedInstanceState != null) {
            savedDateInMillis = savedInstanceState.getInt(STATE_DISPLAY_DATE, 0);
            if(savedDateInMillis == 0)
                displayDate = DateTime.now();
            else {
                displayDate = new DateTime(savedDateInMillis);
            }
            mealName = savedInstanceState.getString(STATE_MEAL_NAME, firstMealOffered(diningCommon));
        } else {
            displayDate = DateTime.now();
            mealName = firstMealOffered(diningCommon);
        }

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
        generateMealButtons();
        buttonToday.setBackgroundColor(Color.LTGRAY);
        setRecyclerAdapter(diningCommon, displayDate, mealName);
        return view;
    }

    /**
     * To be triggered from outside MenuFragment upon the switching of a diningCommon
     * @param diningCommon
     */
    public void switchDiningCommon(String diningCommon) {
        resetMealButtonBackgrounds();
        this.diningCommon = diningCommon;
        this.mealName = firstMealOffered(diningCommon);
        generateMealButtons();
        setRecyclerAdapter(diningCommon, displayDate, mealName);
    }

    /**
     * setRecyclerAdapter sets a new recyclerAdapter based off the given parameters
     * @param diningCommon the diningCommon whose menu will be viewed
     * @param date the date of the menu which will be viewed
     * @param mealName the name of the meal of the menu which will be viewed
     */
    private void setRecyclerAdapter(String diningCommon, DateTime date, String mealName) {
        // Log.d(LOG_TAG, "Setting new recyclerAdapter for " + diningCommon + " " + date.toString("MM/dd") + " " + mealName);
        // Update member variables
        this.diningCommon = diningCommon;
        this.displayDate = date;
        this.mealName = mealName;
        // Remove current recyclerAdapater
        if(menuRecyclerAdapter != null) {
            recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            menuRecyclerAdapter.close();
            executorService.shutdownNow();
        }
        // Create new adapter an dexecutor
        menuRecyclerAdapter = new MenuRecyclerAdapter(diningCommon, date, mealName, getContext());
        executorService = Executors.newSingleThreadExecutor();
        // Set executor and adapter
        menuRecyclerAdapter.setExecutor(executorService);
        recyclerView.setAdapter(menuRecyclerAdapter);
        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Start async query
        menuRecyclerAdapter.queryAsync();
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

    private String firstMealOffered(String diningCommon) {
        String[] mealNames = getResources().getStringArray(R.array.MenuFragment_mealStrings);
        int diningCommonId = data.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first().getId();
        for(int i = 0; i < mealNames.length; i++) {
            Menu menu = data.select(Menu.class)
                    .join(RepeatedEvent.class).on(Menu.EVENT_ID.eq(RepeatedEvent.ID))
                    .join(DiningCommon.class).on(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonId))
                    .join(Meal.class).on(RepeatedEvent.MEAL_ID.eq(Meal.ID))
                    .where(DiningCommon.NAME.eq(diningCommon)
                            .and(Menu.DATE.eq(displayDate.toLocalDate()))
                            .and(Meal.NAME.eq(mealNames[i]))).get().firstOrNull();
            if(menu != null && !menu.getMenuItems().toList().isEmpty()) {
                return mealNames[i];
            }
        }
        return "";
    }


    /**
     * generateMealButtons() generates the text for and sets the visibility of all meal buttons
     * also, highlights the button of the currently selected meal
     */
    private void generateMealButtons() {
        String[] mealNames = getResources().getStringArray(R.array.MenuFragment_mealStrings);
        String breakfastString = getString(R.string.MenuFragment_breakfast_string);
        String brunchString = getString(R.string.MenuFragment_brunch_string);
        String lunchString = getString(R.string.MenuFragment_lunch_string);
        String dinnerString = getString(R.string.MenuFragment_dinner_string);
        String lateNightString = getString(R.string.MenuFragment_latenight_string);
        ArrayList<String> offeredMeals = new ArrayList<>();
        for(String mealName : mealNames) {
            int diningCommonId = data.select(DiningCommon.class).where(DiningCommon.NAME.eq(diningCommon)).get().first().getId();
            Menu menu = data.select(Menu.class)
                    .join(RepeatedEvent.class).on(Menu.EVENT_ID.eq(RepeatedEvent.ID))
                    .join(DiningCommon.class).on(RepeatedEvent.DINING_COMMON_ID.eq(diningCommonId))
                    .join(Meal.class).on(RepeatedEvent.MEAL_ID.eq(Meal.ID))
                    .where(DiningCommon.NAME.eq(diningCommon)
                            .and(Menu.DATE.eq(displayDate.toLocalDate()))
                            .and(Meal.NAME.eq(mealName))).get().firstOrNull();
            if(menu != null && !menu.getMenuItems().toList().isEmpty()) {
                offeredMeals.add(mealName);
            }
        }
        for(String mealName : offeredMeals) {
            //Log.d(LOG_TAG, mealName);
            if(mealName.equals(breakfastString) || mealName.equals(brunchString)) {
                breakfastButton.setText(mealName);
            }
        }
        //Log.d(LOG_TAG, "MealName = " + mealName);
        if(!offeredMeals.contains(breakfastString) && !offeredMeals.contains(brunchString)) {
            breakfastButton.setVisibility(View.INVISIBLE);
        } else {
            breakfastButton.setVisibility(View.VISIBLE);
            if(mealName.equals(breakfastString) || mealName.equals(brunchString))
                breakfastButton.setBackgroundColor(Color.LTGRAY);
        }
        if(!offeredMeals.contains(lunchString)) {
            lunchButton.setVisibility(View.INVISIBLE);
        } else {
            lunchButton.setVisibility(View.VISIBLE);
            if(mealName.equals(lunchString))
                lunchButton.setBackgroundColor(Color.LTGRAY);
        }
        if(!offeredMeals.contains(dinnerString)) {
            dinnerButton.setVisibility(View.INVISIBLE);
        } else {
            dinnerButton.setVisibility(View.VISIBLE);
            if(mealName.equals(dinnerString))
                dinnerButton.setBackgroundColor(Color.LTGRAY);
        }
        if(!offeredMeals.contains(lateNightString)) {
            lateNightButton.setVisibility(View.INVISIBLE);
        } else {
            lateNightButton.setVisibility(View.VISIBLE);
            if(mealName.equals(lateNightString))
                lateNightButton.setBackgroundColor(Color.LTGRAY);
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
    }

    @OnClick({R.id.MenuFragment_mealButton_breakfast, R.id.MenuFragment_mealButton_lunch, R.id.MenuFragment_mealButton_dinner, R.id.MenuFragment_mealButton_late_night})
    public void handleMealButtonClick(Button button) {
        resetMealButtonBackgrounds();
        button.setBackgroundColor(Color.LTGRAY);
        mealName = button.getText().toString();
        setRecyclerAdapter(diningCommon, displayDate, mealName);
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
