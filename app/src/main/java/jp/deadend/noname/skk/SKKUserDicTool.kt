package jp.deadend.noname.skk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.CharacterCodingException
import jdbm.RecordManager
import jdbm.RecordManagerFactory
import jdbm.btree.BTree
import jdbm.helper.StringComparator
import jdbm.helper.Tuple
import jp.deadend.noname.dialog.ConfirmationDialogFragment
import jp.deadend.noname.dialog.SimpleMessageDialogFragment
import jp.deadend.noname.skk.databinding.ActivityUserDicToolBinding

class SKKUserDicTool : AppCompatActivity() {
    private lateinit var mRecMan: RecordManager
    private lateinit var mBtree: BTree
    private var isOpened = false
    private var mEntryList = mutableListOf<Tuple>()
    private lateinit var mAdapter: EntryAdapter

    private val importFileActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                openUserDict()

                result.data?.getStringExtra(FileChooser.KEY_FILEPATH)?.let {
                    try {
                        loadFromTextDic(it, mRecMan, mBtree, false)
                    } catch (e: IOException) {
                        if (e is CharacterCodingException) {
                            SimpleMessageDialogFragment.newInstance(
                                getString(R.string.error_text_dic_coding)
                            ).show(supportFragmentManager, "dialog")
                        } else {
                            SimpleMessageDialogFragment.newInstance(
                                getString(R.string.error_file_load, it)
                            ).show(supportFragmentManager, "dialog")
                        }
                    }

                    updateListItems()
                }
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserDicToolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.userDictoolList.emptyView = binding.EmptyListItem
        binding.userDictoolList.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
            val dialog = ConfirmationDialogFragment.newInstance(getString(R.string.message_confirm_remove))
            dialog.setListener(
                object : ConfirmationDialogFragment.Listener {
                    override fun onPositiveClick() {
                        try {
                            mBtree.remove(mEntryList[position].key)
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }

                        mEntryList.removeAt(position)
                        (parent.adapter as EntryAdapter).notifyDataSetChanged()
                    }
                    override fun onNegativeClick() {}
                })
            dialog.show(supportFragmentManager, "dialog")
        }

        val intent = Intent(SKKService.ACTION_COMMAND)
        intent.putExtra(SKKService.KEY_COMMAND, SKKService.COMMAND_COMMIT_USERDIC)
        sendBroadcast(intent)

        mAdapter = EntryAdapter(this, mEntryList)
        binding.userDictoolList.adapter = mAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_dic_tool, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_user_dic_tool_import -> {
                val dir = getExternalFilesDir(null)
                if (dir != null) {
                    val intent = Intent(this@SKKUserDicTool, FileChooser::class.java)
                    intent.putExtra(FileChooser.KEY_MODE, FileChooser.MODE_OPEN)
                    intent.putExtra(FileChooser.KEY_DIRNAME, dir.path)
                    importFileActivity.launch(intent)
                } else {
                    val errorDialog = SimpleMessageDialogFragment.newInstance(
                            getString(R.string.error_open_external_storage)
                    )
                    errorDialog.show(supportFragmentManager, "dialog")
                }
                return true
            }
            R.id.menu_user_dic_tool_export -> {
                val dir = getExternalFilesDir(null)
                if (dir != null) {
                    try {
                        writeToExternalStorage(dir)
                    } catch (e: IOException) {
                        val errorDialog = SimpleMessageDialogFragment.newInstance(
                                getString(R.string.error_write_to_external_storage)
                        )
                        errorDialog.show(supportFragmentManager, "dialog")
                        return true
                    }

                    val msgDialog = SimpleMessageDialogFragment.newInstance(
                            getString(
                                    R.string.message_written_to_external_storage,
                                    dir.path + "/" + getString(R.string.dic_name_user) + ".txt"
                            )
                    )
                    msgDialog.show(supportFragmentManager, "dialog")
                } else {
                    val errorDialog = SimpleMessageDialogFragment.newInstance(
                            getString(R.string.error_open_external_storage)
                    )
                    errorDialog.show(supportFragmentManager, "dialog")
                }
                return true
            }
            R.id.menu_user_dic_tool_clear -> {
                val cfDialog = ConfirmationDialogFragment.newInstance(
                        getString(R.string.message_confirm_clear)
                )
                cfDialog.setListener(
                    object : ConfirmationDialogFragment.Listener {
                        override fun onPositiveClick() { recreateUserDic() }
                        override fun onNegativeClick() {}
                    })
                cfDialog.show(supportFragmentManager, "dialog")
                return true
            }
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (!isOpened) openUserDict()
    }

    public override fun onPause() {
        closeUserDict()

        super.onPause()
    }

    private fun recreateUserDic() {
        closeUserDict()

        val dicName = getString(R.string.dic_name_user)
        deleteFile("$dicName.db")
        deleteFile("$dicName.lg")

        try {
            mRecMan = RecordManagerFactory.createRecordManager(filesDir.absolutePath + "/" + dicName)
            mBtree = BTree.createInstance(mRecMan, StringComparator())
            mRecMan.setNamedObject(getString(R.string.btree_name), mBtree.recid)
            mRecMan.commit()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        dlog("New user dictionary created")
        isOpened = true
        mEntryList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun onFailToOpenUserDict() {
        val dialog = ConfirmationDialogFragment.newInstance(getString(R.string.error_open_user_dic))
        dialog.setListener(
            object : ConfirmationDialogFragment.Listener {
                override fun onPositiveClick() {
                    val dir = getExternalFilesDir(null)
                    dir?.let {
                        try {
                            writeToExternalStorage(dir)
                        } catch (e: IOException) {
                            dlog("onFailToOpenUserDict(): $e")
                        }
                    }

                    recreateUserDic()
                }
                override fun onNegativeClick() { finish() }
            })
        dialog.show(supportFragmentManager, "dialog")

    }

    private fun openUserDict() {
        val recID: Long?
        try {
            mRecMan = RecordManagerFactory.createRecordManager(
                    filesDir.absolutePath + "/" + getString(R.string.dic_name_user)
            )
            recID = mRecMan.getNamedObject(getString(R.string.btree_name))
        } catch (e: IOException) {
            onFailToOpenUserDict()
            return
        }

        if (recID == 0L) {
            onFailToOpenUserDict()
            return
        } else {
            try {
                mBtree = BTree.load(mRecMan, recID)
            } catch (e: IOException) {
                onFailToOpenUserDict()
                return
            }

            isOpened = true
        }

        updateListItems()
    }

    private fun closeUserDict() {
        if (!isOpened) return
        try {
            mRecMan.commit()
            mRecMan.close()
            isOpened = false
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun updateListItems() {
        if (!isOpened) {
            mEntryList.clear()
            mAdapter.notifyDataSetChanged()
            return
        }

        val tuple = Tuple()

        mEntryList.clear()
        try {
            val browser = mBtree.browse()
            if (browser == null) {
                onFailToOpenUserDict()
                return
            }

            while (browser.getNext(tuple)) {
                mEntryList.add(Tuple(tuple.key as String, tuple.value as String))
            }
        } catch (e: IOException) {
            onFailToOpenUserDict()
            return
        }

        mAdapter.notifyDataSetChanged()
    }

    @Throws(IOException::class)
    private fun writeToExternalStorage(dir: File) {
        if (!isOpened) return
        val outputFile = File(dir, getString(R.string.dic_name_user) + ".txt")

        val tuple = Tuple()
        val browser = mBtree.browse()
        if (browser == null) {
            onFailToOpenUserDict()
            return
        }

        val bw = BufferedWriter(FileWriter(outputFile), 1024)

        while (browser.getNext(tuple)) {
            bw.write(tuple.key.toString() + " " + tuple.value + "\n")
        }
        bw.flush()
        bw.close()
    }

    private class EntryAdapter(
            context: Context,
            items: List<Tuple>
    ) : ArrayAdapter<Tuple>(context, 0, items) {
        private val mLayoutInflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val tv = convertView
                    ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)

            val item = getItem(position) ?: Tuple("", "")
            (tv as TextView).text = (item.key as String) + "  " + (item.value as String)

            return tv
        }
    }
}
