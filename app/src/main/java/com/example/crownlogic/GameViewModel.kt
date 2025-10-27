package com.example.crownlogic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class CellState { EMPTY, X, QUEEN }

@kotlinx.serialization.Serializable
data class PuzzleState(
  val size: Int,
  val regions: List<Int>,
  val board: List<Int>,      // 0=EMPTY,1=X,2=QUEEN
  val solution: List<Boolean>,
  val difficulty: Int,
  val moveStack: List<Int> = emptyList()
)

class GameViewModel(private val repo: StateRepo): ViewModel() {
  var state: PuzzleState? = null; private set
  var message: String? = null; private set

  init { viewModelScope.launch { state = repo.load() ?: newPuzzle(8,1) } }

  fun newPuzzle(size:Int, difficulty:Int): PuzzleState {
    val gen = PuzzleGenerator(size, difficulty, kotlin.random.Random(System.nanoTime()))
    val (solution, regions) = gen.generate()
    val p = PuzzleState(size, regions, List(size*size){0}, solution, difficulty, emptyList())
    viewModelScope.launch { repo.save(p) }
    state = p
    return p
  }

  private fun push(move:Int) {
    state = state?.copy(moveStack = state!!.moveStack + move)
  }

  fun cycleCell(i:Int) {
    state = state?.let { s ->
      val old = s.board[i]
      val next = (old + 1) % 3
      val nb = s.board.toMutableList(); nb[i] = next
      push(i*9 + old*3 + next)
      val ns = s.copy(board = nb)
      viewModelScope.launch { repo.save(ns) }
      ns
    }
  }

  fun setCell(i:Int, value:Int) {
    state = state?.let { s ->
      val old = s.board[i]
      if (old==value) return s
      val nb = s.board.toMutableList(); nb[i]=value
      push(i*9 + old*3 + value)
      val ns = s.copy(board=nb)
      viewModelScope.launch { repo.save(ns) }
      ns
    }
  }

  fun undo() {
    state = state?.let { s ->
      if (s.moveStack.isEmpty()) return s
      val last = s.moveStack.last()
      val idx = last/9; val old = (last%9)/3; val _new = last%3
      val nb = s.board.toMutableList(); nb[idx]=old
      val ns = s.copy(board=nb, moveStack=s.moveStack.dropLast(1))
      viewModelScope.launch { repo.save(ns) }
      ns
    }
  }

  fun clearAll(confirm: Boolean) {
    if (!confirm) return
    state = state?.let { s ->
      val nb = List(s.size*s.size){0}
      val ns = s.copy(board=nb, moveStack=emptyList())
      viewModelScope.launch { repo.save(ns) }
      ns
    }
  }

  fun giveUpAndNext() {
    state?.let { st ->
      state = newPuzzle(nextSize(st), nextDifficulty(st))
    }
  }

  fun quitSave() {
    message = "Progress saved."
  }

  fun hint() {
    val s = state ?: return
    val candidates = Logic.candidates(s)
    val singleRow = Logic.rows(s.size).firstOrNull { row -> row.count { candidates[it] } == 1 }
    if (singleRow != null) {
      val idx = singleRow.first { candidates[it] }
      val toX = Logic.neighbors(idx, s.size).filter { s.board[it]==0 }
      toX.take(3).forEach { setCell(it,1) }
      message = "Hint: That row has only one legal spot. I crossed a few nearby cells."
      return
    }
    val forceX = Logic.forcedXBy2x2(s)
    if (forceX.isNotEmpty()) {
      forceX.take(4).forEach { setCell(it,1) }
      message = "Hint: 2×2 areas can have at most one queen; I crossed a few cells."
      return
    }
    val claimXs = Logic.claimingElims(s, candidates)
    if (claimXs.isNotEmpty()) {
      claimXs.take(4).forEach { setCell(it,1) }
      message = "Hint: Region candidates align on one line; I trimmed other cells on that line."
      return
    }
    val any = (0 until s.size*s.size).firstOrNull { s.board[it]==0 && !candidates[it] }
    if (any != null) { setCell(any,1); message="Hint: That square can’t be a queen."; return }
    message = "No gentle hint available—nice narrowing!"
  }

  private fun nextSize(st:PuzzleState) = if (st.size < 10) st.size + 1 else st.size
  private fun nextDifficulty(st:PuzzleState) = (st.difficulty + 1).coerceAtMost(5)

  companion object {
    fun factory(ctx: Context) = object: ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(StateRepo(ctx)) as T
      }
    }
  }
}
