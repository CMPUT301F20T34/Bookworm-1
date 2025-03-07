package com.example.bookworm;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.bookworm.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test class for LoginActivity. All the UI tests are written here.
 * Robotium test framework is used
 */
public class LoginActivityTest {
    private Solo solo; // Main test class of robotium

    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<>(LoginActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fAuth.signOut();
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets the activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }



    /**
     * Ensures that a user that does exist entered with a password
     * that doesn't exist is rejected by the DB
     */
    @Test
    public void wrongPassword() {
        // Somehow get to the correct activity
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);

        // Enter random values
        solo.enterText((EditText) solo.getView(R.id.username_login), "username");
        solo.enterText((EditText) solo.getView(R.id.password_login), Util.getRandomString(100));

        // Confirm the login
        solo.clickOnView(solo.getView(R.id.login_button));

        // Somehow get that the db has rejected the values
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    /**
     * Ensures that a user that doesn't exist entered with a password
     * that doesn't exist is rejected by the DB
     */
    @Test
    public void wrongUsername() {
        // Somehow get to the correct activity
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);

        // Enter random values
        solo.enterText((EditText) solo.getView(R.id.username_login), Util.getRandomString(100));
        solo.enterText((EditText) solo.getView(R.id.password_login), "password");

        // Confirm the login
        solo.clickOnView(solo.getView(R.id.login_button));

        // Somehow get that the db has rejected the values
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    /**
     * Ensures that a user that doesn't exist entered with a password
     * that doesn't exist is rejected by the DB
     */
    @Test
    public void wrongUsernameAndPassword() {
        // Somehow get to the correct activity
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);

        // Enter random values
        solo.enterText((EditText) solo.getView(R.id.username_login), Util.getRandomString(100));
        solo.enterText((EditText) solo.getView(R.id.password_login), Util.getRandomString(100));

        // Confirm the login
        solo.clickOnView(solo.getView(R.id.login_button));

        // Somehow get that the db has rejected the values
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
}
// /**
//     * Ensures that a user that does exist along with the user's
//     * password is confirmed by the database
//     */
//    @Test
//    public void goodUser() {
//        // Somehow get to the correct activity
//        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
//        String user = "alsdjkfa;lsdjfka;sldfjk";
//        String password = "apsdlkfjaspdlfjkaspdlfjk";
//
//        // Enter random values
//        solo.enterText((EditText) solo.getView(R.id.username_login), user);
//        solo.enterText((EditText) solo.getView(R.id.password_login), password);
//
//        // Confirm the login
//        solo.clickOnView(solo.getView(R.id.login_button));
//        // Somehow get that the db has confirmed
//    }