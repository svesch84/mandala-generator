package com.example.demo

class PatternGenerator() {

    fun repeatShape(center: Point, shape: Shape, repetitions: Int, distance: Double): CompositeShape {
        val startY = center.y
        val shapes = (0 until repetitions).map { shape.move(Point(center.x, startY - it * distance)) }
        return CompositeShape(shapes)
    }
}