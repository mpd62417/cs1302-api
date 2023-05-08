package cs1302.api;

/**
 * Represents a response from the Wikipedia API. This class is used by Gson
 * to create an object from the JSON response body. It is used to access
 * the fullurl field.
 */
public class WikiResponse {

    Query query;

    /**
     * Method to return the fullurl field from the nested response.
     *
     * @return String the fullurl
     */
    public String getUrl() {
        if (query != null && query.pages != null) {
            Page page = query.pages.values().iterator().next();
            return page.fullUrl;
        }
        return "";
    }
}
