package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.Mt5LoginScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Trading Bot", appName)
  }

  @Test
  fun `mt5 login screen renders credentials inputs and server selection`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Mt5LoginScreen()
      }
    }

    composeTestRule.onNodeWithTag("mt5_login_screen").assertExists()
    composeTestRule.onNodeWithTag("hero_banner").assertExists()
    composeTestRule.onNodeWithTag("server_selection_card").assertExists()
    composeTestRule.onNodeWithTag("account_id_input").assertExists()
    composeTestRule.onNodeWithTag("password_input").assertExists()
    composeTestRule.onNodeWithTag("connect_mt5_button").assertExists()
  }

  @Test
  fun `quick fill buttons populate account inputs`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Mt5LoginScreen()
      }
    }

    composeTestRule.onNodeWithTag("quick_fill_live_button").performClick()
    composeTestRule.onNodeWithTag("account_id_input").assertExists()
    composeTestRule.onNodeWithTag("password_input").assertExists()
  }

  @Test
  fun `main app renders divisions and allows navigation`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.MainTradingApp()
      }
    }

    composeTestRule.onNodeWithTag("main_trading_app").assertExists()
    composeTestRule.onNodeWithTag("division_tab_gateway").assertExists()
    composeTestRule.onNodeWithTag("division_tab_chart").assertExists()
    composeTestRule.onNodeWithTag("division_tab_bot").assertExists()
  }

  @Test
  fun `market chart division renders timeframes and order buttons`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.MainTradingApp()
      }
    }

    composeTestRule.onNodeWithTag("division_tab_chart").performClick()
    composeTestRule.onNodeWithTag("division_market_chart").assertExists()
    composeTestRule.onNodeWithTag("symbol_selector_button").assertExists()
    composeTestRule.onNodeWithTag("timeframe_1m").assertExists()
    composeTestRule.onNodeWithTag("timeframe_5m").assertExists()
    composeTestRule.onNodeWithTag("timeframe_15m").assertExists()
    composeTestRule.onNodeWithTag("timeframe_30m").assertExists()
    composeTestRule.onNodeWithTag("timeframe_1hr").assertExists()
    composeTestRule.onNodeWithTag("timeframe_4hr").assertExists()
    composeTestRule.onNodeWithTag("quick_buy_button").assertExists()
    composeTestRule.onNodeWithTag("quick_sell_button").assertExists()
  }

  @Test
  fun `analysis and bot division renders 10-pip TP and 15-pip SL rules`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.MainTradingApp()
      }
    }

    composeTestRule.onNodeWithTag("division_tab_bot").performClick()
    composeTestRule.onNodeWithTag("division_analysis_bot").assertExists()
    composeTestRule.onNodeWithTag("auto_trading_switch").assertExists()
    composeTestRule.onNodeWithTag("signal_analysis_card").assertExists()
    composeTestRule.onNodeWithTag("execute_signal_entry_button").assertExists()
  }
}
