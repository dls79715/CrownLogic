package com.example.crownlogic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.crownlogic.*

@Composable
fun GameScreen(vm: GameViewModel) {
  val st = vm.state
  if (st==null) { CircularProgressIndicator(); return }

  var showClearConfirm by remember { mutableStateOf(false) }

  Scaffold(
    topBar = { CenterAlignedTopAppBar(title = { Text("CrownLogic") }) },
    bottomBar = {
      Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Button(onClick = { vm.undo() }, enabled = st.moveStack.isNotEmpty()) { Text("Undo") }
          Button(onClick = { vm.hint() }) { Text("Hint") }
          Button(onClick = { showClearConfirm = true }) { Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedButton(onClick = { vm.quitSave() }) { Text("Quit (save)") }
          Button(onClick = { vm.giveUpAndNext() }) { Text("Give up & Next") }
        }
        st.message?.let { Text(it, color = Color.Gray) }
      }
    }
  ) { pad ->
    Column(Modifier.padding(pad).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(Modifier.height(8.dp))
      Board(st, onCell = { vm.cycleCell(it) })
    }
  }

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm=false },
      confirmButton = {
        TextButton(onClick={ vm.clearAll(true); showClearConfirm=false }) { Text("Clear") }
      },
      dismissButton = { TextButton(onClick={ showClearConfirm=false }){ Text("Cancel") } },
      title = { Text("Clear board?") },
      text = { Text("This will remove all X’s and queens from the current puzzle.") }
    )
  }
}

@Composable
private fun Board(st: PuzzleState, onCell:(Int)->Unit) {
  val n = st.size
  val colors = listOf(
    Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
    Color(0xFFFFB74D), Color(0xFF90A4AE), Color(0xFFA1887F),
    Color(0xFFBA68C8), Color(0xFFFF8A65), Color(0xFF4DB6AC)
  )
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    for (r in 0 until n) {
      Row {
        for (c in 0 until n) {
          val idx = r*n+c
          val bg = colors[st.regions[idx] % colors.size]
          Cell(st.board[idx], bg, Modifier.size(36.dp).padding(1.dp)) { onCell(idx) }
        }
      }
    }
  }
}

@Composable
private fun Cell(value:Int, bg:Color, modifier:Modifier, onTap:()->Unit) {
  val border = Color.Black
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg.copy(alpha = 0.6f))
      .border(1.dp, border, RoundedCornerShape(4.dp))
      .clickable { onTap() },
    contentAlignment = Alignment.Center
  ) {
    when (value) {
      0 -> {}
      1 -> Text("×", style = MaterialTheme.typography.titleLarge)
      2 -> Text("♛", style = MaterialTheme.typography.titleLarge)
    }
  }
}
