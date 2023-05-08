package cs1302.api;

import cs1302.api.Place;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a response from the Zippopotam.us API. This class is used by Gson
 * to create an object from the JSON response body. It contains the latitude,
 * longitude, and city name fields.
 */
public class ZipResponse {

    private Place[] places;

    /**
     * Getter method for latitude of city.
     *
     * @return double latitude value
     */
    public double getLatitude() {
        if (places != null && places.length > 0) {
            return places[0].latitude;
        } // if
        return 0.0;
    } // getLatitude

    /**
     * Getter method for longitude of city.
     *
     * @return double longitude value
     */
    public double getLongitude() {
        if (places != null && places.length > 0) {
            return places[0].longitude;
        } // if
        return 0.0;
    } // getLongitude

    /**
     * Getter method for city name.
     *
     * @return String the city name
     */
    public String getPlaceName() {
        if (places != null && places.length > 0) {
            return places[0].placeName;
        } // if
        return "";
    } // getPlaceName

} // ZipResponse
