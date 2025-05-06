package com.example.tictactoeonline

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoeonline.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding
    private var gameModel: GameModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GameData.fetchGameModel()

        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        binding.startGameBtn.setOnClickListener { handleMainButtonClick() }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    private fun handleMainButtonClick() {
        gameModel?.apply {
            when (gameStatus) {
                GameStatus.CREATED, GameStatus.JOINED -> startGame()
                GameStatus.INPROGRESS -> {
                    winner = if (currentPlayer == "X") "O" else "X"
                    gameStatus = GameStatus.FINISHED
                    updateGameData(this)
                }
                GameStatus.FINISHED -> {
                    if(gameId=="-1"){
                        binding.startGameBtn.text="Restart Game"
                        binding.startGameBtn.setOnClickListener{
                            startGame()
                        }
                    }
                }
            }
        } ?: Toast.makeText(this, "Game not initialized", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus == GameStatus.CREATED) {
                Toast.makeText(applicationContext, "Waiting for opponent", Toast.LENGTH_SHORT).show()
                return
            }
            if (gameStatus == GameStatus.FINISHED) {
                Toast.makeText(applicationContext, "Game finished", Toast.LENGTH_SHORT).show()
                return
            }
            if (gameId != "-1" && currentPlayer != GameData.myId) {
                Toast.makeText(applicationContext, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }
            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkWinner()
                updateGameData(this)
            }
        } ?: Toast.makeText(applicationContext, "Game model null", Toast.LENGTH_SHORT).show()
    }

    private fun startGame() {
        gameModel?.apply {
            if (gameStatus == GameStatus.INPROGRESS || gameStatus == GameStatus.FINISHED) return
            updateGameData(
                GameModel(
                    gameId = gameId,
                    gameStatus = GameStatus.INPROGRESS,
                    filledPos = filledPos,
                    currentPlayer = currentPlayer,
                    winner = winner
                )
            )
        }
    }

    private fun setUI() {
        gameModel?.apply {
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]

            binding.gameStatusText.text = when (gameStatus) {
                GameStatus.CREATED -> {
                    binding.startGameBtn.visibility = View.VISIBLE
                    binding.startGameBtn.text = "return to menu"
                    binding.startGameBtn.setOnClickListener {
                        returnMenu()
                    }
                    "Waiting for Opponent\nGame ID: $gameId"
                }
                GameStatus.JOINED -> {
                    binding.startGameBtn.text = "return to menu"
                    binding.startGameBtn.visibility = View.VISIBLE
                    binding.startGameBtn.setOnClickListener {
                        returnMenu()
                    }
                    startGame()
                    "Start the Game!"

                }
                GameStatus.INPROGRESS -> {
                    if(gameId=="-1"||currentPlayer==GameData.myId){
                        binding.startGameBtn.visibility=View.VISIBLE
                    }
                    else{
                        binding.startGameBtn.visibility=View.INVISIBLE
                    }
                    binding.startGameBtn.text = "End Game"
                    binding.startGameBtn.setOnClickListener{
                        gameStatus=GameStatus.FINISHED
                        returnMenu()
                    }
                    if (currentPlayer == GameData.myId){
                        "Your Turn"
                    }
                    else {
                        "$currentPlayer's Turn"
                    }
                }
                GameStatus.FINISHED -> {
                    binding.startGameBtn.visibility = View.VISIBLE
                    binding.startGameBtn.text = "Return to menu"
                    if(gameId=="-1"){
                        binding.startGameBtn.setOnClickListener{
                            returnMenu()
                        }
                    }
                    if (winner.isNotEmpty()) {
                        if (winner == GameData.myId) "You Won!" else "$winner Won!"
                    } else "It's a Draw!"
                }
            }
        }
    }

    private fun checkWinner() {
        val winnerPos = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        gameModel?.apply {
            for (i in winnerPos) {
                if (filledPos[i[0]].isNotEmpty() && filledPos[i[0]] == filledPos[i[1]] && filledPos[i[1]] == filledPos[i[2]]) {
                    winner = filledPos[i[0]]
                    gameStatus = GameStatus.FINISHED
                    updateGameData(this)
                    return
                }
            }
            if (filledPos.none { it.isEmpty() }) {
                gameStatus = GameStatus.FINISHED
                updateGameData(this)
            }
        }
    }
    fun returnMenu(){
        startActivity(Intent(this,MainActivity::class.java))
    }
}