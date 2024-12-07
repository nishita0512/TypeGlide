package com.typeglide.typeglide.composables

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerEventType
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.pow
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun KeyboardScreen(textKeys: Array<Array<KeyboardButton>>){

    val numKeys = arrayOf(
        arrayOf(
            KeyboardButton('1', null, null, null, '\n', 1),
            KeyboardButton('2', null, null, null, null, 1),
            KeyboardButton('3', null, null, null, null, 1),
            KeyboardButton('4', null, null, null, null, 1),
            KeyboardButton('5', null, null, null, null, 1),
            KeyboardButton('6', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('7', null, null, null, null, 1),
            KeyboardButton(' ', null, null, null, null, 2),
            KeyboardButton('8', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('9', null, null, null, null, 1),
            KeyboardButton('0', null, null, null, null, 1),
            KeyboardButton('a', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('b', null, null, null, null, 1)
        )
    )

    val quickActionKeys = arrayOf(
        arrayOf(
            KeyboardButton('a', null, null, null, null, 1),
            KeyboardButton('b', null, null, null, null, 1),
            KeyboardButton('c', null, null, null, null, 1),
            KeyboardButton('d', null, null, null, null, 1),
            KeyboardButton('e', null, null, null, null, 1),
            KeyboardButton('f', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('g', null, null, null, null, 1),
            KeyboardButton('h', null, null, null, null, 1),
            KeyboardButton('i', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('j', null, null, null, null, 1),
            KeyboardButton('k', null, null, null, null, 1),
            KeyboardButton('0', null, null, null, null, 1),
        ),
        arrayOf(
            KeyboardButton('1', null, null, null, null, 1)
        )
    )

    val layoutMode = remember { mutableStateOf(KeyTextMode.TextMode) }
    val lastLayoutMode = remember { mutableStateOf(KeyTextMode.TextMode) }
    val circularRadius = remember { ArrayList<Double>() }
    val canvasWidth = remember{ mutableFloatStateOf(0f) }
    val canvasHeight = remember{ mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val currentContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val pressStartTime = remember{ mutableLongStateOf(0L) }
    val pressStartOffset = remember{ mutableStateOf(Offset(0f,0f)) }
    val pressEndOffset = remember{ mutableStateOf(Offset(0f,0f)) }
    val longPressJob = remember{ mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val currentKeys = remember { mutableStateOf(textKeys) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height((configuration.screenHeightDp / 3).dp)
            .background(Color.Transparent)
            .clipToBounds()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when(event.type) {
                            PointerEventType.Press -> {
                                // Press started
                                Log.d("Tap Press","Long press started")
                                pressStartTime.longValue = System.currentTimeMillis()
                                pressStartOffset.value = event.changes[0].position

                                val distance = sqrt(
                                    abs(canvasWidth.floatValue.toDouble() - pressStartOffset.value.x).pow(2.0)
                                            + abs(canvasHeight.floatValue.toDouble() - pressStartOffset.value.y).pow(2.0)
                                )
                                // Start Key is Backspace
                                if(distance<circularRadius[0]) {
                                    longPressJob.value = coroutineScope.launch {
                                        delay(1000)
                                        Log.d(
                                            "Long Press Performed",
                                            "Quick Action Mode Activated"
                                        )
                                        lastLayoutMode.value = layoutMode.value
                                        layoutMode.value = KeyTextMode.QuickActionMode
                                    }
                                }
                            }
                            PointerEventType.Release -> {
                                // Press released
                                if (pressStartOffset.value.x != 0f && pressStartOffset.value.y != 0f) {
                                    val duration = System.currentTimeMillis() - pressStartTime.longValue
                                    Log.d("Tap Release","Press ended after $duration")

                                    longPressJob.value?.cancel()
                                    longPressJob.value = null

                                    pressEndOffset.value = event.changes[0].position

                                    val centerX = canvasWidth.floatValue.toDouble()
                                    val centerY = canvasHeight.floatValue.toDouble()
                                    val startKey = determineKey(
                                        layoutMode.value,
                                        currentKeys.value,
                                        pressStartOffset.value.x.toDouble(),
                                        pressStartOffset.value.y.toDouble(),
                                        centerX,
                                        centerY,
                                        circularRadius
                                    )
                                    val endKey = determineKey(
                                        layoutMode.value,
                                        currentKeys.value,
                                        pressEndOffset.value.x.toDouble(),
                                        pressEndOffset.value.y.toDouble(),
                                        centerX,
                                        centerY,
                                        circularRadius
                                    )

                                    if(startKey?.center != endKey?.center){
                                        val distance = sqrt(
                                            abs(canvasWidth.floatValue.toDouble() - pressStartOffset.value.x).pow(2.0)
                                                    + abs(canvasHeight.floatValue.toDouble() - pressStartOffset.value.y).pow(2.0)
                                        )
                                        Log.d("Drag Performed", "Circular Radius: ${circularRadius.size} ${circularRadius.joinToString(",")}")
                                        //Start Key is Backspace
                                        if(distance<circularRadius[circularRadius.size-1]){
                                            quickActionDragPerformed(
                                                pressStartOffset.value,
                                                pressEndOffset.value,
                                                centerX,
                                                centerY,
                                                circularRadius,
                                                currentKeys.value,
                                                currentContext,
                                                lastLayoutMode,
                                                layoutMode
                                            )
                                        }
                                        textDragPerformed(
                                            pressStartOffset.value,
                                            pressEndOffset.value,
                                            centerX,
                                            centerY,
                                            circularRadius,
                                            currentKeys.value,
                                            currentContext,
                                            layoutMode
                                        )
                                    }
                                    else if(duration<1000){
                                        textTapPerformed(
                                            pressStartOffset.value,
                                            centerX,
                                            centerY,
                                            circularRadius,
                                            currentKeys.value,
                                            currentContext,
                                            layoutMode
                                        )
                                    }
                                    else{
                                        textLongPressPerformed(
                                            pressStartOffset.value,
                                            centerX,
                                            centerY,
                                            circularRadius,
                                            currentKeys.value,
                                            currentContext,
                                            lastLayoutMode,
                                            layoutMode
                                        )
                                    }

                                    pressStartOffset.value = Offset(0f,0f)
                                    pressStartTime.longValue = 0L
                                }
                            }
                        }
                    }
                }
            }
    ) {
        currentKeys.value = when(layoutMode.value){
            KeyTextMode.TextMode -> {textKeys}
            KeyTextMode.NumMode -> {numKeys}
            KeyTextMode.QuickActionMode -> {quickActionKeys}
        }

        canvasWidth.floatValue = size.width
        canvasHeight.floatValue = size.height

        var h = canvasHeight.floatValue
        val difference = h / 5
        val distanceFromCenterOfCurrentSection = ((h / 5.0) / 2.0) - 1f

        circularRadius.clear()
        for (currentRowNo in currentKeys.value.indices) {
            val noOfButtons = currentKeys.value[currentRowNo].size
            var totalSizeOfButtons = 0
            currentKeys.value[currentRowNo].forEach { keyboardButton ->
                totalSizeOfButtons += keyboardButton.keySize
            }

            circularRadius.add(h.toDouble())
            println("Radius: ${circularRadius[currentRowNo]}")
            println("Center: ${canvasWidth.floatValue} ${canvasHeight.floatValue}")

            drawCircle(
                brush = Brush.radialGradient(
                    radius = h,
                    center = Offset(canvasWidth.floatValue, canvasHeight.floatValue),
                    colorStops = arrayOf(
                        Pair(0.9f, Color.White), Pair(0.9999f, Color(0xFFA7A7A7))
                    )
                ),
                radius = h,
                center = Offset(canvasWidth.floatValue, canvasHeight.floatValue)
            )

            val startPoint = Offset(canvasWidth.floatValue, canvasHeight.floatValue)
            val lineLength = h

            val anglePerButton = 90.0 / totalSizeOfButtons
            var angle = 180.0
            var keyCount = 0

            for(currentButtonNo in 0 ..< noOfButtons) {
                val prevAngle = angle
                val keySize = currentKeys.value[currentRowNo][currentButtonNo].keySize
                for(i in 0 ..< keySize) {
                    angle += anglePerButton
                }

                try {
                    val angleRadians = Math.toRadians(angle)
                    val endPointX =
                        startPoint.x + (lineLength * cos(angleRadians)).toFloat()
                    val endPointY =
                        startPoint.y + (lineLength * sin(angleRadians)).toFloat()

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

                    //Top Text
                    var imaginaryLength =
                        lineLength - (distanceFromCenterOfCurrentSection / 3)
                    val topTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val topTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val topTextOfKey =
                        currentKeys.value[currentRowNo][keyCount].top.toString()

                    rotatedText(
                        layoutMode.value,
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
                    imaginaryLength =
                        lineLength - (distanceFromCenterOfCurrentSection * 1.6f)
                    val bottomTextY =
                        startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val bottomTextX =
                        startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()
                    val bottomTextOfKey =
                        currentKeys.value[currentRowNo][keyCount].bottom.toString()

                    rotatedText(
                        layoutMode.value,
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
                    val centerTextOfKey =
                        currentKeys.value[currentRowNo][keyCount].center.toString()

                    rotatedText(
                        layoutMode.value,
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
                    val leftTextOfKey =
                        currentKeys.value[currentRowNo][keyCount].left.toString()

                    rotatedText(
                        layoutMode.value,
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
                    val rightTextOfKey =
                        currentKeys.value[currentRowNo][keyCount].right.toString()

                    rotatedText(
                        layoutMode.value,
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
                catch (e: Exception){
                    e.printStackTrace()
                }

            }
            h -= difference
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
        KeyTextMode.QuickActionMode -> {
            when(text){
                "0"->{"NUM"}
                "1"->{"Backspace"}
                " "->{"Space"}
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
            Log.d("Text", "Text: $updatedText Text Size: $textSize Font Weight: $fontWeight Degrees: $degrees")
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
            KeyTextMode.QuickActionMode -> {

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
            KeyTextMode.QuickActionMode -> {

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
            KeyTextMode.QuickActionMode -> {

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
            KeyTextMode.QuickActionMode -> {

            }
        }
    }

    return null
}

fun determineSwipeKey(
    dragStartX: Double,
    dragStartY: Double,
    dragEndX: Double,
    dragEndY: Double,
    dragStartAngle: Double,
    key: KeyboardButton
): String{

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

fun textTapPerformed(
    tapCoordinates: Offset,
    centerX: Double,
    centerY: Double,
    circularRadius: ArrayList<Double>,
    keys: Array<Array<KeyboardButton>>,
    currentContext: Context,
    layoutMode: MutableState<KeyTextMode>
){
    val touchX = tapCoordinates.x.toDouble()
    val touchY = tapCoordinates.y.toDouble()

    val key = determineKey(
        layoutMode.value,
        keys,
        touchX,
        touchY,
        centerX,
        centerY,
        circularRadius
    )
    if (key == null) {
        return
    }

    val textToInsert = key.center.toString()

    when(layoutMode.value){
        KeyTextMode.TextMode -> {
            when(textToInsert){
                '1'.toString() -> {
                    (currentContext as IMEService).currentInputConnection.deleteSurroundingText(
                        1,
                        0
                    )
                }
                '0'.toString() -> {
                    layoutMode.value = KeyTextMode.NumMode
                }
                else -> {
                    (currentContext as IMEService).currentInputConnection.commitText(
                        textToInsert,
                        1
                    )
                }
            }
        }
        KeyTextMode.NumMode -> {
            when(textToInsert) {
                'b'.toString() -> {
                    (currentContext as IMEService).currentInputConnection.deleteSurroundingText(
                        1,
                        0
                    )
                }
                'a'.toString() -> {
                    layoutMode.value = KeyTextMode.TextMode
                }
                else -> {
                    (currentContext as IMEService).currentInputConnection.commitText(
                        textToInsert,
                        1
                    )
                }
            }
        }
        KeyTextMode.QuickActionMode -> {}
    }

}

fun textLongPressPerformed(
    longTapCoordinates: Offset,
    centerX: Double,
    centerY: Double,
    circularRadius: ArrayList<Double>,
    keys: Array<Array<KeyboardButton>>,
    currentContext: Context,
    lastLayoutMode: MutableState<KeyTextMode>,
    layoutMode: MutableState<KeyTextMode>
){
    val touchY = longTapCoordinates.y.toDouble()
    val touchX = longTapCoordinates.x.toDouble()

    val distance = sqrt(
        pow(abs(centerX - touchX), 2.0) + pow(abs(centerY - touchY), 2.0)
    )

    //Checking Pressed Key is Backspace
    if (distance < circularRadius[3]) {
        Log.d("Long Press Performed", "Quick Action Mode Deactivated")
        layoutMode.value = lastLayoutMode.value
    }

    val key = determineKey(
        layoutMode.value,
        keys,
        touchX,
        touchY,
        centerX,
        centerY,
        circularRadius
    )
    if (key == null) {
        return
    }

    val textToInsert = key.center.toString()


    if (textToInsert == '1'.toString()) {
        (currentContext as IMEService).currentInputConnection.deleteSurroundingText(
            1,
            0
        )
    } else if (textToInsert == '0'.toString()) {
        layoutMode.value = KeyTextMode.NumMode
    } else {
        (currentContext as IMEService).currentInputConnection.commitText(
            textToInsert,
            1
        )
    }
}

fun textDragPerformed(
    dragStartOffset: Offset,
    dragEndOffset: Offset,
    centerX: Double,
    centerY: Double,
    circularRadius: ArrayList<Double>,
    keys: Array<Array<KeyboardButton>>,
    currentContext: Context,
    layoutMode: MutableState<KeyTextMode>
){

    val dragStartAngle = Math.toDegrees(
        Math.atan2(
            (dragStartOffset.y.toDouble() - centerY),
            (dragStartOffset.x.toDouble() - centerX)
        )
    )

    val key = determineKey(
        layoutMode.value,
        keys,
        dragStartOffset.x.toDouble(),
        dragStartOffset.y.toDouble(),
        centerX,
        centerY,
        circularRadius
    )

    if (key == null) {
        return
    }

    val textToInsert = determineSwipeKey(
        dragStartOffset.x.toDouble(),
        dragStartOffset.y.toDouble(),
        dragEndOffset.x.toDouble(),
        dragEndOffset.y.toDouble(),
        dragStartAngle,
        key
    )

    if (textToInsert == "null") {
        return
    }

    (currentContext as IMEService).currentInputConnection.commitText(
        textToInsert,
        1
    )
}

fun quickActionDragPerformed(
    startOffset: Offset,
    endOffset: Offset,
    centerX: Double,
    centerY: Double,
    circularRadius: ArrayList<Double>,
    keys: Array<Array<KeyboardButton>>,
    currentContext: Context,
    lastLayoutMode: MutableState<KeyTextMode>,
    layoutMode: MutableState<KeyTextMode>
) {
    if (layoutMode.value != KeyTextMode.QuickActionMode) {
        return
    }
    Log.d("Quick Action", "Quick Action Drag Performed Function Called")
    layoutMode.value = lastLayoutMode.value
}

@Preview(showBackground = true)
@Composable
fun KeyboardScreenPreview(){
    val backspace = 8
    KeyboardScreen(
        arrayOf(
            arrayOf(
                KeyboardButton('m', '~', null, null, '\n', 1),
                KeyboardButton('t', '$', '<', null, 'j', 1),
                KeyboardButton('i', '"', '?', null, 'c', 1),
                KeyboardButton('a', ':', '[', null, 'u', 1),
                KeyboardButton('e', ']', ';', null, 'b', 1),
                KeyboardButton('h', '%', '/', null, 'g', 1),
                KeyboardButton('o', '>', '&', null, 'v', 1),
                KeyboardButton('p', null, '\\', null, '#', 1),
            ),
            arrayOf(
                KeyboardButton('y', '*', null, '^', '{', 1),
                KeyboardButton('r', 'f', '(', '.', '\'', 1),
                KeyboardButton(' ', null, null, null, null, 2),
                KeyboardButton('s', ')', 'w', ',', '_', 1),
                KeyboardButton('d', null, '@', '|', '}', 1),
            ),
            arrayOf(
                KeyboardButton('l', '-', null, 'x', null, 1),
                KeyboardButton('k', '=', '+', 'z', null, 1),
                KeyboardButton('n', '`', '!', 'q', null, 1),
                KeyboardButton('0', null, null, null, null, 1),
            ),
            arrayOf(
                KeyboardButton('1', null, null, null, null, 1)
            )
        )
    )
}

