package cs1302.api;

import cs1302.api.Place;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a response from the Zippopotam.us API. This class is used by Gson
 * to create an object from the JSON response body. It contains the latitude and
 * longitude fields.
 */
public class ZipResponse {

    private Place[] places;

    public double getLatitude() {
        if (places != null && places.length > 0) {
            return places[0].latitude;
        }
        return 0.0;
    }

    public double getLongitude() {
        if (places != null && places.length > 0) {
            return places[0].longitude;
        }
        return 0.0;
    }


} // ZipResponse
