package com.aos.beacondistance.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.aos.beacondistance.R
import com.aos.beacondistance.data.CircleInfoData
import com.aos.beacondistance.http.CircleData
import kotlin.random.Random

class DrawingView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val widthItemNum: Int
    private val circleRadius: Int
    private val circleWidth: Int
    private val xPositionList: ArrayList<Int>
    private val yPosition: Int

    init {
        setWillNotDraw(false)
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        circleRadius = resources.getDimension(R.dimen.circle_item_size).toInt()
        circleWidth = circleRadius * 2 + 12
        yPosition = screenHeight / 4
        xPositionList = ArrayList()
        widthItemNum = (screenWidth / circleWidth) - 1 // 화면의 너비를 원의 크기로 나누어 한 줄에 총 몇 개의 원이 들어갈 수 있는지 개수 저장
        for(i in 1 .. widthItemNum) {
            // 원의 크기를 배수로 이용해 x축에 좌표를 widthItemNum 개수 만큼 구한다. 원이 그려지기 시작하는 좌표는 원의 중심축이 기준이므로 각 값마다 반지름 만큼을 더한다. positon 배열에 저장
            xPositionList.add(circleWidth * (i) + circleRadius)
        }
    }

    // 원 색상 리스트
    private val circleColorList = arrayOf(
        "#4dadb8", "#df83b3", "#2786f9", "#33817e", "#0d1e1c", "#4ba339", "#92f5a6", "#4dd0ff", "#163a0b",
    )

    //원을 그리기 위한 페인트
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // 텍스트를 그리기 위한 페인트
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE // 텍스트 색상
        textSize = 28f // 텍스트 크기
        textAlign = Paint.Align.CENTER // 텍스트를 중앙 정렬
    }

    // 생성된 원의 정보를 담아놓는 리스트
    private val circleItemMap = HashMap<String, CircleInfoData>()

    //그리는 곳 : drawnCircleList에서 원의 대한 정보를 가져와 그리도록한다
    override fun onDraw(canvas: Canvas) {
        var farItemNum = 0
        var nearItemNum = 0
        var immediateItemNum = 0

        circleItemMap.entries.withIndex().forEach { (index, entry) ->
            val circle = entry.value
            paint.color = Color.parseColor(circle.color)

            // 원 그리기
            // 거리가 far인 경우
            if(circle.distance <= -85) {
                circle.xPos = if(farItemNum != 0) xPositionList[farItemNum % widthItemNum] else xPositionList[0] // x좌표 위치를 저장한 배열의 인덱스를 이용해 xPos 결정
                circle.yPos = yPosition - ( circleRadius * 2) // yPosition 결정
                if(farItemNum >= widthItemNum) circle.yPos += circleWidth // 해당 위치의 한 줄에 들어갈 수 있는 아이템 갯수를 초과했으면 원의 크기만큼 y좌표 내려서 그림
                farItemNum++

            // 거리가 near인 경우
            } else if(circle.distance <= -60) {
                circle.xPos = if(nearItemNum != 0) xPositionList[nearItemNum % widthItemNum] else xPositionList[0]
                circle.yPos = yPosition * 2
                if(nearItemNum >= widthItemNum) circle.yPos += circleWidth
                nearItemNum++

            // 거리가 immediate인 경우
            } else {
                circle.xPos = if(immediateItemNum != 0) xPositionList[immediateItemNum % widthItemNum] else xPositionList[0]
                circle.yPos = (yPosition*3) + ( circleRadius * 2)
                if(immediateItemNum >= widthItemNum) circle.yPos += circleWidth
                immediateItemNum++
            }
            drawCircle(circle, canvas, paint)

            // 텍스트 그리기
            drawTextInCircle(circle, canvas)
        }
    }

    // 원을 그리기
    private fun drawCircle(circle: CircleInfoData, canvas: Canvas, paint: Paint) {
        canvas.drawCircle(circle.xPos.toFloat(), circle.yPos.toFloat(), circle.radius.toFloat(), paint)
    }

    // 원 안에 텍스트 그리기
    private fun drawTextInCircle(circle: CircleInfoData, canvas: Canvas) {
        val xPos = circle.xPos.toFloat()
        val lineHeight = textPaint.descent() - textPaint.ascent()
        var yPos = (circle.yPos.toFloat() - textPaint.descent()) // 텍스트의 Y 좌표 중앙 맞추기
        canvas.drawText(circle.name, xPos, yPos, textPaint)
        yPos += lineHeight // 다음 줄을 위한 Y 좌표 증가
        canvas.drawText("${circle.distance}", xPos, yPos, textPaint)
    }

    fun setCircleList(dataList: ArrayList<CircleData>) {
        dataList.map { data ->
            if (circleItemMap.containsKey(data.name)) {
                // name이 일치하는 CircleData가 있는 경우 처리
                moveCircleShape(data)
            } else {
                // 일치하는 CircleData가 없는 경우 처리
                createCircle(data)
            }
        }
        circleItemMap.map {

        }
        this.invalidate()
    }

    // 원 객체 생성
    private fun createCircle(data: CircleData) {
        val circleColor = circleColorList[circleItemMap.size % 9]
        val circleInfoData = CircleInfoData(data.name, data.distance, circleColor, 0, 0, circleRadius)
        circleItemMap.put(data.name, circleInfoData)
    }

    // 원 객체 이동
    private fun moveCircleShape(data: CircleData) {
        circleItemMap[data.name]?.distance = data.distance
    }
}
