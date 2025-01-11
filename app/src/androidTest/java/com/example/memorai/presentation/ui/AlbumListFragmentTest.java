//package com.example.memorai.presentation.ui;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import android.view.View;
//
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.espresso.contrib.RecyclerViewActions;
//
//import com.example.memorai.R;
//import com.example.memorai.presentation.ui.activity.MainActivity;
//
//import org.hamcrest.Matcher;
//import org.junit.Rule;
//import org.junit.Test;
//
//public class AlbumListFragmentTest {
//
//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule =
//            new ActivityScenarioRule<>(MainActivity.class);
//
//    @Test
//    public void testRecyclerViewDisplaysAlbums_success() {
//        // Check if the RecyclerView displays the correct title for the first album
//        onView(withId(R.id.rv_album_list))
//                .perform(RecyclerViewActions.scrollToPosition(0))
//                .check(matches(atPositionOnView(0, R.id.tv_album_title, withText("Album 1")))); // Should pass
//    }
//
//    @Test
//    public void testRecyclerViewDisplaysAlbums_failure() {
//        // Intentionally failing test case: Check for an incorrect album title
//        onView(withId(R.id.rv_album_list))
//                .perform(RecyclerViewActions.scrollToPosition(0))
//                .check(matches(atPositionOnView(0, R.id.tv_album_title, withText("Invalid Title")))); // Should fail
//    }
//
//    // Custom matcher to check RecyclerView items
//    private static Matcher<View> atPositionOnView(int position, int viewId, Matcher<View> itemMatcher) {
//        return RecyclerViewMatcher.withRecyclerView(R.id.rv_album_list)
//                .atPositionOnView(position, viewId)
//                .matches(itemMatcher);
//    }
//}
