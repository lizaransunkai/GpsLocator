package com.liz.gpslocator.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.liz.gpslocator.data.LoginData
import com.liz.gpslocator.data.LoginDataResponse
import com.liz.gpslocator.helpers.AuthRequests
import com.google.gson.Gson
import com.liz.gpslocator.contexts.MainContext
import com.liz.gpslocator.databinding.ActivitySignInBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SignIn : AppCompatActivity() {

  // Late
  private lateinit var binding: ActivitySignInBinding

  // Contexts
  private val mainContext = MainContext.instance

  private val gson = Gson()
  private val authRequests = AuthRequests()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignInBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    setActivitySettings()
  }

  private fun setActivitySettings () {
    addListeners()
  }

  private fun addListeners () {
    binding.siBtnSignIn.setOnClickListener { startSignIn() }
  }

  private fun getSubmitSignInValues (): LoginData {
    val username = binding.siTilUsername.editText?.text.toString()
    val password = binding.siTilPassword.editText?.text.toString()
    return LoginData(username, password)
  }

  private fun startSignIn () {
    handleComponents(0)
    val call = authRequests.submitLoginData(getSubmitSignInValues())
    call.enqueue(object:Callback {
      override fun onFailure(call: Call, e: IOException) { onErrorSignIn() }
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) onSuccessSignIn(response)
        else onErrorSignIn()
      }
    })
  }

  private fun onErrorSignIn () {
    handleComponents(1)
    runOnUiThread {
      val builder = AlertDialog.Builder(this)
      builder.setTitle("Usuario o Contraseña Incorrecta")
      builder.setMessage("Revisa tu información e inténtalo de nuevo")
      builder.setNeutralButton("Ok") { dialog, which -> dialog.cancel() }
      builder.show()
    }
  }

  private fun onSuccessSignIn(response: Response) {
    handleComponents(1)
    val tokenResult = gson.fromJson(response.body?.string(), LoginDataResponse::class.java)
    mainContext.token = tokenResult.token
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
  }

  private fun handleComponents (state:Int) {
    runOnUiThread {
      when (state) {
        0 -> {
          binding.siTilUsername.isEnabled = false
          binding.siTilPassword.isEnabled = false
          binding.siBtnSignIn.isEnabled = false
        }
        1 -> {
          binding.siTilUsername.isEnabled = true
          binding.siTilPassword.isEnabled = true
          binding.siBtnSignIn.isEnabled = true
        }
      }
    }
  }

}