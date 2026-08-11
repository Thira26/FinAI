package com.example.finai.data;

import com.example.finai.model.TransactionModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.finai.FinAIApp;
import com.example.finai.util.CloudSync;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRepository {

private static FirebaseRepository INSTANCE;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean ready;

public static synchronized FirebaseRepository getInstance() {
        if (INSTANCE == null) INSTANCE = new FirebaseRepository();
        return INSTANCE;
    }

    private FirebaseRepository() {
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            ready = true;
        } catch (IllegalStateException e) {
            ready = false;
        }
    }

    public boolean isReady() { return ready; }

private String uid() { return (auth != null && auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : "guest"; }

public Task<DocumentReference> addTransactionFromSms(String message, TransactionModel parsed) {
        if (!ready || !CloudSync.isEnabled(FinAIApp.get())) return Tasks.forException(new IllegalStateException("Cloud sync disabled"));
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("amount", parsed.amount);
        data.put("merchant", parsed.merchant);
        data.put("category", parsed.category);
        data.put("confidence", parsed.confidence);
        data.put("date", parsed.dateIso);
        data.put("type", parsed.type);
        data.put("aiSource", "sms_regex");
        return db.collection("users").document(uid())
                .collection("transactions").add(data);
    }

}
