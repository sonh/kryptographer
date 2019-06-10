package com.sonhvp.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.sonhvp.kryptographer.Kryptographer
import com.sonhvp.kryptographer.key.spec.AsymmetricKeySpec
import com.sonhvp.kryptographer.key.spec.SymmetricKeySpec
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startTime: Calendar = Calendar.getInstance()
        val endTime: Calendar = Calendar.getInstance().apply { add(Calendar.YEAR, 24) }

        Log.d("cryptographer", "startTime: ${startTime.get(Calendar.YEAR)} \nendTime: ${endTime.get(Calendar.YEAR)}")

        updateUI()

        encrypt_btn.setOnClickListener {
            encrypted_data_tv.text = Kryptographer.getKey(aliases_spn.selectedItem.toString()).encrypt(data_edt.text.toString())
        }
        decrypt_btn.setOnClickListener {
            decrypted_data_tv.text = Kryptographer.getKey(aliases_spn.selectedItem.toString()).decrypt(encrypted_data_tv.text.toString())
        }

        add_symmetric_key_btn.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.add_symmetric_key_hint)
                input { _, text ->
                    Kryptographer.initKeys(this@MainActivity, SymmetricKeySpec(alias = text.toString()))
                    updateUI()
                }
                positiveButton(R.string.add_asymmetric_key)
                negativeButton(R.string.cancel)
            }
        }

        add_asymmetric_key_btn.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.add_asymmetric_key_hint)
                input { _, text ->
                    Kryptographer.initKeys(this@MainActivity, AsymmetricKeySpec(alias = text.toString()))
                    updateUI()
                }
                positiveButton(R.string.add_asymmetric_key)
                negativeButton(R.string.cancel)
            }
        }

        show_all_key_btn.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.key_aliases)
                listItems(items = Kryptographer.getKeyAliases())
                positiveButton(R.string.ok)
            }
        }

        delete_all_key_btn.setOnClickListener {
            MaterialDialog(this).show {
                message(R.string.delete_all_key_warning)
                positiveButton(R.string.ok) {
                    Kryptographer.deleteAllKeys()
                    updateUI()
                }
                negativeButton(R.string.cancel)
            }
        }
    }

    private fun updateUI() {
        aliases_spn.apply {
            adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, Kryptographer.getKeyAliases())
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    encrypted_data_tv.text = ""
                    decrypted_data_tv.text = ""
                }
            }
        }
    }

}