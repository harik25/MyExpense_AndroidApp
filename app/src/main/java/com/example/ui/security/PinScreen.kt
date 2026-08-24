package com.example.ui.security

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary

enum class PinMode {
  SETUP,
  UNLOCK
}

@Composable
fun PinScreen(
  mode: PinMode = PinMode.UNLOCK,
  useBiometric: Boolean = false,
  onPinSuccess: (String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var enteredPin by remember { mutableStateOf("") }
  var setupFirstPin by remember { mutableStateOf("") }
  var isConfirmStep by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  val title = when {
    mode == PinMode.SETUP && !isConfirmStep -> "Create a PIN"
    mode == PinMode.SETUP && isConfirmStep -> "Confirm PIN"
    else -> "Enter PIN"
  }

  val subtitle = when {
    mode == PinMode.SETUP && !isConfirmStep -> "Enter a 4-digit PIN for your secret transactions"
    mode == PinMode.SETUP && isConfirmStep -> "Re-enter your 4-digit PIN to confirm"
    else -> "Enter your 4-digit security PIN"
  }

  fun triggerBiometricPrompt() {
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(
      activity,
      executor,
      object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
          super.onAuthenticationSucceeded(result)
          onPinSuccess("BIOMETRIC_SUCCESS")
        }
      }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("Bachat Security")
      .setSubtitle("Unlock secret transactions")
      .setNegativeButtonText("Use PIN")
      .build()
    prompt.authenticate(promptInfo)
  }

  LaunchedEffect(Unit) {
    if (mode == PinMode.UNLOCK && useBiometric) {
      try {
        triggerBiometricPrompt()
      } catch (_: Exception) {}
    }
  }

  fun handleDigit(digit: String) {
    if (enteredPin.length < 4) {
      val next = enteredPin + digit
      enteredPin = next
      errorMessage = ""

      if (next.length == 4) {
        if (mode == PinMode.SETUP) {
          if (!isConfirmStep) {
            setupFirstPin = next
            enteredPin = ""
            isConfirmStep = true
          } else {
            if (next == setupFirstPin) {
              onPinSuccess(next)
            } else {
              errorMessage = "PINs do not match. Try again."
              enteredPin = ""
              isConfirmStep = false
              setupFirstPin = ""
            }
          }
        } else {
          // Unlock mode
          onPinSuccess(next)
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      errorMessage = ""
    }
  }

  Scaffold(
    topBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White)
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .align(Alignment.CenterStart)
            .testTag("pin_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BachatTextPrimary
          )
        }
      }
    },
    containerColor = Color.White,
    modifier = modifier.testTag("pin_screen")
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header & Pin Indicator dots
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp)
      ) {
        Text(
          text = title,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = subtitle,
          fontSize = 14.sp,
          color = BachatTextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 4 Indicator Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(4) { index ->
            val isFilled = index < enteredPin.length
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isFilled) BachatInk else BachatDivider)
            )
          }
        }

        if (errorMessage.isNotBlank()) {
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = errorMessage,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatDanger,
            textAlign = TextAlign.Center
          )
        }
      }

      // Keypad
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf(if (mode == PinMode.UNLOCK && useBiometric) "BIO" else "", "0", "BACK")
        )

        rows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            row.forEach { key ->
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(if (key.isNotEmpty() && key != "BIO" && key != "BACK") BachatSurface else Color.Transparent)
                  .clickable(enabled = key.isNotEmpty()) {
                    when (key) {
                      "BACK" -> handleBackspace()
                      "BIO" -> triggerBiometricPrompt()
                      "" -> {}
                      else -> handleDigit(key)
                    }
                  }
                  .testTag("pin_key_$key"),
                contentAlignment = Alignment.Center
              ) {
                when (key) {
                  "BACK" -> {
                    Icon(
                      imageVector = Icons.Default.Backspace,
                      contentDescription = "Backspace",
                      tint = BachatTextPrimary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  "BIO" -> {
                    Icon(
                      imageVector = Icons.Default.Fingerprint,
                      contentDescription = "Use Biometric",
                      tint = BachatInk,
                      modifier = Modifier.size(32.dp)
                    )
                  }
                  "" -> {}
                  else -> {
                    Text(
                      text = key,
                      fontSize = 24.sp,
                      fontWeight = FontWeight.Bold,
                      color = BachatTextPrimary
                    )
                  }
                }
              }
            }
          }
        }

        if (mode == PinMode.UNLOCK && useBiometric) {
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = { triggerBiometricPrompt() },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BachatSurface,
              contentColor = BachatInk
            ),
            modifier = Modifier.testTag("use_biometric_button")
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Use Biometric", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
