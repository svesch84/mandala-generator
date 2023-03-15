package com.example.demo

import kotlin.math.cos
import kotlin.math.sin

data class Point(val x: Double, val y: Double) {

    fun move(oldCenter: Point, newCenter: Point): Point =
        Point(x + (newCenter.x - oldCenter.x), y + (newCenter.y - oldCenter.y))

    fun scale(pivot: Point, factor: Double): Point {
        val x = (x - pivot.x) * factor + pivot.x
        val y = (y - pivot.y) * factor + pivot.y
        return Point(x, y)
    }

    fun rotate(pivot: Point, angle: Double): Point {
        val radians = Math.toRadians(angle)
        val newX = pivot.x + (x - pivot.x) * cos(radians) - (y - pivot.y) * sin(radians)
        val newY = pivot.y + (x - pivot.x) * sin(radians) + (y - pivot.y) * cos(radians)
        return Point(newX, newY)
    }
}

data class Bounds(val points: List<Point>)

sealed class Shape() {
    abstract fun rotate(pivot: Point, angle: Double): Shape
    abstract fun scale(pivot: Point, factor: Double): Shape
    abstract fun move(newCenter: Point): Shape
    abstract fun center(): Point
    protected fun center(points: List<Point>): Point {
        val distinctX = points.distinctBy { it.x }
        val distinctY = points.distinctBy { it.y }
        return Point(distinctX.sumOf { it.x } / distinctX.size, distinctY.sumOf { it.y } / distinctY.size)
    }
}

data class Layer(val shapes: MutableList<Shape> = mutableListOf())

sealed class BasicShape() : Shape() {
    abstract fun bounds(): Bounds
}

data class CompositeShape(val shapes: List<Shape>) : Shape() {
    override fun rotate(pivot: Point, angle: Double): Shape = copy(shapes = shapes.map { it.rotate(pivot, angle) })
    override fun scale(pivot: Point, factor: Double): Shape = copy(shapes = shapes.map { it.scale(pivot, factor) })
    override fun center(): Point = center(shapes.map { it.center() })
    override fun move(newCenter: Point): Shape {
        val c = center()
        return copy(shapes = shapes.map {
            val shapeCenter = it.center()
            it.move(Point(shapeCenter.x + newCenter.x - c.x, shapeCenter.y + newCenter.y - c.y))
        })
    }
}

data class Polygon(val points: List<Point>, val fill: Boolean = true) : BasicShape() {
    override fun center(): Point = center(points)
    override fun rotate(pivot: Point, angle: Double): Polygon = copy(points = points.map { it.rotate(pivot, angle) })
    override fun scale(pivot: Point, factor: Double): Polygon = copy(points = points.map { it.scale(pivot, factor) })
    override fun bounds(): Bounds = Bounds(points)

    override fun move(newCenter: Point): Polygon {
        val center = center()
        return copy(points = points.map { it.move(center, newCenter) })
    }
}

data class Circle(val center: Point, val radius: Double) : BasicShape() {
    override fun rotate(pivot: Point, angle: Double): Shape = copy(center = center.rotate(pivot, angle))
    override fun scale(pivot: Point, factor: Double): Shape = copy(radius = radius * factor)
    override fun move(newCenter: Point): Shape = copy(center = newCenter)
    override fun center(): Point = center
    override fun bounds(): Bounds = Bounds(
        listOf(
            center.copy(x = center.x - radius, y = center.y - radius), // left top
            center.copy(x = center.x + radius, y = center.y - radius), // right top
            center.copy(x = center.x + radius, y = center.y + radius), // right bottom
            center.copy(x = center.x - radius, y = center.y + radius), // left bottom
        )
    )
}

