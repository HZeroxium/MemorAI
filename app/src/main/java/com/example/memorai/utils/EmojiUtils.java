package com.example.memorai.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import com.example.memorai.R;

public class EmojiUtils {
    private static List<String> emojisList = new ArrayList<>();

    public static void init(Context context) {
        if (context == null) {
            Log.e("EmojiUtil", "Context is null, cannot load emojis.");
            return;
        }
        emojisList = getEmojis(context);
    }

    public static List<String> getEmojis(@Nullable Context context) {
        List<String> convertedEmojiList = new ArrayList<>();
        if (context == null) return convertedEmojiList;

        Resources res = context.getResources();
        String[] emojiList = res.getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }

    private static String convertEmoji(String unicode) {
        try {
            return new String(Character.toChars(Integer.parseInt(unicode.replace("u+", ""), 16)));
        } catch (NumberFormatException e) {
            return ""; // Trả về chuỗi rỗng nếu có lỗi
        }
    }

    public static List<String> getEmojisList() {
        return emojisList;
    }
}
