package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.formatRupee
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary
import com.example.ui.theme.BachatWarning
import com.example.ui.theme.BachatWarningTint
import kotlin.math.atan2

data class DonutSegment(
  val category: String,
  val amount: Double,
  val percentage: Double,
  val color: Color,
  val tintColor: Color
)

data class SpendBarItem(
  val label: String,
  val subLabel: String = "",
  val amount: Double,
  val isHighlighted: Boolean = false
)

@Composable
fun DonutChart(
  totalAmount: Double,
  segments: List<DonutSegment>,
  modifier: Modifier = Modifier,
  onSegmentClick: ((DonutSegment) -> Unit)? = null
) {
  var selectedSegment by remember(segments) { mutableStateOf<DonutSegment?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(segments) {
    animProgress.snapTo(0f)
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
    )
  }

  val displayAmount = selectedSegment?.amount ?: totalAmount
  val displayLabel = selectedSegment?.category ?: "Total Spent"
  val displayColor = selectedSegment?.color ?: BachatTextPrimary

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Donut Ring with interactive center info & tap response
      Box(
        modifier = Modifier
          .size(150.dp)
          .testTag("donut_chart_canvas"),
        contentAlignment = Alignment.Center
      ) {
        Canvas(
          modifier = Modifier
            .size(130.dp)
            .pointerInput(segments, totalAmount) {
              detectTapGestures { offset ->
                if (segments.isEmpty() || totalAmount <= 0) return@detectTapGestures
                val center = size.width / 2f
                val dx = offset.x - center
                val dy = offset.y - center
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angle < 0) angle += 360f
                // Rotate by 90deg because startAngle is -90 (top)
                val normalizedAngle = (angle + 90f) % 360f

                var accumAngle = 0f
                for (seg in segments) {
                  val sweep = (seg.percentage.toFloat() / 100f) * 360f
                  if (normalizedAngle >= accumAngle && normalizedAngle < accumAngle + sweep) {
                    selectedSegment = if (selectedSegment == seg) null else seg
                    onSegmentClick?.invoke(seg)
                    break
                  }
                  accumAngle += sweep
                }
              }
            }
        ) {
          val strokeWidth = 20.dp.toPx()
          val selStrokeWidth = 24.dp.toPx()

          if (segments.isEmpty() || totalAmount <= 0) {
            drawCircle(
              color = Color(0xFFF1F5F9),
              style = Stroke(width = strokeWidth)
            )
          } else {
            var startAngle = -90f
            for (seg in segments) {
              val rawSweep = (seg.percentage.toFloat() / 100f) * 360f * animProgress.value
              val isSel = selectedSegment == seg
              if (rawSweep > 0) {
                drawArc(
                  color = seg.color.copy(alpha = if (selectedSegment == null || isSel) 1f else 0.35f),
                  startAngle = startAngle,
                  sweepAngle = (rawSweep - 2.5f).coerceAtLeast(1f),
                  useCenter = false,
                  style = Stroke(
                    width = if (isSel) selStrokeWidth else strokeWidth,
                    cap = StrokeCap.Round
                  )
                )
                startAngle += (seg.percentage.toFloat() / 100f) * 360f * animProgress.value
              }
            }
          }
        }

        // Center Content
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Text(
            text = formatRupee(displayAmount),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = displayColor,
            textAlign = TextAlign.Center,
            maxLines = 1
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = displayLabel.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = BachatTextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
          )
          if (selectedSegment != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${selectedSegment!!.percentage.toInt()}%",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = selectedSegment!!.color
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Clean, Structured Legend List with Perfect Tabular Alignment
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (segments.isEmpty()) {
          Text(
            text = "No expenses in this period",
            fontSize = 13.sp,
            color = BachatTextSecondary
          )
        } else {
          segments.take(4).forEach { segment ->
            val isSelected = selectedSegment == segment
            val rowBg by animateColorAsState(
              targetValue = if (isSelected) segment.tintColor else Color.Transparent,
              label = "legend_bg"
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(rowBg)
                .then(
                  if (isSelected) {
                    Modifier.border(1.dp, segment.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  } else {
                    Modifier
                  }
                )
                .clickable {
                  selectedSegment = if (selectedSegment == segment) null else segment
                  onSegmentClick?.invoke(segment)
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Left: Indicator dot + category name + amount
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(segment.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = segment.category,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = BachatTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = formatRupee(segment.amount),
                    fontSize = 11.sp,
                    color = BachatTextSecondary
                  )
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              // Right: Percentage pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(999.dp))
                  .background(if (isSelected) segment.color else segment.tintColor)
                  .padding(horizontal = 7.dp, vertical = 2.5.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${segment.percentage.toInt()}%",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else segment.color
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SpendingComparisonCard(
  thisMonthExpense: Double,
  lastMonthExpense: Double,
  comparisonPercentage: Double,
  isLower: Boolean,
  periodLabel: String = "Month",
  topCategory: String = "Food",
  dailyAverage: Double = 0.0,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  // Subtle animated elevation for interactive depth
  val elevation by animateDpAsState(
    targetValue = if (isExpanded) 5.dp else 2.dp,
    label = "comparison_card_elevation"
  )

  val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    label = "chevron_rotation"
  )

  val difference = kotlin.math.abs(lastMonthExpense - thisMonthExpense)
  val maxCompAmount = maxOf(thisMonthExpense, lastMonthExpense, 100.0)
  val thisMonthRatio = (thisMonthExpense / maxCompAmount).toFloat().coerceIn(0.08f, 1f)
  val lastMonthRatio = (lastMonthExpense / maxCompAmount).toFloat().coerceIn(0.08f, 1f)

  AppCard(
    modifier = modifier
      .clickable { isExpanded = !isExpanded }
      .testTag("spending_comparison_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(if (isLower) BachatSuccessTint else BachatDangerTint),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLower) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
              contentDescription = null,
              tint = if (isLower) BachatSuccess else BachatDanger,
              modifier = Modifier.size(17.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Spending Comparison",
              fontSize = 16.5.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
            Text(
              text = "vs Last $periodLabel",
              fontSize = 12.sp,
              color = BachatTextSecondary
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          BadgePill(
            text = if (isLower) "▼ ${comparisonPercentage.toInt()}% lower" else "▲ ${comparisonPercentage.toInt()}% higher",
            backgroundColor = if (isLower) BachatSuccessTint else BachatDangerTint,
            textColor = if (isLower) BachatSuccess else BachatDanger
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = BachatTextSecondary,
            modifier = Modifier
              .size(22.dp)
              .rotate(rotationAngle)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Compact side-by-side summary row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(BachatSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          Column {
            Text("This Month", fontSize = 11.5.sp, color = BachatTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = formatRupee(thisMonthExpense),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(BachatSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          Column {
            Text("Last Month", fontSize = 11.5.sp, color = BachatTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = formatRupee(lastMonthExpense),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextSecondary
            )
          }
        }
      }

      // Animated Expandable Section
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ) {
          HorizontalDivider(color = BachatDivider, thickness = 0.8.dp)
          Spacer(modifier = Modifier.height(14.dp))

          // Proportional Comparative Bars
          Text(
            text = "Comparative Spending Volume",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatTextSecondary
          )

          Spacer(modifier = Modifier.height(10.dp))

          // This Month Bar
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("This Month", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BachatTextPrimary)
              Text(formatRupee(thisMonthExpense), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLower) BachatSuccess else BachatDanger)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(BachatSurface)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(thisMonthRatio)
                  .height(10.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(if (isLower) BachatSuccess else BachatDanger)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Last Month Bar
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Last Month", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BachatTextSecondary)
              Text(formatRupee(lastMonthExpense), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BachatTextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(BachatSurface)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(lastMonthRatio)
                  .height(10.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(Color(0xFFCBD5E1))
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Net Difference Highlight Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(if (isLower) BachatSuccessTint else BachatDangerTint)
              .padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isLower) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isLower) BachatSuccess else BachatDanger,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (isLower) {
                  "You've spent ${formatRupee(difference)} less than this time last month! Great savings discipline."
                } else {
                  "You've spent ${formatRupee(difference)} more than last month. Check ${topCategory.lowercase()} expenses."
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isLower) BachatSuccess else BachatDanger,
                lineHeight = 18.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun WeeklySpendBarChart(
  items: List<SpendBarItem>,
  modifier: Modifier = Modifier,
  activeColor: Color = BachatInk,
  baseColor: Color = BachatSurface
) {
  val maxAmount = items.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 100.0
  
  // Default to highlighted item or last item
  val defaultIndex = items.indexOfFirst { it.isHighlighted }.takeIf { it >= 0 } ?: items.lastIndex.takeIf { it >= 0 }
  var selectedIndex by remember(items) { mutableStateOf<Int?>(defaultIndex) }

  Column(modifier = modifier.fillMaxWidth()) {
    // Selected bar tooltip
    val activeItem = selectedIndex?.let { items.getOrNull(it) }
    if (activeItem != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(BachatInk)
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = "${activeItem.label}: ${formatRupee(activeItem.amount)}${if (activeItem.subLabel.isNotEmpty()) " (${activeItem.subLabel})" else ""}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    } else {
       Spacer(modifier = Modifier.height(36.dp))
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      items.forEachIndexed { idx, item ->
        val ratio = (item.amount / maxAmount).toFloat().coerceIn(0.06f, 1f)
        val isSelected = selectedIndex == idx
        val isPeak = item.amount == maxAmount && item.amount > 0

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedIndex = if (selectedIndex == idx) null else idx
            }
            .padding(horizontal = 4.dp)
        ) {
          // Bar
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
          ) {
            Box(
              modifier = Modifier
                .width(22.dp)
                .fillMaxHeight(ratio)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                  when {
                    isSelected -> BachatAccentIndigo
                    isPeak -> BachatInk
                    item.isHighlighted -> BachatSuccess
                    else -> Color(0xFFE2E8F0)
                  }
                )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Day / Week Label
          Text(
            text = item.label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected || isPeak || item.isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected || isPeak || item.isHighlighted) BachatTextPrimary else BachatTextSecondary
          )
        }
      }
    }
  }
}
