package jp.techacademy.yusuke.sato.qa_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_send.*

import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.HashMap

class QuestionSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {
    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mGenre: Int = 0
    private var mPictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        val extras = intent.extras
        mGenre = extras.getInt("genre")

        title = "質問作成"

        sendButton.setOnClickListener(this)
        imageView.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            val uri = if (data == null || data.data == null) mPictureUri else data.data

            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage =
                Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            imageView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    override fun onClick(v: View?) {
        if (v === imageView) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showChooser()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

                    return
                }
            } else {
                showChooser()
            }
        } else if (v === sendButton) {
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(ContentsPATH).child(mGenre.toString())

            val data = HashMap<String, String>()

            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
            val title = titleText.text.toString()
            val body = bodyText.text.toString()

            if (title.isEmpty()) {
                Snackbar.make(v, "タイトルを入力してください", Snackbar.LENGTH_LONG).show()
                return
            }

            if (body.isEmpty()) {
                Snackbar.make(v, "質問を入力してください", Snackbar.LENGTH_LONG).show()
                return
            }

            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")

            data["title"] = title
            data["body"] = body
            data["name"] = name

            val drawable = imageView.drawable as? BitmapDrawable

            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }

            genreRef.push().setValue(data, this)
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showChooser()
                }
                return
            }
        }
    }

    private fun showChooser() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }

}



















