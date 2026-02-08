package com.example.plate_mate.data.auth.datastore.remote;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class AuthRemoteDataSource {
    private final FirebaseAuth firebaseAuth;

    public AuthRemoteDataSource() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public Completable signIn(String email, String password) {
        return Completable.create(emitter ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> emitter.onComplete())
                        .addOnFailureListener(emitter::onError)
        );
    }
    public Completable signInWithGoogle(String idToken) {
        return Completable.create(emitter -> {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Completable signUp(String email, String password) {
        return Completable.create(emitter ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> emitter.onComplete())
                        .addOnFailureListener(emitter::onError)
        );
    }
    public void logout() {
        firebaseAuth.signOut();
    }
    public Single<Boolean> isUserLoggedIn() {
        return Single.just(firebaseAuth.getCurrentUser() != null);
    }
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
