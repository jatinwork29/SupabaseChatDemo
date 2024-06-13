package com.example.supabasechatdemo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supabasechatdemo.utils.Utils
import com.example.supabasechatdemo.utils.Utils.Companion.isOnline
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.utils.SharedPreferenceHelper
import com.example.supabasechatdemo.adapter.ChatAdapter
import com.example.supabasechatdemo.data.model.ChatModel
import com.example.supabasechatdemo.data.model.UserModel
import com.example.supabasechatdemo.data.network.SupabaseClient.supabaseClient
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    companion object {
        const val tableName = "User-Messages"
    }

    private val chatList = ArrayList<ChatModel>()

    private lateinit var tvData: TextView
    private lateinit var etAdd: EditText
    private lateinit var btnLogout: TextView
    private lateinit var rvChat: RecyclerView
    private lateinit var ivSend: ImageView
    private lateinit var sharedPref: SharedPreferenceHelper
    private var uId = ""
    private var email = ""

    private lateinit var adapter: ChatAdapter
    private lateinit var receiverUser: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isOnline = true
        sharedPref = SharedPreferenceHelper(this@ChatActivity)
        uId = sharedPref.getStringData("uId")!!
        email = sharedPref.getStringData("email")!!
        receiverUser = Gson().fromJson(intent.getStringExtra("user"), UserModel::class.java)
        Utils.initLoader(this)

        init()
        adListener()
        fetchDataFromSupabase()

        lifecycleScope.launch {
            realtimeUpdates(lifecycleScope)
        }
    }

    private fun adListener() {
        ivSend.setOnClickListener {
            if (etAdd.text.toString().isNotEmpty()) {
                sendMessage(etAdd.text.toString())
            } else {
                Toast.makeText(this@ChatActivity, "Please add something", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            Utils.startLoader()
            logout(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isOnline = false
    }

    override fun onStop() {
        super.onStop()
        isOnline = false
    }

    private fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        lifecycleScope.launch {
            try {
                supabaseClient.auth.signOut()
                sharedPref.clearPreferences()
                Utils.stopLoader()
                startActivity(Intent(this@ChatActivity, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Utils.stopLoader()
                Toast.makeText(
                    this@ChatActivity,
                    "Facing some issue to logout please try again ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("TAG", "logout: Error ${e.message}")
            }
        }
    }

    private fun sendMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (receiverUser.user_fcm != null) {
                // Trigger notification
                sendNotification(receiverUser.user_fcm!!, message, email)
            }
            supabaseClient.from(tableName).insert(
                ChatModel(
                    message = message,
                    sender_id = uId,
                    receiver_id = receiverUser.user_uid,
                )
            ) {
                select()
                single()
            }.decodeAs<ChatModel>()
        }
        etAdd.setText("")
    }

    private suspend fun getAccessToken(): String? {
        // Generate access token
        val scopes = listOf("https://www.googleapis.com/auth/firebase.messaging")
        var inputStream: InputStream? = null
        return try {
            // Replace `R.raw.service_account` with the actual resource name of your JSON key file
            inputStream = resources.openRawResource(R.raw.service_account_json)
            val googleCredentials = GoogleCredentials.fromStream(inputStream).createScoped(scopes)
            withContext(Dispatchers.IO) {
                googleCredentials.refreshIfExpired()
            }
            googleCredentials.accessToken.tokenValue
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    private suspend fun sendNotification(fcm: String, message: String?, title: String?) {
        val accessToken = getAccessToken()
        if (accessToken == null) {
            println("Failed to get access token")
            return
        }

        val client = OkHttpClient()

        // Prepare the JSON payload
        val json = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", fcm)
                put("notification", JSONObject().apply {
                    put("title", title ?: "No Title")
                    put("body", message ?: "No Message")
                })
            })
        }

        // Prepare request
        val requestBody =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/fcm-demo-81bcf/messages:send") // Replace with your project ID
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("TAG", "Notification Failed to send notification: ${response.message}")
                }
            }
        }
    }

    private fun init() {
        tvData = findViewById(R.id.tv_data)
        etAdd = findViewById(R.id.add_text)
        ivSend = findViewById(R.id.iv_send)
        btnLogout = findViewById(R.id.btn_logout)
        rvChat = findViewById(R.id.rv_chat)

        rvChat.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(chatList, uId)
        rvChat.adapter = adapter
    }

    private fun fetchDataFromSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // fetch data from User-Messages table
                val data = supabaseClient
                    .from(tableName)
                    .select()
                    .decodeList<ChatModel>()

                val userList = filterChats(data, uId, receiverUser.user_uid)

                val processedList = processChatMessages(userList)

                chatList.clear()
                chatList.addAll(processedList)

                if (chatList.size > 0) {
                    tvData.visibility = View.GONE
                    // Process the fetched data on the main thread
                    withContext(Dispatchers.Main) {
                        adapter = ChatAdapter(chatList, uId)
                        rvChat.adapter = adapter
                        if (adapter.itemCount != 0) {
                            rvChat.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    }
                } else {
                    tvData.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Log.e("SupabaseError", "Error fetching data: ${e.message}", e)
                Toast.makeText(
                    this@ChatActivity,
                    "Facing some issue to get messages please try again ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun realtimeUpdates(scope: CoroutineScope) {
        lifecycleScope.launch {
            try {
                // Create channel for realtime updates
                val channel = supabaseClient.channel(tableName)
                val dataFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public")

                dataFlow.onEach {
                    when (it) {
                        is PostgresAction.Delete -> {
                            Log.i("TAG", "realtimeDb: delete ")
                        }

                        is PostgresAction.Insert -> {
                            val stringifiedData = it.record.toString()
                            try {
                                val data = Json.decodeFromString<ChatModel>(stringifiedData)
                                if ((data.sender_id == uId && data.receiver_id == receiverUser.user_uid) ||
                                    (data.sender_id == receiverUser.user_uid && data.receiver_id == uId)
                                ) {
//                                    adapter.addData(data)
//                                    rvChat.smoothScrollToPosition(adapter.itemCount - 1)
                                    if (chatList.isEmpty()) {
                                        fetchDataFromSupabase()
                                    } else {
                                        adapter.addData(data)
                                        rvChat.smoothScrollToPosition(adapter.itemCount - 1)
                                    }
                                }
                            } catch (e: Exception) {
                                // Handle the exception (e.g., log, notify user, etc.)
                                e.printStackTrace()
                            }
                        }

                        is PostgresAction.Select -> {
                            Log.i("TAG", "realtimeDb: Select ")
                        }

                        is PostgresAction.Update -> {
                            Log.i("TAG", "realtimeDb: updated   $it")
                        }
                    }
                }.launchIn(scope)
                // subscribe channel
                channel.subscribe()
            } catch (e: Exception) {
                Log.e("TAG", "realtimeDb: error ${e.message}")
            }
        }
    }

    // Function to filter the list based on the two IDs
    private fun filterChats(chats: List<ChatModel>, id1: String, id2: String): List<ChatModel> {
        return chats.filter { chat ->
            (chat.sender_id == id1 && chat.receiver_id == id2) ||
                    (chat.sender_id == id2 && chat.receiver_id == id1)
        }
    }

    // Function to filter the list base on date
    private fun processChatMessages(chatList: List<ChatModel>): MutableList<ChatModel> {
        // Define a formatter to parse the created_at date string
        val isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Get the system's time zone
        val systemTimeZone = ZoneId.systemDefault()

        // Group messages by the LocalDate extracted from created_at in the system's time zone
        val groupedByDate = chatList.groupBy {
            LocalDateTime.parse(
                (it.created_at).substring(0, 19).replace("T", " "),
                isoDateFormatter
            )
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(systemTimeZone) // Convert to system's time zone
                .toLocalDate() // Extract LocalDate
        }

        // Create a new list to store the final result
        val result = mutableListOf<ChatModel>()

        // Define a date formatter for output
        val outputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Iterate over the grouped data, sorted by date
        for ((date, messages) in groupedByDate.entries.sortedBy { it.key }) {
            // Add a header for each date
            result.add(ChatModel(date = date.format(outputDateFormatter)))

            // Add all messages for the date
            result.addAll(messages)
        }

        return result
    }
}