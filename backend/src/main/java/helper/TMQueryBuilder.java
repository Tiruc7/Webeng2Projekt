package helper;

public class TMQueryBuilder {

    public String buildSearchUrl(String baseUrl, String apiKey, String keyword, String city, int size) {
        String url = baseUrl + "/events.json?apikey=" + apiKey + "&classificationName=music";

        if (keyword != null && !keyword.isBlank()) {
            url += "&keyword=" + keyword;
        }

        if (city != null && !city.isBlank()) {
            url += "&city=" + city;
        }

        if (size > 0) {
            url += "&size=" + size;
        }

        return url;
    }
}
