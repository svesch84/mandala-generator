package com.example.demo

import com.github.nwillc.ksvg.RenderMode
import com.github.nwillc.ksvg.elements.SVG
import java.io.File
import java.io.FileWriter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    val layer = Layer()
    val layers = mutableListOf(layer)

    val shapeGenerator = ShapeGenerator()

    val radius = 60.0
    val height = 120.0
    val zero = Point(100.0, 100.0)
    val shapes = listOf<Shape>(
        shapeGenerator.circle(zero, radius),
        shapeGenerator.halfCircle(zero, radius),
        shapeGenerator.ring(zero, radius - 20.0, 10.0),
        shapeGenerator.triangle(zero, 120.0, height),
        shapeGenerator.diamond(zero, 60.0, 120.0),
        shapeGenerator.diamond(zero, 60.0, 120.0, 0.25),
        shapeGenerator.rectangle(zero, height, 80.0),
        shapeGenerator.trapeze(zero, height, 60.0, 120.0),
        shapeGenerator.star(zero, 5, radius),
        shapeGenerator.petal(zero, 80.0, height),
        shapeGenerator.drop(zero, 100.0, height),
        shapeGenerator.flower(zero, radius, 9, Random.nextDouble(0.5, 1.0)),
    )

    for (i in shapes.indices) {
        val newCenter = Point(100 + i * 150.0, 100.0)
        layer.shapes.add(shapes[i].move(newCenter))
    }
    for (i in shapes.indices) {
        val newCenter = Point(100 + i * 150.0, 250.0)
        layer.shapes.add(
            shapes[i]
                .move(newCenter)
                .rotate(newCenter, 45.0)
                .scale(newCenter, 0.5)
        )
        layer.shapes.add(shapeGenerator.circle(newCenter, 5.0))
    }

//    val ring = shapeGenerator.ring(center, 300.0, 25.0)
//    layer.shapes.add(ring)
//    layer.shapes.add(shapeGenerator.star(center, 24, 350.0, 300.0).copy(fill = false))

//    val smallCircle = shapeGenerator.circle(center.copy(y = 250.0), 30.0)
////    val smallTriangle = shapeGenerator.triangle(center, 60.0, 60.0)
////    val cs = patternGenerator.repeatShape(center, smallTriangle, 5, 80.0)
//    val cr = patternGenerator.repeatShape(center.copy(y = 250.0), smallCircle, 2, 100.0)
//    val angle = 360 / 8
//
//    for (i in 0..360 step angle) {
//        layer.shapes.add(shapeGenerator.star(center.copy(y = 200.0), 5, 60.0).rotate(center, i.toDouble()))
//        layer.shapes.add(shapeGenerator.star(center.copy(y = 300.0), 5, 40.0).rotate(center, i.toDouble()))
//        layer.shapes.add(halfCircle.rotate(center, i.toDouble()))
////        layer.shapes.add(cs.rotate(center, i.toDouble()))
//        layer.shapes.add(cr.rotate(center, i + angle / 2.0))
//    }
//    layer.shapes.add(Circle(center, 80.0))
//    layer.shapes.add(Ring(center, 360.0, 100.0))

    draw(layers, (1 + shapes.size) * 150.0, 350.0)
}

fun draw(layers: List<Layer>, w: Double, h: Double) {
    val svg = SVG.svg {
        height = h.toString()
        width = w.toString()
        style {
            body = """
                 svg .black-stroke { stroke: black; stroke-width: 2; }
                 svg .fur-color { fill: white; }
             """.trimIndent()
        }

        for (layer in layers) {
            for (shape in layer.shapes) {
                drawShape(shape)
            }
        }
    }
    FileWriter("build/tmp/codeMonkey.svg").use {
        svg.render(it, RenderMode.FILE)
    }
}

private fun SVG.drawShape(shape: Shape) {
    when (shape) {
        is Trapeze -> path {
            cssClass = "black-stroke fur-color"
            d = "M ${shape.leftTop.x} ${shape.leftTop.y} " +
                    "L ${shape.rightTop.x} ${shape.rightTop.y} " +
                    "L ${shape.rightBottom.x} ${shape.rightBottom.y} " +
                    "L ${shape.leftBottom.x} ${shape.leftBottom.y} " +
                    "L ${shape.leftTop.x} ${shape.leftTop.y} "
        }

        is Circle -> circle {
            cssClass = "black-stroke fur-color"
            cx = shape.center.x.toString()
            cy = shape.center.y.toString()
            r = shape.radius.toString()
        }

        is HalfCircle -> path {
            cssClass = "black-stroke fur-color"
            d = "M ${shape.leftBottom.x} ${shape.leftBottom.y} " +
                    "A ${shape.radius} ${shape.radius} 0 0 1 ${shape.rightBottom.x} ${shape.rightBottom.y} " +
                    "L ${shape.leftBottom.x} ${shape.leftBottom.y} "
        }

        is Polygon -> path {
            cssClass = "black-stroke " + if (shape.fill) "fur-color" else ""
            d = "M ${shape.points[0].x} ${shape.points[0].y}" +
                    shape.points.subList(1, shape.points.size)
                        .joinToString(separator = " ") { " L ${it.x} ${it.y}" }
            fill = if (shape.fill) "white" else "none"
        }

        is Diamond -> path {
            cssClass = "black-stroke fur-color"
            d = "M ${shape.centerTop.x} ${shape.centerTop.y} " +
                    "L ${shape.rightCenter.x} ${shape.rightCenter.y}" +
                    "L ${shape.centerBottom.x} ${shape.centerBottom.y}" +
                    "L ${shape.leftCenter.x} ${shape.leftCenter.y}" +
                    "L ${shape.centerTop.x} ${shape.centerTop.y}"
        }

        is Ring -> path {
            val center = shape.center;
            val innerRadius = shape.innerRadius
            val outerRadius = shape.innerRadius + shape.width * 2

            cssClass = "black-stroke fur-color"
            d = "M ${center.x} ${center.y - outerRadius} " +
                    "A $outerRadius $outerRadius 0 1 0 ${center.x} ${center.y + outerRadius}" +
                    "A $outerRadius $outerRadius 0 1 0 ${center.x} ${center.y - outerRadius}" +
                    "Z" +
                    "M ${center.x} ${center.y - innerRadius} " +
                    "A $innerRadius $innerRadius 0 1 1 ${center.x} ${center.y + innerRadius}" +
                    "A $innerRadius $innerRadius 0 1 1 ${center.x} ${center.y - innerRadius}" +
                    "Z"
        }

        is Drop -> path {
            cssClass = "black-stroke fur-color"
            d = "M ${shape.centerTop.x} ${shape.centerTop.y} " +
                    "C ${shape.leftBottom.x} ${shape.leftBottom.y}, " +
                    "${shape.rightBottom.x} ${shape.rightBottom.y}," +
                    "${shape.centerTop.x} ${shape.centerTop.y} "
        }

        is Petal -> path {
            cssClass = "black-stroke fur-color"
            d = "M ${shape.start.x} ${shape.start.y} " +
                    "Q ${shape.middleLeft.x} ${shape.middleLeft.y} ${shape.end.x} ${shape.end.y} " +
                    "Q ${shape.middleRight.x} ${shape.middleRight.y} ${shape.start.x} ${shape.start.y} "
        }

        is CompositeShape -> shape.shapes.forEach { drawShape(it) }
    }
}
