package com.ardovic.carmovementsimulation

import android.graphics.Rect

import com.ardovic.carmovementsimulation.engine.Renderer

class Car {

    private var destRect = Rect()
    private var targetDestRect: Rect = Rect()
    private var orangeRectScrRect = Rect(2, 2, 8, 8) // orange square
    private var redDotSrcRect = Rect(20, 0, 30, 10) // red dot

    private var x = 0F
    private var y = 0F
    private var h = 0F
    private var w = 0F

    private var r = 0.0
    private var dr = 0.2
    private var cww = true

    private var minRadius = 0F
    private var maxSpeed = 0F
    private var speed = 0F
    private var accelerating = true

    private var turnEnabled = false
    private var turnInitiated = false

    private var centerX = 0F
    private var centerY = 0F
    private var leftCenterX = 0F
    private var leftCenterY = 0F
    private var rightCenterX = 0F
    private var rightCenterY = 0F

    private var targetX = 0F
    private var targetY = 0F

    // Testing & debugging

//    private var logSkipFrame = 0
//    private var centerDestRect: Rect = Rect()
//    private var leftCenterDestRect: Rect = Rect()
//    private var rightCenterDestRect: Rect = Rect()
//    private var greenDotSrcRect = Rect(10, 0, 20, 10) // green dot

    init {
        maxSpeed = 10F
        minRadius = maxSpeed * 28

        w = C.screenW / 5F
        h = w / 2
        x = (C.screenW - w) / 2
        y = (C.screenH - h) / 2
        targetX = x
        targetY = y
    }

    fun driveTo(targetX: Float, targetY: Float) {
        this.targetX = targetX
        this.targetY = targetY

        targetDestRect = Rect(
            (targetX - 10).toInt(),
            (targetY - 10).toInt(),
            (targetX + 10).toInt(),
            (targetY + 10).toInt()
        )

        turnEnabled = true
        turnInitiated = false
        accelerating = true

        cww = if (carAngleDeg() <= 180)
            targetAngleDeg() > carAngleDeg() && targetAngleDeg() < (carAngleDeg() + 180)
        else
            targetAngleDeg() > carAngleDeg() || targetAngleDeg() < (carAngleDeg() - 180)

        // Test & debug section below

//        Log.d(
//            "HEX_1",
//            "--- Car angle: " + carAngleDeg() + ", target angle: " + targetAngleDeg() + ". " + (if (cww) "CW" else "CWW") + " ---"
//        )
    }

    fun update() {
        if (!inHardArea())
            turn()

        drive()

        invalidateCenters()

        // Test & debug section below

//        logSkipFrame++
//        if (logSkipFrame == 60) {
//            Log.d(
//                "HEX_2",
//                "--- Car angle: " + carAngleDeg() + ", destination angle: " + targetAngleDeg()
//            )
//            logSkipFrame = 0
//        }
    }

    private fun turn() {
        turnInitiated = true
        if (turnEnabled) {
            if (!lockedOnTarget())
                if (cww) r -= speed * dr
                else r += speed * dr
            else {
                turnEnabled = false
                r = -targetAngleDeg()
            }
            r = normalizeAngle(r)
        }
    }

    private fun drive() {
        if (turnInitiated && closeToTarget()) accelerating = false

        if (accelerating) {
            speed += 0.2F
            if (speed > maxSpeed) speed = maxSpeed
        } else {
            speed -= 0.3F
            if (speed < 0F) speed = 0F
        }

        x += (Math.cos(Math.toRadians(r)) * speed).toFloat()
        y += (Math.sin(Math.toRadians(r)) * speed).toFloat()
    }

    private fun inHardArea(): Boolean {
        if (turnInitiated) return false
        val safeRadius = minRadius * 1.25F
        return distance(targetX, targetY, leftCenterX, leftCenterY) < safeRadius
                || distance(targetX, targetY, rightCenterX, rightCenterY) < safeRadius
    }

    private fun invalidateCenters() {
        centerX = x + w / 2
        centerY = y + h / 2

        leftCenterX = (centerX - Math.sin(Math.toRadians(carAngleDeg())) * minRadius).toFloat()
        leftCenterY = (centerY - Math.cos(Math.toRadians(carAngleDeg())) * minRadius).toFloat()

        rightCenterX = (centerX + Math.sin(Math.toRadians(carAngleDeg())) * minRadius).toFloat()
        rightCenterY = (centerY + Math.cos(Math.toRadians(carAngleDeg())) * minRadius).toFloat()

        // Test & debug section below

//        centerDestRect =
//            Rect(
//                (centerX - 10).toInt(),
//                (centerY - 10).toInt(),
//                (centerX + 10).toInt(),
//                (centerY + 10).toInt()
//            )
//        leftCenterDestRect =
//            Rect(
//                (leftCenterX - 10).toInt(),
//                (leftCenterY - 10).toInt(),
//                (leftCenterX + 10).toInt(),
//                (leftCenterY + 10).toInt()
//            )
//        rightCenterDestRect =
//            Rect(
//                (rightCenterX - 10).toInt(),
//                (rightCenterY - 10).toInt(),
//                (rightCenterX + 10).toInt(),
//                (rightCenterY + 10).toInt()
//            )
    }

    private fun closeToTarget(): Boolean {
        return Math.abs(distance(centerX, centerY, targetX, targetY)) < h * 1.5
    }

    private fun lockedOnTarget(): Boolean {
        return Math.abs(carAngleDeg() - targetAngleDeg()) < 2 * speed * dr
    }

    private fun targetAngleDeg(): Double {
        val angle =
            -Math.toDegrees(Math.atan2((targetY - (centerY)).toDouble(), (targetX - (centerX)).toDouble()))
        return normalizeAngle(angle)
    }

    private fun carAngleDeg(): Double {
        return normalizeAngle(360 - r);
    }

    private fun normalizeAngle(angle: Double): Double {
        var newAngle = angle
        if (newAngle < 0) newAngle += 360
        if (newAngle > 360) newAngle -= 360
        return newAngle
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
    }

    fun draw(renderer: Renderer?) {
        destRect.left = x.toInt()
        destRect.top = y.toInt()
        destRect.right = (x + w).toInt()
        destRect.bottom = (y + h).toInt()
        renderer?.draw(orangeRectScrRect, destRect, Math.round(r).toInt())
        renderer?.draw(redDotSrcRect, targetDestRect, 0)

        // Test & debug section below

//        renderer?.draw(redDotSrcRect, centerDestRect, 0)
//        renderer?.draw(greenDotSrcRect, leftCenterDestRect, 0)
//        renderer?.draw(greenDotSrcRect, rightCenterDestRect, 0)
    }
}