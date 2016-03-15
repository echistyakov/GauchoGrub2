package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.dining_cams;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.activities.MainActivity;

public class DiningCamsFragment extends android.support.v4.app.Fragment implements Runnable {

    private DiningCam currentCam;

    private Handler handler;

    @Bind(R.id.DiningCamsFragment_camImageView)
    public ImageView imageView;

    private final int delay = 10 * 1000;  // Milliseconds (10 seconds)
    private boolean isOn;
    private String diningCommon;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.fab.show();
        activity.updateAppBarTitle(getString(R.string.DiningCamsFragment_app_bar_title), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diningcams, container, false);
        ButterKnife.bind(this, rootView);
        this.handler = new Handler();
        initCam();
        return rootView;
    }

    public void initCam() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), Activity.MODE_PRIVATE);
        diningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON,
                sharedPreferences.getString(getString(R.string.pref_key_default_dining_common),
                        getString(R.string.DLG)));
        List<String> diningCams = Arrays.asList(getResources().getStringArray(R.array.dining_cams));
        setDisplayContent(diningCams.indexOf(diningCommon));
    }

    /**
     *
     * @param index determines which feed to get
     */
    public void setDisplayContent(int index) {
        String[] camUrls = new String[]{DiningCam.CARRILLO, DiningCam.DE_LA_GUERRA, DiningCam.ORTEGA};
        String camUrl = camUrls[index];
        this.currentCam = new DiningCam(camUrl);
        this.startCam();
    }

    /**
     * onPause() is an internally called method overridden to prevent the page
     * from loading data while the screen is off
     */
    @Override
    public void onPause() {
        super.onPause();
        if (this.currentCam != null) {
            this.stopCam();
        }
    }

    /**
     * onResume() restarts the page-loading when the screen is turned back on,
     * or does an initial load as a background check.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (this.currentCam != null) {
            this.startCam();
        }
    }

    /**
     * startCam() updates the image immediately
     */
    private void startCam() {
        this.isOn = true;
        this.handler.post(this); // Update image right now
    }

    /**
     * stopCam() stops the camera from being loaded
     */
    private void stopCam() {
        this.isOn = false;
        this.handler.removeCallbacks(this);
    }

    /**
     * Asynchronous task to load the image on a separate thread
     */
    @Override
    public void run() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... v) {
                return currentCam.getCurrentImage(delay);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                imageView.setImageBitmap(result);
                scheduleCamUpdate();
            }
        }.execute();
    }

    /**
     * scheduleCamUpdate() handles the delay period for the camera refresh
     */
    private void scheduleCamUpdate() {
        if (isOn) {
            handler.postDelayed(this, delay);
        }
    }

    public void switchDiningCommon() {
        MainActivity activity = (MainActivity) getActivity();
        activity.updateAppBarTitle(getString(R.string.DiningCamsFragment_app_bar_title), true);
        initCam();
    }
}