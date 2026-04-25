package com.example.ideagenerator.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ideagenerator.model.Idea;
import com.example.ideagenerator.network.AiApi;
import com.example.ideagenerator.network.AiRequest;
import com.example.ideagenerator.network.AiResponse;
import com.example.ideagenerator.network.RetrofitClient;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class IdeaRepository {

    private static final String TAG = "IdeaRepository";

    private static final String CLIENT_ID = "019dc181-a796-72b5-a9de-babc25a2b24c";
    private static final String CLIENT_SECRET = "fead5a9e-6f9f-4657-b4e7-9b9816d13d13";
    private static final String GIGACHAT_SCOPE = "GIGACHAT_API_PERS";

    private static String accessToken = null;
    private static long tokenExpiresAt = 0;

    private static synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60000) {
            return accessToken;
        }

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) trustAllCerts[0])
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            String basicAuth = android.util.Base64.encodeToString(
                    (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(), android.util.Base64.NO_WRAP);
            Log.d(TAG, "OAuth request - Basic auth: " + basicAuth);

            RequestBody body = new FormBody.Builder()
                    .add("scope", GIGACHAT_SCOPE)
                    .build();

            Log.d(TAG, "OAuth request - URL: https://ngw.devices.sberbank.ru:9443/api/v2/oauth");
            Log.d(TAG, "OAuth request - client_id: " + CLIENT_ID);
            Log.d(TAG, "OAuth request - client_secret: " + CLIENT_SECRET);

            Request request = new Request.Builder()
                    .url("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .addHeader("RqUID", java.util.UUID.randomUUID().toString())
                    .addHeader("Authorization", "Basic " + basicAuth)
                    .post(body)
                    .build();

            Log.d(TAG, "OAuth request built, executing...");

            try (okhttp3.Response response = client.newCall(request).execute()) {
                Log.d(TAG, "OAuth response code: " + response.code());
                Log.d(TAG, "OAuth response message: " + response.message());
                Log.d(TAG, "OAuth response headers: " + response.headers());
                String respBody = response.body() != null ? response.body().string() : "empty";
                Log.d(TAG, "OAuth response body: " + respBody);
                if (response.isSuccessful() && !respBody.isEmpty()) {
                    Log.d(TAG, "OAuth response: " + respBody);
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(respBody).getAsJsonObject();
                    accessToken = json.get("access_token").getAsString();
                    int expiresIn = json.get("expires_at").getAsInt() - 60;
                    tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
                    Log.d(TAG, "Got access token, expires in: " + expiresIn + "s");
                    return accessToken;
                } else {
                    Log.e(TAG, "OAuth failed: " + response.code() + " - " + respBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "OAuth exception", e);
        }
        return null;
    }

    private static final String AI_MODEL = "GigaChat";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final FirebaseFirestore db;
    private final CollectionReference ideasCollection;
    private final AiApi aiApi;

    private final MutableLiveData<List<Idea>> allIdeasLiveData = new MutableLiveData<>();
    private final MutableLiveData<Idea> generatedIdeaLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public IdeaRepository() {
        db = FirebaseFirestore.getInstance();
        ideasCollection = db.collection("ideas");
        aiApi = RetrofitClient.getApi();
        listenToFirestore();
    }

    public LiveData<List<Idea>> getAllIdeas()    { return allIdeasLiveData; }
    public LiveData<Idea> getGeneratedIdea()     { return generatedIdeaLiveData; }
    public LiveData<Boolean> getLoading()         { return loadingLiveData; }
    public LiveData<String> getError()            { return errorLiveData; }

    private void listenToFirestore() {
        ideasCollection
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore listen error", error);
                        errorLiveData.postValue("Ошибка чтения из базы данных");
                        return;
                    }
                    if (snapshots != null) {
                        List<Idea> ideas = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Idea idea = doc.toObject(Idea.class);
                            if (idea != null) {
                                idea.setId(doc.getId());
                                ideas.add(idea);
                            }
                        }
                        allIdeasLiveData.postValue(ideas);
                    }
                });
    }

    public void generateIdea(String category, int difficulty) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(category, difficulty);

        List<AiRequest.Message> messages = Arrays.asList(
                new AiRequest.Message("system", systemPrompt),
                new AiRequest.Message("user", userPrompt)
        );

        AiRequest request = new AiRequest(AI_MODEL, messages, 0.9, 1024);

        executor.execute(() -> {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Ошибка авторизации GigaChat");
                return;
            }

            aiApi.generateIdea("Bearer " + accessToken, request)
                    .enqueue(new Callback<AiResponse>() {
                        @Override
                        public void onResponse(Call<AiResponse> call, retrofit2.Response<AiResponse> response) {
                            loadingLiveData.postValue(false);
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String content = response.body()
                                            .getChoices().get(0).getMessage().getContent();
                                    Idea idea = parseAiResponse(content, category, difficulty);
                                    saveToFirestore(idea);
                                } catch (Exception e) {
                                    Log.e(TAG, "Parse error", e);
                                    errorLiveData.postValue("Ошибка обработки ответа AI");
                                }
                            } else {
                                errorLiveData.postValue("Ошибка генерации. Код: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<AiResponse> call, Throwable t) {
                            loadingLiveData.postValue(false);
                            errorLiveData.postValue("Нет подключения к интернету");
                        }
                    });
        });
    }

    private String buildSystemPrompt() {
        return "You are a creative project idea generator for software developers. " +
                "You MUST respond ONLY with a valid JSON object (no markdown, no extra text). " +
                "The JSON must have exactly these fields:\n" +
                "{\n" +
                "  \"title\": \"Short project name (3-6 words)\",\n" +
                "  \"shortDescription\": \"One sentence summary (max 20 words)\",\n" +
                "  \"fullDescription\": \"Detailed description (3-5 sentences)\",\n" +
                "  \"features\": \"Feature 1, Feature 2, Feature 3, Feature 4, Feature 5\",\n" +
                "  \"technologies\": \"Tech 1, Tech 2, Tech 3, Tech 4\",\n" +
                "  \"estimatedTime\": \"Time estimate, e.g. 2-3 weeks\"\n" +
                "}\n" +
                "All values must be in Russian language. Be creative and unique every time.";
    }

    private String buildUserPrompt(String category, int difficulty) {
        String diffText;
        switch (difficulty) {
            case 1:  diffText = "beginner-friendly, simple, 1-2 weeks"; break;
            case 2:  diffText = "intermediate, some complex features, 2-4 weeks"; break;
            case 3:  diffText = "advanced, complex architecture, 1-2 months"; break;
            default: diffText = "intermediate"; break;
        }
        return "Generate a unique mobile/web app project idea.\n" +
                "Category: " + category + "\n" +
                "Difficulty: " + diffText + "\n" +
                "The idea must be practical, modern, and creative. " +
                "NOT a generic TODO or weather app. Be specific.";
    }

    private Idea parseAiResponse(String raw, String category, int difficulty) {
        String json = raw.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
        }
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start != -1 && end != -1) {
            json = json.substring(start, end + 1);
        }

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        Idea idea = new Idea();
        idea.setTitle(safeGet(obj, "title", "Новая идея"));
        idea.setShortDescription(safeGet(obj, "shortDescription", "Описание"));
        idea.setFullDescription(safeGet(obj, "fullDescription", ""));
        idea.setFeatures(safeGet(obj, "features", ""));
        idea.setTechnologies(safeGet(obj, "technologies", ""));
        idea.setEstimatedTime(safeGet(obj, "estimatedTime", "Не указано"));
        idea.setCategory(category);
        idea.setDifficulty(difficulty);
        idea.setFavorite(false);
        idea.setDateAdded(Timestamp.now());
        return idea;
    }

    private String safeGet(JsonObject obj, String key, String def) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return def;
    }

    private void saveToFirestore(Idea idea) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", idea.getTitle());
        data.put("shortDescription", idea.getShortDescription());
        data.put("fullDescription", idea.getFullDescription());
        data.put("category", idea.getCategory());
        data.put("difficulty", idea.getDifficulty());
        data.put("features", idea.getFeatures());
        data.put("technologies", idea.getTechnologies());
        data.put("estimatedTime", idea.getEstimatedTime());
        data.put("isFavorite", idea.isFavorite());
        data.put("dateAdded", idea.getDateAdded());

        ideasCollection.add(data)
                .addOnSuccessListener(docRef -> {
                    idea.setId(docRef.getId());
                    generatedIdeaLiveData.postValue(idea);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.postValue("Ошибка сохранения в БД");
                });
    }

    public void toggleFavorite(String ideaId, boolean currentStatus) {
        Log.d(TAG, "toggleFavorite: ideaId=" + ideaId + ", currentStatus=" + currentStatus + ", newStatus=" + !currentStatus);
        ideasCollection.document(ideaId)
                .update("isFavorite", !currentStatus)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Favorite updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update favorite", e));
    }

    public void deleteIdea(String ideaId) {
        ideasCollection.document(ideaId).delete();
    }

    public void deleteAllIdeas() {
        ideasCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }
}
