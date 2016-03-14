package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
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

    @Bind(R.id.MenuFragment_recyclerView)
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


    private DateTime displayDate;

    private static final String STATE_DISPLAY_DATE = "STATE_DISPLAY_DATE";

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
        }
        if(savedDateInMillis != 0) {
            displayDate = new DateTime(savedDateInMillis);
        } else {
            displayDate = DateTime.now();
        }

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        MainActivity activity = (MainActivity) getActivity();
        activity.fab.show();
        activity.updateAppBarTitle(getString(R.string.MenuFragment_app_bar_title), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menus, container, false);
        ButterKnife.bind(this, view);
        setDateButtonsText();
        setRecyclerAdapter(displayDate);
        return view;
    }

    private void setRecyclerAdapter(DateTime date) {
        View view = getView();
        // Use currently set display day
        menuRecyclerAdapter = new MenuRecyclerAdapter(date, getContext(), view);
        executorService = Executors.newSingleThreadExecutor();
        menuRecyclerAdapter.setExecutor(executorService);
        recyclerView.setAdapter(menuRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setDateButtonsText() {
        DateTime date = DateTime.now().plusDays(2);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("M/dd");
        button2days.setText(date.toString(dateTimeFormatter));
        button3days.setText(date.plusDays(2).toString(dateTimeFormatter));
        button4days.setText(date.plusDays(3).toString(dateTimeFormatter));
        button5days.setText(date.plusDays(4).toString(dateTimeFormatter));
        button6days.setText(date.plusDays(5).toString(dateTimeFormatter));

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
    public void handleButtonClick(Button view) {
        resetButtonBackgrounds();
        view.setBackgroundColor(Color.CYAN);
        switch(view.getId()) {
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
        menuRecyclerAdapter.close();
        executorService.shutdownNow();
        setRecyclerAdapter(displayDate);
        Snackbar.make(recyclerView, view.getText() + " is now selected", Snackbar.LENGTH_SHORT).show();
    }

    private void resetButtonBackgrounds() {
        buttonToday.setBackgroundColor(Color.WHITE);
        buttonTomorrow.setBackgroundColor(Color.WHITE);
        button2days.setBackgroundColor(Color.WHITE);
        button3days.setBackgroundColor(Color.WHITE);
        button4days.setBackgroundColor(Color.WHITE);
        button5days.setBackgroundColor(Color.WHITE);
        button6days.setBackgroundColor(Color.WHITE);
    }
}
