package com.senspark.cloud.save

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class CloudSaveActivity : AppCompatActivity() {
    private lateinit var btnExit: Button
    private lateinit var grpSaveLoad: LinearLayout
    private lateinit var grpConfirm: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button
    private lateinit var btnOk: Button
    private lateinit var lblConfirm: TextView

    private val _logger = Logger(true)
    private val _impl = CloudSaveImpl(this, _logger)

    companion object {
        private const val K_SAVE_NAME = "save1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_save)
        setupUI()
    }

    private fun setupUI() {
        // Cấu trúc Layout:
        /*
        * - btnExit
        * - grpSaveLoad
        *   - btnSave
        *   - btnLoad
        * - grpConfirm
        *   - lblConfirm
        *   - btnYes
        *   - btnNo
        *   - btnOk
        * */
        // Có 2 group: grpSaveLoad & grpConfirm
        // grpSaveLoad sẽ mặc định show up, ngược lại grpConfirm

        btnExit = findViewById(R.id.btnExit)
        grpSaveLoad = findViewById(R.id.grpSaveLoad)
        grpConfirm = findViewById(R.id.grpConfirm)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)
        btnOk = findViewById(R.id.btnOk)
        lblConfirm = findViewById(R.id.lblConfirm)

        btnExit.setOnClickListener {
            closeActivity()
        }

        grpSaveLoad.visibility = View.VISIBLE
        grpConfirm.visibility = View.GONE

        btnSave.setOnClickListener {
            onBtnSaveClicked()
        }

        btnLoad.setOnClickListener {
            onBtnLoadClicked()
        }
    }

    private fun onBtnSaveClicked() {
        turnOnConfirmGroup()
        lblConfirm.text = getString(R.string.confirm_save)
        turnOnYesNoBtn({
            pushToCloud()
        }, {
            turnOnSaveLoadGroup()
        })
    }

    private fun onBtnLoadClicked() {
        turnOnConfirmGroup()
        lblConfirm.text = getString(R.string.confirm_load)
        turnOnYesNoBtn({
            loadFromCloud()
        }, {
            turnOnSaveLoadGroup()
        })
    }

    private fun pushToCloud() {
        lblConfirm.text = getString(R.string.wait)
        hideAllButtons()

        _impl.pushCloudData(K_SAVE_NAME, "Hihi haha") {
            if (it) {
                lblConfirm.text = getString(R.string.success)
                turnOnOkBtn { closeActivity() }
            } else {
                lblConfirm.text = getString(R.string.fail)
                turnOnOkBtn { turnOnSaveLoadGroup() }
            }
        }
    }

    private fun loadFromCloud() {
        lblConfirm.text = getString(R.string.wait)
        hideAllButtons()

        _impl.getCloudData(K_SAVE_NAME) {
            _logger.log("Get cloud data success: $it")
            if (it != null) {
                lblConfirm.text = getString(R.string.success)
                turnOnOkBtn { closeActivity() }
            } else {
                lblConfirm.text = getString(R.string.fail)
                turnOnOkBtn { turnOnSaveLoadGroup() }
            }
        }
    }

    private fun turnOnSaveLoadGroup() {
        grpSaveLoad.visibility = View.VISIBLE
        grpConfirm.visibility = View.GONE

        btnSave.visibility = View.VISIBLE
        btnLoad.visibility = View.VISIBLE
        btnExit.visibility = View.VISIBLE
    }

    private fun turnOnConfirmGroup() {
        grpSaveLoad.visibility = View.GONE
        grpConfirm.visibility = View.VISIBLE

        btnExit.visibility = View.GONE
    }

    private fun hideAllButtons() {
        btnYes.visibility = View.GONE
        btnNo.visibility = View.GONE
        btnOk.visibility = View.GONE
        btnExit.visibility = View.GONE
        btnSave.visibility = View.GONE
        btnLoad.visibility = View.GONE
    }

    private fun turnOnYesNoBtn(onYes: () -> Unit = {}, onNo: () -> Unit = {}) {
        btnYes.visibility = View.VISIBLE
        btnNo.visibility = View.VISIBLE
        btnOk.visibility = View.GONE

        btnYes.setOnClickListener {
            onYes()
        }
        btnNo.setOnClickListener {
            onNo()
        }
    }

    private fun turnOnOkBtn(onClick: () -> Unit = {}) {
        btnYes.visibility = View.GONE
        btnNo.visibility = View.GONE
        btnOk.visibility = View.VISIBLE

        btnOk.setOnClickListener {
            onClick()
        }
    }

    private fun closeActivity() {
        finish()
    }
}