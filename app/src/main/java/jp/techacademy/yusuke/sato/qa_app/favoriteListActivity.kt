package jp.techacademy.yusuke.sato.qa_app

import java.io.Serializable
import java.util.ArrayList

class favoriteList(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}