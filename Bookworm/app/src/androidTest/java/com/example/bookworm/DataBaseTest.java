package com.example.bookworm;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertThrows;

/**
 * Test class for MainActivity. All the UI tests are written here.
 * Robotium test framework is used since Firebase requires a Context object which it gets from the Activity
 */
public class DataBaseTest {
    private Solo solo; // Main test class of robotium

    // Mock classes for testing purposes.
    private User mockUser() {
        return new User("Mike", "hunter2", "mike@hotmail.com", "592-441-0235");
    }

    private Book mockBook() {
        return new Book("1984", "George Orwell", mockUser().getUsername(), "123123", "available");
    }

    private Request mockRequest() {
        return new Request(mockBook(), mockUser(), "Status");
    }

    @Rule
    public ActivityTestRule<EditBookActivity> rule =
            new ActivityTestRule<>(EditBookActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Tests methods related to Book objects (writing, updating, querying, deleting)
     */
    @Test
    public void testBookMethods() throws InterruptedException {
        //Writes a new book to the database
        Book testBook = mockBook();
        testBook.setIsbn("1621325");
        Database.writeBookSynchronous(testBook);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(),1);

        //Updates a book in the database
        testBook.setTitle("Animal Farm");
        testBook.setDescription(new ArrayList<String>(List.of("Dystopian", "Political")));
        testBook.setStatus("available");
        Database.writeBookSynchronous(testBook);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);

        //Tests for a correct response with a single field
        Database.queryCollection("books", new String[]{"title"}, new String[]{"Animal Farm"})
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        assertEquals(docs.get(0).get("title"), "Animal Farm");
                    }
                });

        //Tests for a correct response with multiple fields
        Database.queryCollection("books", new String[]{"title", "author"}, new String[]{"Animal Farm", "George Orwell"})
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        assertNotEquals(docs.size(), 0);
                        assertEquals(docs.get(0).get("title"), "Animal Farm");
                    }
                });

        //Tests for querying for a book that doesn't exist
        Database.queryCollection("books", new String[]{"title"}, new String[]{"1984"})
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        assertEquals(docs.size(), 0);
                    }
                });

        //Tests for keyword search
        Database.bookKeywordSearch(new String[]{"available"}, "Political")
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        assertEquals(docs.size(), 0);
                    }
                });

        //Tests the deletion of books
        Database.deleteBookSynchronous(testBook);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);
    }

    /**
     * Tests the writing, editing, and deletion of users
     * @throws InterruptedException Thrown when the thread is interrupted
     * while waiting for a callback to complete
     */
    @Test
    public void testUserMethods() throws InterruptedException {
        //Tests the writing/updating of users
        ArrayList<Integer> callback = new ArrayList<Integer>(Collections.singletonList(1));
        User testUser = mockUser();
        Database.updateUser(testUser);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);

        //Tests the deletion of a user
        Database.deleteUser(testUser);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);
    }

    /**
     * Test methods related to getting & setting requests
     * @throws InterruptedException When the sleeping thread is interrupted
     */
    @Test
    public void testRequestMethods() throws InterruptedException {
        //Writes a new request
        Request testRequest = mockRequest();
        Database.createSynchronousRequest(testRequest);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);

        // Updates a request
        testRequest.setStatus("Accepted"); // uses the same ID, so will overwrite in DB
        Database.createSynchronousRequest(testRequest);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);

        //Deletes a request
        Database.deleteRequest(testRequest);
        while (Database.getListenerSignal() == 0){
            Thread.sleep(100);
        }
        assertEquals(Database.getListenerSignal(), 1);
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
