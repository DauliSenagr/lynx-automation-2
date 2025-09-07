Feature: Signup on Ionic Native app
  This feature enables users to sign-up in to the Ionic Native mobile application using valid 10 digit mobile number.

  Background:
    Given Ionic native app has been launched and user has navigated to the 'Login or Register' screen

  @IonicNativeApp @SignUp @SanityTest @RegressionTest
  Scenario: User should be able to register using a valid mobile number and valid OTP on the Ionic Native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    And wait for 2 seconds.
    Then the OTP verification drawer should appear on the Ionic native app.
    When user inputs a valid OTP into the OTP field on the Ionic native app.
    Then the user should be registered successfully and logged in to the Ionic native app.

  @IonicNativeApp @SignUp @SmokeTest @SanityTest @RegressionTest
  Scenario: Database entries should be correct after a successful signup from the mobile app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    And wait for 2 seconds.
    Then the OTP verification drawer should appear on the Ionic native app.
    When user inputs a valid OTP into the OTP field on the Ionic native app.
    Then the user should be registered successfully and logged in to the Ionic native app.
    And verify that the data for the new user singed up from Ionic native app in cardplay.cp_source is correct for all columns

  @IonicNativeApp @SignUp @SanityTest @RegressionTest
  Scenario: Users can update the mobile number by tapping the "Change" button on OTP verification drawer of the Ionic native app.
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    Then the OTP verification drawer should appear on the Ionic native app.
    When the user taps on the Change button on the OTP verification drawer within the Ionic native app.
    Then the OTP verification drawer disappears from the Login or Register screen on the Ionic native app and user can change the mobile number

  @IonicNativeApp @SignUp @SmokeTest @SanityTest @RegressionTest
  Scenario: User should not be able to register without checking the T&Cs checkbox on the Ionic Native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And user taps on the T&Cs checkbox on the Login or Register screen of the Ionic native app.
    Then the get OTP button gets disabled on the Login or Register screen of the Ionic native app.
    When user taps on the T&Cs checkbox on the Login or Register screen of the Ionic native app.
    Then the get OTP button gets enabled on the Login or Register screen of the Ionic native app.

  @IonicNativeApp @SignUp @SmokeTest @SanityTest @RegressionTest
  Scenario Outline: User should be able to register with valid Promo/Referral code on the Ionic Native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps the "Promo/Referral Code?" link on the Login or Register screen of the Ionic native app
    Then the "Promo/Referral Code" drawer should appear on the Login or Register screen of the Ionic native app
    And the user inputs the valid "<Promo/Referral Code>" on the Login or Register screen of the Ionic native app
    Then the Apply button gets enabled on the Login or Register screen of the Ionic native app
    When the user taps the Apply button on the Login or Register screen of the Ionic native app
    And the applied "<Promo/Referral Code>" is displayed on the Login or Register screen of the Ionic native app
    Then the "Promo code applied successfully" toast message appears on the Login or Register screen of the Ionic native app
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    Then the OTP verification drawer should appear on the Ionic native app.
    When user inputs a valid OTP into the OTP field on the Ionic native app.
    Then the user should be registered successfully and logged in to the Ionic native app.
    And verify that the data for the new user singed up from Ionic native app in cardplay.cp_source is correct for all columns
    Examples:
      | Promo/Referral Code |
      | VOOT                |

  @IonicNativeApp @SignUp @SmokeTest @SanityTest @RegressionTest
  Scenario: User should be able to register with valid RAF(Refer-a-friend) referral code on the Ionic Native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps the "Promo/Referral Code?" link on the Login or Register screen of the Ionic native app
    Then the "Promo/Referral Code" drawer should appear on the Login or Register screen of the Ionic native app
    And the user inputs the valid referralCode on the Login or Register screen of the Ionic native app
    Then the Apply button gets enabled on the Login or Register screen of the Ionic native app
    When the user taps the Apply button on the Login or Register screen of the Ionic native app
    And the applied referralCode is displayed on the Login or Register screen of the Ionic native app
    Then the "Promo code applied successfully" toast message appears on the Login or Register screen of the Ionic native app
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    Then the OTP verification drawer should appear on the Ionic native app.
    When user inputs a valid OTP into the OTP field on the Ionic native app.
    Then the user should be registered successfully and logged in to the Ionic native app.
    And verify that the data for new user signed up with referral code in cardplay_poker.cp_raf_referee_details.
    And verify that the data for the new user singed up from Ionic native app in cardplay.cp_source is correct for all columns


  @IonicNativeApp @SignUp @SanityTest @RegressionTest
  Scenario Outline: Error message should be displayed when user enters invalid promo code on the Login or Register screen of the ionic native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps the "Promo/Referral Code?" link on the Login or Register screen of the Ionic native app
    Then the "Promo/Referral Code" drawer should appear on the Login or Register screen of the Ionic native app
    And the user inputs the valid "<Promo/Referral Code>" on the Login or Register screen of the Ionic native app
    Then the Apply button gets enabled on the Login or Register screen of the Ionic native app
    When the user taps the Apply button on the Login or Register screen of the Ionic native app
    Then the "Invalid Code" and "Please enter a valid code" error message should be displayed on the Login or Register screen of the Ionic native app
    Examples:
      | Promo/Referral Code |
      | JJKKI               |

  @IonicNativeApp @SignUp @SanityTest @RegressionTest
  Scenario: User receives error message after 5 Failed registration attempts with invalid OTP on the Ionic Native app
    When the user inputs a valid mobile number into the Enter mobile number field on the Login or Register screen of the Ionic native app.
    And the user taps on the Get OTP button on the Login or Register screen of the Ionic native app.
    Then the OTP verification drawer should appear on the Ionic native app.
    When user input an invalid OTP for "1st" time into the OTP field on the Ionic native app.
    Then the user is unable to register and receives the alert "Please enter valid OTP. 4 attempts remaining" on the Ionic native app
    And the user taps on the Resend OTP Button on the Ionic native app
    Then the "OTP Resent Successfully!" message is displayed on the Ionic native app
    When user input an invalid OTP for "2nd" time into the OTP field on the Ionic native app.
    Then the user is unable to register and receives the alert "Please enter valid OTP. 3 attempts remaining" on the Ionic native app
    And the user taps on the Resend OTP Button on the Ionic native app
    Then the "OTP Resent Successfully!" message is displayed on the Ionic native app
    When user input an invalid OTP for "3rd" time into the OTP field on the Ionic native app.
    Then the user is unable to register and receives the alert "Please enter valid OTP. 2 attempts remaining" on the Ionic native app
    And the user taps on the Resend OTP Button on the Ionic native app
    Then the "OTP Resent Successfully!" message is displayed on the Ionic native app
    When user input an invalid OTP for "4th" time into the OTP field on the Ionic native app.
    Then the user is unable to register and receives the alert "Please enter valid OTP. 1 attempts remaining" on the Ionic native app
    And the user taps on the Resend OTP Button on the Ionic native app
    Then the user is unable to register and receives the alert "Limit reached for maximum resend attempts." on the Ionic native app
    When user input an invalid OTP for "5th" time into the OTP field on the Ionic native app.
    Then the user is unable to register and receives the alert "OTP attempt limit has reached." on the Ionic native app





