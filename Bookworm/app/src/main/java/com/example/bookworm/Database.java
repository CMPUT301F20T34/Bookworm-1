package com.example.bookworm;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all methods related to reading and writing from the database.
 */
public class Database {
    private static final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private static final Library library = new Library();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference libraryCollection = db.collection("Libraries");
    private static final String libraryName = "Main_Library";
    private static final String bookName = "books";
    private static final String requestName = "requests";
    private static final String userName = "users";
    private static final String TAG = "Sample";

    static Library getLibrary() {
        return library;
    }

    /**
     * Writes a library to the Main_Library database
     * @param lib the library to be written
     */
    static void writeLibrary(Library lib){
        final WriteBatch batch = db.batch();
        ArrayList<Book> books = lib.getBooks();
        ArrayList<User> users = lib.getUsers();
        ArrayList<Request> requests = lib.getRequests();

        CollectionReference bookCollection = libraryCollection.document(libraryName).collection("books");
        CollectionReference userCollection = libraryCollection.document(libraryName).collection("users");
        CollectionReference requestCollection = libraryCollection.document(libraryName).collection("requests");


        for (Book book : books) {
            batch.set(bookCollection.document(), book);
        }

        for (User user : users) {
            batch.set(userCollection.document(), user);
        }

        for (Request request : requests) {
            batch.set(requestCollection.document(), request);
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // These are a method which gets executed when the task is succeeded
                        Log.d(TAG, "Data has been added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // These are a method which gets executed if there’s any problem
                        Log.d(TAG, "Data could not be added!" + e.toString());
                    }
                });
    }

    /**
     * Updates a book in the database or writes a new one if it does not exist yet
     * @param book the book to be written
     * @param returnValue an array containing a single value changed to 1 for success and -1 for failure
     */
    static void writeBook(final Book book, final ArrayList<Integer> returnValue){
        final CollectionReference bookCollection = libraryCollection.document(libraryName).collection("books");
        Task bookTask = bookCollection.whereEqualTo("isbn", book.getIsbn()).get();
        if (returnValue.size() == 0){
            throw new IllegalArgumentException("returnValue must have a value in it.");
        }
        bookTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                //If the book does not exist yet then a new one gets added
                if (querySnapshot.getDocuments().size() == 0) {
                    Map<String, Object> bookInfo = new HashMap<>();
                    bookInfo.put("author", book.getAuthor());
                    bookInfo.put("borrower", book.getBorrower());
                    bookInfo.put( "borrowerId", book.getBorrowerId());
                    bookInfo.put("description", book.getDescription());
                    bookInfo.put("isbn", book.getIsbn());
                    bookInfo.put("owner", book.getOwner());
                    bookInfo.put("ownerId", book.getOwnerId());
                    bookInfo.put("photograph", book.getPhotograph());
                    bookInfo.put("status", book.getStatus());
                    bookInfo.put("title", book.getTitle());
                    bookCollection.add(bookInfo)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                    returnValue.set(0, 1);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                    returnValue.set(0, -1);
                                }
                            });
                } else {
                    // If the book already exist it is updated by its id
                    String bookId = querySnapshot.getDocuments().get(0).getId();
                    bookCollection.document(bookId)
                        .update(
                                "author", book.getAuthor(),
                                "borrower", book.getBorrower(),
                                "borrowerId", book.getBorrowerId(),
                                "description", book.getDescription(),
                                "isbn", book.getIsbn(),
                                "owner", book.getOwner(),
                                "ownerId", book.getOwnerId(),
                                "photograph", book.getPhotograph(),
                                "status", book.getStatus(),
                                "title", book.getTitle()
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                returnValue.set(0, 1);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                                returnValue.set(0, -1);
                            }
                        });
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error querying collection");
                returnValue.set(0, -1);
            }
        });
    }

    /**
     * Deletes a book from the database
     * @param book the book to be deleted
     * @param returnValue an array containing a single value changed to 1 for success and -1 for failure
     */
    static void deleteBook(final Book book, final ArrayList<Integer> returnValue){
        if (returnValue.size() == 0){
            throw new IllegalArgumentException("returnValue must have a value in it.");
        }
        final CollectionReference bookCollection = libraryCollection.document(libraryName).collection("books");
        Task bookTask = bookCollection.whereEqualTo("isbn", book.getIsbn()).get();
        bookTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                //If the book does not exist yet then a new one gets added
                if (querySnapshot.getDocuments().size() == 0) {
                    Log.d(TAG, "Book does not exist in database");
                    returnValue.set(0, 1);
                }
                //If the book already exist it is updated by its id
                else{
                    String bookId = querySnapshot.getDocuments().get(0).getId();
                    bookCollection.document(bookId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    returnValue.set(0, 1);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                    returnValue.set(0, -1);
                                }
                            });
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error querying collection");
                returnValue.set(0, -1);
            }
        });
    }

    /**
     * Returns all books that contain the searchTerm as their exact title.
     * Will rework in the future to return books that contain the searchTerm.
     * @param searchTerm The keyword that is being searched
     * @return Task<QuerySnapshot> The result of the query.
     */
    public static Task<QuerySnapshot> searchBooks(final String searchTerm) {
        CollectionReference books = libraryCollection.document(libraryName)
            .collection(bookName);

        return books.whereEqualTo("title", searchTerm).get();
    }

    /**
     * Finds all the books in which the status matches one of the provided and
     * the keyword given
     * @param statuses An array of statues that the book can match
     * @param keyword The keyword to be searched for
     * @return A task containing a querysnapshot that returns all documents matching the parameters
     */
    static Task<QuerySnapshot> bookKeywordSearch(String[] statuses, String keyword){
        if (statuses.length == 0){
            throw new IllegalArgumentException("statuses cannot be empty");
        }

        Query query = libraryCollection.document(libraryName).collection("books")
                .whereIn("status", Arrays.asList(statuses))
                .whereArrayContains("description", keyword);

        return query.get();
    }

    /**
     * Creates a user in the database with their username,
     * email, and phone number
     * @param username the username of the user
     * @param phoneNumber email of the user
     * @param email phone number of the user
     * @return Task containing the result of the creation
     */
    static Task<Void> createUser(final String username, String phoneNumber, String email) {
        DocumentReference documentReference = libraryCollection
                .document(libraryName)
                .collection("users")
                .document(username);
        Map<String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("phoneNumber", phoneNumber);
        userInfo.put("email", email);
        return documentReference.set(userInfo);

    }

    /**
     * Updates the user in the database
     * @param user The user to update with
     * @param returnValue An arraylist with a value that is changed to -1 for failure and 1 for success
     */
    static void updateUser(final User user, final ArrayList<Integer> returnValue){
        final CollectionReference userCollection = libraryCollection.document(libraryName).collection("users");
        Task userTask = userCollection.document(user.getUsername()).get();
        if (returnValue.size() == 0){
            throw new IllegalArgumentException("returnValue must have a value in it.");
        }
        DocumentReference documentReference = libraryCollection
                .document(libraryName)
                .collection("users")
                .document(user.getUsername());
        Map<String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("phoneNumber", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("borrower", user.getBorrower());
        userInfo.put("owner", user.getOwner());
        documentReference.set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User profile updated");
                        returnValue.set(0,1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to update user profile");
                        returnValue.set(0,-1);
                    }
                });
    }

    /**
     * Deletes a user from the database
     * @param user The user to be deleted
     * @param returnValue An arraylist with a value that is changed to -1 for failure and 1 for success
     */
    static void deleteUser(final User user, final ArrayList<Integer> returnValue){
        if (returnValue.size() == 0){
            throw new IllegalArgumentException("returnValue must have a value in it.");
        }
        libraryCollection.document(libraryName).collection("users")
                .document(user.getUsername())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Book does not exist in database");
                        returnValue.set(0, 1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        returnValue.set(0, -1);
                    }
                });
    }

    /**
     * Uploads an image on the user's phone to be the
     * profile image of that user's account
     * @param targetLoc the db storage location of the image
     * @param userID the id of the logged-in user
     * @param photoUri the Uri representing the image file
     * @return An asynchronous task that finishes when the upload finishes.
     */
    static UploadTask writeProfilePhoto(StorageReference targetLoc, String userID, Uri photoUri) {
        return targetLoc.putFile(photoUri);
    }

    /**
     * Returns the contact info associated with a given username
     * @param username the username of the user
     * @return Task<DocumentSnapshot> A Task containing a DocumentSnapshot with the contact info
     */
    static Task<DocumentSnapshot> getUser(final String username){
        return libraryCollection.document(libraryName).collection("users").document(username).get();
    }

    /**
     * Returns the user from a given email.
     * Used to retrieve the user info from the signed in user
     * @param email the email of the user
     * @return Task<QuerySnapshot>
     */
    static Task<QuerySnapshot> getUserFromEmail(final String email) {
        return libraryCollection.document(libraryName).collection(userName).whereEqualTo("email", email).get();
    }

    /**
     * Checks if a user exists in the database with the given username
     * @param username the username to be checked
     * @return Task<DocumentSnapshot>
     */
    static Task<DocumentSnapshot> userExists(final String username) {
        return libraryCollection.document(libraryName)
                .collection("users").document(username)
                .get();
    }

    static Task<Void> createRequest(final Book book, String username) {
        return libraryCollection.document(libraryName)
            .collection(requestName).document(book.getIsbn() + "-" + username)
            .set(book);
    }

    /**
     * Queries a collection for a field matching a value
     * @param collection the collection to be queried
     * @param fields a list of fields to be looked at
     * @param values a list of values to be checked for
     * @return a Task for a QuerySnapshot that contains zero or more DocumentReferences that can be retrieved by QuerySnapshot.getDocuments()
     */
    static Task<QuerySnapshot> queryCollection(String collection, String[] fields, String[] values){
        if (fields.length != values.length){
            throw new IllegalArgumentException("Size of fields must match size of values");
        }
        if (fields.length == 0){
            throw new IllegalArgumentException("ArrayList cannot be empty");
        }
        CollectionReference queryCollection = libraryCollection.document(libraryName).collection(collection);
        Query query = (Query) queryCollection;
        for (int i = 0; i < fields.length; i++){
            query = query.whereEqualTo(fields[i], values[i]);
        }
        return query.get();
    }

    /**
     * Creates a snapshot listener for the given library so it is updated whenever a change is made to the database
     *
     */
    static void createListener(){
        libraryCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
                // Clear the old list
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                {
                    //Log.d(TAG, String.valueOf(doc.getData()));
                    //Updates the array lists in the library object
                    Library newLibrary = doc.toObject(Library.class);
                    library.setBooks(newLibrary.getBooks());
                    library.setRequests(newLibrary.getRequests());
                    library.setUsers(newLibrary.getUsers());
                }
            }
        });
    }
}
