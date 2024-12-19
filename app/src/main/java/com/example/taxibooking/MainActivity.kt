import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.taxibooking.R
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.Arrays
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOGIN_REQUEST_CODE = 7171
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var providers: List<AuthUI.IdpConfig>

    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    override fun onStop() {
        if (::firebaseAuth.isInitialized && ::listener.isInitialized) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        super.onStop()
    }

    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(
                {
                    firebaseAuth.addAuthStateListener(listener)
                },
                { throwable ->
                    // Xử lý lỗi nếu xảy ra
                    throwable.printStackTrace()
                }
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {


        // Cấu hình các nhà cung cấp đăng nhập
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(), // Đăng nhập bằng số điện thoại
            AuthUI.IdpConfig.GoogleBuilder().build() // Đăng nhập bằng Google
        )
        // Khởi tạo FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null)
                Toast.makeText(this@MainActivity, "Welcome, ${user.uid}", Toast.LENGTH_SHORT).show()
            else
                showLoginLayout()
        }

    }

    private fun showLoginLayout() {

        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.activity_login)
            .setPhoneButtonId(R.id.btnPhoneSignIn)
            .setGoogleButtonId(R.id.btnGoogleSignIn)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            LOGIN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser

            } else
                Toast.makeText(
                    this@MainActivity,
                    "" + response!!.error!!.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
        }
    }
}