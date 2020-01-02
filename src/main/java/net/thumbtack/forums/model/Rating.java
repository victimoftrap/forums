package net.thumbtack.forums.model;

import java.util.Objects;

public class Rating {
    private User rater;
    private int rating;

    public Rating(User rater, int rating) {
        this.rater = rater;
        this.rating = rating;
    }

    public User getRater() {
        return rater;
    }

    public void setRater(User rater) {
        this.rater = rater;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating)) return false;
        Rating rating1 = (Rating) o;
        return getRating() == rating1.getRating() &&
                Objects.equals(getRater(), rating1.getRater());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRater(), getRating());
    }

    @Override
    public String toString() {
        return "Rating{" +
                "rater=" + rater +
                ", rating=" + rating +
                '}';
    }
}
