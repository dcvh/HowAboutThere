package tcd.android.com.howaboutthere;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ADMIN on 08/05/2017.
 */

public class PlanDetail {
    private String placeName;
    private String placeAddress;
    private String dateTime;
    private String placeLatLng;
    private HashMap<String, Integer> members;

    public PlanDetail(String placeName, String placeAddress, String dateTime, String latLng, HashMap<String, Integer> members) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.dateTime = dateTime;
        this.members = members;
        this.placeLatLng = latLng;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(String placeLatLng) {
        this.placeLatLng = placeLatLng;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public HashMap<String, Integer> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Integer> members) {
        this.members = members;
    }
}
