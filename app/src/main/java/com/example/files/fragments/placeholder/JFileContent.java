package com.example.files.fragments.placeholder;

import static com.example.files.MainActivity.instance;

import com.example.files.models.JFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class JFileContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<JFile> ITEMS = new ArrayList<>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, JFile> ITEM_MAP = new HashMap<String, JFile>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPlaceholderItem(i));
        }
    }

    private static void addItem(JFile item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static JFile createPlaceholderItem(int position) {
        return new JFile(String.valueOf(position), "path", instance);
    }

//    private static String makeDetails(int position) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Details about Item: ").append(position);
//        for (int i = 0; i < position; i++) {
//            builder.append("\nMore details information here.");
//        }
//        return builder.toString();
//    }

//    public static class PlaceholderItem {
//        public final String id;
//        public final String content;
//        public final String details;
//
//        public PlaceholderItem(String id, String content, String details) {
//            this.id = id;
//            this.content = content;
//            this.details = details;
//        }
//
//        @Override
//        public String toString() {
//            return content;
//        }
//    }
}