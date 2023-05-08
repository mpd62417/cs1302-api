package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Class to represent the nested response from the Wikipedia API.
 * It contains the fullurl field.
 */
public class Page {
    @SerializedName("fullurl")
    String fullUrl;

}
