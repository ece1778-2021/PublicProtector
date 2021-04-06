package com.example.peopleprotector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
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


class SpeechToText : AppCompatActivity(), RecognitionListener {

    private val KWS_SEARCH = "wakeup"
    private val FORECAST_SEARCH = "forecast"
    private val DIGITS_SEARCH = "digits"
    private val PHONE_SEARCH = "phones"
    private val MENU_SEARCH = "menu"
    private var phrase = "start the timer"


    /* Keyword we are looking for to activate menu */
    private val KEYPHRASE = "start the timer"

    /* Used to handle permission request */
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var captions: HashMap<String, String>
    private lateinit var auth: FirebaseAuth
    private var timerVal: Long = 100000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_to_text)

        auth = Firebase.auth
        var db = Firebase.firestore
        val user = auth.currentUser
        val userID: String = user?.uid ?: "WillNotGetHere"
        val movetimer: Button = findViewById(R.id.move2red)
        var res9 = db.collection("users").document(userID)
                .get()
                .addOnSuccessListener {document ->

                    if(document != null) {
                        phrase = (document.data?.get("phrase") as String?).toString()
                        val timerString = (document.data?.get("timer") as String?).toString()
                        (findViewById<View>(R.id.text) as TextView).text = "Say '" + phrase + "' to start the timer."
                        timerVal = timerString.toLong() * 1000
                    }
                }
        // Prepare the data for UI
        captions = HashMap<String, String>()
        captions[KWS_SEARCH] = "Say '" + phrase + "' to start the timer."
        captions[MENU_SEARCH] = "say something"
        captions[DIGITS_SEARCH] = "one two three four five"
        captions[PHONE_SEARCH] = "phone search"
        captions[FORECAST_SEARCH] = "forecase search"

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

        movetimer.setOnClickListener {
            //move2Timer()
            val intent: Intent = Intent(this, Timer_temp::class.java)
            intent.putExtra("timerVal", timerVal)
            startActivity(intent)
        }
    }

    private fun move2Timer(){
        val intent: Intent = Intent(this, Timer_temp::class.java)
        intent.putExtra("timerVal", timerVal)
        startActivity(intent)
    }
    private class SetupTask(activity: SpeechToText)  : AsyncTask<Void, Void, java.lang.Exception>() {
        var activityReference: WeakReference<SpeechToText> = WeakReference<SpeechToText>(activity)
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
        if (text == phrase || text == "oh mighty computer") {
            recognizer.shutdown()
            //move2Timer()
            val intent: Intent = Intent(this, Timer_temp::class.java)
            intent.putExtra("timerVal", timerVal)
            startActivity(intent)
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
        //(findViewById<View>(R.id.text) as TextView).text = caption
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
        recognizer.addKeyphraseSearch(KWS_SEARCH, phrase)

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