data class HalfCircle(
    val center: Point,
    val radius: Double,
    val leftBottom: Point = center.copy(x = center.x - radius),
    val rightBottom: Point = center.copy(x = center.x + radius)
) : BasicShape() {

    override fun rotate(pivot: Point, angle: Double): Shape =
        copy(
            center = center.rotate(pivot, angle),
            leftBottom = leftBottom.rotate(pivot, angle),
            rightBottom = rightBottom.rotate(pivot, angle)
        )

    override fun scale(pivot: Point, factor: Double): Shape = copy(
        radius = radius * factor,
        leftBottom = leftBottom.scale(center, factor),
        rightBottom = rightBottom.scale(center, factor)
    )

    override fun move(newCenter: Point): Shape = copy(
        center = newCenter,
        leftBottom = leftBottom.move(center, newCenter),
        rightBottom = rightBottom.move(center, newCenter)
    )

    override fun center(): Point = center

    override fun bounds(): Bounds = Bounds(
        listOf(
            center.copy(x = center.x - radius), // left top
            center.copy(x = center.x + radius), // right top,
            rightBottom,
            leftBottom
        )
    )
}

data class Ring(
    val center: Point,
    val innerRadius: Double,
    val width: Double,
    val outerRadius: Double = innerRadius + width
) : BasicShape() {
    override fun rotate(pivot: Point, angle: Double): Shape = copy(center = center.rotate(pivot, angle))
    override fun scale(pivot: Point, factor: Double): Shape =
        copy(innerRadius = innerRadius * factor, outerRadius = outerRadius * factor, width = width * factor)

    override fun move(newCenter: Point): Shape = copy(center = newCenter)
    override fun center(): Point = center
    override fun bounds(): Bounds = Bounds(
        listOf(
            center.copy(x = center.x - outerRadius, y = center.y - outerRadius), // left top
            center.copy(x = center.x + outerRadius, y = center.y - outerRadius), // right top
            center.copy(x = center.x + outerRadius, y = center.y + outerRadius), // right bottom
            center.copy(x = center.x - outerRadius, y = center.y + outerRadius), // left bottom
        )
    )
}

data class Trapeze(
    val center: Point,
    val height: Double,
    val lowerWidth: Double,
    val upperWidth: Double,
    val leftTop: Point = center.copy(x = center.x - upperWidth / 2, y = center.y - height / 2),
    val rightTop: Point = center.copy(x = center.x + upperWidth / 2, y = center.y - height / 2),
    val rightBottom: Point = center.copy(x = center.x + lowerWidth / 2, y = center.y + height / 2),
    val leftBottom: Point = center.copy(x = center.x - lowerWidth / 2, y = center.y + height / 2),
) : BasicShape() {

    override fun rotate(pivot: Point, angle: Double): Shape = copy(
        center = center.rotate(pivot, angle),
        leftTop = leftTop.rotate(pivot, angle),
        rightTop = rightTop.rotate(pivot, angle),
        rightBottom = rightBottom.rotate(pivot, angle),
        leftBottom = leftBottom.rotate(pivot, angle)
    )

    override fun scale(pivot: Point, factor: Double): Shape = copy(
        center = center.scale(pivot, factor),
        leftTop = leftTop.scale(pivot, factor),
        rightTop = rightTop.scale(pivot, factor),
        rightBottom = rightBottom.scale(pivot, factor),
        leftBottom = leftBottom.scale(pivot, factor)
    )

    override fun move(newCenter: Point): Shape = copy(
        center = newCenter,
        leftTop = leftTop.move(center, newCenter),
        rightTop = rightTop.move(center, newCenter),
        rightBottom = rightBottom.move(center, newCenter),
        leftBottom = leftBottom.move(center, newCenter)
    )

    override fun center(): Point = center
    override fun bounds(): Bounds = Bounds(listOf(leftTop, rightTop, rightBottom, leftBottom))
}

