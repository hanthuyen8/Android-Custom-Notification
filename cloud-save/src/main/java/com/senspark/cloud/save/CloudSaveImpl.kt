package com.senspark.cloud.save

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange

class CloudSaveImpl(
    private val _activity: Activity,
    private val _logger: Logger
) {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    fun start() {
        signInSilently()
    }

    fun pushCloudData(saveName: String, saveData: String, completion: (Boolean) -> Unit) {
        val client = PlayGames.getSnapshotsClient(_activity)

        client.open(saveName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnSuccessListener { task ->
                if (task.isConflict) {
                    _logger.log("snapshot isConflict")
                    completion(false)
                    return@addOnSuccessListener
                }
                val snapshot = task.data
                if (snapshot == null) {
                    _logger.log("snapshot is null")
                    completion(false)
                    return@addOnSuccessListener
                }

                val bytes = saveData.toByteArray()
                snapshot.snapshotContents.writeBytes(bytes)

                val snapshotMetadataChange = SnapshotMetadataChange.Builder()
                    .setDescription("Game save data")
                    .build()
                client.commitAndClose(snapshot, snapshotMetadataChange).addOnSuccessListener {
                    completion(it != null)
                }
            }
    }

    fun getCloudData(saveName: String, completion: (String?) -> Unit) {
        val client = PlayGames.getSnapshotsClient(_activity)
        client.open(saveName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnSuccessListener { task ->
                if (task.isConflict) {
                    _logger.log("snapshot isConflict")
                    completion(null)
                    return@addOnSuccessListener
                }
                val snapshot = task.data
                if (snapshot == null) {
                    _logger.log("snapshot is null")
                    completion(null)
                    return@addOnSuccessListener
                }

                val bytes = snapshot.snapshotContents.readFully()
                val saveData = String(bytes)
                completion(saveData)
            }
    }

    private fun signInSilently() {
        PlayGamesSdk.initialize(_activity)

        val signInOption =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestScopes(Drive.SCOPE_APPFOLDER) // Add the APPFOLDER scope for Snapshot support.
                .build()
        val signInClient: GoogleSignInClient = GoogleSignIn.getClient(_activity, signInOption)
        signInClient.silentSignIn().addOnCompleteListener(
            _activity
        ) { task ->
            if (task.isSuccessful) {
                onConnected(task.result)
            } else {
                // Player will need to sign-in explicitly using via UI
                val signInIntent = signInClient.signInIntent
                _activity.startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun onConnected(googleSignInAccount: GoogleSignInAccount?) {
        if (googleSignInAccount == null) {
            return
        }
        val acc = googleSignInAccount.account
        val id = googleSignInAccount.id
        val email = googleSignInAccount.email
        _logger.log("acc: $acc - id: $id - email: $email")
    }
}