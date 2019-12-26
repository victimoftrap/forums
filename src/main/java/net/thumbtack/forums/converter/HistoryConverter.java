package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.HistoryItem;

import java.util.List;
import java.util.ArrayList;

public class HistoryConverter {
    public static List<String> historyToBodyHistoryList(final List<HistoryItem> history) {
        if (history == null) {
            return new ArrayList<>();
        }
        final List<String> historyList = new ArrayList<>();
        history.forEach(hist -> historyList.add(hist.getBody()));
        return historyList;
    }
}
