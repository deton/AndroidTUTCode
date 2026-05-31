package jp.deadend.noname.skk

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import jp.deadend.noname.skk.databinding.ActivityTextViewBinding
import java.io.File
import java.io.IOException

class TextViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fileName = intent.extras?.getString(getString(R.string.key_text_file_name)) ?: return

        val dir = getExternalFilesDir(null) ?: return
        val file = File(dir, fileName)
        if (file.exists()) {
            val lines = try {
                file.bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                dlog("TextViewActivity: File IO error " + file.absolutePath)
                return
            }
            binding.TextView.text = lines
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return true
    }
}