data class Drop(
    val center: Point,
    val width: Double,
    val height: Double,
    val centerTop: Point = Point(center.x, center.y - (height / 2)),
    val leftBottom: Point = Point(center.x - width / 2, center.y + (height / 2)),
    val rightBottom: Point = Point(center.x + width / 2, center.y + (height / 2)),
) : BasicShape() {
    override fun rotate(pivot: Point, angle: Double): Shape = copy(
        center = center.rotate(pivot, angle),
        centerTop = centerTop.rotate(pivot, angle),
        leftBottom = leftBottom.rotate(pivot, angle),
        rightBottom = rightBottom.rotate(pivot, angle),
    )

    override fun scale(pivot: Point, factor: Double): Shape = copy(
        centerTop = centerTop.scale(pivot, factor),
        leftBottom = leftBottom.scale(pivot, factor),
        rightBottom = rightBottom.scale(pivot, factor),
    )

    override fun move(newCenter: Point): Shape = copy(
        center = newCenter,
        centerTop = centerTop.move(center, newCenter),
        leftBottom = leftBottom.move(center, newCenter),
        rightBottom = rightBottom.move(center, newCenter),
    )

    override fun center(): Point = center
    override fun bounds(): Bounds = Bounds(listOf(centerTop, leftBottom, rightBottom))
}

data class Diamond(
    val center: Point,
    val width: Double,
    val height: Double,
    val verticalOffsetFactor: Double = 0.0,
    val centerTop: Point = Point(center.x, center.y - height / 2),
    val leftCenter: Point = Point(center.x - width / 2, center.y - height * verticalOffsetFactor),
    val rightCenter: Point = Point(center.x + width / 2, center.y - height * verticalOffsetFactor),
    val centerBottom: Point = Point(center.x, center.y + height / 2)
) : BasicShape() {
    override fun rotate(pivot: Point, angle: Double): Shape = copy(
        center = center.rotate(pivot, angle),
        centerTop = centerTop.rotate(pivot, angle),
        leftCenter = leftCenter.rotate(pivot, angle),
        rightCenter = rightCenter.rotate(pivot, angle),
        centerBottom = centerBottom.rotate(pivot, angle)
    )

    override fun scale(pivot: Point, factor: Double): Shape = copy(
        center = center.scale(pivot, factor),
        centerTop = centerTop.scale(pivot, factor),
        leftCenter = leftCenter.scale(pivot, factor),
        rightCenter = rightCenter.scale(pivot, factor),
        centerBottom = centerBottom.scale(pivot, factor)
    )

    override fun move(newCenter: Point): Shape = copy(
        center = newCenter,
        centerTop = centerTop.move(center, newCenter),
        leftCenter = leftCenter.move(center, newCenter),
        rightCenter = rightCenter.move(center, newCenter),
        centerBottom = centerBottom.move(center, newCenter)
    )

    override fun center(): Point = center
    override fun bounds(): Bounds = Bounds(listOf(centerTop, leftCenter, centerBottom, rightCenter))
}

data class Petal(
    val center: Point,
    val height: Double,
    val width: Double,
    val start: Point = Point(center.x, center.y - height / 2),
    val end: Point = Point(center.x, center.y + height / 2),
    val middleLeft: Point = Point(center.x - width / 2, center.y),
    val middleRight: Point = Point(center.x + width / 2, center.y),
) : BasicShape() {
    override fun center(): Point = center(listOf(start, middleLeft, middleRight, end))

    override fun rotate(pivot: Point, angle: Double): Shape = copy(
        start = start.rotate(pivot, angle),
        middleLeft = middleLeft.rotate(pivot, angle),
        middleRight = middleRight.rotate(pivot, angle),
        end = end.rotate(pivot, angle)
    )

    override fun scale(pivot: Point, factor: Double): Shape = copy(
        start = start.scale(pivot, factor),
        middleLeft = middleLeft.scale(pivot, factor),
        middleRight = middleRight.scale(pivot, factor),
        end = end.scale(pivot, factor),
    )

    override fun move(newCenter: Point): Shape {
        val center = center()
        return copy(
            start = start.move(center, newCenter),
            middleLeft = middleLeft.move(center, newCenter),
            middleRight = middleRight.move(center, newCenter),
            end = end.move(center, newCenter)
        )
    }

    override fun bounds(): Bounds = Bounds(listOf(start, middleLeft, end, middleRight))
}