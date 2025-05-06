package com.example.tictactoeonline

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoeonline.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlin.random.Random
import kotlin.random.nextInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.btnOffline.setOnClickListener{
            createOfflineGame()
        }
        binding.btnCreateOnline.setOnClickListener{
            createOnlineGame()
        }
        binding.btnJoinGame.setOnClickListener{
            joinOnlineGame()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun createOfflineGame(){
        GameData.saveGameModel(
            GameModel(gameStatus = GameStatus.JOINED)
        )
        startGame()
    }
    fun startGame(){
        startActivity(Intent(this,GameActivity::class.java))
    }
    fun createOnlineGame(){
        GameData.myId="X"
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId= Random.nextInt(10000..99999).toString()
            )
        )
        startGame()
    }

    fun joinOnlineGame(){
        var gameId=binding.gameIdInput.text.toString()
        if(gameId.isEmpty()){
            binding.gameIdInput.setError("Please enter game ID")
            return
        }
        GameData.myId="O"
        Firebase.firestore.collection("games").document(gameId).get().addOnSuccessListener {
            val model=it?.toObject(GameModel::class.java)
            if(model==null){
                binding.gameIdInput.setError("Invalid Game ID")
            }
            else{
                model.gameStatus=GameStatus.JOINED
                GameData.saveGameModel(model)
                startGame()
            }
        }
    }
}