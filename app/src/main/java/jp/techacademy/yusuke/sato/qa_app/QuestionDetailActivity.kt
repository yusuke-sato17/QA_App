package jp.techacademy.yusuke.sato.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import android.R.drawable

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*



import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    val user = FirebaseAuth.getInstance().currentUser
    var flag = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val mEventListener2 = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            flag = true

            if (flag == true) {
                favoriteButton.text = "済"
            } else {
                favoriteButton.text = "いいね"
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null){
            favoriteButton.visibility = View.GONE
        } else {
            favoriteButton.visibility = View.VISIBLE
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val favoriteRef = dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(mQuestion.questionUid)

        favoriteRef.addChildEventListener(mEventListener2)

        favoriteButton.setOnClickListener {

            if(flag == true){
                favoriteRef.setValue(null)
                flag = false
                favoriteButton.text = "いいね"
            }else{
                val data = HashMap<String, String>()
                data["mGenre"] = mQuestion.genre.toString()
                data["questionUid"] = mQuestion.questionUid

                favoriteRef.setValue(data)
                flag = true
                favoriteButton.text = "済"
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val extras = intent.extras

        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val favoriteRef = dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(mQuestion.questionUid)



        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}














