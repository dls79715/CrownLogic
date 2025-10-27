package com.example.crownlogic

import kotlin.random.Random
import kotlin.math.abs

object Logic {
  fun rows(n:Int) = List(n){ r -> List(n){ c -> r*n + c } }
  fun cols(n:Int) = List(n){ c -> List(n){ r -> r*n + c } }
  fun neighbors(idx:Int, n:Int): List<Int> {
    val r = idx / n; val c = idx % n
    val res = mutableListOf<Int>()
    for (dr in -1..1) for (dc in -1..1) if (dr!=0 || dc!=0) {
      val rr = r+dr; val cc = c+dc
      if (rr in 0 until n && cc in 0 until n) res += rr*n+cc
    }
    return res
  }
  private fun touches(a:Int,b:Int,n:Int):Boolean{
    val r1=a/n; val c1=a%n; val r2=b/n; val c2=b%n
    return kotlin.math.abs(r1-r2)<=1 && kotlin.math.abs(c1-c2)<=1
  }
  fun candidates(s: PuzzleState): BooleanArray {
    val n = s.size
    val queenRows = BooleanArray(n)
    val queenCols = BooleanArray(n)
    val queenReg  = mutableSetOf<Int>()
    val occupied  = mutableSetOf<Int>()
    s.board.forEachIndexed { idx, v ->
      if (v==2) {
        occupied += idx
        queenRows[idx/n]=true
        queenCols[idx%n]=true
        queenReg += s.regions[idx]
      }
    }
    val arr = BooleanArray(n*n)
    for (i in 0 until n*n) {
      if (s.board[i]!=0) { arr[i]=false; continue }
      val r=i/n; val c=i%n; val reg = s.regions[i]
      if (queenRows[r] || queenCols[c] || queenReg.contains(reg)) { arr[i]=false; continue }
      val bad = occupied.any { touches(it,i,n) }
      arr[i] = !bad
    }
    return arr
  }

  fun forcedXBy2x2(s:PuzzleState): List<Int> {
    val n=s.size
    val out = mutableListOf<Int>();
    for (r in 0 until n-1) for (c in 0 until n-1) {
      val ids = listOf(r*n+c, r*n+c+1, (r+1)*n+c, (r+1)*n+c+1)
      if (ids.count { s.board[it]==2 } >= 1) {
        ids.filter { s.board[it]==0 }.forEach { out += it }
      }
    }
    return out
  }

  fun claimingElims(s:PuzzleState, cand:BooleanArray): Set<Int> {
    val n=s.size
    val out = mutableSetOf<Int>()
    val byRegion = (0 until n*n).groupBy { s.regions[it] }
    for ((reg, cells) in byRegion) {
      val C = cells.filter { cand[it] }
      if (C.isEmpty()) continue
      val rows = C.map { it/n }.toSet()
      val cols = C.map { it%n }.toSet()
      if (rows.size==1) {
        val r = rows.first()
        for (c in 0 until n) {
          val idx = r*n+c
          if (idx !in cells && cand[idx] && s.board[idx]==0) out += idx
        }
      }
      if (cols.size==1) {
        val c = cols.first()
        for (r in 0 until n) {
          val idx = r*n+c
          if (idx !in cells && cand[idx] && s.board[idx]==0) out += idx
        }
      }
    }
    return out
  }
}

class PuzzleGenerator(private val n:Int, private val difficulty:Int, private val rng: Random) {
  private fun buildRegions(): List<Int> {
    val reg = IntArray(n*n){-1}
    val k = (n*n / (n+2)).coerceIn(4, 9)
    fun neighbors(idx:Int): List<Int> {
      val r=idx/n; val c=idx%n
      return listOf(r-1 to c, r+1 to c, r to c-1, r to c+1)
        .filter { it.first in 0 until n && it.second in 0 until n }
        .map { it.first*n+it.second }
    }
    val seeds = (0 until n*n).shuffled(rng).take(k)
    val fronts = seeds.map { mutableListOf(it) }.toMutableList()
    seeds.forEachIndexed { i,s -> reg[s]=i }
    val targets = List(k){ (n*n)/k }
    while (reg.any { it==-1 }) {
      for (i in 0 until k) {
        val f = fronts[i]; if (f.isEmpty()) continue
        val from = f[rng.nextInt(f.size)]
        for (nb in neighbors(from).shuffled(rng)) {
          if (reg[nb]==-1) { reg[nb]=i; f += nb; if (reg.count{it==i}>=targets[i]) break }
        }
      }
      if (reg.any { it==-1 }) {
        val u = reg.indexOfFirst { it==-1 }
        val nb = neighbors(u).firstOrNull { reg[it]!=-1 } ?: continue
        reg[u]=reg[nb]
      }
    }
    return reg.toList()
  }

  private fun touches(a:Int,b:Int):Boolean {
    val r1=a/n; val c1=a%n; val r2=b/n; val c2=b%n
    return kotlin.math.abs(r1-r2)<=1 && kotlin.math.abs(c1-c2)<=1
  }

  fun generate(): Pair<List<Boolean>, List<Int>> {
    val regions = buildRegions()
    val usedRows = BooleanArray(n)
    val usedCols = BooleanArray(n)
    val usedReg  = BooleanArray(regions.max()+1)
    val queens = BooleanArray(n*n)

    fun backtrack(row:Int):Boolean {
      if (row==n) return true
      val cols = (0 until n).shuffled(rng)
      for (c in cols) {
        val i = row*n+c
        val g = regions[i]
        if (usedRows[row] || usedCols[c] || usedReg[g]) continue
        val conflict = queens.indices.any { queens[it] && touches(it,i) }
        if (conflict) continue
        queens[i]=true; usedRows[row]=true; usedCols[c]=true; usedReg[g]=true
        if (backtrack(row+1)) return true
        queens[i]=false; usedRows[row]=false; usedCols[c]=false; usedReg[g]=false
      }
      return false
    }
    require(backtrack(0)) { "Generation failed" }
    return Pair(queens.toList(), regions)
  }
}
