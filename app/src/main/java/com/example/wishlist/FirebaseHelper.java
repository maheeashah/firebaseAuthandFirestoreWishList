package com.example.wishlist;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * The purpose of this class is to hold ALL the code to communicate with Firebase.  This class
 * will connect with Firebase auth and Firebase firestore.  Each class that needs to verify
 * authentication OR access data from the database will reference a variable of this class and
 * call a method of this class to handle the task.  Essentially this class is like a "gopher" that
 * will go and do whatever the other classes want or need it to do.  This allows us to keep all
 * our other classes clean of the firebase code and also avoid having to update firebase code
 * in many places.  This is MUCH more efficient and less error prone.
 */
public class FirebaseHelper {
    public final String TAG = "Denna";
    private static String uid = null;            // var will be updated for currently signed in user
    //Create 2 instance vars for FirebaseAuth and Firebase firestorw that will give me access to
    //my project on firebase. The json file is what links this pp to that project

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    private ArrayList<WishListItem> myItems = new ArrayList<>();

    public FirebaseHelper() {
        //instantiate mAuth and db by calling the getInstance() method and this will create the
        //connection to the instance of Auth and Firestore to this project
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //we need to connect the data reading to logged in user if applicable
        attachReadDataToUser();
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void attachReadDataToUser() {

    }


    public void addUserToFirestore(String name, String newUID) {
        //Create a new user with their name
        //Use a simple HashMap
        Map<String, Object> user = new HashMap<>();
        user.put("name", name); //adding data that is in the parameter name
                                    // it will be called "name" in the firestore database
        //Add a new document to the collection called users with docID = UID of the
        //authorized user. By passing in new UID a param to document, we are able to tell the document what we want its docID
        //too be equal to

        db.collection("users").document(newUID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, name + "'s user account added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error adding user account", e);
                    }
                });

        }


    public void addData(WishListItem wish) {

    }

    public ArrayList<WishListItem> getWishListItems() {

         return null;
    }
    
    public void editData(WishListItem w) {

    }

    public void deleteData(WishListItem w) {

    }

    public void updateUid(String uid) {

    }

    /* https://www.youtube.com/watch?v=0ofkvm97i0s
    This video is good!!!   Basically he talks about what it means for tasks to be asychronous
    and how you can create an interface and then using that interface pass an object of the interface
    type from a callback method and access it after the callback method.  It also allows you to delay
    certain things from occuring until after the onSuccess is finished.
     */

    private void readData(FirestoreCallback firestoreCallback) {
    //This is necessary to avoid the issues we have ran into with data displaying before we return from the async get method

        //Clear out array list of data
        myItems.clear();
        db.collection("users").document(uid).collection("myWishList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            //iterate through all the items in the query
                            //each item is a document snapshot
                            for (DocumentSnapshot doc: task.getResult()) {
                                WishListItem w = doc.toObject(WishListItem.class);
                                myItems.add(w);
                            }
                            //I am done getting all the data
                            Log.i(TAG, "Success reading all data: " + myItems.toString());
                            firestoreCallback.onCallback(myItems);
                        }
                    }
                });
}

//https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method/48500679#48500679
public interface FirestoreCallback {
        //we use the ArrayList of the data type we are working with in Firestore
    void onCallback(ArrayList<WishListItem>myList);
    }
}

