sed -i '486,521c\
      Row(\
        modifier = Modifier.fillMaxWidth(),\
        verticalAlignment = Alignment.CenterVertically,\
        horizontalArrangement = Arrangement.SpaceBetween\
      ) {\
        Row(verticalAlignment = Alignment.CenterVertically) {\
          Box(\
            modifier = Modifier\
              .size(44.dp)\
              .clip(CircleShape)\
              .background(BachatTheme.colors.surface),\
            contentAlignment = Alignment.Center\
          ) {\
            Icon(\
              imageVector = getCategoryIcon(goal.category),\
              contentDescription = null,\
              tint = statusColor,\
              modifier = Modifier.size(24.dp)\
            )\
          }\
          Spacer(modifier = Modifier.width(12.dp))\
          Column {\
            Text(\
              text = goal.title,\
              fontSize = 17.sp,\
              fontWeight = FontWeight.Bold,\
              color = BachatTextPrimary\
            )\
            Spacer(modifier = Modifier.height(2.dp))\
            Text(\
              text = "${percentage}% of limit used",\
              fontSize = 13.sp,\
              color = BachatTextSecondary,\
              fontWeight = FontWeight.Medium\
            )\
          }\
        }\
        Text(\
          text = "${formatRupee(spentAmount)}\\n/ ${formatRupee(target)}",\
          fontSize = 15.sp,\
          fontWeight = FontWeight.Bold,\
          color = BachatTextPrimary,\
          textAlign = androidx.compose.ui.text.style.TextAlign.End\
        )\
      }\
      Spacer(modifier = Modifier.height(16.dp))\
      ThinProgressBar(\
        progress = ratio,\
        fillColor = statusColor,\
        height = 8.dp\
      )' app/src/main/java/com/example/ui/components/BachatComponents.kt
