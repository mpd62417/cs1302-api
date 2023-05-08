package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Class to represent the nested response from the Zippopotam.us API.
 * It contains the latitude, longitude, and place name fields.
 */
public class Place {
    double latitude;
    double longitude;

    @SerializedName("place name")
    String placeName;

} // Place
