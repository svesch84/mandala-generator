package com.example.demo

import kotlin.random.Random

class ShapeGenerator {

    fun star(center: Point, count: Int, outerRadius: Double, innerRadius: Double = outerRadius * 0.45): Polygon {
        val angle = 360.0 / count

        val outerCircle = Point(center.x, center.y - outerRadius)
        val innerCircle = Point(center.x, center.y - innerRadius)

        val points = mutableListOf<Point>()
        for (i in 0 until count) {
            points.add(outerCircle.rotate(center, i * angle))
            points.add(innerCircle.rotate(center, i * angle + (angle / 2)))
        }
        points.add(outerCircle)
        return Polygon(points)
    }

    fun flower(center: Point, radius: Double, petalCount: Int, petalWidthFactor: Double = 0.5): CompositeShape {
        val shapes = mutableListOf<Shape>()
        val angle = 360.0 / petalCount
        val petalCenter = center.copy(y = center.y - radius / 2)
        val p = when (Random.nextInt(0, 3)) {
            0 -> drop(petalCenter, radius * petalWidthFactor, radius).rotate(petalCenter, 180.0)
            1 -> drop(petalCenter, radius * petalWidthFactor, radius)
            else -> petal(petalCenter, radius * petalWidthFactor, radius)
        }

        for (i in 0 until petalCount) {
            shapes.add(p.rotate(center, angle * i))
        }
        shapes.add(circle(center, radius * 0.2))
        return CompositeShape(shapes)
    }

    fun diamond(center: Point, width: Double, height: Double, factor: Double = 0.0): Diamond =
        Diamond(center, width, height, factor)

    fun drop(center: Point, width: Double, height: Double): Drop = Drop(center, width, height)

    fun petal(center: Point, width: Double, height: Double): Petal = Petal(center, height, width)

    fun trapeze(
        center: Point,
        height: Double,
        upperWidth: Double,
        lowerWidth: Double,
        radius: Double? = null
    ): Trapeze =
        Trapeze(center, height, lowerWidth, upperWidth)

    fun rectangle(center: Point, height: Double, width: Double, radius: Double? = null): Trapeze =
        Trapeze(center, height, width, width)

    fun ring(center: Point, innerRadius: Double, width: Double): Ring = Ring(center, innerRadius, width)

    fun circle(center: Point, radius: Double): Circle = Circle(center, radius)

    fun halfCircle(center: Point, radius: Double): HalfCircle = HalfCircle(center, radius)

    fun triangle(center: Point, width: Double, height: Double): Polygon = Polygon(
        points = listOf(
            Point(center.x - width / 2, center.y + height / 2),  // left bottom
            Point(center.x, center.y - height / 2),                 // middle top
            Point(center.x + width / 2, center.y + height / 2),  // right bottom
            Point(center.x - width / 2, center.y + height / 2)   // left bottom
        )
    )
}