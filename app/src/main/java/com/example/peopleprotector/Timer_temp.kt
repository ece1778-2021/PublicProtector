package com.example.peopleprotector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.cmu.pocketsphinx.*
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


class Timer_temp : AppCompatActivity(), RecognitionListener {

    private val KWS_SEARCH = "wakeup"
    private val FORECAST_SEARCH = "forecast"
    private val DIGITS_SEARCH = "digits"
    private val PHONE_SEARCH = "phones"
    private val MENU_SEARCH = "menu"


    /* Keyword we are looking for to activate menu */
    private val KEYPHRASE = "skip the timer"

    /* Used to handle permission request */
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var captions: HashMap<String, String>
    private lateinit var auth: FirebaseAuth
    val time = 30000L
    private lateinit var timer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        var db = Firebase.firestore
        val user = auth.currentUser
        val userID: String = user?.uid ?: "WillNotGetHere"


        val timerVal = intent.getLongExtra("timerVal", 35000L)
        timer = object : CountDownTimer(timerVal, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var timeLeft = millisUntilFinished/1000
                findViewById<TextView>(R.id.timer).text = ("${timeLeft}")
            }

            override fun onFinish() {
                // call here other methods from activity
                move2RedMode()
            }
        }


        // Prepare the data for UI
        captions = HashMap<String, String>()
        captions[KWS_SEARCH] = "Say 'Skip the timer' to activate red mode."
        captions[MENU_SEARCH] = "say something"
        captions[DIGITS_SEARCH] = "one two three four five"
        captions[PHONE_SEARCH] = "phone search"
        captions[FORECAST_SEARCH] = "forecase search"
        setContentView(R.layout.activity_timer)

        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        SetupTask(this).execute()
        /*var activityReference = WeakReference<SpeechToText>(this)
        var assets: Assets = Assets(activityReference.get())
        var assetDir: File = assets.syncAssets()
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(assetDir, "en-us-ptm"))
                .setDictionary(File(assetDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetDir)
                .recognizer
        recognizer.addListener(this)
        recognizer.addKeyphraseSearch("wakeup", "oh mighty computer")
        makeText(this, "aaa", Toast.LENGTH_SHORT).show()*/
        val redModeStart: Button = findViewById(R.id.redModeButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val textTimer: TextView = findViewById(R.id.timer)

        timer.start()


        cancelButton.setOnClickListener{
            timer.cancel()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        redModeStart.setOnClickListener{
            move2RedMode()
        }
    }

    private fun move2RedMode(){
        timer.cancel()
        val red: Intent = Intent(this, AmberMode::class.java)
        startActivity(red)
    }
    private fun move2Timer(){
        val intent: Intent = Intent(this, Timer::class.java)
        startActivity(intent)
    }
    private class SetupTask(activity: Timer_temp)  : AsyncTask<Void, Void, java.lang.Exception>() {
        var activityReference: WeakReference<Timer_temp> = WeakReference<Timer_temp>(activity)
        protected override fun doInBackground(vararg params: Void): java.lang.Exception? {
            try {
                val assets = Assets(activityReference.get())
                val assetDir = assets.syncAssets()
                activityReference.get()?.setupRecognizer(assetDir)
            } catch (e: IOException) {
                return e
            }
            return null
        }

        override fun onPostExecute(result: java.lang.Exception?) {
            if (result != null) {
                (activityReference.get()?.findViewById(R.id.text) as TextView).text = "Failed to init recognizer $result"
            } else {
                activityReference.get()?.switchSearch("wakeup")
            }
        }

    }

    override fun onResult(hypothesis: Hypothesis) {
        findViewById<TextView>(R.id.text).setText("")
        if(hypothesis != null) {
            var text = hypothesis.hypstr
            //makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (recognizer != null) {
            recognizer.cancel()
            recognizer.shutdown()
        }
    }

    /*override fun onPartialResult(hypothesis: Hypothesis) {
        if(hypothesis == null) return

        var text = hypothesis.hypstr
        if(text.equals("oh mighty computer")) {
            switchSearch("oh mighty computer")
            makeText(this, "oh mighty computer", Toast.LENGTH_SHORT).show()
        }
        else {
            findViewById<TextView>(R.id.text).setText(text)
            makeText(this, "no", Toast.LENGTH_SHORT).show()
        }

    }*/
    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null) return
        val text = hypothesis.hypstr
        if (text == KEYPHRASE) {
            recognizer.shutdown()
            move2RedMode()
        }
        else (findViewById<View>(R.id.text) as TextView).text = text
    }

    /*private fun switchSearch(searchName: String) {
        recognizer.stop()

        if(searchName.equals("wakeup")) recognizer.startListening(searchName)
        else recognizer.startListening(searchName, 10000)

        val caption = "oh mighty computer"
        makeText(this, "oh mighty computer", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.text).setText(caption)

    }*/
    private fun switchSearch(searchName: String) {
        recognizer.stop()

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName == KWS_SEARCH) recognizer.startListening(searchName)
        else recognizer.startListening(searchName, 10000)
        val caption = captions[searchName]
        (findViewById<View>(R.id.text) as TextView).text = caption
    }

    override fun onTimeout() {
        switchSearch(KWS_SEARCH)
    }

    override fun onBeginningOfSpeech() {
    }

    /*override fun onEndOfSpeech() {
        if(!recognizer.searchName.equals("wakeup")) {
            makeText(this, "fug", Toast.LENGTH_SHORT).show()
            switchSearch("wakeup")
        }
        makeText(this, "aaa", Toast.LENGTH_SHORT).show()
    }*/
    override fun onEndOfSpeech() {
        if (recognizer.searchName != KWS_SEARCH) switchSearch(KWS_SEARCH)
    }

    /*override fun onError(error: Exception) {
        findViewById<TextView>(R.id.text).setText(error.message)
    }*/

    override fun onError(error: java.lang.Exception) {
        (findViewById<View>(R.id.text) as TextView).text = error.message
    }


    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(assetsDir, "en-us-ptm"))
                .setDictionary(File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .recognizer
        recognizer.addListener(this)

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE)

        // Create grammar-based search for selection between demos
        val menuGrammar = File(assetsDir, "menu.gram")
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar)

        // Create grammar-based search for digit recognition
        val digitsGrammar = File(assetsDir, "digits.gram")
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar)

        // Create language model search
        val languageModel = File(assetsDir, "weather.dmp")
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel)

        // Phonetic search
        val phoneticModel = File(assetsDir, "en-phone.dmp")
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel)
    }


}


