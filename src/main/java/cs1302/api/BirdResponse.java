package cs1302.api;

/**
 * Represents a response from the eBird API. This is used by Gson to
 * create an object from the JSON response body.
 */
public class BirdResponse {
    int resultCount;
    BirdResult[] results;


} // BirdResponse
