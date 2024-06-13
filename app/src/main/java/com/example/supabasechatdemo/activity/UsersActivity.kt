package com.example.supabasechatdemo.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supabasechatdemo.utils.Utils
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.utils.SharedPreferenceHelper
import com.example.supabasechatdemo.adapter.UserAdapter
import com.example.supabasechatdemo.data.model.UserModel
import com.example.supabasechatdemo.data.network.SupabaseClient.supabaseClient
import com.google.gson.Gson
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersActivity : AppCompatActivity() {

    companion object {
        const val tableName = "User"
    }

    private lateinit var adapter: UserAdapter
    private val userList = ArrayList<UserModel>()
    private var uId = ""
    private var email = ""

    private lateinit var tvLogout: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var sharedPref : SharedPreferenceHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPref = SharedPreferenceHelper(this@UsersActivity)
        uId = sharedPref.getStringData("uId")!!
        email = sharedPref.getStringData("email")!!

        init()
        adListener()
        Utils.initLoader(this@UsersActivity)
        fetchDataFromSupabase()
        requestPostNotificationPermission()
    }

    private fun adListener() {
        tvLogout.setOnClickListener {
            Utils.startLoader()
            logout(this)
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            // permission granted
        } else {
            Toast.makeText(this@UsersActivity, "Permission required to get notification!", Toast.LENGTH_SHORT).show()
            // permission denied or forever denied
        }
    }

    private fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        lifecycleScope.launch {
            try {
                // Logout Request
                sharedPref.clearPreferences()
                Utils.stopLoader()
                startActivity(Intent(this@UsersActivity, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Utils.stopLoader()
                e.printStackTrace()
                Toast.makeText(this@UsersActivity, "Facing some issue please try again ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        tvLogout = findViewById(R.id.tv_logout)
        rvUsers = findViewById(R.id.rv_users)

        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList) {
            startActivity(Intent(this@UsersActivity, ChatActivity::class.java).putExtra("user", Gson().toJson(it)))
        }
        rvUsers.adapter = adapter
    }

    private fun requestPostNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                // show rationale and then launch launcher to request permission
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun fetchDataFromSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch data from user table
                val data = supabaseClient
                    .from(tableName)
                    .select()
                    .decodeList<UserModel>()

                userList.addAll(data)

                // Process the fetched data on the main thread
                withContext(Dispatchers.Main) {
                    userList.removeIf { it.user_email == email }
                    rvUsers.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("SupabaseError", "Error fetching data: ${e.message}", e)
                Toast.makeText(this@UsersActivity, "Unable to get users list", Toast.LENGTH_SHORT).show()
                // Handle the exception on the main thread
            }
        }
    }
}