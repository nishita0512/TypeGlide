package com.typeglide.typeglide.composables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typeglide.typeglide.models.KeyboardButton
import com.typeglide.typeglide.services.IMEService
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardScreen(keys: Array<Array<KeyboardButton>>){
    var circularRadius = ArrayList<Double>()
    val canvasWidth = remember{ mutableStateOf(0f) }
    val canvasHeight = remember{ mutableStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val ctx = LocalContext.current
    Canvas(
      modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .background(Color.Transparent)
          .clipToBounds()
          .pointerInteropFilter { motionEvent ->
              val centerX = canvasWidth.value
              val centerY = canvasHeight.value
              val touchX = motionEvent.x
              val touchY = motionEvent.y

              val angle = Math.toDegrees(
                  Math.atan2(
                      (touchY - centerY).toDouble(),
                      (touchX - centerX).toDouble()
                  )
              )
              val distance = sqrt(
                  pow(abs(centerX - touchX).toDouble(), 2.0)
                          + pow(abs(centerY - touchY).toDouble(), 2.0)
              )
              val key = determineKey(angle, distance, circularRadius, keys)
              if(key=='1'.toString()){
                  (ctx as IMEService).currentInputConnection.deleteSurroundingText(1,0)
              }
              else{
                  (ctx as IMEService).currentInputConnection.commitText(
                      key,
                      1
                  )
              }
              Log.d("Key press", "Key: $key Angle: $angle")
              true
          }
    ){
        canvasWidth.value = size.width
        canvasHeight.value = size.height

        var h = size.height
        val difference = 120
        var noOfButtons = 8
        var distanceFromCenterOfCurrentSection = ((h/5.0)/2.0)-1f

        for (i in 0..3){
            circularRadius.add(h.toDouble())
            drawCircle(
                brush = Brush.radialGradient(
                  radius = h,
                    center = Offset(size.width,size.height),
                    colorStops = arrayOf(Pair(0.9f,Color.White)
                        ,Pair(0.9999f,Color.Gray))
                ),
                radius = h,
                center = Offset(size.width,size.height)
            )
            val startPoint = Offset(size.width,size.height)
            val lineLength = h

            if (i==3){
                break
            }
            var anglePerButton = 90.0/noOfButtons
            var angle = 180.0
            var keyCount = 0
            while(angle<270){
                val prevAngle = angle
                angle+=anglePerButton

                if(i==1 && angle.toInt()==225){
                    val centerAngleRadians = Math.toRadians(225.0)

                    val imaginaryLength = lineLength-distanceFromCenterOfCurrentSection

                    val textY = startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                    val textX = startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()

                    rotate(-45f,Offset(textX-10f, textY-10f)){
                        drawIntoCanvas {
                            val textOfKey = "Space"
                            val textLayoutResult= textMeasurer.measure(
                                text = textOfKey,
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = textOfKey,
                                topLeft = Offset(
                                    textX-(textLayoutResult.size.width/2f),
                                    textY-(textLayoutResult.size.height/3f)
                                )
                            )
                        }
                    }
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

                if(i==1 && angle.toInt()==240){
                    continue
                }

                drawLine(
                    color = Color.Black,
                    start = startPoint,
                    end = Offset(endPointX,endPointY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                val centerAngle = (prevAngle+angle)/2.0
                val centerAngleRadians = Math.toRadians(centerAngle)

                val imaginaryLength = lineLength-distanceFromCenterOfCurrentSection

                val textY = startPoint.y + (imaginaryLength * sin(centerAngleRadians)).toFloat()
                val textX = startPoint.x + (imaginaryLength * cos(centerAngleRadians)).toFloat()

                rotate((centerAngle+90).toFloat(),Offset(textX-10f, textY-10f)){
                    drawIntoCanvas {
                        val textOfKey = keys[i][keyCount++].center.toString()
                        val textLayoutResult= textMeasurer.measure(
                            text = textOfKey,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = textOfKey,
                            topLeft = Offset(
                                textX-(textLayoutResult.size.width/2f),
                                textY-(textLayoutResult.size.height/3f)
                            )
                        )
                    }
                }
            }
            noOfButtons-=2
            h-=difference
        }

        val centerAngleRadians = Math.toRadians(225.0)

        val imaginaryLength = h-(1.2*difference)

        val textY = canvasHeight.value + (imaginaryLength * sin(centerAngleRadians)).toFloat()
        val textX = canvasWidth.value + (imaginaryLength * cos(centerAngleRadians)).toFloat()

        rotate(315f,Offset(textX-10f, textY-10f)){
            drawIntoCanvas {
                val textOfKey = "Backspace"
                val textLayoutResult= textMeasurer.measure(
                    text = textOfKey,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = textOfKey,
                    topLeft = Offset(
                        textX-(textLayoutResult.size.width/2.25f),
                        textY-(textLayoutResult.size.height/2f)
                    )
                )
            }
        }

    }
}

fun determineKey(angle: Double, distance: Double, circularRadius: ArrayList<Double>, keys: Array<Array<KeyboardButton>>): String{
    if(circularRadius[0]>distance && circularRadius[1]<distance){
        if (angle in -180.0..-168.75) {
            return keys[0][0].center.toString()
        }
        else if (angle in -168.75..-157.5){
            return keys[0][1].center.toString()
        }
        else if (angle in -157.5..-146.25){
            return keys[0][2].center.toString()
        }
        else if (angle in -146.25..-135.0){
            return keys[0][3].center.toString()
        }
        else if (angle in -135.0..-123.75){
            return keys[0][4].center.toString()
        }
        else if (angle in -123.75..-112.5){
            return keys[0][5].center.toString()
        }
        else if (angle in -112.5..-101.25){
            return keys[0][6].center.toString()
        }
        else if (angle in -101.25..-90.0){
            return keys[0][7].center.toString()
        }
    }
    else if(circularRadius[1]>distance && circularRadius[2]<distance){
        if(angle in -180.0 .. -165.0){
            return keys[1][0].center.toString()
        }
        else if(angle in -165.0 .. -150.0){
            return keys[1][1].center.toString()
        }
        else if(angle in -150.0 .. -120.0){
            return keys[1][2].center.toString()
        }
        else if(angle in -120.0.. -105.0){
            return keys[1][3].center.toString()
        }
        else if(angle in -105.0 .. -90.0){
            return keys[1][4].center.toString()
        }
    }
    else if(circularRadius[2]>distance && circularRadius[3]<distance){
        if(angle in -180.0 .. -157.5 ){
            return keys[2][0].center.toString()
        }
        else if(angle in -157.5 .. -135.0){
            return keys[2][1].center.toString()
        }
        else if(angle in -135.0 .. -112.5){
            return keys[2][2].center.toString()
        }
        else if(angle in -112.5 .. -90.0){
            return keys[2][3].center.toString()
        }
    }
    else if(circularRadius[3]>distance){
        return keys[3][0].center.toString()
    }

    return ""
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

