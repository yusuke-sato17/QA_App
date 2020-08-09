package jp.techacademy.yusuke.sato.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64
import android.util.Log
import android.widget.ListView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var mFavoriteRef: DatabaseReference? = null

    private var favoritelist = arrayListOf<String>()

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            Log.d("loglog", "おはよう")
            val map = dataSnapshot.value as Map<String, String>
                    val title = map["title"] ?: ""
                    val body = map["body"] ?: ""
                    val name = map["name"] ?: ""
                    val uid = map["uid"] ?: ""
                    val imageString = map["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?:""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)

            if(flag == 0) {
                mQuestionArrayList.add(question)
                Log.d("loglog", title)
                mAdapter.notifyDataSetChanged()
            } else {
                for (item in favoritelist){
                    if (item.equals(dataSnapshot.key)) {
                        mQuestionArrayList.add(question)
                        Log.d("loglog", title)
                        mAdapter.notifyDataSetChanged()
                    }
                }

            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s:String?) {
            val map = dataSnapshot.value as Map<String, String>

            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }



    private val mEventListener2 = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<String, String>

            val questionUid = map["questionUid"] ?: ""
            val genre = map["genre"] ?: ""
            Log.d("loglog", questionUid)

            favoritelist.add(questionUid.toString())

        }
        override fun onChildChanged(dataSnapshot: DataSnapshot, s:String?) {
        }
        override fun onChildRemoved(p0: DataSnapshot) {
        }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }
        override fun onCancelled(p0: DatabaseError) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->

            if (mGenre == 0) {
                Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
            } else {

            }

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {

                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener {parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
    }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    var flag = 0

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val user = FirebaseAuth.getInstance().currentUser

        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
            flag = 0
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
            flag = 0
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
            flag = 0
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
            flag = 0
        } else if (id == R.id.nav_favorite) {
            mToolbar.title = "お気に入り"
            mGenre = 10
            flag = 1

        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }

        if (mGenre == 10) {

            favoritelist.clear()

            mFavoriteRef = mDatabaseReference.child(FavoritesPATH).child(user!!.uid)
            mFavoriteRef!!.addChildEventListener(mEventListener2)

            mGenreRef = mDatabaseReference.child(ContentsPATH).child("1")
            mGenreRef!!.addChildEventListener(mEventListener)

            mGenreRef = mDatabaseReference.child(ContentsPATH).child("2")
            mGenreRef!!.addChildEventListener(mEventListener)

            mGenreRef = mDatabaseReference.child(ContentsPATH).child("3")
            mGenreRef!!.addChildEventListener(mEventListener)

            mGenreRef = mDatabaseReference.child(ContentsPATH).child("4")
            mGenreRef!!.addChildEventListener(mEventListener)

        } else {
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        }
        return true
    }
}
