package bgu.spl181.net.api.users;

import bgu.spl181.net.data.JsonParsable;
import bgu.spl181.net.data.JsonParser;

import java.util.List;

/**
 * Created by avielber on 1/9/18.
 *
 * A class representing a movie
 */
public class Movie implements JsonParsable {

    private final int id;
    private final String name;
    private final int price;
    private final List<String> bannedCountries;
    private final int availableAmount;
    private final int totalAmount;

    public Movie(int id, String name, int price, List<String> bannedCountries, int availableAmount, int totalAmount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.bannedCountries = bannedCountries;
        this.availableAmount = availableAmount;
        this.totalAmount = totalAmount;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public List<String> getBannedCountries() {
        return bannedCountries;
    }

    public int getAvailableAmount() {
        return availableAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toJsonString() {
        JsonParser parser = new JsonParser();
        return  "{" +
                parser.toJsonString("id") + ": " +  parser.toJsonString(getId()) + "," +
                parser.toJsonString("name") + ": " + parser.toJsonString(getName()) + "," +
                parser.toJsonString("price") + ": " +  parser.toJsonString(getPrice()) + "," +
                parser.toJsonString("bannedCountries") + ": " +  parser.stringsToJsonString(getBannedCountries()) + "," +
                parser.toJsonString("availableAmount") + ": " + parser.toJsonString(getAvailableAmount()) + "," +
                parser.toJsonString("totalAmount") + ": "  + parser.toJsonString(getTotalAmount()) +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (! (o instanceof Movie)) {
            return false;
        }

        return ((Movie)o).getId() == getId();
    }
}
