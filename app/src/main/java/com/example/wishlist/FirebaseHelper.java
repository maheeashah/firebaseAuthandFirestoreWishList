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

    public void updateUid(String uid) {
        this.uid = uid;
        Log.i(TAG, "users uid set to: " + uid);

    }

    public ArrayList<WishListItem> getWishListItems() {
        return myItems;
    }

    public void attachReadDataToUser() {
        //this method will do an initial read of database when we log in OR create a user
        if (mAuth.getCurrentUser() != null) {
            //redundant??
            uid = mAuth.getCurrentUser().getUid();
            readData(new FirestoreCallback() {
                @Override
                public void onCallback(ArrayList<WishListItem> myList) {
                    Log.d(TAG, "Inside attachReadDataToUser, onCallback " + myList.toString());
                }
            });
        }
        else {
            Log.d(TAG, "No one logged in");
        }
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
                        Log.w(TAG, "Error adding user account", e);
                    }
                });

        }


    public void addData(WishListItem w) {
        //This is the method that is called from the activity
        //It receives the WishListItem we want to add to firestore
        //This method is overloaded with a private addData method that incorporates
        //the interface

        addData(w, new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<WishListItem> myList) {
                Log.i(TAG, "Inside addData, onCallBack");
            }
        });
    }

        private void addData(WishListItem w, FirestoreCallback firestoreCallback) {
        db.collection("users").document(uid).collection("myWishList")
                .add(w)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //comments
                        db.collection("users").document(uid).collection("myWishList")
                                .document(documentReference.getId())
                                .update("docID", documentReference.getId() );
                        Log.i(TAG, "just added" + w.getItemName());
                        readData(firestoreCallback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "item failed to add ", e);
                    }
                });

    }


    public void editData(WishListItem w) {
        // edit WishListItem w to the database
        // this method is overloaded and incorporates the interface to handle the asynch calls
        editData(w, new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<WishListItem> myList) {
                Log.i(TAG, "Inside editData, onCallback " + myList.toString());
            }
        });
    }
    private void editData(WishListItem w, FirestoreCallback firestoreCallback) {
        String docId = w.getDocID();
        db.collection("users").document(uid).collection("myWishList")
                .document(docId)
                .set(w)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "Success updating document");
                        readData(firestoreCallback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Error updating document", e);
                    }
                });
    }


    public void deleteData(WishListItem w) {
        // delete item w from database
        // this method is overloaded and incorporates the interface to handle the asynch calls
        deleteData(w, new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<WishListItem> myList) {
                Log.i(TAG, "Inside deleteData, onCallBack" + myList.toString());
            }
        });

    }

    public void deleteData(WishListItem w, FirestoreCallback firestoreCallback) {
        // delete item w from database
        String docId = w.getDocID();
        db.collection("users").document(uid).collection("myWishList")
                .document(docId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, w.getItemName() + " successfully deleted");
                        readData(firestoreCallback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Error deleting document", e);
                    }
                });
    }


    /* https://www.youtube.com/watch?v=0ofkvm97i0s
    This video is good!!!   Basically he talks about what it means for tasks to be asychronous
    and how you can create an interface and then using that interface pass an object of the interface
    type from a callback method and access it after the callback method.  It also allows you to delay
    certain things from occurring until after the onSuccess is finished.
     */

    private void readData(FirestoreCallback firestoreCallback) {
        myItems.clear();        // empties the AL so that it can get a fresh copy of data
        db.collection("users").document(uid).collection("myWishList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc: task.getResult()) {
                                WishListItem wishListItem = doc.toObject(WishListItem.class);
                                myItems.add(wishListItem);
                            }

                            Log.i(TAG, "Success reading data: "+ myItems.toString());
                            firestoreCallback.onCallback(myItems);
                        }
                        else {
                            Log.d(TAG, "Error getting documents: " + task.getException());
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

