package com.example.demo
import java.io.File
import java.io.PrintWriter
import kotlin.io.path.Path
import kotlin.math.cos
import kotlin.math.sin

fun foo() {
    // Erstelle den Tropfen
    val cx = 100f
    val cy = 100f
    val r1 = 50f
    val r2 = 20f
    val pathData = "M ${cx-r1},$cy " +
            "a $r1,$r1 0 1,0 ${r1*2},0 " +
            "a $r1,$r1 0 1,0 -${r1*2},0 " +
            "M ${cx-r2},${cy-r1+r2} " +
            "a $r2,$r2 0 1,1 ${r2*2},0 " +
            "a $r2,$r2 0 1,1 -${r2*2},0 "

}