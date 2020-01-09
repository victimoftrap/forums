package net.thumbtack.forums.view;

import java.util.Objects;

public class MessageRatingView {
    private int messageId;
    private boolean isMessage;
    private double rating;
    private int rated;

    public MessageRatingView(int messageId, boolean isMessage, double rating, int rated) {
        this.messageId = messageId;
        this.isMessage = isMessage;
        this.rating = rating;
        this.rated = rated;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public boolean isMessage() {
        return isMessage;
    }

    public void setMessage(boolean message) {
        isMessage = message;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRated() {
        return rated;
    }

    public void setRated(int rated) {
        this.rated = rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageRatingView)) return false;
        MessageRatingView that = (MessageRatingView) o;
        return messageId == that.messageId &&
                isMessage == that.isMessage &&
                Double.compare(that.rating, rating) == 0 &&
                rated == that.rated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, isMessage, rating, rated);
    }

    @Override
    public String toString() {
        return "MessageRatingView{" +
                "messageId=" + messageId +
                ", isMessage=" + isMessage +
                ", rating=" + rating +
                ", rated=" + rated +
                '}';
    }
}
