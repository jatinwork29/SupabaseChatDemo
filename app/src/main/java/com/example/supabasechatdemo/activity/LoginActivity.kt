package com.example.supabasechatdemo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.supabasechatdemo.utils.Utils
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.utils.SharedPreferenceHelper
import com.example.supabasechatdemo.data.model.UserModel
import com.example.supabasechatdemo.data.network.SupabaseClient.supabaseClient
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var ivVisible: ImageView
    private lateinit var sharedPref : SharedPreferenceHelper
    private var token = ""
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPref = SharedPreferenceHelper(this@LoginActivity)
        init()
        adListener()
        Utils.initLoader(this)
        getFCMToken()
        isUserLoggedIn()
    }

    private fun adListener() {
        btnLogin.setOnClickListener {
            if (validate()) {
                login(etEmail.text.toString(), etPassword.text.toString())
            }
        }

        ivVisible.setOnClickListener {
            // Manage password visible or not
            managePassword()
        }
    }

    private fun validate(): Boolean {
        // Validate all fields
        if (etEmail.text.toString().isNotEmpty()) {
            if (etPassword.text.toString().isNotEmpty()) {
                return true
            } else {
                Toast.makeText(this@LoginActivity, "Please add password", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@LoginActivity, "Please add email", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun managePassword() {
        isPasswordVisible = !isPasswordVisible

        val transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }

        etPassword.transformationMethod = transformationMethod

        etPassword.setSelection(etPassword.length())

        val drawableRes = if (isPasswordVisible) {
            R.drawable.ic_eye
        } else {
            R.drawable.ic_eye_close
        }

        ivVisible.setImageResource(drawableRes)
    }

    private fun getFCMToken() {
        // Generate FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the new FCM registration token
            token = task.result
            Log.d("FCM", "FCM Token: $token")
        }
    }

    private fun login(email: String, password: String) {
        Utils.startLoader()
        login(this@LoginActivity, email, password)
    }

    private fun login(
        context: Context,
        userEmail: String,
        userPassword: String,
    ) {
        lifecycleScope.launch {
            try {
                // SignIn using supabase
                supabaseClient.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }

                val session = supabaseClient.auth.currentUserOrNull()
                sharedPref.saveStringData("uId", session?.id)
                sharedPref.saveStringData("email", userEmail)
                saveToken()
                updateUserFCM(userEmail)
                Utils.stopLoader()
                Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, UsersActivity::class.java))
                finish()
            } catch (e: Exception) {
                Utils.stopLoader()
                Toast.makeText(context, "Failed to login ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserFCM(email: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            // Update user FCM token
            supabaseClient.from("User").update(
                {
                    UserModel::user_fcm setTo token
                }
            ) {
                filter {
                    UserModel::user_email eq email
                }
            }
        }
    }

    private fun saveToken() {
        lifecycleScope.launch {
            val accessToken = supabaseClient.auth.currentAccessTokenOrNull()
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    private fun getToken(): String? {
        return sharedPref.getStringData("accessToken")
    }

    private fun isUserLoggedIn() {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (!token.isNullOrEmpty()) {
                    startActivity(Intent(this@LoginActivity, UsersActivity::class.java))
                    supabaseClient.auth.retrieveUser(token)
                    supabaseClient.auth.refreshCurrentSession()
                    saveToken()
                    finish()
                }
            } catch (e: RestException) {
                e.printStackTrace()
//                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        ivVisible = findViewById(R.id.iv_visible)
    }
}