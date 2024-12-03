package com.typeglide.typeglide.composables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typeglide.typeglide.models.KeyboardButton
import com.typeglide.typeglide.services.IMEService
import com.typeglide.typeglide.utils.KeyTextMode
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun KeyboardScreen(keys: Array<Array<KeyboardButton>>){

    val numKeys = arrayOf(
        arrayOf(
            KeyboardButton('1', null, null, null, '\n'),
            KeyboardButton('2', null, null, null, null),
            KeyboardButton('3', null, null, null, null),
            KeyboardButton('4', null, null, null, null),
            KeyboardButton('5', null, null, null, null),
            KeyboardButton('6', null, null, null, null),
        ),
        arrayOf(
            KeyboardButton('7', null, null, null, null),
            KeyboardButton(' ', null, null, null, null),
            KeyboardButton('8', null, null, null, null),
        ),
        arrayOf(
            KeyboardButton('9', null, null, null, null),
            KeyboardButton('0', null, null, null, null),
            KeyboardButton('a', null, null, null, null),
        ),
        arrayOf(
            KeyboardButton('b', null, null, null, null)
        )
    )

    val isNumLayout = remember { mutableStateOf(false) }
    val circularRadius = ArrayList<Double>()
    val canvasWidth = remember{ mutableFloatStateOf(0f) }
    val canvasHeight = remember{ mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val ctx = LocalContext.current
    val configuration = LocalConfiguration.current
    val dragStartOffset = remember{ mutableStateOf(Offset(0f,0f)) }
    val dragEndX = remember{ mutableFloatStateOf(0f)}
    val dragEndY = remember{ mutableFloatStateOf(0f)}

    if(!isNumLayout.value) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((configuration.screenHeightDp / 3).dp)
                .background(Color.Transparent)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures { tapCoordinates ->
                        val centerX = canvasWidth.value.toDouble()
                        val centerY = canvasHeight.value.toDouble()
                        val touchX = tapCoordinates.x.toDouble()
                        val touchY = tapCoordinates.y.toDouble()

                        val key = determineKey(KeyTextMode.TextMode, keys, touchX, touchY, centerX, centerY, circularRadius)
                        if (key == null) {
                            return@detectTapGestures
                        }

                        val textToInsert = key.center.toString()

                        if (textToInsert == '1'.toString()) {
                            (ctx as IMEService).currentInputConnection.deleteSurroundingText(1, 0)
                        }
                        else if(textToInsert == '0'.toString()){
                            isNumLayout.value = true
                        }
                        else {
                            (ctx as IMEService).currentInputConnection.commitText(
                                textToInsert,
                                1
                            )
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            dragStartOffset.value = startOffset
                            dragEndX.value = startOffset.x
                            dragEndY.value = startOffset.y
                        },
                        onDragEnd = {
                            val centerX = canvasWidth.value.toDouble()
                            val centerY = canvasHeight.value.toDouble()

                            val dragStartAngle = Math.toDegrees(
                                Math.atan2(
                                    (dragStartOffset.value.y.toDouble() - centerY),
                                    (dragStartOffset.value.x.toDouble() - centerX)
                                )
                            )

                            val key = determineKey(
                                KeyTextMode.TextMode,
                                keys,
                                dragStartOffset.value.x.toDouble(),
                                dragStartOffset.value.y.toDouble(),
                                centerX,
                                centerY,
                                circularRadius
                            )

                            if (key == null) {
                                return@detectDragGestures
                            }

                            val textToInsert = determineSwipeKey(
                                dragStartOffset.value.x.toDouble(),
                                dragStartOffset.value.y.toDouble(),
                                dragEndX.value.toDouble(),
                                dragEndY.value.toDouble(),
                                dragStartAngle,
                                key
                            )

                            if (textToInsert == "null") {
                                return@detectDragGestures
                            }

                            (ctx as IMEService).currentInputConnection.commitText(
                                textToInsert,
                                1
                            )

                        },
                        onDragCancel = {
                            dragStartOffset.value = Offset(0f, 0f)
                            dragEndX.value = 0f
                            dragEndY.value = 0f
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            dragEndX.value += dragAmount.x
                            dragEndY.value += dragAmount.y
                        }
                    )
                }
        ) {
            canvasWidth.value = size.width
            canvasHeight.value = size.height

            var h = canvasHeight.value
            val difference = h / 5
            var noOfButtons = 8
            var distanceFromCenterOfCurrentSection = ((h / 5.0) / 2.0) - 1f

            for (i in 0..3) {
                circularRadius.add(h.toDouble())
                println("Radius: ${circularRadius.get(i)}")
                println("Center: ${canvasWidth.value} ${canvasHeight.value}")
                drawCircle(
                    brush = Brush.radialGradient(
                        radius = h,
                        center = Offset(canvasWidth.value, canvasHeight.value),
                        colorStops = arrayOf(
                            Pair(0.9f, Color.White), Pair(0.9999f, Color(0xFFA7A7A7))
                        )
                    ),
                    radius = h,
                    center = Offset(canvasWidth.value, canvasHeight.value)
                )
                val startPoint = Offset(canvasWidth.value, canvasHeight.value)
                val lineLength = h

                if (i == 3) {
                    break
                }
                val anglePerButton = 90.0 / noOfButtons
                var angle = 180.0
                var keyCount = 0
                while (angle < 270) {
                    val prevAngle = angle
                    angle += anglePerButton

                    if (i == 1 && angle.toInt() == 225) {
                        val centerAngleRadians = Math.toRadians(225.0)

                        val imaginaryLength = lineLength - distanceFromCenterOfCurrentSection

                        val spaceTextY = startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                        val spaceTextX = startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()

                        rotatedText(
                            KeyTextMode.TextMode,
                            -45f,
                            spaceTextX,
                            spaceTextY,
                            "Space",
                            this,
                            textMeasurer,
                            18.sp,
                            FontWeight.Bold
                        )

                        keyCount++
                        continue
                    }

                    val angleRadians = Math.toRadians(angle)
                    val endPointX = startPoint.x + (lineLength * cos(angleRadians)).toFloat()
                    val endPointY = startPoint.y + (lineLength * sin(angleRadians)).toFloat()

                    drawLine(
                        color = Color.Black,
                        start = startPoint,
                        end = Offset(endPointX, endPointY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )

                    if (i == 1 && angle.toInt() == 240) {
                        continue
                    }

                    drawLine(
                        color = Color.Black,
                        start = startPoint,
                        end = Offset(endPointX, endPointY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    val centerAngle = (prevAngle + angle) / 2.0
                    val centerAngleRadians = Math.toRadians(centerAngle)

                    //Top Text
                    var imaginaryLength = lineLength - (distanceFromCenterOfCurrentSection / 3)
                    val topTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val topTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val topTextOfKey = keys[i][keyCount].top.toString()

                    rotatedText(
                        KeyTextMode.TextMode,
                        (centerAngle + 90).toFloat(),
                        topTextX,
                        topTextY,
                        topTextOfKey,
                        this,
                        textMeasurer,
                        14.sp,
                        FontWeight.Light
                    )

                    //Bottom Text
                    imaginaryLength = lineLength - (distanceFromCenterOfCurrentSection * 1.6f)
                    val bottomTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val bottomTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val bottomTextOfKey = keys[i][keyCount].bottom.toString()

                    rotatedText(
                        KeyTextMode.TextMode,
                        (centerAngle + 90).toFloat(),
                        bottomTextX,
                        bottomTextY,
                        bottomTextOfKey,
                        this,
                        textMeasurer,
                        14.sp,
                        FontWeight.Light
                    )

                    //Center Text
                    imaginaryLength = lineLength - distanceFromCenterOfCurrentSection
                    val centerTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val centerTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val centerTextOfKey = keys[i][keyCount].center.toString()

                    rotatedText(
                        KeyTextMode.TextMode,
                        (centerAngle + 90).toFloat(),
                        centerTextX,
                        centerTextY,
                        centerTextOfKey,
                        this,
                        textMeasurer,
                        18.sp,
                        FontWeight.Bold
                    )

                    //Left Text
                    val leftTextAngle = prevAngle + (anglePerButton / 8)
                    val leftTextAngleRadians = Math.toRadians(leftTextAngle)
                    val leftTextY =
                        startPoint.y + (imaginaryLength * sin(leftTextAngleRadians)).toFloat()
                    val leftTextX =
                        startPoint.x + (imaginaryLength * cos(leftTextAngleRadians)).toFloat()
                    val leftTextOfKey = keys[i][keyCount].left.toString()

                    rotatedText(
                        KeyTextMode.TextMode,
                        (leftTextAngle + 90).toFloat(),
                        leftTextX,
                        leftTextY,
                        leftTextOfKey,
                        this,
                        textMeasurer,
                        14.sp,
                        FontWeight.Light
                    )

                    //Right Text
                    val rightTextAngle = angle - (anglePerButton / 8)
                    val rightTextAngleRadians = Math.toRadians(rightTextAngle)
                    val rightTextY =
                        startPoint.y + (imaginaryLength * sin(rightTextAngleRadians)).toFloat()
                    val rightTextX =
                        startPoint.x + (imaginaryLength * cos(rightTextAngleRadians)).toFloat()
                    val rightTextOfKey = keys[i][keyCount].right.toString()

                    rotatedText(
                        KeyTextMode.TextMode,
                        (rightTextAngle + 90).toFloat(),
                        rightTextX,
                        rightTextY,
                        rightTextOfKey,
                        this,
                        textMeasurer,
                        14.sp,
                        FontWeight.Light
                    )

                    keyCount++

                }
                noOfButtons -= 2
                h -= difference
            }

            val centerAngleRadians = Math.toRadians(225.0)
            val imaginaryLength = h - (1.2 * difference)
            val backspaceTextY =
                canvasHeight.value + (imaginaryLength * sin(centerAngleRadians)).toFloat()
            val backspaceTextX =
                canvasWidth.value + (imaginaryLength * cos(centerAngleRadians)).toFloat()

            rotatedText(
                KeyTextMode.TextMode,
                315f,
                backspaceTextX,
                backspaceTextY,
                "Backspace",
                this,
                textMeasurer,
                14.sp,
                FontWeight.Bold
            )

        }
    }
    else{
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((configuration.screenHeightDp / 3).dp)
                .background(Color.Transparent)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures { tapCoordinates ->
                        val centerX = canvasWidth.value.toDouble()
                        val centerY = canvasHeight.value.toDouble()
                        val touchX = tapCoordinates.x.toDouble()
                        val touchY = tapCoordinates.y.toDouble()

                        val key = determineKey(KeyTextMode.NumMode, numKeys, touchX, touchY, centerX, centerY, circularRadius)
                        if (key == null) {
                            return@detectTapGestures
                        }

                        val textToInsert = key.center.toString()

                        if (textToInsert == 'b'.toString()) {
                            (ctx as IMEService).currentInputConnection.deleteSurroundingText(1, 0)
                        }
                        else if(textToInsert == 'a'.toString()){
                            isNumLayout.value = false
                        }
                        else {
                            (ctx as IMEService).currentInputConnection.commitText(
                                textToInsert,
                                1
                            )
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            dragStartOffset.value = startOffset
                            dragEndX.value = startOffset.x
                            dragEndY.value = startOffset.y
                        },
                        onDragEnd = {
                            val centerX = canvasWidth.value.toDouble()
                            val centerY = canvasHeight.value.toDouble()

                            val dragStartAngle = Math.toDegrees(
                                Math.atan2(
                                    (dragStartOffset.value.y.toDouble() - centerY),
                                    (dragStartOffset.value.x.toDouble() - centerX)
                                )
                            )

                            val key = determineKey(
                                KeyTextMode.TextMode,
                                numKeys,
                                dragStartOffset.value.x.toDouble(),
                                dragStartOffset.value.y.toDouble(),
                                centerX,
                                centerY,
                                circularRadius
                            )

                            if (key == null) {
                                return@detectDragGestures
                            }

                            val textToInsert = determineSwipeKey(
                                dragStartOffset.value.x.toDouble(),
                                dragStartOffset.value.y.toDouble(),
                                dragEndX.value.toDouble(),
                                dragEndY.value.toDouble(),
                                dragStartAngle,
                                key
                            )

                            if (textToInsert == "null") {
                                return@detectDragGestures
                            }

                            (ctx as IMEService).currentInputConnection.commitText(
                                textToInsert,
                                1
                            )

                        },
                        onDragCancel = {
                            dragStartOffset.value = Offset(0f, 0f)
                            dragEndX.value = 0f
                            dragEndY.value = 0f
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            dragEndX.value += dragAmount.x
                            dragEndY.value += dragAmount.y
                        }
                    )
                }
        ) {
            canvasWidth.value = size.width
            canvasHeight.value = size.height

            var h = canvasHeight.value
            val difference = h / 5
            var noOfButtons: Int
            var distanceFromCenterOfCurrentSection = ((h / 5.0) / 2.0) - 1f

            for (i in 0..3) {

                noOfButtons = when(i){
                    0 -> {6}
                    1 -> {3}
                    2 -> {3}
                    3 -> {1}
                    else -> {0}
                }

                if(circularRadius.size<4) {
                    circularRadius.add(h.toDouble())
                    println("Radius: ${circularRadius.get(i)}")
                    println("Center: ${canvasWidth.floatValue} ${canvasHeight.value}")
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        radius = h,
                        center = Offset(canvasWidth.value, canvasHeight.value),
                        colorStops = arrayOf(
                            Pair(0.9f, Color.White), Pair(0.9999f, Color(0xFFA7A7A7))
                        )
                    ),
                    radius = h,
                    center = Offset(canvasWidth.value, canvasHeight.value)
                )
                val startPoint = Offset(canvasWidth.value, canvasHeight.value)
                val lineLength = h

                if (i == 3) {
                    break
                }
                val anglePerButton = 90.0 / noOfButtons
                var angle = 180.0
                var keyCount = 0
                while (angle < 270) {
                    Log.d("NumKeys", "keyCount: $keyCount")
                    val prevAngle = angle
                    angle += anglePerButton

                    val angleRadians = Math.toRadians(angle)
                    val endPointX = startPoint.x + (lineLength * cos(angleRadians)).toFloat()
                    val endPointY = startPoint.y + (lineLength * sin(angleRadians)).toFloat()

                    drawLine(
                        color = Color.Black,
                        start = startPoint,
                        end = Offset(endPointX, endPointY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.Black,
                        start = startPoint,
                        end = Offset(endPointX, endPointY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    val centerAngle = (prevAngle + angle) / 2.0
                    val centerAngleRadians = Math.toRadians(centerAngle)

                    //Bottom Text
                    var imaginaryLength = lineLength - (distanceFromCenterOfCurrentSection * 1.6f)
                    val bottomTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val bottomTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val bottomTextOfKey = numKeys[i][keyCount].bottom.toString()

                    rotatedText(
                        KeyTextMode.NumMode,
                        (centerAngle + 90).toFloat(),
                        bottomTextX,
                        bottomTextY,
                        bottomTextOfKey,
                        this,
                        textMeasurer,
                        14.sp,
                        FontWeight.Light
                    )

                    //Center Text
                    imaginaryLength = lineLength - distanceFromCenterOfCurrentSection
                    val centerTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val centerTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val centerTextOfKey = numKeys[i][keyCount].center.toString()

                    rotatedText(
                        KeyTextMode.NumMode,
                        (centerAngle + 90).toFloat(),
                        centerTextX,
                        centerTextY,
                        centerTextOfKey,
                        this,
                        textMeasurer,
                        18.sp,
                        FontWeight.Bold
                    )

                    keyCount++

                }
                h -= difference
            }

            val centerAngleRadians = Math.toRadians(225.0)
            val imaginaryLength = h - (1.2 * difference)
            val backspaceTextY =
                canvasHeight.value + (imaginaryLength * sin(centerAngleRadians)).toFloat()
            val backspaceTextX =
                canvasWidth.value + (imaginaryLength * cos(centerAngleRadians)).toFloat()

            rotatedText(
                KeyTextMode.NumMode,
                315f,
                backspaceTextX,
                backspaceTextY,
                "Backspace",
                this,
                textMeasurer,
                14.sp,
                FontWeight.Bold
            )
        }
    }

}

fun rotatedText(
    mode: KeyTextMode,
    degrees: Float,
    textX: Float,
    textY: Float,
    text: String,
    drawScope: DrawScope,
    textMeasurer: TextMeasurer,
    textSize: TextUnit,
    fontWeight: FontWeight
){
    if(text=="null"){
        return
    }
    val updatedText = when(mode){
        KeyTextMode.TextMode -> {
            when(text){
                "0"->{"NUM"}
                "1"->{"Backspace"}
                " "->{"Space"}
                "\n"->{"Enter"}
                else->{text}
            }
        }
        KeyTextMode.NumMode -> {
            when(text){
                "a"->{"NUM"}
                "b"->{"Backspace"}
                " "->{"Space"}
                "\n"->{"Enter"}
                else->{text}
            }
        }
    }
    drawScope.rotate(degrees,Offset(textX, textY)){
        drawIntoCanvas {
            val textLayoutResult = textMeasurer.measure(
                text = updatedText,
                style = TextStyle(fontSize = textSize, fontWeight = fontWeight)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = updatedText,
                topLeft = Offset(
                    textX-(textLayoutResult.size.width/2f),
                    textY-(textLayoutResult.size.height/2f)
                ),
                style = TextStyle(fontSize = textSize, fontWeight = fontWeight)
            )
        }
    }
}


fun determineKey(
    mode: KeyTextMode,
    keyArray: Array<Array<KeyboardButton>>,
    touchX: Double,
    touchY: Double,
    centerX: Double,
    centerY: Double,
    circularRadius: ArrayList<Double>
): KeyboardButton? {

    val angle = Math.toDegrees(
        Math.atan2(
            (touchY - centerY),
            (touchX - centerX)
        )
    )
    val distance = sqrt(
        pow(abs(centerX - touchX), 2.0) + pow(abs(centerY - touchY), 2.0)
    )

    if(circularRadius[0]>=distance && circularRadius[1]<distance){
        when(mode){
            KeyTextMode.TextMode -> {
                if (angle in -180.0..-168.75) {
                    return keyArray[0][0]
                }
                else if (angle in -168.75..-157.5){
                    return keyArray[0][1]
                }
                else if (angle in -157.5..-146.25){
                    return keyArray[0][2]
                }
                else if (angle in -146.25..-135.0){
                    return keyArray[0][3]
                }
                else if (angle in -135.0..-123.75){
                    return keyArray[0][4]
                }
                else if (angle in -123.75..-112.5){
                    return keyArray[0][5]
                }
                else if (angle in -112.5..-101.25){
                    return keyArray[0][6]
                }
                else if (angle in -101.25..-90.0){
                    return keyArray[0][7]
                }
            }
            KeyTextMode.NumMode -> {
                if (angle in -180.0..-165.0) {
                    return keyArray[0][0]
                }
                else if (angle in -165.0..-150.0){
                    return keyArray[0][1]
                }
                else if (angle in -150.0..-135.0){
                    return keyArray[0][2]
                }
                else if (angle in -135.0..-120.0){
                    return keyArray[0][3]
                }
                else if (angle in -120.0..-105.0){
                    return keyArray[0][4]
                }
                else if (angle in -105.0..-90.0){
                    return keyArray[0][5]
                }
            }
        }

    }
    else if(circularRadius[1]>distance && circularRadius[2]<distance){
        when(mode){
            KeyTextMode.TextMode -> {
                if(angle in -180.0 .. -165.0){
                    return keyArray[1][0]
                }
                else if(angle in -165.0 .. -150.0){
                    return keyArray[1][1]
                }
                else if(angle in -150.0 .. -120.0){
                    return keyArray[1][2]
                }
                else if(angle in -120.0.. -105.0){
                    return keyArray[1][3]
                }
                else if(angle in -105.0 .. -90.0){
                    return keyArray[1][4]
                }
            }
            KeyTextMode.NumMode -> {
                if (angle in -180.0..-150.0) {
                    return keyArray[1][0]
                }
                else if (angle in -150.0..-120.0){
                    return keyArray[1][1]
                }
                else if (angle in -120.0..-90.0){
                    return keyArray[1][2]
                }
            }
        }

    }
    else if(circularRadius[2]>distance && circularRadius[3]<distance){
        when(mode){
            KeyTextMode.TextMode -> {
                if(angle in -180.0 .. -157.5 ){
                    return keyArray[2][0]
                }
                else if(angle in -157.5 .. -135.0){
                    return keyArray[2][1]
                }
                else if(angle in -135.0 .. -112.5){
                    return keyArray[2][2]
                }
                else if(angle in -112.5 .. -90.0){
                    return keyArray[2][3]
                }
            }
            KeyTextMode.NumMode -> {
                if (angle in -180.0..-150.0) {
                    return keyArray[2][0]
                }
                else if (angle in -150.0..-120.0){
                    return keyArray[2][1]
                }
                else if (angle in -120.0..-90.0){
                    return keyArray[2][2]
                }
            }
        }

    }
    else if(circularRadius[3]>distance){
        when(mode){
            KeyTextMode.TextMode -> {
                return keyArray[3][0]
            }
            KeyTextMode.NumMode -> {
                return keyArray[3][0]
            }
        }
    }

    return null
}

fun determineSwipeKey(dragStartX: Double, dragStartY: Double, dragEndX: Double, dragEndY: Double, dragStartAngle: Double, key: KeyboardButton): String{

    val dragAngle = Math.toDegrees(
        Math.atan2(
            (dragEndY - dragStartY),
            (dragEndX - dragStartX)
        )
    )

    val deltaAngle = abs(dragAngle-dragStartAngle)

    println("Delta: " +
            "\tdragStartAngle: $dragStartAngle" +
            "\tangle: $dragAngle" +
            "\tdeltaAngle: $deltaAngle, "
    )

    return when(deltaAngle){
        in 45.0 .. 135.0 ->{
            key.right.toString()
        }
        in 135.0 .. 225.0 ->{
            key.bottom.toString()
        }
        in 225.0 .. 315.0 ->{
            key.left.toString()
        }
        else ->{
            key.top.toString()
        }
    }

}

@Preview(showBackground = true)
@Composable
fun KeyboardScreenPreview(){
    val backspace = 8
    KeyboardScreen(arrayOf(
        arrayOf(
            KeyboardButton('m', '~', null, null, '\n'),
            KeyboardButton('t', '$', '<', null, 'j'),
            KeyboardButton('i', '"', '?', null, 'c'),
            KeyboardButton('a', ':', '[', null, 'u'),
            KeyboardButton('e', ']', ';', null, 'b'),
            KeyboardButton('h', '%', '/', null, 'g'),
            KeyboardButton('o', '>', '&', null, 'v'),
            KeyboardButton('p', null, '\\', null, '#'),
        ),
        arrayOf(
            KeyboardButton('y', '*', null, '^', '{'),
            KeyboardButton('r', 'f', '(', '.', '\''),
            KeyboardButton(' ', null, null, null, null),
            KeyboardButton('s', ')', 'w', ',', '_'),
            KeyboardButton('d', null, '@', '|', '}'),
        ),
        arrayOf(
            KeyboardButton('l', '-', null, 'x', null),
            KeyboardButton('k', '=', '+', 'z', null),
            KeyboardButton('n', '`', '!', 'q', null),
            KeyboardButton('0', null, null, null, null),
        ),
        arrayOf(
            KeyboardButton(backspace.toChar(), null, null, null, null)
        )
    ))
}

