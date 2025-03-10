package com.example.bookworm;

import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AcceptDeclineRequestTest {
    private Solo solo;
    private FirebaseAuth fAuth;
    private String title = "dbdkdl";

    /**
     * Signs in with a provided username and password, assumes that the activity
     * is already the login activity.
     * @param username the username credential
     * @param password the password credential
     */
    private void login(String username, String password) {
        solo.enterText((EditText) solo.getView(R.id.username_login), username);
        solo.enterText((EditText) solo.getView(R.id.password_login), password);
        solo.clickOnButton("LOGIN");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    /**
     * Logs out the currently signed-in user. Assumes that user is on the main activity
     */
    private void logout() {
        solo.clickOnView(solo.getView(R.id.profile_button));
        solo.clickOnView(solo.getView(R.id.logout_button));
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    /**
     * Handles all actions related to making the request.
     */
    private void makeRequest() {
        // Enter the title of the book to search
        solo.enterText((EditText) solo.getView(R.id.keywordSearchBar), title);
        solo.clickOnView(solo.getView(R.id.search_button));

        // Ensure that we went to the search activity
        solo.assertCurrentActivity("Wrong Activity", SearchResultsActivity.class);
        solo.clickInRecyclerView(0);
        solo.assertCurrentActivity("Wrong Activity", ViewBookActivity.class);
        solo.clickOnView(solo.getView(R.id.view_book_request));
        solo.assertCurrentActivity("Wrong Activity", SearchResultsActivity.class);
        solo.goBack();
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @Rule
    public ActivityTestRule<LoginActivity> rule =
        new ActivityTestRule<>(LoginActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance,
     * Signs out of firebase and goes to the SignUp activity
     */
    @Before
    public void setUp() {
        fAuth = FirebaseAuth.getInstance();
        fAuth.signOut();
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    /**
     * Tests that declining the request removes it from the database
     */
    @Test
    public void testDeclineRequest() {
        login("pahasa1", "abcdefg");
        makeRequest();
        logout();
        login("pahasa", "abcdefg");
        makeRequest();
        clickOnRequest();
        int views = solo.getCurrentViews().size();

        // Click on the decline button, automatically return
        solo.clickOnView(solo.getView(R.id.accept_decline_request_decline_button));
        solo.assertCurrentActivity("Wrong Activity", ViewRequestsActivity.class);

        // Ensure the request is no longer there
        assertNotEquals(solo.getCurrentViews().size(),  views);
        Database.setBookStatus("123456789", "available");
    }

    /**
     * Tests that accepting a request causes all other requests to be declined
     */
    @Test
    public void testAcceptRequest() {
        login("pahasa1", "abcdefg");
        makeRequest();
        logout();
        login("pahasa", "abcdefg");
        makeRequest();
        clickOnRequest();
        int views = solo.getCurrentViews().size();

        // Click on the decline button, automatically return
        solo.clickOnView(solo.getView(R.id.accept_decline_request_accept_button));
        solo.assertCurrentActivity("Wrong Activity", OwnerMapActivity.class);
        solo.clickOnView(solo.getView(R.id.location_confirm));
        solo.assertCurrentActivity("Wrong Activity", ViewRequestsActivity.class);

        // Ensure all requests are no longer available (accepted or declined)
        assertNotEquals(views, solo.getCurrentViews().size());
        Database.setBookStatus("123456789", "available");
    }

    /**
     * Closes the activity after each test.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    /**
     * Handles all actions related to viewing and clicking on
     * the request previously made.
     */
    private void clickOnRequest() {
        // Go to the book where the requests were made
        solo.clickOnView(solo.getView(R.id.booklist_button));
        solo.assertCurrentActivity("Wrong Activity", OwnerBooklistActivity.class);
        solo.clickOnText(title);

        // Go to the list of requests for the book
        solo.assertCurrentActivity("Wrong Activity", EditBookActivity.class);
        solo.clickOnView(solo.getView(R.id.button10));
        solo.assertCurrentActivity("Wrong Activity", ViewRequestsActivity.class);

        // Click on the request that was just made
        assertTrue(solo.waitForText("pahasa", 1, 1000));
        solo.clickInRecyclerView(0);
        solo.assertCurrentActivity("Wrong Activity", AcceptDeclineRequestActivity.class);

        // Check that it fetches account of person that made request
        assertTrue(solo.waitForText("pahasa", 1, 1000));
        assertTrue(solo.waitForText("psaunder@ualberta.ca", 1, 1000));
        assertTrue(solo.waitForText("7802467244", 1, 1000));
    }
}
