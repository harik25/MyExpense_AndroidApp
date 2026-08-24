sed -i '489c\
          }\
          Spacer(modifier = Modifier.height(10.dp))\
          ToggleSwitchRow(\
            title = "Show Goals on Dashboard",\
            subtitle = "Display your monthly spending limits on the home screen",\
            checked = appSettings.showGoalsOnDashboard,\
            onCheckedChange = { onSetShowGoalsOnDashboard(it) }\
          )\
        }' app/src/main/java/com/example/ui/settings/SettingsScreen.kt
